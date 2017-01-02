package nu.huw.clarity.crypto;

import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

@SuppressWarnings("ResultOfMethodCallIgnored") public class OmniSyncDecrypter {

    private static final String TAG           = OmniSyncDecrypter.class.getSimpleName();
    private static       String METADATA_NAME = "encrypted";
    private DocumentKey documentKey;

    /**
     * OmniSyncDecrypter takes in Omni Sync metadata and a passphrase, and can then be used to
     * decrypt any Omni Sync
     * file a user encounters. Probably has a lot of errors and is definitely not cryptographically
     * secure. Modelled
     * off
     * <a href="https://github.com/omnigroup/OmniGroup/blob/master/Frameworks/OmniFileStore/DecryptionExample.py">DecryptionExample.py</a>
     *
     * @param metadataFile File object representing the location of the sync metadata
     * @param passphrase   String of the passphrase used to encrypt the data
     */
    public OmniSyncDecrypter(File metadataFile, String passphrase, Context context)
            throws Exception {

        documentKey = getDocumentKey(metadataFile);
        documentKey.usePassword(passphrase, context);
    }

    /**
     * Decrypts an `OmniFocus.ofocus` directory, file by file
     *
     * @param inputDirectory  File object representing input path
     * @param outputDirectory File object representing output path. Directory structure will be
     *                        created if non-existent.
     */
    public void decryptDirectory(File inputDirectory, File outputDirectory) throws Exception {

        // Create necessary directory structure

        outputDirectory.mkdirs();

        // Decrypt each file

        File[] files = inputDirectory.listFiles();

        if (files != null) {
            for (File file : files) {

                // Calculate paths of in and out files, create if necessary

                if (file.getName().equals(METADATA_NAME)) {
                    continue;
                }
                File outChild = new File(outputDirectory, file.getName());
                outChild.createNewFile();

                documentKey.decryptFile(file.getName(), file, outChild);
            }
        }
    }

    /**
     * Decrypt an individual file.
     *
     * @param inputFile  File object representing encrypted file
     * @param outputFile File object representing desired output location. Will be created if
     *                   non-existent.
     */
    public void decryptFile(File inputFile, File outputFile) throws Exception {

        outputFile.createNewFile();

        documentKey.decryptFile(inputFile.getName(), inputFile, outputFile);
    }

    private DocumentKey getDocumentKey(File metadataFile) throws Exception {

        if (metadataFile.exists()) {

            byte[] blob = getBlob(metadataFile.getAbsolutePath());
            return new DocumentKey(blob);
        } else {

            throw new FileNotFoundException("Metadata file '" + METADATA_NAME + "' does not exist");
        }
    }

    private byte[] getBlob(String pathName) throws IOException {

        File             path = new File(pathName);
        RandomAccessFile file = new RandomAccessFile(path, "r");

        byte[] bytes = new byte[(int) file.length()];
        file.readFully(bytes);

        file.close();

        return bytes;
    }
}
