package nu.huw.clarity.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import nu.huw.clarity.R;

public class LoginServerFragment extends Fragment {

  private static final String TAG = LoginServerFragment.class.getSimpleName();
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

    return view;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
  }
}
