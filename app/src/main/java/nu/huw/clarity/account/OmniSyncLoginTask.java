package nu.huw.clarity.account;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.version.DeltaVConstants;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import nu.huw.clarity.R;

/**
 * Authenticate the user on the Omni Sync Server.
 * TODO: Enable private servers.
 */
public class OmniSyncLoginTask extends AsyncTask<Void, Void, Bundle> {

    private static final String TAG = OmniSyncLoginTask.class.getSimpleName();

    public interface TaskListener {
        void onFinished(Bundle result);
    }

    private String mUsername;
    private String mPassword;
    private final TaskListener taskListener;

    public OmniSyncLoginTask(String username, String password, TaskListener listener) {
        mUsername = username;
        mPassword = password;
        taskListener = listener;
    }

    @Override
    protected Bundle doInBackground(Void... params) {

        HttpClient client = new HttpClient();
        Bundle bundle = new Bundle();

        try {
            URI newHost = verifyOmniSyncHost(client, bundle);

            verifyLogin(client, bundle, newHost);
        } catch (UnknownHostException e) {

            bundle.putBoolean("SUCCESS", false);
            bundle.putInt("ERROR_LOGIN", R.string.error_no_connection);
            bundle.putBoolean("ERROR_LOGIN_RETRY", true);
            Log.e(TAG, "No connection for login", e);

        } catch (URISyntaxException e) {

            bundle.putBoolean("SUCCESS", false);
            Log.e(TAG, "Omni Sync Server returned invalid redirection URI", e);
        } catch (Exception e) {

            bundle.putBoolean("SUCCESS", false);
            Log.e(TAG, "Problem creating/sending request", e);

        }

        return bundle;
    }

    @Override
    protected void onPostExecute(final Bundle result) {
        super.onPostExecute(result);

        if (this.taskListener != null) {
            this.taskListener.onFinished(result);
        }
    }

    /**
     * Omni Sync Server uses a load balancer to handle their data, so a user could
     * exist on any of (it seems) sync1.omnigroup.com-sync99.omnigroup.com. To find
     * the server, polling sync.omnigroup.com/<username> will issue a 300-series
     * redirect, which newer HttpClients will follow.
     *
     * An HttpMethod will craft an HTTP request, and stores the response once
     * executed so we can access it. It also stores the connection, so
     * `.releaseConnection()` needs to be called once data is received.
     *
     * I thought I was going to be able to use a HEAD method to query the server
     * minimally, but it looks like the Omni Sync Server only responds to DavMethods
     * when it wants to (it responds fine to a HEAD request below). For a run-down
     * on DavMethods, see GetFilesToDownloadTask.
     */
    private URI verifyOmniSyncHost(HttpClient client, Bundle bundle) throws Exception {

        HttpMethod findServerMethod = new PropFindMethod(
                "https://sync.omnigroup.com/" + mUsername,
                DavConstants.PROPFIND_PROPERTY_NAMES,
                DavConstants.DEPTH_1
        );

        client.executeMethod(findServerMethod);
        findServerMethod.releaseConnection();

        // Only use 3xx codes which have a HEADER_LOCATION response
        int statusCode = findServerMethod.getStatusCode();
        if ((301 <= statusCode && statusCode <= 304) || (307 <= statusCode && statusCode <= 308)) {

            Log.i(TAG, "User exists (Redirection caught)");

            URI newHost = new URI(findServerMethod
                    .getResponseHeader(DeltaVConstants.HEADER_LOCATION)
                    .getValue()
            );

            // Return these values so they can be used later
            bundle.putString("SERVER_DOMAIN", newHost.getHost());
            bundle.putString("SERVER_PORT", String.valueOf(newHost.getPort()));

            return newHost;

        } else if (findServerMethod.getStatusText().equals("No such user")) {

            bundle.putBoolean("SUCCESS", false);
            bundle.putInt("ERROR_USERNAME", R.string.error_not_registered);
            Log.w(TAG, "User not registered");

            return null;

        } else {

            bundle.putBoolean("SUCCESS", false);
            bundle.putInt("ERROR_LOGIN", R.string.error_server_fault);
            bundle.putBoolean("ERROR_LOGIN_RETRY", true);
            Log.e(TAG, "Unexpected status " + findServerMethod.getStatusCode() + ": "
                    + findServerMethod.getStatusText());

            return null;
        }
    }

    /**
     * WebDAV Login Verification. Will work with all servers,
     * not just official Omni Sync stuff.
     */
    private void verifyLogin(HttpClient client, Bundle bundle, URI host) throws Exception {

        // Set up the Apache credentials manager
        // These credentials will be autosubmitted with the packet.
        // They will be transported over https, so their connection
        // to the server will be encrypted.

        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
                mUsername,
                mPassword
        );
        AuthScope newHostScope = new AuthScope(
                host.getHost(),
                host.getPort(),
                AuthScope.ANY_REALM
        );

        client.getState().setCredentials(newHostScope, credentials);
        client.getParams().setAuthenticationPreemptive(true);

        HttpMethod testLoginMethod = new HeadMethod(host.toString()
                + "OmniFocus.ofocus/");

        client.executeMethod(testLoginMethod);
        testLoginMethod.releaseConnection();

        int statusCode = testLoginMethod.getStatusCode();

        // HTTP Status 200: OK
        // HTTP Status 401: Unauthorised
        // HTTP Status 404: Not Found
        // Anything else  : Probably a serious problem

        bundle.putBoolean("SUCCESS", false);
        switch (statusCode) {
            case 200:

                bundle.putBoolean("SUCCESS", true);
                Log.i(TAG, "Account credentials correct");
                break;

            case 401:

                bundle.putInt("ERROR_PASSWORD", R.string.error_incorrect_password);
                Log.w(TAG, "Incorrect password entered");
                break;

            case 404:

                bundle.putInt("ERROR_LOGIN", R.string.error_no_ofocus);
                bundle.putInt("ERROR_LOGIN_BUTTON", R.string.got_it);
                Log.w(TAG, "No OmniFocus.ofocus folder");
                break;

            default:
                Log.e(TAG, "Returned HTTP status " + statusCode + ": " +
                        testLoginMethod.getStatusText());
                break;
        }
    }
}
