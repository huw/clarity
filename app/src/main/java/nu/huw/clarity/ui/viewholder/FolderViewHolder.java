package nu.huw.clarity.ui.viewholder;

import android.content.Context;
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

    // Set list item name

    int nameTextStyle = folder.getNameTextStyle();
    textview_listitem_name.setTypeface(null, nameTextStyle);
    textview_listitem_name.setText(folder.name);

    // Set list item count (depends on perspective)

    String countString = folder.getCountString(androidContext, perspective);
    textview_listitem_count.setText(countString);

    // Set due soon / overdue badges with text

    if (folder.countDueSoon > 0) {
      String dueSoonString = folder.getCountDueSoonString(androidContext);
      textview_listitem_countduesoon.setText(dueSoonString);
    } else {
      textview_listitem_countduesoon.setVisibility(View.GONE);
      divider_listitem_countduesoon.setVisibility(View.GONE);
    }

    if (folder.countOverdue > 0) {
      String overdueString = folder.getCountOverdueString(androidContext);
      textview_listitem_countoverdue.setText(overdueString);
    } else {
      textview_listitem_countoverdue.setVisibility(View.GONE);
      divider_listitem_countoverdue.setVisibility(View.GONE);
    }

    this.entry = folder;
  }
}
