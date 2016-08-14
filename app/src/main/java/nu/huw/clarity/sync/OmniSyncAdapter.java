package nu.huw.clarity.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.DavMethod;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.DeltaVConstants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import nu.huw.clarity.R;
import nu.huw.clarity.account.AccountManagerHelper;
import nu.huw.clarity.crypto.OmniSyncDecrypter;
import nu.huw.clarity.db.RecursiveColumnUpdater;
import nu.huw.clarity.db.SyncDownParser;
import nu.huw.clarity.ui.MainActivity;

/**
 * Connect to Android's syncing services
 */
class OmniSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = OmniSyncAdapter.class.getSimpleName();
    private final AccountManager accountManager;

    public OmniSyncAdapter(Context context, boolean autoInitialise) {

        super(context, autoInitialise);
        accountManager = AccountManager.get(context);
    }

    /**
     * This function will create a new HttpClient and automatically add the account's credentials in
     * an appropriate form.
     *
     * It's really useful, because we can just call `getHttpClient()` and run WebDAV/HTTP methods on
     * it straightaway, without having to configure it.
     *
     * This code will be the appropriate setup code 100% of the time, AFTER AN ACCOUNT HAS BEEN
     * MADE.
     */
    static HttpClient getHttpClient() {

        HttpClient client = new HttpClient();

        UsernamePasswordCredentials credentials =
                new UsernamePasswordCredentials(AccountManagerHelper.getUsername(),
                                                AccountManagerHelper.getPassword());
        AuthScope newHostScope = new AuthScope(AccountManagerHelper.getServerDomain(),
                                               AccountManagerHelper.getServerPort(),
                                               AuthScope.ANY_REALM);

        client.getState().setCredentials(newHostScope, credentials);
        client.getParams().setAuthenticationPreemptive(true);
        return client;
    }

    /**
     * Validation for files. Given a WebDAV response, it can determine whether the file should be
     * downloaded (and also if it isn't a file).
     *
     * TODO: Add more logic
     */
    private static boolean shouldDownloadFile(MultiStatusResponse response) {

        DavPropertySet properties  = response.getProperties(200);
        DavProperty    contentType = properties.get(DavPropertyName.GETCONTENTTYPE);

        if (contentType != null) {

            boolean isZip = contentType.getValue().toString().equals("application/zip");

            Log.v(TAG, response.getHref() + (isZip ? " is " : " isn't ") + "valid");
            return isZip;
        } else {

            Log.v(TAG, response.getHref() + " has no content-type property, assuming invalid");
            return false;
        }
    }

    private static List<DownloadFileTask> downloadFiles(List<File> nameList) {

        List<DownloadFileTask> downloadList = new ArrayList<>();

        /*
         * The setup we want for the threading is as follows:
         * | 1  | 2 - n-1  |   n   |
         * | UI | Download | Parse |
         * So we need to create a thread pool with 1 less thread than the maximum,
         * to leave a free thread for parsing.
         *
         * We want to do this because the parsing subprocess is sequential (so has
         * a maximum of 1 thread), but is also bottlenecked by the download process.
         * We may as well start it as soon as possible (i.e. after the first file
         * has downloaded), and it should run parallel to the downloads.
         */
        int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

        ExecutorService downloadPool = Executors.newFixedThreadPool(NUMBER_OF_CORES - 1);

        for (File file : nameList) {

            // Immediately add each file to the downloadList while they're
            // starting to download in the background threads. Then return
            // a list of these objects to be called up later.

            DownloadFileTask download = new DownloadFileTask();
            download.executeOnExecutor(downloadPool, file);
            downloadList.add(download);
        }

        return downloadList;
    }

    /**
     * Automatically performed in a background thread. This runs the full sync routine.
     */
    @Override public void onPerformSync(Account account, Bundle extras, String authority,
                                        ContentProviderClient provider, SyncResult syncResult) {

        File metadataFile = downloadFile("encrypted");

        try {

            Log.i(TAG, "Loading encryption metadata...");

            String                  passphrase = accountManager.getPassword(account);
            final OmniSyncDecrypter decrypter  = new OmniSyncDecrypter(metadataFile, passphrase);

            // Get list of files to download

            List<File>             filesToDownload = getFilesToDownload();
            List<DownloadFileTask> downloadTasks   = downloadFiles(filesToDownload);

            // Download files asynchronously:
            //
            // The reason we do this is probably written about below somewhere, but it's
            // basically because we can download the files in parallel, and then execute them as
            // they arrive in the order that we want. This way we're not blocking the thread that
            // could be parsing them when we're downloading the file.

            Log.i(TAG, "Decrypting and parsing files...");

            for (DownloadFileTask download : downloadTasks) {
                try {

                    // File has likely already been downloaded. If not, halt background thread
                    // until it is (there's nothing else we can do while we're waiting)

                    File file = download.get();

                    // Create a temporary file to hold decrypted contents, and decrypt to that file.

                    File decryptedFile = File.createTempFile("dec-" + file.getName(), null,
                                                             MainActivity.context.getCacheDir());
                    decrypter.decryptFile(file, decryptedFile);

                    // Read that file as a zip file, and then parse its `contents.xml` into the
                    // database adder/parser/thing.

                    ZipFile     zipFile     = new ZipFile(decryptedFile);
                    ZipEntry    contentsXml = zipFile.getEntry("contents.xml");
                    InputStream input       = zipFile.getInputStream(contentsXml);

                    SyncDownParser.parse(input);
                } catch (IOException e) {
                    Log.e(TAG, "Error reading downloaded zip file", e);
                } catch (Exception e) {
                    Log.e(TAG, "Error decrypting or downloading file", e);
                }
            }

            // Now that everything has been added to the database, recursively update the db tree.

            new RecursiveColumnUpdater().updateTree();

            Log.i(TAG, "Database tree updated");
        } catch (Exception e) {

            Log.e(TAG, "Failed sync", e);
        }

        Intent intent = new Intent(getContext().getString(R.string.sync_broadcast_intent));
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    /**
     * Downloads the file at `OmniFocus.ofocus/{name}` into a temporary location. Returns path.
     *
     * @param name Name of the file, relative to the `OmniFocus.ofocus/` folder
     *
     * @return File object representing path of downloaded file
     */
    private File downloadFile(String name) {

        HttpClient client        = getHttpClient();
        GetMethod  getFileMethod = new GetMethod(AccountManagerHelper.getOfocusURI() + name);

        try {

            client.executeMethod(getFileMethod);

            if (getFileMethod.getStatusCode() == 200) {

                // Basically, we make a GetMethod using standard procedure. Then we
                // open its results up into a stream (making sure NOT to call
                // `.releaseConnection()` until we're done), and bitwise copy the in
                // stream to the out stream. THEN we close everything. File downloaded.

                InputStream input = getFileMethod.getResponseBodyAsStream();
                File file = File.createTempFile(name, null, MainActivity.context.getCacheDir());
                RandomAccessFile output = new RandomAccessFile(file, "rw");

                // Copy input stream to output stream, bitwise
                byte data[] = new byte[4096];
                int  count;
                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                }

                // Be extra careful not to cross streams
                // First time this joke has been made, ever

                input.close();
                output.close();

                getFileMethod.releaseConnection();

                Log.v(TAG, name + " successfully downloaded (" + file.length() + " bytes)");

                return file;
            } else {
                Log.e(TAG, "Unexpected WebDAV status " + getFileMethod.getStatusCode() + ": " +
                           getFileMethod.getStatusText());
            }
        } catch (Exception e) {
            Log.e(TAG, "Unexpected " + e.getClass().getName() + ": " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Gets a list of files to download from the server
     *
     * @return List of files to download, as `File` objects
     */
    private List<File> getFilesToDownload() {

        try {
            List<File> filesToDownload = new ArrayList<>();
            HttpClient client          = getHttpClient();

            // Just to be clear, a DavMethod is an extension of an HttpMethod, and is used
            // to send requests to servers. Like you'd execute a GetMethod to get a file
            // from a server, you execute a DavMethod to do non-http things like listing
            // files in a folder or telling the server to copy/move a file.

            DavMethod listAllFiles = new PropFindMethod(AccountManagerHelper.getOfocusURI(),
                                                        DavConstants.PROPFIND_ALL_PROP,
                                                        DavConstants.DEPTH_1);
            client.executeMethod(listAllFiles);
            listAllFiles.releaseConnection();

            // Sometimes Omni will change around their servers,
            // so we need to update the user's server in their
            // account settings.

            int statusCode = listAllFiles.getStatusCode();
            if ((301 <= statusCode && statusCode <= 304) ||
                (307 <= statusCode && statusCode <= 308)) {
                try {

                    // Get the data about the new host, and update it using
                    // a convenience method from AccountManagerHelper

                    URI newHost =
                            new URI(listAllFiles.getResponseHeader(DeltaVConstants.HEADER_LOCATION)
                                                .getValue());

                    AccountManagerHelper.setUserData("SERVER_DOMAIN", newHost.getHost());
                    AccountManagerHelper
                            .setUserData("SERVER_PORT", String.valueOf(newHost.getPort()));

                    // TODO: RESTART SYNC
                } catch (URISyntaxException e) {
                    Log.e(TAG, "Omni Sync Server returned invalid redirection URI", e);
                }
            } else {

                MultiStatusResponse[] responses =
                        listAllFiles.getResponseBodyAsMultiStatus().getResponses();

                for (MultiStatusResponse response : responses) {
                    if (shouldDownloadFile(response)) {

                        // Because we can recreate the URL, and because we
                        // need the name of the file (without the path)
                        // later on, we convert it to the raw name.

                        filesToDownload.add(new File(response.getHref()));
                    }
                }

                Log.i(TAG, "Got " + filesToDownload.size() + " files to download");

                Collections.sort(filesToDownload);
                return filesToDownload;
            }
        } catch (IOException | DavException e) {
            Log.e(TAG, "Problem creating/sending request", e);
        }

        return null;
    }
}
