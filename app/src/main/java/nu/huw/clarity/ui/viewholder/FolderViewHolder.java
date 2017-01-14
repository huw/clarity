package nu.huw.clarity.ui.viewholder;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import nu.huw.clarity.R;
import nu.huw.clarity.model.Folder;
import nu.huw.clarity.model.Perspective;
import nu.huw.clarity.ui.adapter.ListAdapter;

/**
 * A view holder for a folder, using R.layout.fragment_folder
 */
public class FolderViewHolder extends ListAdapter.ViewHolder {

  @BindView(R.id.textview_listitem_name)
  TextView textview_listitem_name;
  @BindView(R.id.textview_listitem_count)
  TextView textview_listitem_count;
  @BindView(R.id.textview_listitem_countduesoon)
  TextView textview_listitem_countduesoon;
  @BindView(R.id.textview_listitem_countoverdue)
  TextView textview_listitem_countoverdue;
  @BindView(R.id.divider_listitem_countduesoon)
  TextView divider_listitem_countduesoon;
  @BindView(R.id.divider_listitem_countoverdue)
  TextView divider_listitem_countoverdue;

  public FolderViewHolder(View view, ListAdapter adapter) {
    super(view, adapter);
    ButterKnife.bind(this, view);
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
      textview_listitem_countduesoon.setText(dueSoonString);
    } else {
      textview_listitem_countduesoon.setVisibility(View.GONE);
      divider_listitem_countduesoon.setVisibility(View.GONE);
    }

    if (overdueCount > 0) {
      String overdueString = res.getString(R.string.overdue, overdueCount);
      textview_listitem_countoverdue.setText(overdueString);
    } else {
      textview_listitem_countoverdue.setVisibility(View.GONE);
      divider_listitem_countoverdue.setVisibility(View.GONE);
    }

    // Bold header rows

    if (folder.headerRow) {
      textview_listitem_name.setTypeface(null, Typeface.BOLD);
    }

    textview_listitem_name.setText(this.entry.name);
    textview_listitem_count.setText(countString);
  }
}
