package nu.huw.clarity.sync;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import nu.huw.clarity.account.AccountManagerHelper;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

public class DownloadHelper {

  private static final String TAG = DownloadHelper.class.getSimpleName();
  private Context androidContext;
  private AccountManagerHelper accountManagerHelper;

  public DownloadHelper(@NonNull Context androidContext) {
    this.androidContext = androidContext;
    this.accountManagerHelper = new AccountManagerHelper(androidContext);
  }

  @Nullable
  public File downloadFile(@NonNull String name, boolean temporary) {
    HttpClient client = accountManagerHelper.getHttpClient();
    Uri uri = accountManagerHelper.getOfocusUri();
    return downloadFile(client, uri, name, temporary);
  }

  /**
   * Downloads the file at `OmniFocus.ofocus/{name}`. Returns path.
   *
   * @param name Name of the file, relative to the `OmniFocus.ofocus/` folder
   * @param temporary If true, store in the cache directory instead
   * @return File object representing path of downloaded file
   */
  @Nullable
  public File downloadFile(@NonNull HttpClient client, @NonNull Uri uri, @NonNull String name,
      boolean temporary) {

    uri = uri.buildUpon().appendPath(name).build();
    GetMethod getFileMethod = new GetMethod(uri.toString());

    try {

      client.executeMethod(getFileMethod);

      if (getFileMethod.getStatusCode() == 200) {

        // Basically, we make a GetMethod using standard procedure. Then we
        // open its results up into a stream (making sure NOT to call
        // `.releaseConnection()` until we're done), and bitwise copy the in
        // stream to the out stream. THEN we close everything. File downloaded.

        InputStream input = getFileMethod.getResponseBodyAsStream();
        File file;
        if (temporary) {
          file = File.createTempFile(name, null, androidContext.getCacheDir());
        } else {
          file = File.createTempFile(name, null, androidContext.getFilesDir());
        }
        RandomAccessFile output = new RandomAccessFile(file, "rw");

        // Copy input stream to output stream, bitwise
        byte data[] = new byte[4096];
        int count;
        while ((count = input.read(data)) != -1) {
          output.write(data, 0, count);
        }

        // Be extra careful not to cross streams
        // First time this joke has been made, ever

        input.close();
        output.close();

        getFileMethod.releaseConnection();

        Log.v(TAG, name + " successfully downloaded");

        return file;
      } else {
        Log.e(TAG, "Unexpected WebDAV status " + getFileMethod.getStatusCode() + ": " +
            getFileMethod.getStatusText());
      }
    } catch (IOException e) {
      Log.e(TAG, "Couldn't download file", e);
    } finally {
      client.getHttpConnectionManager().closeIdleConnections(0);
    }

    return null;
  }

  public interface DownloadFileTaskListener {

    void onFinished(File file);
  }

  /**
   * Downloads the file in the background, using our beautiful DownloadFile method.
   */
  public class DownloadFileTask extends AsyncTask<Void, Void, File> {

    private DownloadFileTaskListener listener;
    private String name;
    private boolean temporary;

    DownloadFileTask(String name, boolean temporary, DownloadFileTaskListener listener) {
      this.listener = listener;
      this.name = name;
      this.temporary = temporary;
    }

    @Override
    protected File doInBackground(Void... voids) {
      return downloadFile(name, temporary);
    }

    @Override
    protected void onPostExecute(File file) {
      super.onPostExecute(file);
      listener.onFinished(file);
    }
  }
}
