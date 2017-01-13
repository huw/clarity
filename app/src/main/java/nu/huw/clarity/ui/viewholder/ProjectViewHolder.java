package nu.huw.clarity.ui.viewholder;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;
import nu.huw.clarity.R;
import nu.huw.clarity.model.Task;
import nu.huw.clarity.ui.adapter.ListAdapter;

/**
 * A view holder for a folder, using R.layout.fragment_folder
 */
public class ProjectViewHolder extends ListAdapter.ViewHolder {

  public final TextView nameView;
  public final TextView remainingView;
  public final TextView dueSoonView;
  public final TextView overdueView;
  public final TextView dueSoonDivider;
  public final TextView overdueDivider;

  public ProjectViewHolder(View view, ListAdapter adapter) {

    super(view, adapter);
    nameView = (TextView) view.findViewById(R.id.textview_listitem_name);
    remainingView = (TextView) view.findViewById(R.id.textview_listitem_count);
    dueSoonView = (TextView) view.findViewById(R.id.textview_listitem_countduesoon);
    overdueView = (TextView) view.findViewById(R.id.textview_listitem_overdue);
    dueSoonDivider = (TextView) view.findViewById(R.id.divider_listitem_duesoon);
    overdueDivider = (TextView) view.findViewById(R.id.divider_listitem_countoverdue);
  }

  public void bind(Task project, Context androidContext) {

    this.entry = project;
    long remaining = this.entry.countRemaining;
    long dueSoon = this.entry.countDueSoon;
    long overdue = this.entry.countOverdue;

    Resources res = androidContext.getResources();

    // Remaining item count

    String remainingString;
    if (remaining > 0) {
      remainingString = res.getString(R.string.remaining, remaining);
    } else {
      remainingString = res.getString(R.string.no_remaining);
    }

    // Due soon / overdue badges

    if (dueSoon > 0) {
      String dueSoonString = res.getString(R.string.due_soon, dueSoon);
      dueSoonView.setText(dueSoonString);
    } else {
      dueSoonView.setVisibility(View.GONE);
      dueSoonDivider.setVisibility(View.GONE);
    }

    if (overdue > 0) {
      String overdueString = res.getString(R.string.overdue, overdue);
      overdueView.setText(overdueString);
    } else {
      overdueView.setVisibility(View.GONE);
      overdueDivider.setVisibility(View.GONE);
    }

    // Bold header row

    if (project.headerRow) {
      nameView.setTypeface(null, Typeface.BOLD);
    }

    nameView.setText(this.entry.name);
    remainingView.setText(remainingString);
  }
}
