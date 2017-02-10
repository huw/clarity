package nu.huw.clarity.account;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import java.io.File;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import nu.huw.clarity.crypto.OmniSyncDecrypter;
import nu.huw.clarity.sync.DownloadHelper;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.version.DeltaVConstants;

/**
 * Authenticate the user on a given server
 */
public class SyncLoginTask extends AsyncTask<Void, Void, Integer> {

  public static final int SERVER_FAULT = 1;
  public static final int SERVER_INVALID = 2;
  public static final int SERVER_NOOFOCUS = 3;
  public static final int PASSWORD_INCORRECT = 4;
  public static final int PASSPHRASE_INCORRECT = 5;

  private static final String TAG = SyncLoginTask.class.getSimpleName();
  private final SyncLoginTaskListener syncLoginTaskListener;

  public boolean isRunning = false;
  private Context androidContext;
  private Uri serverUri = null;
  private String username = null;
  private String password = null;
  private String passphrase = null;
  private boolean requiresAuth = false;

  public SyncLoginTask(@NonNull Context androidContext,
      @Nullable SyncLoginTaskListener syncLoginTaskListener) {
    this.androidContext = androidContext;
    this.syncLoginTaskListener = syncLoginTaskListener;
  }

  public void setDetails(Uri serverUri, String passphrase) {
    this.serverUri = serverUri;
    this.passphrase = passphrase;
    this.username = this.password = null;
    requiresAuth = false;
  }

  public void setDetails(Uri serverUri, String username, String password, String passphrase) {
    this.serverUri = serverUri;
    this.username = username;
    this.password = password;
    this.passphrase = passphrase;
    requiresAuth = true;
  }

  @Override
  protected void onPreExecute() {
    super.onPreExecute();
    isRunning = true;
  }

  @Override
  protected Integer doInBackground(Void... params) {

    HttpClient client = new HttpClient();

    try {

      ////////////////////////////////////
      // Verify host & follow redirects //
      ////////////////////////////////////

      HttpMethod pollUrlMethod = new PropFindMethod(serverUri.toString(),
          DavConstants.PROPFIND_PROPERTY_NAMES, DavConstants.DEPTH_1);
      client.executeMethod(pollUrlMethod);

      // Use status code to see if we can verify on the spot or need to redirect

      int pollUrlStatusCode = pollUrlMethod.getStatusCode();
      if ((pollUrlStatusCode >= 301 && pollUrlStatusCode <= 304) || pollUrlStatusCode == 307
          || pollUrlStatusCode == 308) {

        // Valid redirect codes are 301-304, 307, 308

        Log.i(TAG, "Host exists - Redirection caught");

        // Save the Uri for later

        String redirectUriString = pollUrlMethod.getResponseHeader(DeltaVConstants.HEADER_LOCATION)
            .getValue();
        serverUri = Uri.parse(redirectUriString);

      } else if ((pollUrlStatusCode >= 200 && pollUrlStatusCode < 300) || pollUrlStatusCode == 401
          || pollUrlStatusCode == 403) {

        // In the 200 range, this means nothing went wrong and our server URL is completely valid
        // For 401 or 403, URL is correct and the user simply isn't logged in

        Log.i(TAG, "Host exists");

      } else if (pollUrlStatusCode >= 400 && pollUrlStatusCode < 500) {

        // In the rest of the 400 range, the user has entered an incorrect server URL

        return SERVER_INVALID;

      } else {

        // For anything else, the server is having a problem

        Log.w(TAG, "Host exists but is encountering an error: " + pollUrlStatusCode);
        return SERVER_FAULT;

      }

      pollUrlMethod.releaseConnection();

      /////////////////////////////////////////////
      // Verify credentials and OmniFocus.ofocus //
      /////////////////////////////////////////////

      if (requiresAuth) {

        // Setup Apache credential manager

        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username,
            password);
        AuthScope authScope = new AuthScope(serverUri.getHost(), serverUri.getPort(),
            AuthScope.ANY_REALM);
        client.getState().setCredentials(authScope, credentials);
        client.getParams().setAuthenticationPreemptive(true);

      }

      // Test "OmniFocus.ofocus"

      Uri ofocusUri = serverUri.buildUpon().appendEncodedPath("OmniFocus.ofocus").build();
      HttpMethod testOfocusMethod = new HeadMethod(ofocusUri.toString());
      client.executeMethod(testOfocusMethod);

      int testOfocusStatusCode = testOfocusMethod.getStatusCode();
      if (testOfocusStatusCode >= 200 && testOfocusStatusCode < 300) {

        // Accept any 200-level code as an indication that OmniFocus.ofocus is there and accessible

        Log.i(TAG, "OmniFocus.ofocus exists");

      } else if (testOfocusStatusCode == 401 || testOfocusStatusCode == 403) {

        // A 401 or 403 indicates that the account credentials are incorrect

        return PASSWORD_INCORRECT;

      } else if (testOfocusStatusCode == 404) {

        // A 404 means there is no OmniFocus.ofocus at this location

        return SERVER_NOOFOCUS;

      } else {

        // Any other error is a general server fault

        Log.w(TAG, "Host provided an unexpected response: " + testOfocusStatusCode);
        return SERVER_FAULT;
      }

      testOfocusMethod.releaseConnection();

      ///////////////////////
      // Verify passphrase //
      ///////////////////////

      DownloadHelper downloadHelper = new DownloadHelper(androidContext);
      File encryptionMetadataFile = downloadHelper
          .downloadFile(client, ofocusUri, "encrypted", true);
      new OmniSyncDecrypter(encryptionMetadataFile, passphrase, androidContext, true);

    } catch (InvalidKeyException e) {
      Log.e(TAG, "Invalid passphrase", e);
      return PASSPHRASE_INCORRECT;
    } catch (IllegalStateException | UnknownHostException e) {
      Log.e(TAG, "Invalid server URL", e);
      return SERVER_INVALID;
    } catch (Exception e) {
      Log.e(TAG, "Problem executing web request " + e.getClass() + " " + e.getMessage(), e);
      return SERVER_FAULT;
    } finally {
      client.getHttpConnectionManager().closeIdleConnections(0);
    }

    return null;
  }

  @Override
  protected void onPostExecute(final Integer result) {

    super.onPostExecute(result);

    if (this.syncLoginTaskListener != null) {
      if (result == null) {
        syncLoginTaskListener.onSyncLoginSuccess(serverUri, username, password, passphrase);
      } else {
        syncLoginTaskListener.onSyncLoginFailure(result);
      }
    }

    isRunning = false;
  }

  public interface SyncLoginTaskListener {

    void onSyncLoginSuccess(Uri serverUri, String username, String password, String passphrase);

    void onSyncLoginFailure(Integer errorType);
  }
}
