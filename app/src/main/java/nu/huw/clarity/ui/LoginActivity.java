package nu.huw.clarity.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import butterknife.ButterKnife;
import nu.huw.clarity.R;
import nu.huw.clarity.account.AccountManagerHelper;
import nu.huw.clarity.ui.fragment.LoginOmniSyncFragment;
import nu.huw.clarity.ui.fragment.LoginOmniSyncFragment.OnOmniSyncLoginSuccessListener;
import nu.huw.clarity.ui.fragment.LoginOtherSyncFragment;
import nu.huw.clarity.ui.fragment.LoginOtherSyncFragment.OnOtherSyncLoginSuccessListener;
import nu.huw.clarity.ui.fragment.LoginServerFragment;

public class LoginActivity extends AppCompatActivity implements OnOmniSyncLoginSuccessListener,
    OnOtherSyncLoginSuccessListener {

  public static final String KEY_REQUEST = "REQUEST";
  public static final String KEY_ACCOUNTTYPE = "TYPE";
  public static final int VALUE_NOACCOUNT = 0;
  public static final int VALUE_LOGINAGAIN = 1;
  private static final String TAG = LoginActivity.class.getSimpleName();
  static int RESULT_OK = 1;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
    ButterKnife.bind(this);

    // Look at intent for what to show

    Intent intent = getIntent();
    Fragment fragment;

    switch (intent.getIntExtra(KEY_REQUEST, VALUE_NOACCOUNT)) {
      case VALUE_LOGINAGAIN:

        // Log in again with the given login fragment

        String type = intent.getStringExtra(KEY_ACCOUNTTYPE);
        if (type == null || type.equals(AccountManagerHelper.TYPE_OMNISYNC)) {
          fragment = LoginOmniSyncFragment.newInstance();
        } else {
          fragment = LoginOtherSyncFragment.newInstance();
        }
        break;

      case VALUE_NOACCOUNT:
      default:
        fragment = LoginServerFragment.newInstance();
        break;
    }

    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.framelayout_login_container, fragment)
        .commit();
  }

  @Override
  public void onBackPressed() {

    // If there's things on the backstack, go back to them.
    // Then quit.

    if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
      super.onBackPressed();
    } else {
      moveTaskToBack(true);
    }
  }

  @Override
  public void onOmniSyncLoginSuccess() {
    setResult(RESULT_OK);
    finish();
  }

  @Override
  public void onOtherSyncLoginSuccess() {
    onOmniSyncLoginSuccess();
  }
}
