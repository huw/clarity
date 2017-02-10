package nu.huw.clarity.ui.fragment;

import android.accounts.Account;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import nu.huw.clarity.R;
import nu.huw.clarity.account.AccountManagerHelper;
import nu.huw.clarity.account.SyncLoginTask;
import nu.huw.clarity.account.SyncLoginTask.SyncLoginTaskListener;

public class LoginOtherSyncFragment extends Fragment implements SyncLoginTaskListener {

  private static final String TAG = LoginServerFragment.class.getSimpleName();

  @BindView(R.id.switch_loginothersync_encryption)
  Switch switch_loginothersync_encryption;
  @BindView(R.id.textinputlayout_loginothersync_server)
  TextInputLayout textinputlayout_loginothersync_server;
  @BindView(R.id.textinputlayout_loginothersync_username)
  TextInputLayout textinputlayout_loginothersync_username;
  @BindView(R.id.textinputlayout_loginothersync_password)
  TextInputLayout textinputlayout_loginothersync_password;
  @BindView(R.id.textinputlayout_loginothersync_passphrase)
  TextInputLayout textinputlayout_loginothersync_passphrase;
  @BindView(R.id.textinputedittext_loginothersync_server)
  TextInputEditText textinputedittext_loginothersync_server;
  @BindView(R.id.textinputedittext_loginothersync_username)
  TextInputEditText textinputedittext_loginothersync_username;
  @BindView(R.id.textinputedittext_loginothersync_password)
  TextInputEditText textinputedittext_loginothersync_password;
  @BindView(R.id.textinputedittext_loginothersync_passphrase)
  TextInputEditText textinputedittext_loginothersync_passphrase;
  @BindView(R.id.progressbar_loginothersync_spinner)
  ProgressBar progressbar_loginothersync_spinner;
  @BindView(R.id.relativelayout_loginothersync_form)
  RelativeLayout relativelayout_loginothersync_form;
  @BindView(R.id.button_loginothersync_signin)
  Button button_loginothersync_signin;

  private Unbinder unbinder;
  private AccountManagerHelper accountManagerHelper;
  private SyncLoginTask syncLoginTask;
  private OnOtherSyncLoginSuccessListener otherSyncLoginSuccessListener;

  public LoginOtherSyncFragment() {
  }

