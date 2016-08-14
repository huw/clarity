package nu.huw.clarity.crypto;

import com.dd.plist.NSArray;
import com.dd.plist.NSData;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSNumber;
import com.dd.plist.NSObject;
import com.dd.plist.NSString;
import com.dd.plist.PropertyListParser;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Struct-like data type for holding the data of a 'slot' on the AES key.
 * Equivalent to `Slot = collections.namedtuple('Slot', ('tp', 'id', 'contents'))` in
 * `DecryptionExample.py`.
 */
class Slot {

    byte   type;
    int    id;
    byte[] contents;

    Slot(byte type, int id, byte[] contents) {

        this.type = type;
        this.id = id;
        this.contents = contents;
    }

    /**
     * Analogue of `SlotType` from `DecryptionExample`, but kind of the other way around
     *
     * @return A human-readable string indicating the type of slot
     */
    String getTypeString() {

        switch (type) {

            case 1:
                return "ActiveAESWrap";         // (Obsolete) Currently active AES-wrap key
            case 2:
                return "RetiredAESWrap";        // (Obsolete) Old AES-wrap key (from rollover)
            case 3:
                return "ActiveAES_CTR_HMAC";    // Active CTR + HMAC key
            case 4:
                return "RetiredAES_CTR_HMAC";   // Old CTR + HMAC key
            case 5:
                return "PlaintextMask";         // Filename patterns which should not be encrypted
            case 6:
                return "RetiredPlaintextMask";  // Filename patterns which have legacy
            // unencrypted entries
            default:
                return "None";                  // Trailing padding
        }
    }

    /**
     * @return True if slot type is a plaintext mask. See `getTypeString()` above.
     */
    boolean plaintextReadable() {

        return type == 5 || type == 6;
    }

    /**
     * @return True if slot type is an AES wrap. Use of AES wrap slots are deprecated.
     */
    boolean typeAESWrap() {

        return type == 1 || type == 2;
    }

    /**
     * @return True if slot type is a modern AES/CTR/HMAC slot.
     */
    boolean typeAESCTR() {

        return type == 3 || type == 4;
    }
}

/**
 * Main decryption routine
 */
class DocumentKey {

    private byte[] FILE_MAGIC = "OmniFileEncryption\00\00".getBytes();
    private NSDictionary metadata;
    private byte[]       unwrapped;
    private ArrayList<Slot> secrets = new ArrayList<>();

    DocumentKey(byte[] blob) throws Exception {

        NSObject parsed = PropertyListParser.parse(blob);

        if (parsed instanceof NSDictionary) {
            this.metadata = (NSDictionary) parsed;
        } else if (parsed instanceof NSArray && ((NSArray) parsed).count() == 1) {
            this.metadata = (NSDictionary) ((NSArray) parsed).objectAtIndex(0);
        }
    }

    /**
     * Trims null bytes from end of a byte array. http://stackoverflow.com/a/17004488.
     *
     * @param bytes Byte array with trailing zeros
     *
     * @return Byte array without trailing zeros
     */
    private static byte[] trimPadding(byte[] bytes) {

        int i = bytes.length - 1;
        while (i >= 0 && bytes[i] == 0) {
            --i;
        }

        return Arrays.copyOf(bytes, i + 1);
    }

    /**
     * Mostly the same as OmniGroup's `DecryptionExample.py`
     * See
     * <a href="https://github.com/omnigroup/OmniGroup/blob/master/Frameworks/OmniFileStore/DecryptionExample.py">GitHub</a>.
     * That means that this class uses the passphrase to unwrap the AES key used to encrypt the
     * files, and unlocks a
     * list of secrets that we later use to unlock other files.
     *
     * @param passphrase The user's passphrase
     */
    void usePassword(String passphrase) throws Exception {

        // Establish algorithm & method

        String method    = ((NSString) metadata.get("method")).getContent();
        String algorithm = ((NSString) metadata.get("algorithm")).getContent();

        // Input parameters

        int    rounds       = ((NSNumber) metadata.get("rounds")).intValue();
        byte[] salt         = ((NSData) metadata.get("salt")).bytes();
        byte[] encryptedKey = ((NSData) metadata.get("key")).bytes();

        // Generate cipher engine
        // (default key length is 128 (= AES128-wrap))

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(passphrase.toCharArray(),   // passphrase (byte[])
                                      salt,                       // salt (byte[])
                                      rounds,                     // rounds (int)
                                      128                         // key length (in bits)
        );
        SecretKeySpec derivedKey =
                new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");

        // Unwrap AES key

        Cipher cipher = Cipher.getInstance("AESWRAP");
        cipher.init(Cipher.UNWRAP_MODE, derivedKey);

        unwrapped =
                cipher.unwrap(encryptedKey, "AES/CTR/NOPADDING", Cipher.SECRET_KEY).getEncoded();

        // Generate list of file type secrets

        int idx = 0;

        while (idx != unwrapped.length) {

            byte slotType = unwrapped[idx];
            if (slotType == 0) {
                break;
            } // sentinel value

            // Get data from slot

            int slotLength = 4 * unwrapped[idx + 1];
            int slotID = ByteBuffer.wrap(Arrays.copyOfRange(unwrapped, idx + 2, idx + 4))
                                   .getShort();   // only 2 bytes
            byte[] slotData = Arrays.copyOfRange(unwrapped, idx + 4, idx + 4 + slotLength);

            // Add to array

            Slot slot = new Slot(slotType, slotID, slotData);
            secrets.add(slot);

            idx += 4 + slotLength;
        }
    }

