package nu.huw.clarity.sync;

import android.accounts.Account;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
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
import nu.huw.clarity.model.Client;
import nu.huw.clarity.model.ID;
import nu.huw.clarity.sync.DownloadHelper.DownloadConnectionException;
import nu.huw.clarity.sync.DownloadHelper.DownloadFileTask;
import nu.huw.clarity.sync.DownloadHelper.DownloadRedirectException;
import nu.huw.clarity.ui.LoginActivity;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.DavMethod;
import org.apache.jackrabbit.webdav.client.methods.DeleteMethod;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.client.methods.PutMethod;
import org.threeten.bp.Instant;

class SyncAdapter extends AbstractThreadedSyncAdapter {

  private static final int ERROR_CONNECTION = 0;
  private static final int ERROR_PASSPHRASE = 1;

  private static final String TAG = SyncAdapter.class.getSimpleName();
  private final AccountManagerHelper accountManagerHelper;
  private final DownloadHelper downloadHelper;
  private final SharedPreferences sharedPreferences;
  private int redirects = 0;

  SyncAdapter(Context androidContext, boolean autoInitialise) {
    super(androidContext, autoInitialise);
    accountManagerHelper = new AccountManagerHelper(androidContext);
    downloadHelper = new DownloadHelper(androidContext);
    sharedPreferences = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
  }

