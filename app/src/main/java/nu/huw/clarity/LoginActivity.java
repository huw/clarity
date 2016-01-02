package nu.huw.clarity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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

public class LoginActivity extends AppCompatActivity implements ErrorDialog.onErrorDismissListener {

    private static final String TAG = LoginActivity.class.getSimpleName();

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private OmniLoginTask mAuthTask = null;

    /**
     * References for the UI.
     */
    private EditText mUsernameView;
    private EditText mPasswordView;
    private TextInputLayout mUsernameIL;
    private TextInputLayout mPasswordIL;
    private View mProgressView;
    private View mLoginFormView;

    /**
     * Account-specific things.
     */
    private AccountManager mAccountManager;
    private String mUsername;
    private String mPassword;
    private URI mServerDomain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAccountManager = AccountManager.get(getBaseContext());

        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password);

        mUsernameIL = (TextInputLayout) findViewById(R.id.username_il);
        mPasswordIL = (TextInputLayout) findViewById(R.id.password_il);

        mUsernameIL.setErrorEnabled(true);
        mPasswordIL.setErrorEnabled(true);

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

        mProgressView = findViewById(R.id.login_progress);
        mLoginFormView = findViewById(R.id.login_form);
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
        mUsernameIL.setError(null);
        mPasswordIL.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(password)) {
            mPasswordIL.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordIL.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUsernameIL.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            mUsernameIL.setError(getString(R.string.error_invalid_username));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, hide the keyboard, and kick off a
            // background task to perform the user login attempt.
            showProgress(true);

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mLoginFormView.getWindowToken(), 0);

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

        mLoginFormView.setVisibility(View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onErrorDismiss(int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            attemptLogin();
        }
    }

    /**
     * Authenticate the user on the Omni Sync Server.
     * TODO: Enable private servers.
     */
    public class OmniLoginTask extends AsyncTask<Void, Void, Boolean> {

        OmniLoginTask(String username, String password) {
            mUsername = username;
            mPassword = password;
        }

        // There are three types of error we can show on this page. Username and password
        // errors are problems with validation/authentication for those specific data types.
        // mLoginError is for connection/other errors which need to be shown in an alert.
        private String mLoginError = "";
        private String mUsernameError = "";
        private String mPasswordError = "";

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

                    Log.i(TAG, "Redirection caught (User exists)");

                    URI newHost = new URI(findServerMethod
                            .getResponseHeader(DeltaVConstants.HEADER_LOCATION)
                            .getValue()
                    );
                    mServerDomain = newHost;

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

                    if (testLoginMethod.succeeded()) {
                        Log.i(TAG, "Account credentials correct");
                        return true;
                    } else {
                        mPasswordError = getString(R.string.error_incorrect_password);
                        Log.e(TAG, "Incorrect password entered");
                    }
                } else if (findServerMethod.getStatusText().equals("No such user")) {
                    mUsernameError = getString(R.string.error_not_registered);
                    Log.e(TAG, "User not registered");
                } else {
                    mLoginError = getString(R.string.error_server_fault);
                    Log.e(TAG, "Unexpected status " + findServerMethod.getStatusCode() + ": "
                            + findServerMethod.getStatusText());
                }
            } catch (UnknownHostException e) {
                mLoginError = getString(R.string.error_no_connection);
                Log.e(TAG, "No connection for login");
            } catch (IOException e) {
                Log.e(TAG, "Problem creating/sending request");
            } catch (URISyntaxException e) {
                Log.e(TAG, "Omni Sync Server returned invalid redirection URI");
            }

            // Catch all errors
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                addAccount();
            } else {

                showProgress(false);

                // The order is a little important here. The most crucial errors,
                // connection issues, should be notified first without showing any
                // other errors. Then username issues should be presented, and
                // finally password errors.

                if (!mLoginError.isEmpty()) {

                    DialogFragment errorDialog = new ErrorDialog();
                    Bundle args = new Bundle();
                    args.putString("message", mLoginError);
                    errorDialog.setArguments(args);
                    errorDialog.show(getSupportFragmentManager(), "Error Dialog");

                } else if (!mUsernameError.isEmpty()) {

                    mUsernameIL.setError(mUsernameError);
                    mUsernameIL.requestFocus();

                } else if (!mPasswordError.isEmpty()) {

                    mPasswordIL.setError(mPasswordError);
                    mPasswordIL.requestFocus();

                }
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private void addAccount() {

        Account account = new Account(mUsername, this.getPackageName());
        Bundle userData = new Bundle();
        userData.putString("SERVER_DOMAIN", mServerDomain.getHost());

        mAccountManager.addAccountExplicitly(account, mPassword, userData);

        finish();
    }
}

