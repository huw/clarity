package nu.huw.clarity.ui.viewholder;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
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
  public final View view;
  private final TextView nameView;
  private final TextView contextView;
  private final TextView dateView;
  private final CheckCircle checkCircleView;

  public TaskViewHolder(View view, ListAdapter adapter) {

    super(view, adapter);
    this.view = view;
    nameView = (TextView) view.findViewById(R.id.textview_listitem_name);
    contextView = (TextView) view.findViewById(R.id.textview_listitem_context);
    dateView = (TextView) view.findViewById(R.id.textview_listitem_date);
    checkCircleView = (CheckCircle) view.findViewById(R.id.checkcircle_listitem);
  }

  public void bind(final Task task, Context androidContext, Perspective perspective) {

    String date = "";
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy");

    // Due / effective due dates (and italicising)

    if (task.dateDue != null) {
      date = "Due " + task.dateDue.format(dateTimeFormatter);
    } else if (task.dateDueEffective != null) {
      date = "Due " + task.dateDueEffective.format(dateTimeFormatter);
      this.dateView.setTypeface(null, Typeface.ITALIC);
    }

    // Due soon / overdue / unavailable colours & backgrounds

    int color = R.color.secondary_text_light;
    int background = 0;

    if (!task.isRemaining()) {

      // If the task isn't available, then show the user by changing its colours.

      color = R.color.disabled_text_light;
      nameView.setTextColor(ContextCompat.getColor(androidContext, color));
      contextView.setTextColor(ContextCompat.getColor(androidContext, color));
    } else if (task.dueSoon) {
      color = R.color.foreground_due_soon;
      background = R.drawable.background_due_soon;
    } else if (task.overdue) {
      color = R.color.foreground_overdue;
      background = R.drawable.background_overdue;
    }

    dateView.setTextColor(ContextCompat.getColor(androidContext, color));
    dateView.setBackgroundResource(background);

    // Bold header row

    if (task.headerRow) {
      nameView.setTypeface(null, Typeface.BOLD);
    }

    nameView.setText(task.name);
    dateView.setText(date);
    if (task.contextID != null) {
      contextView.setText(task.getContext(androidContext).name);
    }

    // Check circle
    // Available tasks can have a flag, but they can't have colorised overdue/due soon
    // circles because the user doesn't want to start them yet.
    checkCircleView.setChecked(task.dateCompleted != null);
    checkCircleView.setFlagged(task.flagged);

    if (task.isRemaining()) {
      checkCircleView.setOverdue(task.overdue);
      checkCircleView.setDueSoon(task.dueSoon);
    } else {
      checkCircleView.setOverdue(false);
      checkCircleView.setDueSoon(false);
    }

    // Check circle callback
    // Adapter will handle necessary logic for removal
    checkCircleView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
      }
    });

    this.entry = task;
  }
}
