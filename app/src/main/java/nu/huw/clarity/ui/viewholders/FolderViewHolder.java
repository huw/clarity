package nu.huw.clarity.ui.viewholders;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import nu.huw.clarity.R;
import nu.huw.clarity.model.Folder;
import nu.huw.clarity.ui.adapters.ListAdapter;

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
        nameView = (TextView) view.findViewById(R.id.name);
        remainingView = (TextView) view.findViewById(R.id.remaining);
        dueSoonView = (TextView) view.findViewById(R.id.due_soon);
        overdueView = (TextView) view.findViewById(R.id.overdue);
        dueSoonDivider = (TextView) view.findViewById(R.id.divider_due_soon);
        overdueDivider = (TextView) view.findViewById(R.id.divider_overdue);
    }

    public void bind(Folder folder, Context androidContext) {

        this.entry = folder;
        int remaining = this.entry.countRemaining;
        int dueSoon   = this.entry.countDueSoon;
        int overdue   = this.entry.countOverdue;

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

        // Bold header rows

        if (folder.headerRow) {
            nameView.setTypeface(null, Typeface.BOLD);
        }

        nameView.setText(this.entry.name);
        remainingView.setText(remainingString);
    }
}
