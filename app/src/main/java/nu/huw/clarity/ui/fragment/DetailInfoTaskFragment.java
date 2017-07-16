package nu.huw.clarity.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;
import butterknife.BindView;
import butterknife.ButterKnife;
import nu.huw.clarity.R;
import nu.huw.clarity.model_old.Entry;
import nu.huw.clarity.model_old.Perspective;
import nu.huw.clarity.model_old.Task;

public class DetailInfoTaskFragment extends DetailInfoFragment {


  @BindView(R.id.relativelayout_detailitem_project)
  RelativeLayout relativelayout_detailitem_project;
  @BindView(R.id.textview_detailitem_projectvalue)
  TextView textview_detailitem_projectvalue;
  @BindView(R.id.imagebutton_detailitem_project)
  ImageButton imagebutton_detailitem_project;
  @BindView(R.id.relativelayout_detailitem_context)
  RelativeLayout relativelayout_detailitem_context;
  @BindView(R.id.spinner_detailitem_contextvalue)
  Spinner textview_detailitem_contextvalue;
  @BindView(R.id.imagebutton_detailitem_context)
  ImageButton imagebutton_detailitem_context;
  @BindView(R.id.relativelayout_detailitem_flag)
  RelativeLayout relativelayout_detailitem_flag;
  @BindView(R.id.textview_detailitem_flagvalue)
  ToggleButton textview_detailitem_flagvalue;
  @BindView(R.id.relativelayout_detailitem_duration)
  RelativeLayout relativelayout_detailitem_duration;
  @BindView(R.id.textview_detailitem_durationvalue)
  TextView textview_detailitem_durationvalue;
  @BindView(R.id.relativelayout_detailitem_defer)
  RelativeLayout relativelayout_detailitem_defer;
  @BindView(R.id.textview_detailitem_defervalue)
  TextView textview_detailitem_defervalue;
  @BindView(R.id.relativelayout_detailitem_due)
  RelativeLayout relativelayout_detailitem_due;
  @BindView(R.id.textview_detailitem_duevalue)
  TextView textview_detailitem_duevalue;
  @BindView(R.id.relativelayout_detailitem_repeat)
  RelativeLayout relativelayout_detailitem_repeat;
  @BindView(R.id.textview_detailitem_repeatvalue)
  TextView textview_detailitem_repeatvalue;

  public DetailInfoTaskFragment() {
  }

  public static DetailInfoTaskFragment newInstance(Entry entry, Perspective perspective) {

    DetailInfoTaskFragment fragment = new DetailInfoTaskFragment();
    fragment.setRetainInstance(true);
    Bundle args = new Bundle();
    args.putParcelable("ENTRY", entry);
    args.putParcelable("PERSPECTIVE", perspective);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    onAttachToParentFragment(getParentFragment());
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

    View view = inflater.inflate(R.layout.fragment_detail_task, container, false);
    unbinder = ButterKnife.bind(this, view);
    Task task = (Task) entry;

    bindProjectValue(task, relativelayout_detailitem_project, textview_detailitem_projectvalue,
        imagebutton_detailitem_project);
    bindContextValue(task, relativelayout_detailitem_context, textview_detailitem_contextvalue,
        imagebutton_detailitem_context);
    bindFlaggedValue(task, relativelayout_detailitem_flag, textview_detailitem_flagvalue);
    bindDurationValue(task, relativelayout_detailitem_duration, textview_detailitem_durationvalue);
    bindDeferValue(task, relativelayout_detailitem_defer, textview_detailitem_defervalue);
    bindDueValue(task, relativelayout_detailitem_due, textview_detailitem_duevalue);
    bindRepeatValue(task, relativelayout_detailitem_repeat, textview_detailitem_repeatvalue);

    return view;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
  }

  /**
   * Binds the "Project" field
   */
  private void bindProjectValue(Task task, RelativeLayout container, TextView textView,
      ImageButton imageButton) {
    if (task.projectID != null) {
      final Task project = task.getProject(getContext());

      textView.setText(project.name);
      textView.setTextColor(TEXT_COLOR_PRIMARY);

      imageButton.setVisibility(View.VISIBLE);
      imageButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {

          listener.onProjectClick(project, perspective);
        }
      });
    }
  }
}