    /**
     * Returns secret that matches the given ID. Fails if more than one match is found.
     *
     * @param id ID of the requested secret
     *
     * @return Matching slot object
     */
    private Slot getMatchingSecret(int id) throws Exception {

        ArrayList<Slot> slots = new ArrayList<>();

        for (Slot slot : secrets) {

            if (slot.id == id) {

                slots.add(slot);
            }
        }

        if (slots.size() == 0) {

            throw new Exception("No slot ID match for " + id);
        }

        return slots.get(0);
    }

    /**
     * Get an appropriate nu.huw.omnisyncdecrypter.CTRDecrypter object for this file's key
     * information
     *
     * @param info Byte array representing the key information for a file (see usage)
     *
     * @return A nu.huw.omnisyncdecrypter.CTRDecrypter object
     */
    private CTRDecrypter getDecrypter(byte[] info) throws Exception {

        int keyID = ByteBuffer.wrap(Arrays.copyOf(info, 2)).getShort();

        Slot slot = getMatchingSecret(keyID);

        if (slot.typeAESCTR()) {

            return new CTRDecrypter(slot.contents);
        } else if (slot.typeAESWrap()) {

            byte[] wrappedKey = Arrays.copyOfRange(info, 2, info.length);

            Cipher        cipher         = Cipher.getInstance("AESWRAP");
            SecretKeySpec wrappedKeySpec = new SecretKeySpec(wrappedKey, "AESWRAP");
            cipher.init(Cipher.UNWRAP_MODE, wrappedKeySpec);

            unwrapped = cipher.unwrap(slot.contents, "AES/CTR/NoPadding", Cipher.SECRET_KEY)
                              .getEncoded();
            return new CTRDecrypter(unwrapped);
        } else {

            throw new Exception("Key slot type " + String.valueOf(slot.type) + " not found");
        }
    }

    /**
     * Compares equality of two unevenly-size byte arrays, from the end. Will match true if all of
     * the bytes in the
     * smaller array are equal to the bytes at the end of the larger array, regardless of the rest
     * of the larger array.
     * Useful for comparing file extensions.
     */
    private boolean compareBytesFromEnd(byte[] one, byte[] two) {

        // Null check

        if (one == null || two == null) {
            return one == two;
        }

        // Determine which to compare

        byte[] big = one, small = two;

        if (two.length > one.length) {

            big = two;
            small = one;
        }

        boolean match = true;

        // For each byte from the end (see above), break if match not found

        for (int i = 1; i < small.length + 1; i++) {

            if (big[big.length - i] != small[small.length - i]) {

                match = false;
                break;
            }
        }

        return match;
    }

    /**
     * Somewhat equivalent to `applicable_policy_slots`, but returns a bool depending on whether
     * reading plaintext is
     * allowed for the given filename.
     */
    private boolean canReadPlaintext(String filename) {

        byte[] filenameBytes = filename.getBytes();

        for (Slot slot : this.secrets) {

            byte[] fileType = trimPadding(slot.contents);

            if (slot.plaintextReadable() && compareBytesFromEnd(filenameBytes, fileType)) {

                return true;
            }
        }

        return false;
    }

    void decryptFile(String filename, File inPath, File outPath) throws Exception {

        boolean canReadPlaintext = canReadPlaintext(filename);

        byte[]           magic  = new byte[FILE_MAGIC.length];
        RandomAccessFile inFile = new RandomAccessFile(inPath, "r");
        inFile.read(magic);   // Read the magic bytes

        // Unencrypted plaintext files

        if (!Arrays.equals(magic, FILE_MAGIC) && canReadPlaintext) {

            RandomAccessFile outFile = new RandomAccessFile(outPath, "rw");

            // Copy all bytes over

            inFile.seek(0);
            byte[] inFileBytes = new byte[(int) inFile.length()];
            inFile.readFully(inFileBytes);
            outFile.write(inFileBytes);

            inFile.close();
            outFile.close();
            return;
        }

        // Read the key information

        byte[] infoLengthBytes = new byte[2];
        inFile.read(infoLengthBytes);
        int infoLength = ByteBuffer.wrap(infoLengthBytes).getShort();

        byte[] info = new byte[infoLength];
        inFile.read(info);

        // Skip and verify padding bytes

        int    offset        = FILE_MAGIC.length + 2 + infoLength;
        int    paddingLength = (16 - (offset % 16)) % 16;
        byte[] paddingBytes  = new byte[paddingLength];
        inFile.read(paddingBytes);

        byte[] expectedPadding = new byte[paddingLength];

        if (!Arrays.equals(paddingBytes, expectedPadding)) {

            throw new Exception("Padding length doesn't match expected");
        }

        CTRDecrypter decrypter = getDecrypter(info);

        // Read encrypted boundaries and HMAC

        long   encryptedStart = inFile.getChannel().position();
        long   encryptedEnd   = inFile.getChannel().size() - decrypter.FILE_MAC_LENGTH;
        byte[] fileHMAC       = new byte[decrypter.FILE_MAC_LENGTH];

        inFile.seek(encryptedEnd);
        inFile.read(fileHMAC);

        // Verify HMAC

        if (decrypter.checkHMAC(inFile, (int) encryptedStart, (int) encryptedEnd, fileHMAC)) {

            if (outPath.exists()) {

                RandomAccessFile outFile = new RandomAccessFile(outPath, "rw");
                decrypter.decrypt(inFile, outFile, (int) encryptedStart, (int) encryptedEnd);
            }
        } else {

            throw new Exception("Invalid file HMAC");
        }
    }
}
