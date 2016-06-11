package nu.huw.clarity.ui.adapters;

import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;

import nu.huw.clarity.R;
import nu.huw.clarity.model.Context;

/**
 * A view holder for a context, using R.layout.fragment_context
 */
public class ContextViewHolder extends ListAdapter.ViewHolder {

    public final TextView nameView;
    public final TextView availableView;
    public final TextView dueSoonView;
    public final TextView overdueView;
    public final TextView dueSoonDivider;
    public final TextView overdueDivider;
    public       Context  context;

    public ContextViewHolder(View view) {

        super(view);
        nameView = (TextView) view.findViewById(R.id.name);
        availableView = (TextView) view.findViewById(R.id.available);
        dueSoonView = (TextView) view.findViewById(R.id.due_soon);
        overdueView = (TextView) view.findViewById(R.id.overdue);
        dueSoonDivider = (TextView) view.findViewById(R.id.divider_due_soon);
        overdueDivider = (TextView) view.findViewById(R.id.divider_overdue);
    }

    public void bind(Context context, android.content.Context androidContext) {

        this.context = context;
        int available = this.context.countAvailable;
        int dueSoon   = this.context.countDueSoon;
        int overdue   = this.context.countOverdue;

        Resources res = androidContext.getResources();

        // If there are no available items, then change the string to read 'no available items'
        // (or language-dependent equivalent). If there are, then use the proper Android string
        // formatting tools to allow international users to properly read the string.

        String availableString;
        if (available > 0) {
            availableString = res.getString(R.string.available, available);
        } else {
            availableString = res.getString(R.string.no_available);
        }

        // For Due Soon or Overdue items, we only display the little card (and divider) if there
        // are any. So there's no need for empty state strings.

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

        nameView.setText(this.context.name);
        availableView.setText(availableString);
    }
}
