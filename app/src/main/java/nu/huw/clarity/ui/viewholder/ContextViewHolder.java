package nu.huw.clarity.ui.viewholder;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;
import nu.huw.clarity.R;
import nu.huw.clarity.model.Context;
import nu.huw.clarity.model.Perspective;
import nu.huw.clarity.ui.adapter.ListAdapter;

/**
 * A view holder for a context, using R.layout.fragment_context
 */
public class ContextViewHolder extends ListAdapter.ViewHolder {

  private final TextView nameView;
  private final TextView countView;
  private final TextView dueSoonCountView;
  private final TextView overdueCountView;
  private final TextView dueSoonDivider;
  private final TextView overdueDivider;

  public ContextViewHolder(View view, ListAdapter adapter) {

    super(view, adapter);
    nameView = (TextView) view.findViewById(R.id.textview_listitem_name);
    countView = (TextView) view.findViewById(R.id.textview_listitem_count);
    dueSoonCountView = (TextView) view.findViewById(R.id.textview_listitem_countduesoon);
    overdueCountView = (TextView) view.findViewById(R.id.textview_listitem_countoverdue);
    dueSoonDivider = (TextView) view.findViewById(R.id.divider_listitem_duesoon);
    overdueDivider = (TextView) view.findViewById(R.id.divider_listitem_countoverdue);
  }

  public void bind(Context context, android.content.Context androidContext,
      Perspective perspective) {

    this.entry = context;
    long count = this.entry.getCount(perspective);
    long dueSoonCount = this.entry.countDueSoon;
    long overdueCount = this.entry.countOverdue;

    Resources res = androidContext.getResources();

    // If there are no available items, then change the string to read 'no available items'
    // (or language-dependent equivalent). If there are, then use the proper Android string
    // formatting tools to allow international users to properly read the string.

    int countStringID = this.entry.getCountString(count, perspective);
    String countString = res.getString(countStringID, count);

    // For Due Soon or Overdue items, we only display the little card (and divider) if there
    // are any. So there's no need for empty state strings.

    if (dueSoonCount > 0) {
      String dueSoonString = res.getString(R.string.due_soon, dueSoonCount);
      dueSoonCountView.setText(dueSoonString);
    } else {
      dueSoonCountView.setVisibility(View.GONE);
      dueSoonDivider.setVisibility(View.GONE);
    }

    if (overdueCount > 0) {
      String overdueString = res.getString(R.string.overdue, overdueCount);
      overdueCountView.setText(overdueString);
    } else {
      overdueCountView.setVisibility(View.GONE);
      overdueDivider.setVisibility(View.GONE);
    }

    // If inactive, then fade this out

    if (!context.id.equals("NO_CONTEXT") && context.droppedEffective &&
        !context.headerRow) {

      nameView.setTextColor(
          ContextCompat.getColor(androidContext, R.color.disabled_text_light));
    }

    // If the item is a header row, bold it

    if (context.headerRow) {
      nameView.setTypeface(null, Typeface.BOLD);
    }

    nameView.setText(this.entry.name);
    countView.setText(countString);
  }
}
