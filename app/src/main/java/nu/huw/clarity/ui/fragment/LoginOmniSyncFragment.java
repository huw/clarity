package nu.huw.clarity.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Switch;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import nu.huw.clarity.R;

public class LoginOmniSyncFragment extends Fragment {

  private static final String TAG = LoginServerFragment.class.getSimpleName();
  @BindView(R.id.switch_loginomnisync_encryption)
  Switch switch_loginomnisync_encryption;
  @BindView(R.id.textinputlayout_loginomnisync_passphrase)
  TextInputLayout textinputlayout_loginomnisync_passphrase;
  private Unbinder unbinder;

  public LoginOmniSyncFragment() {
  }

  public static LoginOmniSyncFragment newInstance() {
    LoginOmniSyncFragment fragment = new LoginOmniSyncFragment();
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

    View view = inflater.inflate(R.layout.fragment_login_omnisync, container, false);
    unbinder = ButterKnife.bind(this, view);

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

    return view;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
  }
}
