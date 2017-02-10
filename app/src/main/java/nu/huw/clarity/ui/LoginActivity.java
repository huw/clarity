package nu.huw.clarity.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import butterknife.ButterKnife;
import nu.huw.clarity.R;
import nu.huw.clarity.ui.fragment.LoginOmniSyncFragment.OnOmniSyncLoginSuccessListener;
import nu.huw.clarity.ui.fragment.LoginServerFragment;

public class LoginActivity extends AppCompatActivity implements OnOmniSyncLoginSuccessListener {

  private static final String TAG = LoginActivity.class.getSimpleName();
  static int RESULT_OK = 1;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
    ButterKnife.bind(this);

    // Setup server picker fragment

    LoginServerFragment loginServerFragment = LoginServerFragment.newInstance();

    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.framelayout_login_container, loginServerFragment)
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
}
