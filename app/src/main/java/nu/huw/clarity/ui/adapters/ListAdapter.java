package nu.huw.clarity.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import nu.huw.clarity.R;
import nu.huw.clarity.model.Context;
import nu.huw.clarity.model.Entry;
import nu.huw.clarity.model.Folder;
import nu.huw.clarity.model.Task;
import nu.huw.clarity.ui.fragments.ListFragment.OnListFragmentInteractionListener;
import nu.huw.clarity.ui.viewholders.ContextViewHolder;
import nu.huw.clarity.ui.viewholders.FolderViewHolder;
import nu.huw.clarity.ui.viewholders.NestedTaskViewHolder;
import nu.huw.clarity.ui.viewholders.ProjectViewHolder;
import nu.huw.clarity.ui.viewholders.TaskViewHolder;

/**
 * This class handles the display of the data in the RecyclerView—it's passed a data set and
 * deals with the way that the set's items are distributed amongst the appropriate child views.
 */
public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private static final String TAG = ListAdapter.class.getSimpleName();
    private final OnListFragmentInteractionListener mListener;
    private final List<Entry>                       mValues;
    private final android.content.Context           mContext;

    public ListAdapter(android.content.Context context, List<Entry> items,
                       OnListFragmentInteractionListener listener) {

        mContext = context;
        mValues = items;
        mListener = listener;
    }

    @Override public int getItemViewType(int position) {

        Entry item = mValues.get(position);

        if (item instanceof Task) {
            if (((Task) item).project) {
                return 4;
            } else if (item.hasChildren) {
                return 5;
            }
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
            case 4:  // PROJECT VIEW
                view = LayoutInflater.from(parent.getContext())
                                     .inflate(R.layout.fragment_project, parent, false);
                return new ProjectViewHolder(view);
            case 5:  // NESTED TASK
                view = LayoutInflater.from(parent.getContext())
                                     .inflate(R.layout.fragment_nested_task, parent, false);
                return new NestedTaskViewHolder(view);
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
                ((TaskViewHolder) holder).bind(task, mContext);
                break;
            case 2: // CONTEXT VIEW
                Context context = (Context) mValues.get(position);
                ((ContextViewHolder) holder).bind(context, mContext);
                break;
            case 3: // FOLDER VIEW
                Folder folder = (Folder) mValues.get(position);
                ((FolderViewHolder) holder).bind(folder, mContext);
                break;
            case 4: // PROJECT VIEW
                Task project = (Task) mValues.get(position);
                ((ProjectViewHolder) holder).bind(project, mContext);
                break;
            case 5: // NESTED TASK VIEW
                Task nestedTask = (Task) mValues.get(position);
                ((NestedTaskViewHolder) holder).bind(nestedTask, mContext);
                break;
        }

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {

                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder);
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

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final View view;

        public ViewHolder(View view) {

            super(view);
            this.view = view;
        }
    }
}
