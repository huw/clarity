package nu.huw.clarity.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import nu.huw.clarity.R;
import nu.huw.clarity.model.Context;
import nu.huw.clarity.model.Entry;
import nu.huw.clarity.model.Perspective;

public class DetailInfoContextFragment extends DetailInfoFragment {

  @BindView(R.id.textview_detailitem_statusvalue)
  TextView textview_detailitem_statusvalue;

  public DetailInfoContextFragment() {
  }

  public static DetailInfoContextFragment newInstance(Entry entry, Perspective perspective) {

    DetailInfoContextFragment fragment = new DetailInfoContextFragment();
    Bundle args = new Bundle();
    args.putParcelable("ENTRY", entry);
    args.putParcelable("PERSPECTIVE", perspective);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    Bundle args = getArguments();
    if (args != null) {
      entry = args.getParcelable("ENTRY");
      perspective = args.getParcelable("PERSPECTIVE");
    } else {
      throw new IllegalArgumentException("DetailFragment requires argument bundle");
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_detail_context, container, false);
    unbinder = ButterKnife.bind(this, view);
    Context context = (Context) entry;

    bindContextStatusValue(context, textview_detailitem_statusvalue);

    return view;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
  }

  /**
   * Binds the "Status" field
   */
  private void bindContextStatusValue(Context context, TextView textView) {
    if (context.droppedEffective) {
      textView.setText(R.string.detail_dropped);
    }
  }
}
