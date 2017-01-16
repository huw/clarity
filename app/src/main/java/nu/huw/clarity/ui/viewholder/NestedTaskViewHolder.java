package nu.huw.clarity.ui.viewholder;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.v7.widget.AppCompatImageView;
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
  @BindView(R.id.imageview_listitem_arrow)
  AppCompatImageView imageview_listitem_arrow;

  public NestedTaskViewHolder(View view, ListAdapter adapter) {
    super(view, adapter);
    ButterKnife.bind(this, view);
  }

  public void bind(Task task, Context androidContext, Perspective perspective) {

    // Set list item name

    @ColorInt int nameColor = task.getPrimaryTextColor(androidContext);
    textview_listitem_name.setTextColor(nameColor);
    textview_listitem_name.setText(task.name);

    // Set list item count (depends on perspective)

    String countString = task.getCountString(androidContext, perspective);
    @ColorInt int countColor = task.getSecondaryTextColor(androidContext);

    textview_listitem_count.setText(countString);
    textview_listitem_count.setTextColor(countColor);

    // Set the sort string (date due / defer / estimated time / etc.)

    String dueString = task.getDueString(androidContext);
    int dueTextStyle = task.getDueTextStyle();
    @ColorInt int dueColor = task.getDueColor(androidContext);
    @DrawableRes int dueBackgroundDrawable = task.getDueBackgroundDrawable();

    textview_listitem_sort.setText(dueString);
    textview_listitem_sort.setTypeface(null, dueTextStyle);
    textview_listitem_sort.setTextColor(dueColor);
    textview_listitem_sort.setBackgroundResource(dueBackgroundDrawable);

    this.entry = task;
  }
}
