package nu.huw.clarity.ui.adapters;

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
}
