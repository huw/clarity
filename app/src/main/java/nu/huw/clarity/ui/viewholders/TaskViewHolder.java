package nu.huw.clarity.ui.viewholders;

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
import nu.huw.clarity.ui.adapters.ListAdapter;

/**
 * A view holder for a task, using R.layout.fragment_task
 */
public class TaskViewHolder extends ListAdapter.ViewHolder {

    public final View     view;
    public final TextView nameView;
    public final TextView contextView;
    public final TextView dateView;

    public TaskViewHolder(View view) {

        super(view);
        this.view = view;
        nameView = (TextView) view.findViewById(R.id.name);
        contextView = (TextView) view.findViewById(R.id.context);
        dateView = (TextView) view.findViewById(R.id.date);
    }

    public void bind(Task task, Context androidContext) {

        String     date        = "";
        DateFormat localFormat = SimpleDateFormat.getDateInstance();

        // Due / effective due dates (and italicising)

        if (task.dateDue != null) {
            date = "Due " + localFormat.format(task.dateDue);
        } else if (task.dateDueEffective != null) {
            date = "Due " + localFormat.format(task.dateDueEffective);
            this.dateView.setTypeface(null, Typeface.ITALIC);
        }

        // Due soon / overdue / unavailable colours & backgrounds

        int color      = R.color.secondary_text_light;
        int background = 0;

        if (!task.isAvailable()) {

            // If the task isn't available, then show the user by changing its colours.

            color = R.color.disabled_text_light;
            nameView.setTextColor(ContextCompat.getColor(androidContext, color));
            contextView.setTextColor(ContextCompat.getColor(androidContext, color));
        } else if (task.dueSoon) {
            color = R.color.foreground_due_soon;
            background = R.drawable.background_due_soon;
        } else if (task.overdue) {
            color = R.color.foreground_overdue;
            background = R.drawable.background_overdue;
        }

        dateView.setTextColor(ContextCompat.getColor(androidContext, color));
        dateView.setBackgroundResource(background);

        // Get context name & set

        DataModelHelper     dmHelper       = new DataModelHelper(androidContext);
        Map<String, String> contextNameMap = dmHelper.getContextNameMap();
        String              context        = contextNameMap.get(task.context);

        if (context == null) context = "";

        // Bold header row

        if (task.headerRow) {
            nameView.setTypeface(null, Typeface.BOLD);
        }

        nameView.setText(task.name);
        dateView.setText(date);
        contextView.setText(context);

        this.entry = task;
    }
}
