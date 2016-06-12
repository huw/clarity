package nu.huw.clarity.ui.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

import nu.huw.clarity.R;
import nu.huw.clarity.db.DataModelHelper;
import nu.huw.clarity.model.Task;

/**
 * A view holder for a task, using R.layout.fragment_task
 */
public class TaskViewHolder extends ListAdapter.ViewHolder {

    public final View     view;
    public final TextView nameView;
    public final TextView contextView;
    public final TextView dateView;
    public       Task     task;

    public TaskViewHolder(View view) {

        super(view);
        this.view = view;
        nameView = (TextView) view.findViewById(R.id.name);
        contextView = (TextView) view.findViewById(R.id.context);
        dateView = (TextView) view.findViewById(R.id.date);
    }

    public void bind(Task task, Context androidContext) {

        this.task = task;
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

        int color      = R.color.secondary_text_light;
        int background = 0;

        if (!this.task.isAvailable()) {

            // If the task isn't available, then show the user by changing its colours.

            color = R.color.disabled_text_light;
            nameView.setTextColor(ContextCompat.getColor(androidContext, color));
            contextView.setTextColor(ContextCompat.getColor(androidContext, color));
        } else if (this.task.dueSoon) {
            color = R.color.foreground_due_soon;
            background = R.drawable.background_due_soon;
        } else if (this.task.overdue) {
            color = R.color.foreground_overdue;
            background = R.drawable.background_overdue;
        }

        dateView.setTextColor(ContextCompat.getColor(androidContext, color));
        dateView.setBackgroundResource(background);

        DataModelHelper     dmHelper       = new DataModelHelper(androidContext);
        Map<String, String> contextNameMap = dmHelper.getContextNameMap();
        String              context        = contextNameMap.get(this.task.context);

        if (context == null) context = "";

        nameView.setText(this.task.name);
        dateView.setText(date);
        contextView.setText(context);
    }
}
