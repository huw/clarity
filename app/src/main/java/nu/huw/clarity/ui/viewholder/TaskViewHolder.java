package nu.huw.clarity.ui.viewholder;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import nu.huw.clarity.R;
import nu.huw.clarity.model.Perspective;
import nu.huw.clarity.model.Task;
import nu.huw.clarity.ui.adapter.ListAdapter;
import nu.huw.clarity.ui.misc.CheckCircle;

/**
 * A view holder for a task, using R.layout.fragment_task
 */
public class TaskViewHolder extends ListAdapter.ViewHolder {

  private static final String TAG = TaskViewHolder.class.getSimpleName();

  @BindView(R.id.textview_listitem_name)
  TextView textview_listitem_name;
  @BindView(R.id.textview_listitem_viewmode)
  TextView textview_listitem_viewmode;
  @BindView(R.id.textview_listitem_sort)
  TextView textview_listitem_sort;
  @BindView(R.id.checkcircle_listitem)
  CheckCircle checkcircle_listitem;

  public TaskViewHolder(View view, ListAdapter adapter) {
    super(view, adapter);
    ButterKnife.bind(this, view);
  }

  public void bind(final Task task, Context androidContext, Perspective perspective) {

    // Set list item name

    @ColorInt int nameColor = task.getPrimaryTextColor(androidContext);
    textview_listitem_name.setTextColor(nameColor);
    textview_listitem_name.setText(task.name);

    // Set the viewMode string (project/context name)

    String viewModeString = task.getViewModeString(androidContext, perspective);
    int viewModeColor = task.getSecondaryTextColor(androidContext);

    textview_listitem_viewmode.setText(viewModeString);
    textview_listitem_viewmode.setTextColor(viewModeColor);

    // Set the due string

    String dueString = task.getDueString(androidContext);
    int dueTextStyle = task.getDueTextStyle();
    @ColorInt int dueColor = task.getDueColor(androidContext);
    @DrawableRes int dueBackgroundDrawable = task.getDueBackgroundDrawable();

    textview_listitem_sort.setText(dueString);
    textview_listitem_sort.setTypeface(null, dueTextStyle);
    textview_listitem_sort.setTextColor(dueColor);
    textview_listitem_sort.setBackgroundResource(dueBackgroundDrawable);

    // Check circle
    // Available tasks can have a flag, but they can't have colourised overdue/due soon
    // circles because the user doesn't want to start them yet.

    checkcircle_listitem.setChecked(task.dateCompleted != null);
    checkcircle_listitem.setFlagged(task.flaggedEffective);

    if (task.isRemaining()) {
      checkcircle_listitem.setOverdue(task.overdue);
      checkcircle_listitem.setDueSoon(task.dueSoon);
    } else {
      checkcircle_listitem.setOverdue(false);
      checkcircle_listitem.setDueSoon(false);
    }

    // Check circle callback
    // Adapter will handle necessary logic for removal

    /*checkcircle_listitem.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
      }
    });*/

    this.entry = task;
  }
}
