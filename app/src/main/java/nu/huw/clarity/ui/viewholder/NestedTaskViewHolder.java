package nu.huw.clarity.ui.viewholder;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;
import nu.huw.clarity.R;
import nu.huw.clarity.model.Perspective;
import nu.huw.clarity.model.Task;
import nu.huw.clarity.ui.adapter.ListAdapter;
import org.threeten.bp.format.DateTimeFormatter;

/**
 * A view holder for a folder, using R.layout.fragment_folder
 */
public class NestedTaskViewHolder extends ListAdapter.ViewHolder {

  public final TextView nameView;
  public final TextView remainingView;
  public final TextView dateView;

  public NestedTaskViewHolder(View view, ListAdapter adapter) {

    super(view, adapter);
    nameView = (TextView) view.findViewById(R.id.textview_listitem_name);
    remainingView = (TextView) view.findViewById(R.id.textview_listitem_count);
    dateView = (TextView) view.findViewById(R.id.textview_listitem_date);
  }

  public void bind(Task task, Context androidContext, Perspective perspective) {

    long count = task.getCount(perspective.filterStatus);

    Resources res = androidContext.getResources();

    String date = "";
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy");

    // Due dates / effective due dates
    // + italicise effective due dates

    if (task.dateDue != null) {
      date = "Due " + task.dateDue.format(dateTimeFormatter);
    } else if (task.dateDueEffective != null) {
      date = "Due " + task.dateDueEffective.format(dateTimeFormatter);
      this.dateView.setTypeface(null, Typeface.ITALIC);
    }

    // Due soon / overdue / unavailable backgrounds & colours on items

    int color = R.color.secondary_text_light;
    int background = 0;

    if (!task.isRemaining()) {

      // If the task isn't available, then show the user by changing its colours.

      color = R.color.disabled_text_light;
      nameView.setTextColor(ContextCompat.getColor(androidContext, color));
      remainingView.setTextColor(ContextCompat.getColor(androidContext, color));
    } else if (task.dueSoon) {

      color = R.color.foreground_due_soon;
      background = R.drawable.background_due_soon;
    } else if (task.overdue) {

      color = R.color.foreground_overdue;
      background = R.drawable.background_overdue;
    }

    dateView.setTextColor(ContextCompat.getColor(androidContext, color));
    dateView.setBackgroundResource(background);

    // Remaining items count

    int countStringID = task.getCountString(count, perspective.filterStatus);
    String countString = res.getString(countStringID, count);

    // Bold header row

    if (task.headerRow) {
      nameView.setTypeface(null, Typeface.BOLD);
    }

    nameView.setText(task.name);
    dateView.setText(date);
    remainingView.setText(countString);

    this.entry = task;
  }
}
