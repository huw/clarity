package nu.huw.clarity.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import nu.huw.clarity.R;

public class LoginServerFragment extends Fragment {

  private static final String TAG = LoginServerFragment.class.getSimpleName();
  @BindView(R.id.button_login_omnisync)
  Button button_login_omnisync;
  @BindView(R.id.button_login_othersync)
  Button button_login_othersync;
  private Unbinder unbinder;

  public LoginServerFragment() {
  }

  public static LoginServerFragment newInstance() {
    LoginServerFragment fragment = new LoginServerFragment();
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_login_server, container, false);
    unbinder = ButterKnife.bind(this, view);

    // When the user clicks the 'Use the omni sync server' button, open that screen

    button_login_omnisync.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        LoginOmniSyncFragment loginOmniSyncFragment = LoginOmniSyncFragment.newInstance();

        // Perform transition

        getFragmentManager()
            .beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
            .replace(R.id.framelayout_login_container, loginOmniSyncFragment)
            .addToBackStack(null)
            .commit();
      }
    });

    button_login_othersync.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        LoginOtherSyncFragment loginOtherSyncFragment = LoginOtherSyncFragment.newInstance();

        // Perform transition

        getFragmentManager()
            .beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
            .replace(R.id.framelayout_login_container, loginOtherSyncFragment)
            .addToBackStack(null)
            .commit();
      }
    });

    return view;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
  }
}