  /**
   * Automatically performed in a background thread.
   * Runs the following:
   * 1. Attempt login & verify decryption
   * 2. Build sync histories
   * 3. Download necessary data
   * 4. Parse and save into database
   * 5. Organise data using tree operations
   * 6. (Concurrently) Update .client files
   */
  @Override
  public void onPerformSync(Account account, Bundle bundle, String s,
      ContentProviderClient contentProviderClient, SyncResult syncResult) {

    //////////////////////////
    // Verify login details //
    //////////////////////////

    Log.i(TAG, "Beginning sync");
    Log.v(TAG, "Verifying login details");

    HttpClient client = accountManagerHelper.getHttpClient();
    Uri omniSyncUri = accountManagerHelper.getOfocusUri();
    final OmniSyncDecrypter decrypter;

    try {

      File encryptedMetadataFile = downloadHelper
          .downloadFile(client, omniSyncUri, "encrypted", true);
      if (encryptedMetadataFile != null) {
        String passphrase = accountManagerHelper.getPassphrase();
        decrypter = new OmniSyncDecrypter(encryptedMetadataFile, passphrase, getContext(), false);
      } else {
        throw new NullPointerException("Downloaded file is null");
      }

    } catch (DownloadRedirectException e) {

      // We've saved the new path into the account manager, so just retry immediately
      // IMPORTANT: Limit the number of redirects to prevent a loop

      if (redirects < 3) {
        redirects++;
        onPerformSync(account, bundle, s, contentProviderClient, syncResult);
      } else {
        Log.e(TAG, "Too many redirects at sync location");
        notifyFailure(ERROR_CONNECTION);
        syncResult.stats.numAuthExceptions++;
      }
      return;
    } catch (DownloadConnectionException e) {
      Log.e(TAG, "There was a problem with the download connection", e);
      notifyFailure(ERROR_CONNECTION);
      syncResult.stats.numAuthExceptions++;
      return;
    } catch (InvalidKeyException e) {
      Log.e(TAG, "There was a problem with the encryption passphrase", e);
      notifyFailure(ERROR_PASSPHRASE);
      syncResult.stats.numAuthExceptions++;
      return;
    } catch (Exception e) {
      // Silently fail here, since this is presumably a one-off syncing problem
      Log.e(TAG, e.getMessage(), e);
      syncResult.stats.numIoExceptions++;
      return;
    } finally {
      client.getHttpConnectionManager().closeIdleConnections(0);
    }

    //////////////////////////
    // Build sync histories //
    //////////////////////////

    Log.v(TAG, "Building sync histories");

    // Get last synced ID from shared preferences

    String tailIdentifier = sharedPreferences.getString("TAIL_IDENTIFIER", null);
    if (tailIdentifier != null && !ID.validate(tailIdentifier)) {tailIdentifier = null;}

    // Get list of downloadable files

    List<Uri> filesToDownload;
    List<Uri> clientFiles = new ArrayList<>();
    boolean didDownloadFiles = false;

    try {

      // This DavMethod polls the server to find a list of all files at this directory,
      // at a depth of 1. For DavMethods, we need to add a trailing slash also.

      DavMethod listFiles = new PropFindMethod(omniSyncUri.toString() + "/",
          DavConstants.PROPFIND_ALL_PROP,
          DavConstants.DEPTH_1);
      client.executeMethod(listFiles);
      listFiles.releaseConnection();

      // The server should return a multi-status response

      if (listFiles.getStatusCode() != 207) {
        throw new DownloadConnectionException(
            "Server returned an unexpected response " + listFiles.getStatusCode() + ": " + listFiles
                .getStatusText());
      }

      // Each multi-status response is a different file in the directory, and each has its own URL
      // We can also look at the responses to determine whether we want to download this file

      List<Uri> filesOnServer = new ArrayList<>();
      String clientID = sharedPreferences.getString("CLIENT_IDENTIFIER", null);

      MultiStatusResponse[] responses = listFiles.getResponseBodyAsMultiStatus().getResponses();
      for (MultiStatusResponse response : responses) {
        Uri responseUri = Uri.parse(response.getHref());
        if (ID.isValidHistoryFile(responseUri)) {
          filesOnServer.add(responseUri);
        } else if (clientID != null && ID.isValidClientFile(responseUri, clientID)) {
          clientFiles.add(responseUri);
        }
      }

      Collections.sort(filesOnServer);
      Collections.sort(clientFiles);

      // Find the URI of the tail ID

      Uri tailUri = null;
      if (tailIdentifier == null) {
        tailUri = filesOnServer.get(0);
      } else {
        for (Uri fileUri : filesOnServer) {
          if (ID.getDestination(fileUri).equals(tailIdentifier)) {
            tailUri = fileUri;
          }
        }
      }

      if (tailUri == null) {

        // There's a chance (particularly if we're not uploading .client files) that the tail
        // identifier can't be found on the server and is still null at this point. In this case,
        // download all of the files again.

        tailUri = filesOnServer.get(0);
      }

      // Remove all Uris that are chronologically before the tail ID to speed up this loop
      // (remember that the files list is ordered by date)

      int tailIndex = filesOnServer.indexOf(tailUri);
      for (int index = tailIndex - 1; index >= 0; index--) {

        // i.e. Remove the 5th object, then the 4th, etc. until the 0th object

        filesOnServer.remove(index);
      }

      // Build and prune through a bunch of alternate syncing histories to find the correct
      // download sequence

      List<List<Uri>> syncHistories = new ArrayList<>();
      List<Uri> firstHistory = new ArrayList<>();
      firstHistory.add(tailUri);
      syncHistories.add(firstHistory);

      boolean done = false;
      while (!done) {
        done = true; // track whether we've added anything to a history this loop

        for (int i = 0; i < syncHistories.size(); i++) {
          List<Uri> history = syncHistories.get(i);

          // Find the current tail ID...

          boolean remove = false;
          Uri currentTailUri = history.get(history.size() - 1);
          String currentTailID = ID.getDestination(currentTailUri);
          List<List<Uri>> subhistories = new ArrayList<>();

          for (Uri uri : filesOnServer) {

            // ...find where it leads to next

            if (currentTailID.equals(ID.getOrigin(uri))) {

              // Copy out the current history, add the new tail, and save it

              List<Uri> historyCopy = new ArrayList<>(history); // copies the list
              historyCopy.add(uri);
              subhistories.add(historyCopy);

              remove = true;
            } else if (currentTailID.equals(ID.getFirst(uri))) {

              // If the ID is the first of three, then that means the second ID's history is the
              // canonical one, so just remove the current history without adding anything new

              remove = true;
            }
          }

          if (remove) {

            // If the ID doesn't have a match (remove = false), then don't remove the current
            // history, and leave `done` as true to end the loop. That way if none of the histories
            // update in a loop, we finish up.

            syncHistories.remove(i);
            i--;
            syncHistories.addAll(subhistories);
            done = false;
          }

          // Then also remove the current tail URI, because no given history is going to need it in
          // a search and it only slows us down (unless it's a merge file, in which case it can be
          // used multiple times).

          if (!ID.isValidMergeFile(currentTailUri)) {
            filesOnServer.remove(currentTailUri);
          }
        }
      }

      // If there are multiple tails, merge them by creating new merge history files
      // TODO
      // For now we just figure out which one has the latest changes and use that as the override
      // To do so, just find the history with the latest date on its final transaction

      filesToDownload = syncHistories.get(0);
      if (syncHistories.size() > 1) {
        int lastDate = ID.getDate(filesToDownload.get(filesToDownload.size() - 1));
        for (int i = 1; i < syncHistories.size(); i++) {
          List<Uri> history = syncHistories.get(i);
          int date = ID.getDate(history.get(history.size() - 1));
          if (date > lastDate) {
            lastDate = date;
            filesToDownload = history;
          }
        }
      }

      // Remove the first URI from filesToDownload if it's the one with the tailIdentifier, because
      // we already downloaded it last time.

      if (ID.getDestination(filesToDownload.get(0)).equals(tailIdentifier)) {
        filesToDownload.remove(0);
      }

      // Save the tail identifier into the sync preferences

      if (filesToDownload.size() > 0) {
        tailIdentifier = ID.getDestination(filesToDownload.get(filesToDownload.size() - 1));
        sharedPreferences.edit().putString("TAIL_IDENTIFIER", tailIdentifier).commit();
        didDownloadFiles = true;
      }

    } catch (IOException | DavException e) {
      Log.e(TAG, "There was a problem with the web request", e);
      syncResult.stats.numIoExceptions++;
      return;
    } finally {
      client.getHttpConnectionManager().closeIdleConnections(0);
    }

    ////////////////
    // Wipe cache //
    ////////////////

    Log.v(TAG, "Clearing out the cache");

    if (didDownloadFiles) {
      File[] files = getContext().getCacheDir().listFiles();
      for (File file : files) {
        file.delete();
      }
    }

    /////////////////////////////
    // Download necessary data //
    /////////////////////////////

    Log.v(TAG, "Downloading files");

    if (didDownloadFiles) {

      // The setup we want for the threading is as follows:
      // | 1  | 2 - n-1  |   n   |
      // | UI | Download | Parse |
      // So we need to create a thread pool with 1 less thread than the maximum,
      // to leave a free thread for parsing.
      //
      // We want to do this because the parsing subprocess is sequential
      // (so has a maximum of 1 thread), but is also bottlenecked by the download process.
      // We may as well start it as soon as possible (i.e. after the first file has downloaded),
      // and it should run parallel to the downloads.

      List<DownloadFileTask> downloadList = new ArrayList<>();
      int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
      ExecutorService downloadPool = Executors.newFixedThreadPool(NUMBER_OF_CORES - 1);

      for (Uri uri : filesToDownload) {

        // Immediately add each file to the downloadList while they're starting to download in the background threads.

        DownloadFileTask download = downloadHelper.new DownloadFileTask(uri.getLastPathSegment(),
            true);
        download.executeOnExecutor(downloadPool);
        downloadList.add(download);
      }

      // We've queued up and started executing the downloads in a background thread, now we should go
      // through (in order) and decrypt each one. Once that's done, we parse it. This is because we
      // can download the files in parallel, but _must_ parse them in the correct order to process
      // the updates and deletions correctly.

      try {

        for (DownloadFileTask download : downloadList) {

          // File has likely already been downloaded. If not, halt background thread
          // until it is (there's nothing else we can do while we're waiting)

          File file = download.get();
          if (file == null) {
            throw new IOException("Downloaded file is null");
          }

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

            if (!entry.isDirectory()) {
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

          // Parse and save into database

          ZipEntry contentsXml = zipFile.getEntry("contents.xml");
          InputStream input = zipFile.getInputStream(contentsXml);

          Log.i(TAG, "Parsing " + file.getName());

          new SyncDownParser(getContext()).parse(input);
          zipFile.close();
        }
      } catch (IOException e) {
        Log.e(TAG, "Error downloading the file", e);
        syncResult.stats.numIoExceptions++;
        return;
      } catch (Exception e) {
        Log.e(TAG, "Error decrypting the file", e);
        syncResult.stats.numIoExceptions++;
        return;
      } finally {
        client.getHttpConnectionManager().closeIdleConnections(0);
      }
    }

    /////////////////////////////////////////
    // Organise data using tree operations //
    /////////////////////////////////////////

    Log.v(TAG, "Organising data using tree operations");

    if (didDownloadFiles) {
      new TreeOperations(getContext()).updateSubtree(null);
    }

    ////////////////////////////
    // Organise .client files //
    ////////////////////////////

    Log.v(TAG, "Organising .client files");

    if (didDownloadFiles) {

      // Create and upload a new client files

      sharedPreferences.edit().putString("LAST_SYNC_DATE", Instant.now().toString()).commit();
      Client clientFile = new Client(getContext());

      try {

        // Convert client file to put request

        Uri putUri = omniSyncUri.buildUpon().appendPath(clientFile.filename).build();
        PutMethod putMethod = new PutMethod(putUri.toString());
        RequestEntity requestEntity = new StringRequestEntity(clientFile.toPlistString(),
            "document",
            null);
        putMethod.setRequestEntity(requestEntity);

        // Execute request

        client.executeMethod(putMethod);

        int statusCode = putMethod.getStatusCode();
        if (statusCode != 201) {
          putMethod.releaseConnection();
          throw new IOException(
              "Unexpected status " + statusCode + " " + putMethod.getStatusText());
        }
        putMethod.releaseConnection();
      } catch (UnsupportedEncodingException e) {
        Log.e(TAG, e.getMessage(), e);
        syncResult.stats.numParseExceptions++;
      } catch (IOException e) {
        Log.e(TAG, "Failed to upload client file", e);
        syncResult.stats.numIoExceptions++;
      } finally {
        client.getHttpConnectionManager().closeIdleConnections(0);
      }

      // Remove old client files (keep the latest two)

      int numberOfClientFiles = clientFiles.size();
      if (numberOfClientFiles > 2) {
        for (int i = 0; i < numberOfClientFiles - 2; i++) {
          try {

            // Form a delete method & execute

            Uri deleteUri = omniSyncUri.buildUpon()
                .appendPath(clientFiles.get(i).getLastPathSegment())
                .build();
            DeleteMethod deleteMethod = new DeleteMethod(deleteUri.toString());
            client.executeMethod(deleteMethod);

            // Check status

            int statusCode = deleteMethod.getStatusCode();
            if (statusCode != 204) {
              deleteMethod.releaseConnection();
              throw new IOException(
                  "Unexpected status " + statusCode + " " + deleteMethod.getStatusText());
            }
            deleteMethod.releaseConnection();

          } catch (IOException e) {
            Log.e(TAG, "Failed to delete old client file", e);
            syncResult.stats.numIoExceptions++;
          } finally {
            client.getHttpConnectionManager().closeIdleConnections(0);
          }
        }
      }
    }

    ///////////////////////////

    Intent intent = new Intent(getContext().getString(R.string.sync_broadcastintent));
    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    Log.i(TAG, "Sync complete");
  }

  /**
   * Displays a notification informing the user that sync is having issues that they can rectify by
   * logging in. The notification is a handy link to log in.
   *
   * @param failureType One of the codes in {@link SyncAdapter}, used to set the string resources
   */
  private void notifyFailure(int failureType) {

    int ID = 104289999; // random, but consistent so that we replace existing notifications

    // Get string resources

    @StringRes int titleStringRes = R.string.sync_errortitle;
    @StringRes int contentStringRes;
    switch (failureType) {
      case ERROR_PASSPHRASE:
        contentStringRes = R.string.sync_errorpassphrase;
        break;
      case ERROR_CONNECTION:
      default:
        contentStringRes = R.string.sync_errorconnection;
        break;
    }

    String titleString = getContext().getString(titleStringRes);
    String contentString = getContext().getString(contentStringRes);

    // Set result intent to login screen

    Intent intent = new Intent(getContext(), LoginActivity.class);
    intent.putExtra(LoginActivity.KEY_REQUEST, LoginActivity.VALUE_LOGINAGAIN);
    intent.putExtra(LoginActivity.KEY_ACCOUNTTYPE, accountManagerHelper.getType());
    PendingIntent pendingIntent = PendingIntent
        .getActivity(getContext(), ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    // Set wearable notification background

    Bitmap background_wearable = BitmapFactory
        .decodeResource(getContext().getResources(), R.drawable.background_wearable);
    NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
        .setBackground(background_wearable);

    // Build notification

    NotificationCompat.Builder builder = new Builder(getContext())
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(titleString)
        .setContentText(contentString)
        .setContentIntent(pendingIntent)
        .setColor(ContextCompat.getColor(getContext(), R.color.primary))
        .setCategory(NotificationCompat.CATEGORY_ERROR)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setAutoCancel(true)
        .extend(wearableExtender);

    NotificationManagerCompat.from(getContext()).notify(ID, builder.build());
  }
}
