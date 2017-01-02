package nu.huw.clarity.crypto;

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