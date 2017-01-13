package nu.huw.clarity.ui.viewholder;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;
import nu.huw.clarity.R;
import nu.huw.clarity.model.Folder;
import nu.huw.clarity.model.Perspective;
import nu.huw.clarity.ui.adapter.ListAdapter;

/**
 * A view holder for a folder, using R.layout.fragment_folder
 */
public class FolderViewHolder extends ListAdapter.ViewHolder {

  public final TextView nameView;
  public final TextView remainingView;
  public final TextView dueSoonView;
  public final TextView overdueView;
  public final TextView dueSoonDivider;
  public final TextView overdueDivider;

  public FolderViewHolder(View view, ListAdapter adapter) {

    super(view, adapter);
    nameView = (TextView) view.findViewById(R.id.textview_listitem_name);
    remainingView = (TextView) view.findViewById(R.id.textview_listitem_count);
    dueSoonView = (TextView) view.findViewById(R.id.textview_listitem_countduesoon);
    overdueView = (TextView) view.findViewById(R.id.textview_listitem_countoverdue);
    dueSoonDivider = (TextView) view.findViewById(R.id.divider_listitem_duesoon);
    overdueDivider = (TextView) view.findViewById(R.id.divider_listitem_countoverdue);
  }

  public void bind(Folder folder, Context androidContext, Perspective perspective) {

    this.entry = folder;
    long count = this.entry.getCount(perspective);
    long dueSoonCount = this.entry.countDueSoon;
    long overdueCount = this.entry.countOverdue;

    Resources res = androidContext.getResources();

    // Remaining item count

    int countStringID = this.entry.getCountString(count, perspective);
    String countString = res.getString(countStringID, count);

    // Due soon / overdue badges

    if (dueSoonCount > 0) {
      String dueSoonString = res.getString(R.string.due_soon, dueSoonCount);
      dueSoonView.setText(dueSoonString);
    } else {
      dueSoonView.setVisibility(View.GONE);
      dueSoonDivider.setVisibility(View.GONE);
    }

    if (overdueCount > 0) {
      String overdueString = res.getString(R.string.overdue, overdueCount);
      overdueView.setText(overdueString);
    } else {
      overdueView.setVisibility(View.GONE);
      overdueDivider.setVisibility(View.GONE);
    }

    // Bold header rows

    if (folder.headerRow) {
      nameView.setTypeface(null, Typeface.BOLD);
    }

    nameView.setText(this.entry.name);
    remainingView.setText(countString);
  }
}
