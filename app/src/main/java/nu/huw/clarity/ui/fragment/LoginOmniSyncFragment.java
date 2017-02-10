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

public class LoginOmniSyncFragment extends Fragment implements SyncLoginTaskListener {

  private static final String TAG = LoginServerFragment.class.getSimpleName();

  @BindView(R.id.switch_loginomnisync_encryption)
  Switch switch_loginomnisync_encryption;
  @BindView(R.id.textinputlayout_loginomnisync_username)
  TextInputLayout textinputlayout_loginomnisync_username;
  @BindView(R.id.textinputlayout_loginomnisync_password)
  TextInputLayout textinputlayout_loginomnisync_password;
  @BindView(R.id.textinputlayout_loginomnisync_passphrase)
  TextInputLayout textinputlayout_loginomnisync_passphrase;
  @BindView(R.id.textinputedittext_loginomnisync_username)
  TextInputEditText textinputedittext_loginomnisync_username;
  @BindView(R.id.textinputedittext_loginomnisync_password)
  TextInputEditText textinputedittext_loginomnisync_password;
  @BindView(R.id.textinputedittext_loginomnisync_passphrase)
  TextInputEditText textinputedittext_loginomnisync_passphrase;
  @BindView(R.id.progressbar_loginomnisync_spinner)
  ProgressBar progressbar_loginomnisync_spinner;
  @BindView(R.id.relativelayout_loginomnisync_form)
  RelativeLayout relativelayout_loginomnisync_form;
  @BindView(R.id.button_loginomnisync_signin)
  Button button_loginomnisync_signin;

  private Unbinder unbinder;
  private AccountManagerHelper accountManagerHelper;
  private SyncLoginTask syncLoginTask;
  private OnOmniSyncLoginSuccessListener omniSyncLoginSuccessListener;

  public LoginOmniSyncFragment() {
  }

