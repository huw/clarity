package nu.huw.clarity.ui.viewholder;

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
import nu.huw.clarity.ui.adapter.ListAdapter;

/**
 * A view holder for a folder, using R.layout.fragment_folder
 */
public class NestedTaskViewHolder extends ListAdapter.ViewHolder {

    public final TextView nameView;
    public final TextView remainingView;
    public final TextView dateView;

    public NestedTaskViewHolder(View view, ListAdapter adapter) {

        super(view, adapter);
        nameView = (TextView) view.findViewById(R.id.name);
        remainingView = (TextView) view.findViewById(R.id.remaining);
        dateView = (TextView) view.findViewById(R.id.date);
    }

    public void bind(Task task, Context androidContext) {

        int remaining = task.countRemaining;

        Resources res = androidContext.getResources();

        String     date        = "";
        DateFormat localFormat = SimpleDateFormat.getDateInstance();

        // Due dates / effective due dates
        // + italicise effective due dates

        if (task.dateDue != null) {
            date = "Due " + localFormat.format(task.dateDue);
        } else if (task.dateDueEffective != null) {
            date = "Due " + localFormat.format(task.dateDueEffective);
            this.dateView.setTypeface(null, Typeface.ITALIC);
        }

        // Due soon / overdue / unavailable backgrounds & colours on items

        int color      = R.color.secondary_text_light;
        int background = 0;

        if (!task.isAvailable()) {

            // If the task isn't available, then show the user by changing its colours.

            color = R.color.disabled_text_light;
            nameView.setTextColor(ContextCompat.getColor(androidContext, color));
            remainingView.setTextColor(ContextCompat.getColor(androidContext, color));
        } else if (task.dueSoon) {

            color = R.color.foreground_due_soon;
            background = R.drawable.background_due_soon;
        } else if (task.overdue) {

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

        nameView.setText(task.name);
        dateView.setText(date);
        remainingView.setText(remainingString);

        this.entry = task;
    }
}