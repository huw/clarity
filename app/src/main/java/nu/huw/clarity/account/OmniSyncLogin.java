package nu.huw.clarity.account;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.client.methods.DavMethod;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.version.DeltaVConstants;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import nu.huw.clarity.R;

/**
 * Authenticate the user on the Omni Sync Server.
 * TODO: Test if the user has an OmniFocus.ofocus, enable private servers.
 */
public class OmniSyncLogin extends AsyncTask<Void, Void, Bundle> {

    private static final String TAG = OmniSyncLogin.class.getSimpleName();

    public interface TaskListener {
        void onFinished(Bundle result);
    }

    private String mUsername;
    private String mPassword;
    private final TaskListener taskListener;

    public OmniSyncLogin(String username, String password, TaskListener listener) {
        mUsername = username;
        mPassword = password;
        taskListener = listener;
    }

    @Override
    protected Bundle doInBackground(Void... params) {

        HttpClient client = new HttpClient();
        Bundle bundle = new Bundle();

        try {
            // Omni Sync Server uses a load balancer to handle their data, so a user could
            // exist on any of (it seems) sync1.omnigroup.com-sync99.omnigroup.com. To find
            // the server, polling sync.omnigroup.com/<username> will issue a 300-series
            // redirect, which newer HttpClients will follow.
            //
            // We don't have that luxury, so we need to issue a request to sync.omni and
            // follow the response packet. We use a PropFindMethod because it's the least
            // damaging of the methods, and we keep it as minimal as possible.

            DavMethod findServerMethod = new PropFindMethod(
                    "https://sync.omnigroup.com/" + mUsername,
                    DavConstants.PROPFIND_ALL_PROP,
                    DavConstants.DEPTH_0
            );

            client.executeMethod(findServerMethod);
            findServerMethod.releaseConnection();

            // Only use 3xx codes which have a HEADER_LOCATION response
            int statusCode = findServerMethod.getStatusCode();
            if (
                    (301 <= statusCode && statusCode <= 304) ||
                            (307 <= statusCode && statusCode <= 308)
                    ) {

                Log.i(TAG, "User exists (Redirection caught)");

                URI newHost = new URI(findServerMethod
                        .getResponseHeader(DeltaVConstants.HEADER_LOCATION)
                        .getValue()
                );
                bundle.putString("SERVER_DOMAIN", newHost.getHost());
                bundle.putString("SERVER_PORT", String.valueOf(newHost.getPort()));

                // Set up the credentials manager
                UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
                        mUsername,
                        mPassword
                );
                AuthScope newHostScope = new AuthScope(
                        newHost.getHost(),
                        newHost.getPort(),
                        AuthScope.ANY_REALM
                );

                client.getState().setCredentials(newHostScope, credentials);
                client.getParams().setAuthenticationPreemptive(true);

                DavMethod testLoginMethod = new PropFindMethod(
                        newHost.toString(),
                        DavConstants.PROPFIND_ALL_PROP,
                        DavConstants.DEPTH_0
                );

                client.executeMethod(testLoginMethod);
                testLoginMethod.releaseConnection();

                if (testLoginMethod.succeeded()) {

                    bundle.putBoolean("SUCCESS", true);
                    Log.i(TAG, "Account credentials correct");

                } else {

                    bundle.putBoolean("SUCCESS", false);
                    bundle.putInt("ERROR_PASSWORD", R.string.error_incorrect_password);
                    Log.w(TAG, "Incorrect password entered");

                }
            } else if (findServerMethod.getStatusText().equals("No such user")) {

                bundle.putBoolean("SUCCESS", false);
                bundle.putInt("ERROR_USERNAME", R.string.error_not_registered);
                Log.w(TAG, "User not registered");

            } else {

                bundle.putBoolean("SUCCESS", false);
                bundle.putInt("ERROR_LOGIN", R.string.error_server_fault);
                Log.e(TAG, "Unexpected status " + findServerMethod.getStatusCode() + ": "
                        + findServerMethod.getStatusText());

            }
        } catch (UnknownHostException e) {

            bundle.putBoolean("SUCCESS", false);
            bundle.putInt("ERROR_LOGIN", R.string.error_no_connection);
            Log.e(TAG, "No connection for login");

        } catch (IOException e) {

            bundle.putBoolean("SUCCESS", false);
            Log.e(TAG, "Problem creating/sending request");

        } catch (URISyntaxException e) {

            bundle.putBoolean("SUCCESS", false);
            Log.e(TAG, "Omni Sync Server returned invalid redirection URI");
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
}
