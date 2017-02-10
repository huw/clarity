package nu.huw.clarity.crypto;

/**
 * Struct-like data type for holding the data of a 'slot' on the AES key.
 * Equivalent to `Slot = collections.namedtuple('Slot', ('tp', 'id', 'contents'))` in
 * `DecryptionExample.py`.
 */
class Slot {

  static final int NONE = 0;
  static final int ACTIVE_AESWRAP = 1;
  static final int RETIRED_AESWRAP = 2;
  static final int ACTIVE_AESCTRHMAC = 3;
  static final int RETIRED_AESCTRHMAC = 4;
  static final int ACTIVE_PLAINTEXT = 5;
  static final int RETIRED_PLAINTEXT = 6;


  byte type;
  int id;
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

      case ACTIVE_AESWRAP:
        return "ActiveAESWrap";         // (Obsolete) Currently active AES-wrap key
      case RETIRED_AESWRAP:
        return "RetiredAESWrap";        // (Obsolete) Old AES-wrap key (from rollover)
      case ACTIVE_AESCTRHMAC:
        return "ActiveAES_CTR_HMAC";    // Active CTR + HMAC key
      case RETIRED_AESCTRHMAC:
        return "RetiredAES_CTR_HMAC";   // Old CTR + HMAC key
      case ACTIVE_PLAINTEXT:
        return "PlaintextMask";         // Filename patterns which should not be encrypted
      case RETIRED_PLAINTEXT:
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