  public static LoginOmniSyncFragment newInstance() {
    LoginOmniSyncFragment fragment = new LoginOmniSyncFragment();
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

    View view = inflater.inflate(R.layout.fragment_login_omnisync, container, false);
    unbinder = ButterKnife.bind(this, view);

    // Listen for a switch change to display the passphrase view

    switch_loginomnisync_encryption.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {

        // If the switch is checked, then make the passphrase input invisible (it is unnecessary)
        // Otherwise, show it again.
        // Animations are handled automatically with the animateLayoutChanges flag.

        Switch passphraseSwitch = (Switch) view;
        if (passphraseSwitch.isChecked()) {
          textinputlayout_loginomnisync_passphrase.setVisibility(View.GONE);
        } else {
          textinputlayout_loginomnisync_passphrase.setVisibility(View.VISIBLE);
        }
      }
    });

    // Listen for attempts to sign in

    button_loginomnisync_signin.setOnClickListener(new View.OnClickListener() {
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

    if (context instanceof OnOmniSyncLoginSuccessListener) {
      omniSyncLoginSuccessListener = (OnOmniSyncLoginSuccessListener) context;
    } else {
      throw new RuntimeException(
          context.toString() + " must implement OnOmniSyncLoginSuccessListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    omniSyncLoginSuccessListener = null;
  }

  void attemptLogin() {

    // If we're currently logging in, don't try again

    if (syncLoginTask.isRunning) return;

    // Reset error fields

    textinputlayout_loginomnisync_username.setErrorEnabled(false);
    textinputlayout_loginomnisync_password.setErrorEnabled(false);
    textinputlayout_loginomnisync_passphrase.setErrorEnabled(false);
    textinputlayout_loginomnisync_username.setError(null);
    textinputlayout_loginomnisync_password.setError(null);
    textinputlayout_loginomnisync_passphrase.setError(null);

    // Store values at the time of the login attempt

    String username = textinputedittext_loginomnisync_username.getText().toString();
    String password = textinputedittext_loginomnisync_password.getText().toString();
    String passphrase = textinputedittext_loginomnisync_passphrase.getText().toString();

    // Check if password should be used as passphrase

    boolean separatePassphrase = switch_loginomnisync_encryption.isChecked();
    if (separatePassphrase) {
      passphrase = password;
    }

    // Validate username

    if (username.isEmpty()) {
      textinputlayout_loginomnisync_username.setErrorEnabled(true);
      textinputlayout_loginomnisync_username
          .setError(getString(R.string.login_errorusernamerequired));
      textinputedittext_loginomnisync_username.requestFocus();
      return;
    }

    // Validate password

    if (password.isEmpty()) {
      textinputlayout_loginomnisync_password.setErrorEnabled(true);
      textinputlayout_loginomnisync_password
          .setError(getString(R.string.login_errorpasswordrequired));
      textinputedittext_loginomnisync_password.requestFocus();
      return;
    }

    // Validate passphrase

    if (separatePassphrase && passphrase.isEmpty()) {
      textinputlayout_loginomnisync_passphrase.setErrorEnabled(true);
      textinputlayout_loginomnisync_passphrase
          .setError(getString(R.string.login_errorpassphraserequired));
      textinputedittext_loginomnisync_passphrase.requestFocus();
      return;
    }

    // Show the progress spinner (for now)

    toggleProgressSpinner(true);

    // Hide the keyboard

    InputMethodManager imm =
        (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(relativelayout_loginomnisync_form.getWindowToken(), 0);

    // Grab Omni Sync server constants

    Uri omniSyncUri = new Uri.Builder()
        .scheme("https")
        .authority("sync.omnigroup.com")
        .appendPath(username)
        .build();

    // Run login task

    syncLoginTask.setDetails(omniSyncUri, username, password, passphrase);
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

    if (omniSyncLoginSuccessListener != null) {
      omniSyncLoginSuccessListener.onOmniSyncLoginSuccess();
    }
  }

  @Override
  public void onSyncLoginFailure(Integer errorType) {

    Log.i(TAG, "FAILURE+ " + errorType);

    syncLoginTask = new SyncLoginTask(getContext(), this); // Reinitialise
    toggleProgressSpinner(false);

    switch (errorType) {

      case SyncLoginTask.SERVER_INVALID:
        textinputlayout_loginomnisync_username.setErrorEnabled(true);
        textinputlayout_loginomnisync_username
            .setError(getString(R.string.login_error_usernameincorrect));
        textinputlayout_loginomnisync_username.requestFocus();
        break;

      case SyncLoginTask.SERVER_FAULT:
        textinputlayout_loginomnisync_username.setErrorEnabled(true);
        textinputlayout_loginomnisync_username
            .setError(getString(R.string.login_error_serverfault));
        break;

      case SyncLoginTask.SERVER_NOOFOCUS:
        textinputlayout_loginomnisync_username.setErrorEnabled(true);
        textinputlayout_loginomnisync_username
            .setError(getString(R.string.login_error_servernoofocus));
        textinputlayout_loginomnisync_username.requestFocus();
        break;

      case SyncLoginTask.PASSWORD_INCORRECT:
        textinputlayout_loginomnisync_password.setErrorEnabled(true);
        textinputlayout_loginomnisync_password
            .setError(getString(R.string.login_error_passwordincorrect));
        textinputlayout_loginomnisync_password.requestFocus();
        break;

      case SyncLoginTask.PASSPHRASE_INCORRECT:
        textinputlayout_loginomnisync_passphrase.setErrorEnabled(true);
        textinputlayout_loginomnisync_passphrase
            .setError(getString(R.string.login_error_passphraseincorrect));
        textinputlayout_loginomnisync_passphrase.requestFocus();
        break;

    }
  }

  /**
   * Changes whether the progress spinner or login form is visible.
   */
  private void toggleProgressSpinner(final boolean showSpinner) {

    int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

    relativelayout_loginomnisync_form.setVisibility(View.VISIBLE);
    relativelayout_loginomnisync_form.animate().setDuration(shortAnimTime)
        .alpha(showSpinner ? 0 : 1)
        .setListener(new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            relativelayout_loginomnisync_form.setVisibility(showSpinner ? View.GONE : View.VISIBLE);
          }
        });
    progressbar_loginomnisync_spinner.animate().setDuration(shortAnimTime)
        .alpha(showSpinner ? 1 : 0)
        .setListener(new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            progressbar_loginomnisync_spinner.setVisibility(showSpinner ? View.VISIBLE : View.GONE);
          }
        });
  }

  public interface OnOmniSyncLoginSuccessListener {

    void onOmniSyncLoginSuccess();
  }
}
