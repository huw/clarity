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

/**
 * A view holder for a folder, using R.layout.fragment_folder
 */
public class NestedTaskViewHolder extends ListAdapter.ViewHolder {

  @BindView(R.id.textview_listitem_name)
  TextView textview_listitem_name;
  @BindView(R.id.textview_listitem_count)
  TextView textview_listitem_count;
  @BindView(R.id.textview_listitem_sort)
  TextView textview_listitem_sort;

  public NestedTaskViewHolder(View view, ListAdapter adapter) {
    super(view, adapter);
    ButterKnife.bind(this, view);
  }

  public void bind(Task task, Context androidContext, Perspective perspective) {

    // Set list item name

    int nameTextStyle = task.getNameTextStyle();
    @ColorInt int nameColor = task.getPrimaryTextColor(androidContext);

    textview_listitem_name.setText(task.name);
    textview_listitem_name.setTypeface(null, nameTextStyle);
    textview_listitem_name.setTextColor(nameColor);

    // Set list item count (depends on perspective)

    String countString = task.getCountString(androidContext, perspective);
    @ColorInt int countColor = task.getSecondaryTextColor(androidContext);

    textview_listitem_count.setText(countString);
    textview_listitem_count.setTextColor(countColor);

    // Set the sort string (date due / defer / estimated time / etc.)

    String sortString = task.getSortString(androidContext, perspective);
    int sortTextStyle = task.getSortTextStyle(perspective);
    @ColorInt int sortColor = task.getSortColor(androidContext, perspective);
    @DrawableRes int sortBackgroundDrawable = task.getSortBackgroundDrawable(perspective);

    textview_listitem_sort.setText(sortString);
    textview_listitem_sort.setTypeface(null, sortTextStyle);
    textview_listitem_sort.setTextColor(sortColor);
    textview_listitem_sort.setBackgroundResource(sortBackgroundDrawable);

    this.entry = task;
  }
}
