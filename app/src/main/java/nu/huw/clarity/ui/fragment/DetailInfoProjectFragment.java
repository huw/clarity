package nu.huw.clarity.ui.fragment;

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;
import butterknife.BindView;
import butterknife.ButterKnife;
import nu.huw.clarity.R;
import nu.huw.clarity.model.Entry;
import nu.huw.clarity.model.Perspective;
import nu.huw.clarity.model.Task;

public class DetailInfoProjectFragment extends DetailInfoFragment {

  @BindView(R.id.textview_detailitem_projecttypevalue)
  TextView textview_detailitem_projecttypevalue;
  @BindView(R.id.textview_detailitem_statusvalue)
  TextView textview_detailitem_statusvalue;
  @BindView(R.id.switch_detailitem_projectcomplete)
  Switch switch_detailitem_projectcomplete;
  @BindView(R.id.textview_detailitem_contextvalue)
  TextView textview_detailitem_contextvalue;
  @BindView(R.id.imagebutton_detailitem_context)
  ImageButton imagebutton_detailitem_context;
  @BindView(R.id.textview_detailitem_flagvalue)
  ToggleButton textview_detailitem_flagvalue;
  @BindView(R.id.textview_detailitem_durationvalue)
  TextView textview_detailitem_durationvalue;
  @BindView(R.id.textview_detailitem_defervalue)
  TextView textview_detailitem_defervalue;
  @BindView(R.id.textview_detailitem_duevalue)
  TextView textview_detailitem_duevalue;
  @BindView(R.id.textview_detailitem_repeatvalue)
  TextView textview_detailitem_repeatvalue;

  public DetailInfoProjectFragment() {
  }

  public static DetailInfoProjectFragment newInstance(Entry entry, Perspective perspective) {

    DetailInfoProjectFragment fragment = new DetailInfoProjectFragment();
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

    View view = inflater.inflate(R.layout.fragment_detail_project, container, false);
    unbinder = ButterKnife.bind(this, view);
    Task project = (Task) entry;

    bindProjectTypeValue(project, textview_detailitem_projecttypevalue);
    bindProjectStatusValue(project, textview_detailitem_statusvalue);
    bindProjectCompleteValue(project, switch_detailitem_projectcomplete);
    bindContextValue(project, textview_detailitem_contextvalue, imagebutton_detailitem_context);
    bindFlaggedValue(project, textview_detailitem_flagvalue);
    bindDurationValue(project, textview_detailitem_durationvalue);
    bindDeferValue(project, textview_detailitem_defervalue);
    bindDueValue(project, textview_detailitem_duevalue);
    bindRepeatValue(project, textview_detailitem_repeatvalue);

    return view;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
  }

  /**
   * Binds the "Type" field
   */
  private void bindProjectTypeValue(Task project, TextView textView) {

    @StringRes int typeStringID = R.string.detail_projectsequential;
    if (project.type.equals("single")) {
      typeStringID = R.string.detail_projectsingleactions;
    } else if (project.type.equals("parallel")) {
      typeStringID = R.string.detail_projectparallel;
    }

    textView.setText(typeStringID);
  }

  /**
   * Binds the "Status" field
   */
  private void bindProjectStatusValue(Task project, TextView textView) {
    if (project.dropped) {
      textView.setText(R.string.detail_dropped);
    }
  }

  /**
   * Binds the "Complete when completing last action" field
   */
  private void bindProjectCompleteValue(Task project, Switch switchButton) {
    if (project.completeWithChildren) {
      switchButton.setChecked(true);
    }
  }
}
