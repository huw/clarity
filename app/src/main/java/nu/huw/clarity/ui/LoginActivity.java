package nu.huw.clarity.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import nu.huw.clarity.R;
import nu.huw.clarity.account.OmniSyncLoginTask;
import nu.huw.clarity.ui.fragment.ErrorDialogFragment;

public class LoginActivity extends AppCompatActivity
    implements ErrorDialogFragment.onErrorDismissListener {

  private static final String TAG = LoginActivity.class.getSimpleName();
  static int RESULT_OK = 1;

  @BindView(R.id.textinputedittext_login_username)
  TextInputEditText textinputedittext_login_username;
  @BindView(R.id.textinputedittext_login_password)
  TextInputEditText textinputedittext_login_password;
  @BindView(R.id.textinputlayout_login_username)
  TextInputLayout textinputlayout_login_username;
  @BindView(R.id.textinputlayout_login_password)
  TextInputLayout textinputlayout_login_password;
  @BindView(R.id.progressbar_login_spinner)
  ProgressBar progressbar_login_spinner;
  @BindView(R.id.relativelayout_login_form)
  RelativeLayout relativelayout_login_form;
  @BindView(R.id.button_login_signin)
  Button button_login_signin;

  // Keep track of login task so we can cancel it later
  private OmniSyncLoginTask omniSyncLoginTask = null;
  private AccountManager accountManager;
  private String username;
  private String password;
  private boolean retryOnErrorDismiss;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
    ButterKnife.bind(this);

    accountManager = AccountManager.get(getBaseContext());

    // Set up the login form.
    textinputlayout_login_username.setErrorEnabled(true);
    textinputlayout_login_password.setErrorEnabled(true);

    textinputedittext_login_password
        .setOnEditorActionListener(new TextView.OnEditorActionListener() {
          @Override
          public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {

            if (id == R.id.login || id == EditorInfo.IME_NULL) {
              attemptLogin();
              return true;
            }
            return false;
          }
        });

    button_login_signin.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {

        attemptLogin();
      }
    });
  }

  /**
   * Attempts to sign in or register the account specified by the login form. If there are form
   * errors (invalid email, missing fields, etc.), the errors are presented and no actual login
   * attempt is made.
   */
  private void attemptLogin() {

    if (omniSyncLoginTask != null) {
      return;
    }

    // Reset errors.
    textinputlayout_login_username.setError(null);
    textinputlayout_login_password.setError(null);

    // Store values at the time of the login attempt.
    username = textinputedittext_login_username.getText().toString();
    password = textinputedittext_login_password.getText().toString();

    boolean cancel = false;
    View focusView = null;

    // Check for a valid password.
    if (TextUtils.isEmpty(password)) {
      textinputlayout_login_password.setError(getString(R.string.error_field_required));
      focusView = textinputedittext_login_password;
      cancel = true;
    } else if (!isPasswordValid(password)) {
      textinputlayout_login_password.setError(getString(R.string.error_invalid_password));
      focusView = textinputedittext_login_password;
      cancel = true;
    }

    // Check for a valid email address.
    if (TextUtils.isEmpty(username)) {
      textinputlayout_login_username.setError(getString(R.string.error_field_required));
      focusView = textinputedittext_login_username;
      cancel = true;
    } else if (!isUsernameValid(username)) {
      textinputlayout_login_username.setError(getString(R.string.error_invalid_username));
      focusView = textinputedittext_login_username;
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
      imm.hideSoftInputFromWindow(relativelayout_login_form.getWindowToken(), 0);

      omniSyncLoginTask = new OmniSyncLoginTask(username, password, new LoginListener());
      omniSyncLoginTask.execute((Void) null);
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

    relativelayout_login_form.setVisibility(View.VISIBLE);
    relativelayout_login_form.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1)
        .setListener(new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {

            relativelayout_login_form.setVisibility(show ? View.GONE : View.VISIBLE);
          }
        });

    progressbar_login_spinner.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0)
        .setListener(new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {

            progressbar_login_spinner.setVisibility(show ? View.VISIBLE : View.GONE);
          }
        });
  }

  public void onErrorDismiss(int resultCode) {

    if (resultCode == Activity.RESULT_OK && retryOnErrorDismiss) {
      attemptLogin();
    }
  }

  private void addAccount(String username, String password, String serverDomain,
      String serverPort) {

    Account account = new Account(username, this.getPackageName());
    Bundle userData = new Bundle();
    userData.putString("SERVER_DOMAIN", serverDomain);
    userData.putString("SERVER_PORT", serverPort);

    accountManager.addAccountExplicitly(account, password, userData);
    Log.i(TAG, "Added account to system");

    setResult(RESULT_OK);
    finish();
  }

  private class LoginListener implements OmniSyncLoginTask.TaskListener {

    @Override
    public void onFinished(Bundle result) {

      // Null the task so it can be rebuilt later
      omniSyncLoginTask = null;

      if (result.getBoolean("SUCCESS")) {

        addAccount(username, password, result.getString("SERVER_DOMAIN"),
            result.getString("SERVER_PORT"));
      } else {

        // There are three types of error we can show on this page. Username and password
        // errors are problems with validation/authentication for those specific data types.
        // These errors show in red text below the input box. mLoginError is for connection/
        // other errors which need to be shown in an alert.
        int loginErrorRef = result.getInt("ERROR_LOGIN");
        int usernameErrorRef = result.getInt("ERROR_USERNAME");
        int passwordErrorRef = result.getInt("ERROR_PASSWORD");

        showProgress(false);

        // The order is a little important here. The most crucial errors,
        // connection issues, should be notified first without showing any
        // other errors. Then username issues should be presented, and
        // finally password errors.

        if (loginErrorRef != 0) {

          retryOnErrorDismiss = result.getBoolean("ERROR_LOGIN_RETRY");

          DialogFragment errorDialog = new ErrorDialogFragment();
          Bundle args = new Bundle();
          args.putString("MESSAGE", getString(loginErrorRef));

          // Text for the positive answer on the button
          int loginErrorButtonRef = result.getInt("ERROR_LOGIN_BUTTON");
          if (loginErrorButtonRef != 0) {

            args.putString("BUTTON_STRING", getString(loginErrorButtonRef));
          }

          errorDialog.setArguments(args);
          errorDialog.show(getSupportFragmentManager(), "Error Dialog");
        } else if (usernameErrorRef != 0) {

          textinputlayout_login_username.setError(getString(usernameErrorRef));
          textinputlayout_login_username.requestFocus();
        } else if (passwordErrorRef != 0) {

          textinputlayout_login_password.setError(getString(passwordErrorRef));
          textinputlayout_login_password.requestFocus();
        }
      }
    }
  }
}

