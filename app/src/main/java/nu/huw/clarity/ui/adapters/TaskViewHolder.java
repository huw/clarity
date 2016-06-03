package nu.huw.clarity.ui.adapters;

import android.view.View;
import android.widget.TextView;

import nu.huw.clarity.R;
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
}
