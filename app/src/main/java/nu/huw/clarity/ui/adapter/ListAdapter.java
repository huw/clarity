package nu.huw.clarity.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import nu.huw.clarity.R;
import nu.huw.clarity.model.Context;
import nu.huw.clarity.model.Entry;
import nu.huw.clarity.model.Folder;
import nu.huw.clarity.model.Task;
import nu.huw.clarity.ui.fragment.ListFragment.OnListFragmentInteractionListener;
import nu.huw.clarity.ui.viewholder.ContextViewHolder;
import nu.huw.clarity.ui.viewholder.FolderViewHolder;
import nu.huw.clarity.ui.viewholder.NestedTaskViewHolder;
import nu.huw.clarity.ui.viewholder.ProjectViewHolder;
import nu.huw.clarity.ui.viewholder.TaskViewHolder;

/**
 * This class handles the display of the data in the RecyclerView—it's passed a data set and
 * deals with the way that the set's items are distributed amongst the appropriate child views.
 */
public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private static final String TAG = ListAdapter.class.getSimpleName();
    private final OnListFragmentInteractionListener mListener;
    private final android.content.Context           mAndroidContext;
    private final int         TASK_VIEW_TYPE        = 1;
    private final int         CONTEXT_VIEW_TYPE     = 2;
    private final int         FOLDER_VIEW_TYPE      = 3;
    private final int         PROJECT_VIEW_TYPE     = 4;
    private final int         NESTED_TASK_VIEW_TYPE = 5;
    private       Entry       mParent               = null;
    private       List<Entry> mEntries              = new ArrayList<>();

    public ListAdapter(android.content.Context context,
                       OnListFragmentInteractionListener listener) {

        mAndroidContext = context;
        mListener = listener;
    }

    public void setData(Entry parent, List<Entry> items) {

        if (mEntries != null) notifyItemRangeRemoved(0, getItemCount());

        this.mParent = parent;
        this.mEntries = items;

        notifyItemRangeInserted(0, getItemCount());
    }

    @Override public int getItemViewType(int position) {

        Entry item = mEntries.get(position);

        if (item instanceof Task) {
            if (((Task) item).isProject) {
                return PROJECT_VIEW_TYPE;
            } else if (item.hasChildren) {
                return NESTED_TASK_VIEW_TYPE;
            }
        } else if (item instanceof Context) {
            return CONTEXT_VIEW_TYPE;
        } else if (item instanceof Folder) {
            return FOLDER_VIEW_TYPE;
        }
        return TASK_VIEW_TYPE;
    }

    /**
     * Run every time a new row is created, returns the ViewHolder for that row, and inflates a
     * fragment.
     */
    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;

        switch (viewType) {
            case TASK_VIEW_TYPE:
                view = LayoutInflater.from(parent.getContext())
                                     .inflate(R.layout.fragment_task, parent, false);
                return new TaskViewHolder(view, this);
            case CONTEXT_VIEW_TYPE:
                view = LayoutInflater.from(parent.getContext())
                                     .inflate(R.layout.fragment_context, parent, false);
                return new ContextViewHolder(view, this);
            case FOLDER_VIEW_TYPE:
                view = LayoutInflater.from(parent.getContext())
                                     .inflate(R.layout.fragment_folder, parent, false);
                return new FolderViewHolder(view, this);
            case PROJECT_VIEW_TYPE:
                view = LayoutInflater.from(parent.getContext())
                                     .inflate(R.layout.fragment_project, parent, false);
                return new ProjectViewHolder(view, this);
            case NESTED_TASK_VIEW_TYPE:
                view = LayoutInflater.from(parent.getContext())
                                     .inflate(R.layout.fragment_nested_task, parent, false);
                return new NestedTaskViewHolder(view, this);
        }

        return new ViewHolder(new View(mAndroidContext), this);
    }

    /**
     * Run every time the row's inner view is created. This is where the data should be populated.
     */
    @Override public void onBindViewHolder(final ViewHolder holder, int position) {

        int type = holder.getItemViewType();

        switch (type) {
            case TASK_VIEW_TYPE:
                Task task = (Task) mEntries.get(position);
                ((TaskViewHolder) holder).bind(task, mAndroidContext);
                break;
            case CONTEXT_VIEW_TYPE:
                Context context = (Context) mEntries.get(position);
                ((ContextViewHolder) holder).bind(context, this.mAndroidContext);
                break;
            case FOLDER_VIEW_TYPE:
                Folder folder = (Folder) mEntries.get(position);
                ((FolderViewHolder) holder).bind(folder, this.mAndroidContext);
                break;
            case PROJECT_VIEW_TYPE:
                Task project = (Task) mEntries.get(position);
                ((ProjectViewHolder) holder).bind(project, this.mAndroidContext);
                break;
            case NESTED_TASK_VIEW_TYPE:
                Task nestedTask = (Task) mEntries.get(position);
                ((NestedTaskViewHolder) holder).bind(nestedTask, this.mAndroidContext);
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

        return mEntries.size();
    }

    public void removeItem(Entry entry) {

        int position = mEntries.indexOf(entry);
        mEntries.remove(position);
        notifyItemRemoved(position);
    }

    public void enableHeader(boolean enable) {

        if (mParent != null) {
            mParent.headerRow = enable;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final View        view;
        public final ListAdapter adapter;
        public       Entry       entry;

        public ViewHolder(View view, ListAdapter adapter) {

            super(view);
            this.view = view;
            this.adapter = adapter;
        }
    }
}
