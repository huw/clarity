package nu.huw.clarity.ui.viewholder;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import nu.huw.clarity.R;
import nu.huw.clarity.model.Perspective;
import nu.huw.clarity.model.Task;
import nu.huw.clarity.ui.adapter.ListAdapter;
import nu.huw.clarity.ui.misc.CheckCircle;
import org.threeten.bp.format.DateTimeFormatter;

/**
 * A view holder for a task, using R.layout.fragment_task
 */
public class TaskViewHolder extends ListAdapter.ViewHolder {

  private static final String TAG = TaskViewHolder.class.getSimpleName();

  @BindView(R.id.textview_listitem_name)
  TextView textview_listitem_name;
  @BindView(R.id.textview_listitem_context)
  TextView textview_listitem_context;
  @BindView(R.id.textview_listitem_date)
  TextView textview_listitem_date;
  @BindView(R.id.checkcircle_listitem)
  CheckCircle checkcircle_listitem;

  public TaskViewHolder(View view, ListAdapter adapter) {
    super(view, adapter);
    ButterKnife.bind(this, view);
  }

  public void bind(final Task task, Context androidContext, Perspective perspective) {

    String date = "";
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy");

    // Due / effective due dates (and italicising)

    if (task.dateDue != null) {
      date = "Due " + task.dateDue.format(dateTimeFormatter);
    } else if (task.dateDueEffective != null) {
      date = "Due " + task.dateDueEffective.format(dateTimeFormatter);
      textview_listitem_date.setTypeface(null, Typeface.ITALIC);
    }

    // Due soon / overdue / unavailable colours & backgrounds

    int color = R.color.secondary_text_light;
    int background = 0;

    if (!task.isRemaining()) {

      // If the task isn't available, then show the user by changing its colours.

      color = R.color.disabled_text_light;
      textview_listitem_name.setTextColor(ContextCompat.getColor(androidContext, color));
      textview_listitem_context.setTextColor(ContextCompat.getColor(androidContext, color));
    } else if (task.dueSoon) {
      color = R.color.foreground_due_soon;
      background = R.drawable.background_due_soon;
    } else if (task.overdue) {
      color = R.color.foreground_overdue;
      background = R.drawable.background_overdue;
    }

    textview_listitem_date.setTextColor(ContextCompat.getColor(androidContext, color));
    textview_listitem_date.setBackgroundResource(background);

    // Bold header row

    if (task.headerRow) {
      textview_listitem_name.setTypeface(null, Typeface.BOLD);
    }

    textview_listitem_name.setText(task.name);
    textview_listitem_date.setText(date);
    if (task.contextID != null) {
      textview_listitem_context.setText(task.getContext(androidContext).name);
    }

    // Check circle
    // Available tasks can have a flag, but they can't have colorised overdue/due soon
    // circles because the user doesn't want to start them yet.
    checkcircle_listitem.setChecked(task.dateCompleted != null);
    checkcircle_listitem.setFlagged(task.flagged);

    if (task.isRemaining()) {
      checkcircle_listitem.setOverdue(task.overdue);
      checkcircle_listitem.setDueSoon(task.dueSoon);
    } else {
      checkcircle_listitem.setOverdue(false);
      checkcircle_listitem.setDueSoon(false);
    }

    // Check circle callback
    // Adapter will handle necessary logic for removal
    checkcircle_listitem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
      }
    });

    this.entry = task;
  }
}
