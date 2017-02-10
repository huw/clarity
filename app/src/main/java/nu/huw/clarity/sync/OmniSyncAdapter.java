package nu.huw.clarity.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import nu.huw.clarity.R;
import nu.huw.clarity.account.AccountManagerHelper;
import nu.huw.clarity.crypto.OmniSyncDecrypter;
import nu.huw.clarity.db.SyncDownParser;
import nu.huw.clarity.db.TreeOperations;
import nu.huw.clarity.sync.DownloadHelper.DownloadFileTask;
import org.apache.commons.httpclient.HttpClient;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.DavMethod;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.DeltaVConstants;

/**
 * Connect to Android's syncing services
 */
class OmniSyncAdapter extends AbstractThreadedSyncAdapter {

  private static final String TAG = OmniSyncAdapter.class.getSimpleName();
  private final AccountManagerHelper accountManagerHelper;
  private final DownloadHelper downloadHelper;

  OmniSyncAdapter(Context context, boolean autoInitialise) {

    super(context, autoInitialise);
    accountManagerHelper = new AccountManagerHelper(context);
    downloadHelper = new DownloadHelper(context);
  }

  /**
   * Validation for files. Given a WebDAV response, it can determine whether the file should be
   * downloaded (and also if it isn't a file).
   *
   * TODO: Add more logic
   */
  private static boolean shouldDownloadFile(MultiStatusResponse response) {

    DavPropertySet properties = response.getProperties(200);
    DavProperty contentType = properties.get(DavPropertyName.GETCONTENTTYPE);

    if (contentType != null) {

      boolean isZip = contentType.getValue().toString().equals("application/zip");

      Log.v(TAG, response.getHref() + (isZip ? " is " : " isn't ") + "valid");
      return isZip;
    } else {

      Log.v(TAG, response.getHref() + " has no content-type property, assuming invalid");
      return false;
    }
  }

  private List<DownloadFileTask> downloadFiles(List<File> nameList) {

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

      DownloadFileTask download = downloadHelper.new DownloadFileTask(file.getName(), true, null);
      download.executeOnExecutor(downloadPool);
      downloadList.add(download);
    }

    return downloadList;
  }

  /**
   * Automatically performed in a background thread. This runs the full sync routine.
   */
  @Override
  public void onPerformSync(Account account, Bundle extras, String authority,
      ContentProviderClient provider, SyncResult syncResult) {

    File metadataFile = downloadHelper.downloadFile("encrypted", true);

    try {

      Log.i(TAG, "Loading encryption metadata...");

      String passphrase = accountManagerHelper.getPassword();
      final OmniSyncDecrypter decrypter =
          new OmniSyncDecrypter(metadataFile, passphrase, getContext(), false);

      // Get list of files to download

      List<File> filesToDownload = getFilesToDownload();
      List<DownloadFileTask> downloadTasks = downloadFiles(filesToDownload);

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
              this.getContext().getCacheDir());
          decrypter.decryptFile(file, decryptedFile);

          // Save any attachments to the normal files directory

          ZipFile zipFile = new ZipFile(decryptedFile);
          Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
          while (zipEntries.hasMoreElements()) {
            ZipEntry entry = zipEntries.nextElement();

            // Skip contents.xml

            if (entry.getName().equals("contents.xml")) continue;
            File entryFile = new File(getContext().getFilesDir(), entry.getName());

            // Preserve directory structure

            if (entry.isDirectory()) {
              entryFile.mkdirs();
            } else {
              File parent = entryFile.getParentFile();

              if (!parent.exists()) {
                parent.mkdirs();
              }

              InputStream inputStream = zipFile.getInputStream(entry);
              RandomAccessFile outputStream = new RandomAccessFile(entryFile, "rw");

              // Copy input stream to output stream, bitwise
              byte data[] = new byte[4096];
              int count;
              while ((count = inputStream.read(data)) != -1) {
                outputStream.write(data, 0, count);
              }

              inputStream.close();
              outputStream.close();
            }
          }

          // Finally start the parser

          ZipEntry contentsXml = zipFile.getEntry("contents.xml");
          InputStream input = zipFile.getInputStream(contentsXml);
          new SyncDownParser(getContext()).parse(input);
          zipFile.close();

        } catch (IOException e) {
          Log.e(TAG, "Error reading downloaded zip file", e);
        } catch (Exception e) {
          Log.e(TAG, "Error decrypting or downloading file", e);
        }
      }

      // Now that everything has been added to the database, recursively update the db tree.

      new TreeOperations(getContext()).updateSubtree(null);

      Log.i(TAG, "Database tree updated");
    } catch (Exception e) {

      Log.e(TAG, "Failed sync", e);
    }

    Intent intent = new Intent(getContext().getString(R.string.sync_broadcastintent));
    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
  }

  /**
   * Gets a list of files to download from the server
   *
   * @return List of files to download, as `File` objects
   */
  private List<File> getFilesToDownload() {

    HttpClient client = accountManagerHelper.getHttpClient();

    try {
      List<File> filesToDownload = new ArrayList<>();

      // Just to be clear, a DavMethod is an extension of an HttpMethod, and is used
      // to send requests to servers. Like you'd execute a GetMethod to get a file
      // from a server, you execute a DavMethod to do non-http things like listing
      // files in a folder or telling the server to copy/move a file.

      DavMethod listAllFiles =
          new PropFindMethod(accountManagerHelper.getOfocusUri().toString(),
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

          accountManagerHelper.setUserData("SERVER_DOMAIN", newHost.getHost());
          accountManagerHelper.setUserData("SERVER_PORT", String.valueOf(newHost.getPort()));

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
    } finally {
      client.getHttpConnectionManager().closeIdleConnections(0);
    }

    return null;
  }
}
