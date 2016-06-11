package nu.huw.clarity.ui.adapters;

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
        int dueSoon   = this.task.countDueSoon;
        int overdue   = this.task.countOverdue;

        Resources res = androidContext.getResources();

        String     date        = "";
        DateFormat localFormat = SimpleDateFormat.getDateInstance();

        // Navigate between date due and effective date due, and also set the date due view to
        // italics if it's an effective due date.
        if (this.task.dateDue != null) {

            date = "Due " + localFormat.format(this.task.dateDue);
        } else if (this.task.dateDueEffective != null) {

            date = "Due " + localFormat.format(this.task.dateDueEffective);
            this.dateView.setTypeface(null, Typeface.ITALIC);
        }

        // Change colours and backgrounds if it's due soon or overdue.

        int color      = android.R.color.secondary_text_light;
        int background = 0;

        if (this.task.dueSoon) {
            color = R.color.foreground_due_soon;
            background = R.drawable.background_due_soon;
        } else if (this.task.overdue) {
            color = R.color.foreground_overdue;
            background = R.drawable.background_overdue;
        }

        dateView.setTextColor(ContextCompat.getColor(androidContext, color));
        dateView.setBackgroundResource(background);

        String remainingString;
        if (remaining > 0) {
            remainingString = res.getString(R.string.remaining, remaining);
        } else {
            remainingString = res.getString(R.string.no_remaining);
        }

        nameView.setText(this.task.name);
        remainingView.setText(remainingString);
    }
}
