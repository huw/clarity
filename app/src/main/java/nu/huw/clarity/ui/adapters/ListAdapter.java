package nu.huw.clarity.ui.adapters;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import nu.huw.clarity.R;
import nu.huw.clarity.db.DataModelHelper;
import nu.huw.clarity.model.Context;
import nu.huw.clarity.model.Entry;
import nu.huw.clarity.model.Folder;
import nu.huw.clarity.model.Task;
import nu.huw.clarity.ui.fragments.ListFragment.OnListFragmentInteractionListener;

/**
 * This class handles the display of the data in the RecyclerView—it's passed a data set and
 * deals with the way that the set's items are distributed amongst the appropriate child views.
 */
public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private static final String TAG = ListAdapter.class.getSimpleName();
    private final OnListFragmentInteractionListener mListener;
    private final List<Entry>                       mValues;
    private final Map<String, String>               mContextNameMap;
    private final android.content.Context           mContext;

    public ListAdapter(android.content.Context context, List items,
                       OnListFragmentInteractionListener listener) {

        mContext = context;
        mValues = items;
        mListener = listener;
        DataModelHelper DMHelper = new DataModelHelper(context);
        mContextNameMap = DMHelper.getContextNameMap();
    }

    @Override public int getItemViewType(int position) {

        Entry item = mValues.get(position);

        if (item instanceof Task) {
            return 1;
        } else if (item instanceof Context) {
            return 2;
        } else if (item instanceof Folder) {
            return 3;
        }

        return 1;
    }

    /**
     * Run every time a new row is created, returns the ViewHolder for that row, and inflates a
     * fragment.
     */
    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;

        switch (viewType) {
            case 1: // TASK VIEW
                view = LayoutInflater.from(parent.getContext())
                                     .inflate(R.layout.fragment_task, parent, false);
                return new TaskViewHolder(view);
            case 2:  // CONTEXT VIEW
                view = LayoutInflater.from(parent.getContext())
                                     .inflate(R.layout.fragment_context, parent, false);
                return new ContextViewHolder(view);
            case 3:  // FOLDER VIEW
                view = LayoutInflater.from(parent.getContext())
                                     .inflate(R.layout.fragment_folder, parent, false);
                return new FolderViewHolder(view);
        }

        return new ViewHolder(new View(mContext));
    }

    /**
     * Run every time the row's inner view is created. This is where the data should be populated.
     */
    @Override public void onBindViewHolder(final ViewHolder holder, int position) {

        int type = holder.getItemViewType();

        switch (type) {
            case 1: // TASK VIEW
                Task task = (Task) mValues.get(position);
                bindTaskViewHolder((TaskViewHolder) holder, task);
                break;
            case 2: // CONTEXT VIEW
                Context context = (Context) mValues.get(position);
                bindContextViewHolder((ContextViewHolder) holder, context);
                break;
            case 3: // FOLDER VIEW
                Folder folder = (Folder) mValues.get(position);
                bindFolderViewHolder((FolderViewHolder) holder, folder);
                break;
        }

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {

                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    //mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    /**
     * This is where we must get the number of values. Good thing it's easy!
     */
    @Override public int getItemCount() {

        return mValues.size();
    }

    private void bindTaskViewHolder(TaskViewHolder holder, Task task) {

        holder.task = task;
        String     date        = "";
        DateFormat localFormat = SimpleDateFormat.getDateInstance();

        // Navigate between date due and effective date due, and also set the date due view to
        // italics if it's an effective due date.
        if (holder.task.dateDue != null) {

            date = "Due " + localFormat.format(holder.task.dateDue);
        } else if (holder.task.dateDueEffective != null) {

            date = "Due " + localFormat.format(holder.task.dateDueEffective);
            holder.dateView.setTypeface(null, Typeface.ITALIC);
        }

        // Change colours and backgrounds if it's due soon or overdue.

        int color      = android.R.color.secondary_text_light;
        int background = 0;

        if (holder.task.dueSoon) {
            color = R.color.foreground_due_soon;
            background = R.drawable.background_due_soon;
        } else if (holder.task.overdue) {
            color = R.color.foreground_overdue;
            background = R.drawable.background_overdue;
        }

        holder.dateView.setTextColor(ContextCompat.getColor(mContext, color));
        holder.dateView.setBackgroundResource(background);

        String context = mContextNameMap.get(holder.task.context);

        if (context == null) context = "";

        holder.nameView.setText(holder.task.name);
        holder.dateView.setText(date);
        holder.contextView.setText(context);
    }

    private void bindContextViewHolder(ContextViewHolder holder, Context context) {

        holder.context = context;
        int available = holder.context.countAvailable;
        int dueSoon   = holder.context.countDueSoon;
        int overdue   = holder.context.countOverdue;

        Resources res = mContext.getResources();

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
            holder.dueSoonView.setText(dueSoonString);
        } else {
            holder.dueSoonView.setVisibility(View.GONE);
            holder.dueSoonDivider.setVisibility(View.GONE);
        }

        if (overdue > 0) {
            String overdueString = res.getString(R.string.overdue, overdue);
            holder.overdueView.setText(overdueString);
        } else {
            holder.overdueView.setVisibility(View.GONE);
            holder.overdueDivider.setVisibility(View.GONE);
        }

        holder.nameView.setText(holder.context.name);
        holder.availableView.setText(availableString);
    }

    private void bindFolderViewHolder(FolderViewHolder holder, Folder folder) {

        holder.folder = folder;
        int remaining = holder.folder.countRemaining;
        int dueSoon   = holder.folder.countDueSoon;
        int overdue   = holder.folder.countOverdue;

        Resources res = mContext.getResources();

        String remainingString;
        if (remaining > 0) {
            remainingString = res.getString(R.string.remaining, remaining);
        } else {
            remainingString = res.getString(R.string.no_remaining);
        }

        if (dueSoon > 0) {
            String dueSoonString = res.getString(R.string.due_soon, dueSoon);
            holder.dueSoonView.setText(dueSoonString);
        } else {
            holder.dueSoonView.setVisibility(View.GONE);
            holder.dueSoonDivider.setVisibility(View.GONE);
        }

        if (overdue > 0) {
            String overdueString = res.getString(R.string.overdue, overdue);
            holder.overdueView.setText(overdueString);
        } else {
            holder.overdueView.setVisibility(View.GONE);
            holder.overdueDivider.setVisibility(View.GONE);
        }

        holder.nameView.setText(holder.folder.name);
        holder.remainingView.setText(remainingString);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final View view;

        public ViewHolder(View view) {

            super(view);
            this.view = view;
        }
    }
}
