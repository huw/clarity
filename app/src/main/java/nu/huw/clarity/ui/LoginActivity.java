package nu.huw.clarity.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
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

import nu.huw.clarity.R;
import nu.huw.clarity.account.OmniSyncLoginTask;
import nu.huw.clarity.ui.fragments.ErrorDialogFragment;

public class LoginActivity extends AppCompatActivity
        implements ErrorDialogFragment.onErrorDismissListener {

    private static final String            TAG       = LoginActivity.class.getSimpleName();
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private              OmniSyncLoginTask mAuthTask = null;
    /**
     * References for the UI.
     */
    private EditText        mUsernameView;
    private EditText        mPasswordView;
    private TextInputLayout mUsernameIL;
    private TextInputLayout mPasswordIL;
    private View            mProgressView;
    private View            mLoginFormView;
    /**
     * Other
     */
    private AccountManager  mAccountManager;
    private String          mUsername;
    private String          mPassword;
    private boolean         mRetryOnErrorDismiss;

    @Override protected void onCreate(Bundle savedInstanceState) {

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
            @Override public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {

                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View view) {

                attemptLogin();
            }
        });

        mProgressView = findViewById(R.id.login_progress);
        mLoginFormView = findViewById(R.id.login_form);
    }

    /**
     * Attempts to sign in or register the account specified by the login form. If there are form
     * errors (invalid email, missing fields, etc.), the errors are presented and no actual login
     * attempt is made.
     */
    private void attemptLogin() {

        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameIL.setError(null);
        mPasswordIL.setError(null);

        // Store values at the time of the login attempt.
        mUsername = mUsernameView.getText().toString();
        mPassword = mPasswordView.getText().toString();

        boolean cancel    = false;
        View    focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(mPassword)) {
            mPasswordIL.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(mPassword)) {
            mPasswordIL.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(mUsername)) {
            mUsernameIL.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isUsernameValid(mUsername)) {
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

            InputMethodManager imm =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mLoginFormView.getWindowToken(), 0);

            mAuthTask = new OmniSyncLoginTask(mUsername, mPassword, new loginListener());
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
        // It also fades out the UI first. If you're interested in following
        // the logic, the spinner doesn't need a `setVisibility()` immediately
        // because its initial state is blank.

        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1)
                      .setListener(new AnimatorListenerAdapter() {
                          @Override public void onAnimationEnd(Animator animation) {

                              mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                          }
                      });

        mProgressView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0)
                     .setListener(new AnimatorListenerAdapter() {
                         @Override public void onAnimationEnd(Animator animation) {

                             mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                         }
                     });
    }

    public void onErrorDismiss(int resultCode) {

        if (resultCode == Activity.RESULT_OK && mRetryOnErrorDismiss) {
            attemptLogin();
        }
    }

    private void addAccount(String username, String password, String serverDomain,
                            String serverPort) {

        Account account  = new Account(username, this.getPackageName());
        Bundle  userData = new Bundle();
        userData.putString("SERVER_DOMAIN", serverDomain);
        userData.putString("SERVER_PORT", serverPort);

        mAccountManager.addAccountExplicitly(account, password, userData);
        Log.i(TAG, "Added account to system");

        finish();
    }

    public class loginListener implements OmniSyncLoginTask.TaskListener {

        @Override public void onFinished(Bundle result) {

            // Null the task so it can be rebuilt later
            mAuthTask = null;

            if (result.getBoolean("SUCCESS")) {

                addAccount(mUsername, mPassword, result.getString("SERVER_DOMAIN"),
                           result.getString("SERVER_PORT"));
            } else {

                // There are three types of error we can show on this page. Username and password
                // errors are problems with validation/authentication for those specific data types.
                // These errors show in red text below the input box. mLoginError is for connection/
                // other errors which need to be shown in an alert.
                int loginErrorRef    = result.getInt("ERROR_LOGIN");
                int usernameErrorRef = result.getInt("ERROR_USERNAME");
                int passwordErrorRef = result.getInt("ERROR_PASSWORD");

                showProgress(false);

                // The order is a little important here. The most crucial errors,
                // connection issues, should be notified first without showing any
                // other errors. Then username issues should be presented, and
                // finally password errors.

                if (loginErrorRef != 0) {

                    mRetryOnErrorDismiss = result.getBoolean("ERROR_LOGIN_RETRY");

                    DialogFragment errorDialog = new ErrorDialogFragment();
                    Bundle         args        = new Bundle();
                    args.putString("MESSAGE", getString(loginErrorRef));

                    // Text for the positive answer on the button
                    int loginErrorButtonRef = result.getInt("ERROR_LOGIN_BUTTON");
                    if (loginErrorButtonRef != 0) {

                        args.putString("BUTTON_STRING", getString(loginErrorButtonRef));
                    }

                    errorDialog.setArguments(args);
                    errorDialog.show(getSupportFragmentManager(), "Error Dialog");
                } else if (usernameErrorRef != 0) {

                    mUsernameIL.setError(getString(usernameErrorRef));
                    mUsernameIL.requestFocus();
                } else if (passwordErrorRef != 0) {

                    mPasswordIL.setError(getString(passwordErrorRef));
                    mPasswordIL.requestFocus();
                }
            }
        }
    }
}