  public static LoginOtherSyncFragment newInstance() {
    LoginOtherSyncFragment fragment = new LoginOtherSyncFragment();
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    accountManagerHelper = new AccountManagerHelper(getContext());
    syncLoginTask = new SyncLoginTask(getContext(), this);

    // Push the window up when the keyboard is open

    getActivity().getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_ADJUST_PAN);
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_login_othersync, container, false);
    unbinder = ButterKnife.bind(this, view);

    // Listen for a switch change to display the passphrase view

    switch_loginothersync_encryption.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {

        // If the switch is checked, then make the passphrase input invisible (it is unnecessary)
        // Otherwise, show it again.
        // Animations are handled automatically with the animateLayoutChanges flag.

        Switch passphraseSwitch = (Switch) view;
        if (passphraseSwitch.isChecked()) {
          textinputlayout_loginothersync_passphrase.setVisibility(View.GONE);
        } else {
          textinputlayout_loginothersync_passphrase.setVisibility(View.VISIBLE);
        }
      }
    });

    // Listen for attempts to sign in

    button_loginothersync_signin.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        attemptLogin();
      }
    });

    return view;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    if (context instanceof OnOtherSyncLoginSuccessListener) {
      otherSyncLoginSuccessListener = (OnOtherSyncLoginSuccessListener) context;
    } else {
      throw new RuntimeException(
          context.toString() + " must implement OnOtherSyncLoginSuccessListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    otherSyncLoginSuccessListener = null;
  }

  void attemptLogin() {

    // If we're currently logging in, don't try again

    if (syncLoginTask.isRunning) return;

    // Reset error fields

    textinputlayout_loginothersync_server.setErrorEnabled(false);
    textinputlayout_loginothersync_username.setErrorEnabled(false);
    textinputlayout_loginothersync_password.setErrorEnabled(false);
    textinputlayout_loginothersync_passphrase.setErrorEnabled(false);
    textinputlayout_loginothersync_server.setError(null);
    textinputlayout_loginothersync_username.setError(null);
    textinputlayout_loginothersync_password.setError(null);
    textinputlayout_loginothersync_passphrase.setError(null);

    // Store values at the time of the login attempt

    Uri serverUri = Uri.parse(textinputedittext_loginothersync_server.getText().toString());
    String username = textinputedittext_loginothersync_username.getText().toString();
    String password = textinputedittext_loginothersync_password.getText().toString();
    String passphrase = textinputedittext_loginothersync_passphrase.getText().toString();

    // Check if password should be used as passphrase

    boolean separatePassphrase = switch_loginothersync_encryption.isChecked();
    if (separatePassphrase) {
      passphrase = password;
    }

    // Validate server uri
    // Check for nulls, empties, and if the URL is a valid URL

    if (serverUri == null || serverUri.toString() == null || serverUri.toString().isEmpty()
        || !Patterns.WEB_URL.matcher(serverUri.toString()).matches()) {
      textinputlayout_loginothersync_server.setErrorEnabled(true);
      textinputlayout_loginothersync_server
          .setError(getString(R.string.login_errorserverurlinvalid));
      textinputlayout_loginothersync_server.requestFocus();
      return;
    }

    // Validate passphrase

    if (separatePassphrase && passphrase.isEmpty()) {
      textinputlayout_loginothersync_passphrase.setErrorEnabled(true);
      textinputlayout_loginothersync_passphrase
          .setError(getString(R.string.login_errorpassphraserequired));
      textinputedittext_loginothersync_passphrase.requestFocus();
      return;
    }

    // Show the progress spinner (for now)

    toggleProgressSpinner(true);

    // Hide the keyboard

    InputMethodManager imm =
        (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(relativelayout_loginothersync_form.getWindowToken(), 0);

    // Run login task

    syncLoginTask.setDetails(serverUri, username, password, passphrase);
    syncLoginTask.execute();

  }

  @Override
  public void onSyncLoginSuccess(Uri serverUri, String username, String password,
      String passphrase) {

    Log.i(TAG, "SUCCESS " + username + " " + password + " " + passphrase);

    // Add account to system

    Account account = new Account(username, getString(R.string.account_type));
    Bundle userData = new Bundle();
    userData.putString("PASSPHRASE", passphrase);
    userData.putString("URI", serverUri.toString());

    accountManagerHelper.getAccountManager().addAccountExplicitly(account, password, userData);
    Log.i(TAG, "Added account to system");

    // Finish underlying activity

    if (otherSyncLoginSuccessListener != null) {
      otherSyncLoginSuccessListener.onOtherSyncLoginSuccess();
    }
  }

  @Override
  public void onSyncLoginFailure(Integer errorType) {

    Log.i(TAG, "FAILURE+ " + errorType);

    syncLoginTask = new SyncLoginTask(getContext(), this); // Reinitialise
    toggleProgressSpinner(false);

    switch (errorType) {

      case SyncLoginTask.SERVER_INVALID:
        textinputlayout_loginothersync_server.setErrorEnabled(true);
        textinputlayout_loginothersync_server
            .setError(getString(R.string.login_error_serverinvalid));
        textinputlayout_loginothersync_server.requestFocus();
        break;

      case SyncLoginTask.SERVER_FAULT:
        textinputlayout_loginothersync_server.setErrorEnabled(true);
        textinputlayout_loginothersync_server
            .setError(getString(R.string.login_error_serverfault));
        break;

      case SyncLoginTask.SERVER_NOOFOCUS:
        textinputlayout_loginothersync_server.setErrorEnabled(true);
        textinputlayout_loginothersync_server
            .setError(getString(R.string.login_error_servernoofocus));
        textinputlayout_loginothersync_server.requestFocus();
        break;

      case SyncLoginTask.PASSWORD_INCORRECT:
        textinputlayout_loginothersync_password.setErrorEnabled(true);
        textinputlayout_loginothersync_password
            .setError(getString(R.string.login_error_passwordincorrect));
        textinputlayout_loginothersync_password.requestFocus();
        break;

      case SyncLoginTask.PASSPHRASE_INCORRECT:
        if (switch_loginothersync_encryption.isChecked()) {
          textinputlayout_loginothersync_password.setErrorEnabled(true);
          textinputlayout_loginothersync_password
              .setError(getString(R.string.login_error_passphraseincorrect));
          textinputlayout_loginothersync_password.requestFocus();
        } else {
          textinputlayout_loginothersync_passphrase.setErrorEnabled(true);
          textinputlayout_loginothersync_passphrase
              .setError(getString(R.string.login_error_passphraseincorrect));
          textinputlayout_loginothersync_passphrase.requestFocus();
        }
        break;

    }
  }

  /**
   * Changes whether the progress spinner or login form is visible.
   */
  private void toggleProgressSpinner(final boolean showSpinner) {

    int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

    relativelayout_loginothersync_form.setVisibility(View.VISIBLE);
    relativelayout_loginothersync_form.animate().setDuration(shortAnimTime)
        .alpha(showSpinner ? 0 : 1)
        .setListener(new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            relativelayout_loginothersync_form
                .setVisibility(showSpinner ? View.GONE : View.VISIBLE);
          }
        });
    progressbar_loginothersync_spinner.animate().setDuration(shortAnimTime)
        .alpha(showSpinner ? 1 : 0)
        .setListener(new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            progressbar_loginothersync_spinner
                .setVisibility(showSpinner ? View.VISIBLE : View.GONE);
          }
        });
  }

  public interface OnOtherSyncLoginSuccessListener {

    void onOtherSyncLoginSuccess();
  }
}
