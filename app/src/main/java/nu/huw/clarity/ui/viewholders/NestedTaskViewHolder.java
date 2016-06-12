package nu.huw.clarity.ui.viewholders;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import nu.huw.clarity.R;
import nu.huw.clarity.model.Task;
import nu.huw.clarity.ui.adapters.ListAdapter;

/**
 * A view holder for a folder, using R.layout.fragment_folder
 */
public class NestedTaskViewHolder extends ListAdapter.ViewHolder {

    public final TextView nameView;
    public final TextView remainingView;
    public final TextView dateView;
    public       Task     task;

    public NestedTaskViewHolder(View view) {

        super(view);
        nameView = (TextView) view.findViewById(R.id.name);
        remainingView = (TextView) view.findViewById(R.id.remaining);
        dateView = (TextView) view.findViewById(R.id.date);
    }

    public void bind(Task task, Context androidContext) {

        this.task = task;
        int remaining = this.task.countRemaining;

        Resources res = androidContext.getResources();

        String     date        = "";
        DateFormat localFormat = SimpleDateFormat.getDateInstance();

        // Due dates / effective due dates
        // + italicise effective due dates

        if (this.task.dateDue != null) {
            date = "Due " + localFormat.format(this.task.dateDue);
        } else if (this.task.dateDueEffective != null) {
            date = "Due " + localFormat.format(this.task.dateDueEffective);
            this.dateView.setTypeface(null, Typeface.ITALIC);
        }

        // Due soon / overdue / unavailable backgrounds & colours on items

        int color      = R.color.secondary_text_light;
        int background = 0;

        if (!this.task.isAvailable()) {

            // If the task isn't available, then show the user by changing its colours.

            color = R.color.disabled_text_light;
            nameView.setTextColor(ContextCompat.getColor(androidContext, color));
            remainingView.setTextColor(ContextCompat.getColor(androidContext, color));
        } else if (this.task.dueSoon) {

            color = R.color.foreground_due_soon;
            background = R.drawable.background_due_soon;
        } else if (this.task.overdue) {

            color = R.color.foreground_overdue;
            background = R.drawable.background_overdue;
        }

        dateView.setTextColor(ContextCompat.getColor(androidContext, color));
        dateView.setBackgroundResource(background);

        // Remaining items count

        String remainingString;
        if (remaining > 0) {
            remainingString = res.getString(R.string.remaining, remaining);
        } else {
            remainingString = res.getString(R.string.no_remaining);
        }

        // Bold header row

        if (task.headerRow) {
            nameView.setTypeface(null, Typeface.BOLD);
        }

        nameView.setText(this.task.name);
        dateView.setText(date);
        remainingView.setText(remainingString);
    }
}
