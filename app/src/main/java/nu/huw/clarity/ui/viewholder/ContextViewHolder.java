package nu.huw.clarity.ui.viewholder;

import android.support.annotation.ColorInt;
import android.support.v7.widget.AppCompatImageView;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import nu.huw.clarity.R;
import nu.huw.clarity.model.Context;
import nu.huw.clarity.model.Perspective;
import nu.huw.clarity.ui.adapter.ListAdapter;

/**
 * A view holder for a context, using R.layout.fragment_context
 */
public class ContextViewHolder extends ListAdapter.ViewHolder {

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
  @BindView(R.id.imageview_listitem_arrow)
  AppCompatImageView imageview_listitem_arrow;

  public ContextViewHolder(View view, ListAdapter adapter) {
    super(view, adapter);
    ButterKnife.bind(this, view);
  }

  public void bind(Context context, android.content.Context androidContext,
      Perspective perspective) {

    // Set list item name

    @ColorInt int nameColor = context.getPrimaryTextColor(androidContext);
    textview_listitem_name.setTextColor(nameColor);
    textview_listitem_name.setText(context.name);


    // Set list item count (depends on perspective)

    String countString = context.getCountString(androidContext, perspective);
    @ColorInt int countColor = context.getSecondaryTextColor(androidContext);

    textview_listitem_count.setText(countString);
    textview_listitem_count.setTextColor(countColor);

    // Set due soon / overdue badges with text

    if (context.countDueSoon > 0) {
      String dueSoonString = context.getCountDueSoonString(androidContext);
      textview_listitem_countduesoon.setText(dueSoonString);
    } else {
      textview_listitem_countduesoon.setVisibility(View.GONE);
      divider_listitem_countduesoon.setVisibility(View.GONE);
    }

    if (context.countOverdue > 0) {
      String overdueString = context.getCountOverdueString(androidContext);
      textview_listitem_countoverdue.setText(overdueString);
    } else {
      textview_listitem_countoverdue.setVisibility(View.GONE);
      divider_listitem_countoverdue.setVisibility(View.GONE);
    }

    this.entry = context;
  }
}
