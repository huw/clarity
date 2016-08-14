package nu.huw.clarity.crypto;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * OmniGroup can't spell. Helper class used by DocumentKey to decrypt individual files.
 */
class CTRDecrypter {

    int FILE_MAC_LENGTH;                       // Full SHA256
    private int  AES_KEY_SIZE       = 16;       // 128 bits = 16 bytes for AES-128
    private int  HMAC_KEY_SIZE      = 16;       // Arbitrary, but fixed
    private int  SEGMENT_IV_LENGTH  = 12;// Four bytes less than blocksize (see CTR mode for why)
    private int  SEGMENT_MAC_LENGTH = 20;       // Arbitrary, but fixed
    private int  SEGMENT_PAGE_SIZE  = 65536;
    private byte FILE_MAC_PREFIX    = 0x01;     // Fixed prefix for file HMAC
    private byte[] AESKey;
    private byte[] HMACKey;

    CTRDecrypter(byte[] keyMaterial) {

        AESKey = Arrays.copyOfRange(keyMaterial, 0, AES_KEY_SIZE);
        HMACKey = Arrays.copyOfRange(keyMaterial, AES_KEY_SIZE, AES_KEY_SIZE + HMAC_KEY_SIZE);
        FILE_MAC_LENGTH = 32;
    }

    /**
     * Get a list of tuples which describe the positions of the encrypted segments of a file. See
     * `DecryptionExample`.
     */
    private ArrayList<ArrayList<Integer>> getSegmentRanges(int encryptedStart, int encryptedEnd) {

        ArrayList<ArrayList<Integer>> ranges = new ArrayList<>();

        int encryptedHDRSize = SEGMENT_IV_LENGTH + SEGMENT_MAC_LENGTH;
        int idx              = 0;
        int position         = encryptedStart;

        while (true) {

            if (position + encryptedHDRSize > encryptedEnd) {
                break;
            } // Done

            ArrayList<Integer> tuple = new ArrayList<>();
            tuple.add(idx);
            tuple.add(position);

            if (position + encryptedHDRSize + SEGMENT_PAGE_SIZE > encryptedEnd) {

                // Trailing page, so only partial

                tuple.add(encryptedEnd - (position + encryptedHDRSize)); // Segment size
                ranges.add(tuple);

                break;
            } else {

                // Full page

                tuple.add(SEGMENT_PAGE_SIZE);
                ranges.add(tuple);

                position += encryptedHDRSize + SEGMENT_PAGE_SIZE;
                idx++;
            }
        }

        return ranges;
    }

    /**
     * Verifies a file's integrity using HMAC hashing
     *
     * @param file           File to check
     * @param encryptedStart Start position of encrypted segments
     * @param encryptedEnd   End position of encrypted segments
     * @param fileHMAC       HMAC hash
     *
     * @return True if HMAC validates
     */
    boolean checkHMAC(RandomAccessFile file, int encryptedStart, int encryptedEnd, byte[] fileHMAC)
            throws Exception {

        // Create hash object

        Mac fileHash = Mac.getInstance("HmacSHA256");
        fileHash.init(new SecretKeySpec(HMACKey, "HmacSHA256"));
        fileHash.update(FILE_MAC_PREFIX);

        ArrayList<ArrayList<Integer>> ranges = getSegmentRanges(encryptedStart, encryptedEnd);

        // For each encrypted segment

        for (ArrayList<Integer> tuple : ranges) {

            int segmentIndex  = tuple.get(0);
            int startPosition = tuple.get(1);
            int segmentLength = tuple.get(2);

            // Read segment IV, MAC, and hash

            file.seek(startPosition);
            byte[] segmentIV   = new byte[SEGMENT_IV_LENGTH];
            byte[] segmentMAC  = new byte[SEGMENT_MAC_LENGTH];
            byte[] segmentData = new byte[segmentLength];
            file.read(segmentIV);
            file.read(segmentMAC);
            file.read(segmentData);

            // Verify the segment's hash against its own data

            Mac segmentHash = Mac.getInstance("HmacSHA256");
            segmentHash.init(new SecretKeySpec(HMACKey, "HmacSHA256"));
            segmentHash.update(segmentIV);
            segmentHash.update(ByteBuffer.allocate(4).putInt(segmentIndex)
                                         .array());    // Convert int to byte[]
            segmentHash.update(segmentData);

            byte[] computed = Arrays.copyOf(segmentHash.doFinal(),
                                            SEGMENT_MAC_LENGTH); // Compare truncated hashes

            if (!Arrays.equals(computed, segmentMAC)) {

                throw new Exception("Segment HMAC does not match!");
            }

            // Add segment MAC to data to be hashed

            fileHash.update(segmentMAC);
        }

        byte[] computed = fileHash.doFinal();

        return Arrays.equals(computed, fileHMAC);
    }

    void decrypt(RandomAccessFile inFile, RandomAccessFile outFile, int encryptedStart,
                 int encryptedEnd) throws Exception {

        ArrayList<ArrayList<Integer>> ranges = getSegmentRanges(encryptedStart, encryptedEnd);

        // Decrypt each segment

        for (ArrayList<Integer> tuple : ranges) {

            int startPosition = tuple.get(1);
            int segmentLength = tuple.get(2);

            // Read segment IV nonce

            inFile.seek(startPosition);

            byte[] segmentIVBytes = new byte[SEGMENT_IV_LENGTH + 4]; // Extra padding to IV
            inFile.read(segmentIVBytes, 0, SEGMENT_IV_LENGTH);
            IvParameterSpec segmentIV = new IvParameterSpec(segmentIVBytes);

            // Initialise decrypter

            Cipher        cipher     = Cipher.getInstance("AES/CTR/NoPadding");
            SecretKeySpec AESKeySpec = new SecretKeySpec(AESKey, "AES");
            cipher.init(Cipher.DECRYPT_MODE, AESKeySpec, segmentIV);

            // Read and decrypt data

            byte[] data = new byte[segmentLength];
            inFile.seek(startPosition + SEGMENT_IV_LENGTH + SEGMENT_MAC_LENGTH);
            inFile.read(data);

            byte[] decrypted = cipher.update(data);

            // Write data

            outFile.write(decrypted);
            byte[] trailing = cipher.doFinal(); // Shouldn't be any, but just in case

            if (trailing.length > 0) {

                outFile.write(trailing);
            }
        }

        inFile.close();
        outFile.close();
    }
}
