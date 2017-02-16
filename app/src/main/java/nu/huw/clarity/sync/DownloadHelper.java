package nu.huw.clarity.sync;

import android.content.Context;
import android.net.Uri;
import android.net.Uri.Builder;
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
import org.apache.jackrabbit.webdav.version.DeltaVConstants;

public class DownloadHelper {

  private static final String TAG = DownloadHelper.class.getSimpleName();
  private Context androidContext;
  private AccountManagerHelper accountManagerHelper;

  public DownloadHelper(@NonNull Context androidContext) {
    this.androidContext = androidContext;
    this.accountManagerHelper = new AccountManagerHelper(androidContext);
  }

  @Nullable
  public File downloadFile(@NonNull String name, boolean temporary)
      throws DownloadConnectionException, DownloadIOException, DownloadRedirectException {
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
      boolean temporary)
      throws DownloadConnectionException, DownloadIOException, DownloadRedirectException {

    uri = uri.buildUpon().appendPath(name).build();
    GetMethod getFileMethod = new GetMethod(uri.toString());
    getFileMethod.setFollowRedirects(false); // So that we can correctly grab the current sync URL

    try {

      try {
        client.executeMethod(getFileMethod);
      } catch (IOException e) {
        throw new DownloadIOException(e);
      }

      int statusCode = getFileMethod.getStatusCode();
      if (statusCode == 200) {

        try {

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
        } catch (IOException e) {
          getFileMethod.releaseConnection();
          throw new DownloadIOException(e);
        }
      } else if ((statusCode >= 301 && statusCode <= 304) || statusCode == 307
          || statusCode == 308) {

        // Save the redirect location and throw the exception so someone potentially can try again

        Uri redirectUri = Uri
            .parse(getFileMethod.getResponseHeader(DeltaVConstants.HEADER_LOCATION).getValue());

        // Rebuild Uri, but without any segments past "OmniFocus.ofocus"

        Uri.Builder builder = new Builder();
        builder.scheme(redirectUri.getScheme());
        builder.encodedAuthority(redirectUri.getEncodedAuthority());

        for (String pathSegment : redirectUri.getPathSegments()) {
          if (pathSegment.equals("OmniFocus.ofocus")) break;
          builder.appendEncodedPath(pathSegment);
        }

        // Save into account manager

        accountManagerHelper.setUserData("URI", builder.build().toString());

        getFileMethod.releaseConnection();
        throw new DownloadRedirectException("Caught and saved redirection");
      } else {
        Log.e(TAG,
            "Error code " + statusCode + " " + getFileMethod.getStatusText() + " " + getFileMethod
                .getPath());
        getFileMethod.releaseConnection();
        throw new DownloadConnectionException("Server returned an unexpected response");
      }
    } catch (DownloadConnectionException | DownloadRedirectException | DownloadIOException e) {
      // This block exists so that the finally block is run afterward,
      // properly closing the connection
      throw e;
    } finally {
      client.getHttpConnectionManager().closeIdleConnections(0);
    }
  }

  public interface DownloadFileTaskListener {

    void onFinished(File file);

    void onException(Exception e);
  }

  private static class DownloadException extends IOException {

    private DownloadException(String message) {
      super(message);
    }

    private DownloadException(Throwable throwable) {
      super(throwable);
    }

    private DownloadException(String message, Throwable throwable) {
      super(throwable);
    }
  }

  public static class DownloadRedirectException extends DownloadException {

    DownloadRedirectException(String message) {
      super(message);
    }

    DownloadRedirectException(Throwable throwable) {
      super(throwable);
    }

    DownloadRedirectException(String message, Throwable throwable) {
      super(throwable);
    }
  }

  public static class DownloadConnectionException extends DownloadException {

    DownloadConnectionException(String message) {
      super(message);
    }

    DownloadConnectionException(Throwable throwable) {
      super(throwable);
    }

    DownloadConnectionException(String message, Throwable throwable) {
      super(throwable);
    }
  }

  public static class DownloadIOException extends DownloadException {

    DownloadIOException(String message) {
      super(message);
    }

    DownloadIOException(Throwable throwable) {
      super(throwable);
    }

    DownloadIOException(String message, Throwable throwable) {
      super(throwable);
    }
  }

  /**
   * Downloads the file in the background, using our beautiful DownloadFile method.
   */
  public class DownloadFileTask extends AsyncTask<Void, Void, File> {

    private Exception e = null;
    private DownloadFileTaskListener listener;
    private String name;
    private boolean temporary;

    DownloadFileTask(String name, boolean temporary) {
      this.name = name;
      this.temporary = temporary;
      this.listener = null;
    }

    @Override
    protected File doInBackground(Void... voids) {
      try {
        return downloadFile(name, temporary);
      } catch (Exception e) {
        this.e = e;
        return null;
      }
    }

    @Override
    protected void onPostExecute(File file) {
      super.onPostExecute(file);
      if (listener != null) {
        if (file != null) {
          listener.onFinished(file);
        } else {
          listener.onException(this.e);
        }
      }
    }
  }
}
