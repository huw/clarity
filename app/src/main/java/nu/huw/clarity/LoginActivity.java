package nu.huw.clarity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.database.Cursor;
import android.os.AsyncTask;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    private static final String TAG = LoginActivity.class.getName();

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private OmniLoginTask mAuthTask = null;

    // UI references.
    private EditText mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            mUsernameView.setError(getString(R.string.error_invalid_username));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new OmniLoginTask(username, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isUsernameValid(String username) {
        return username.length() >= 2 && !username.contains(" ");
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        // This uses the animation controls to fade in the progress spinner.
        // I don't know how it works (yet), because it came with the login
        // activity template.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    /**
     * Authenticate the user on the Omni Sync Server.
     * TODO: Enable private servers.
     */
    public class OmniLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final String mPassword;

        OmniLoginTask(String username, String password) {
            mUsername = username;
            mPassword = password;
        }

        private String mLoginError = getString(R.string.error_incorrect_password);

        @Override
        protected Boolean doInBackground(Void... params) {

            HttpClient client = new HttpClient();

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

                int statusCode = findServerMethod.getStatusCode();
                if (300 <= statusCode && statusCode < 400) {

                    Log.i(TAG, "Redirection caught");

                    URI newHost = new URI(findServerMethod
                            .getResponseHeader(DeltaVConstants.HEADER_LOCATION)
                            .getValue()
                    );

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

                    DavMethod testLoginMethod = new PropFindMethod(
                            newHost.toString(),
                            DavConstants.PROPFIND_ALL_PROP,
                            DavConstants.DEPTH_0
                    );

                    client.executeMethod(testLoginMethod);
                    testLoginMethod.releaseConnection();

                    return testLoginMethod.succeeded();

                } else if (findServerMethod.getStatusText().equals("No such user")) {
                    mLoginError = getString(R.string.error_not_registered);
                    Log.e(TAG, "User not registered");
                } else {
                    mLoginError = getString(R.string.error_server_fault);
                    Log.e(TAG, "Unexpected status " + findServerMethod.getStatusCode() + ": "
                            + findServerMethod.getStatusText());
                }
            } catch (UnknownHostException e) {
                mLoginError = getString(R.string.error_no_connection);
                Log.e(TAG, "No connection for login.");
            } catch (IOException e) {
                Log.e(TAG, "Problem creating/sending request.");
            } catch (URISyntaxException e) {
                Log.e(TAG, "Omni Sync Server returned invalid redirection URI.");
            }

            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(mLoginError);
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

