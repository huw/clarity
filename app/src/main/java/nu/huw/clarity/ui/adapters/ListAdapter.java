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
    private final OnListFragmentInteractionListener listener;
    private final List<Entry>                       items;
    private final android.content.Context           context;
    private final Entry                             parentEntry;

    public ListAdapter(android.content.Context context, Entry parent, List<Entry> items,
                       OnListFragmentInteractionListener listener) {

        this.context = context;
        this.parentEntry = parent;
        this.items = items;
        this.listener = listener;

        if (parentEntry != null) {
            this.items.add(0, parentEntry);
        }
    }

    @Override public int getItemViewType(int position) {

        Entry item = items.get(position);

        if (item instanceof Task) {
            if (((Task) item).isProject) {
                return 4;
            } else if (item.hasChildren) {
                return 5;
            }
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
                return new TaskViewHolder(view, this);
            case 2:  // CONTEXT VIEW
                view = LayoutInflater.from(parent.getContext())
                                     .inflate(R.layout.fragment_context, parent, false);
                return new ContextViewHolder(view, this);
            case 3:  // FOLDER VIEW
                view = LayoutInflater.from(parent.getContext())
                                     .inflate(R.layout.fragment_folder, parent, false);
                return new FolderViewHolder(view, this);
            case 4:  // PROJECT VIEW
                view = LayoutInflater.from(parent.getContext())
                                     .inflate(R.layout.fragment_project, parent, false);
                return new ProjectViewHolder(view, this);
            case 5:  // NESTED TASK
                view = LayoutInflater.from(parent.getContext())
                                     .inflate(R.layout.fragment_nested_task, parent, false);
                return new NestedTaskViewHolder(view, this);
        }

        return new ViewHolder(new View(context), this);
    }

    /**
     * Run every time the row's inner view is created. This is where the data should be populated.
     */
    @Override public void onBindViewHolder(final ViewHolder holder, int position) {

        int type = holder.getItemViewType();

        switch (type) {
            case 1: // TASK VIEW
                Task task = (Task) items.get(position);
                ((TaskViewHolder) holder).bind(task, context);
                break;
            case 2: // CONTEXT VIEW
                Context context = (Context) items.get(position);
                ((ContextViewHolder) holder).bind(context, this.context);
                break;
            case 3: // FOLDER VIEW
                Folder folder = (Folder) items.get(position);
                ((FolderViewHolder) holder).bind(folder, this.context);
                break;
            case 4: // PROJECT VIEW
                Task project = (Task) items.get(position);
                ((ProjectViewHolder) holder).bind(project, this.context);
                break;
            case 5: // NESTED TASK VIEW
                Task nestedTask = (Task) items.get(position);
                ((NestedTaskViewHolder) holder).bind(nestedTask, this.context);
                break;
        }

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {

                if (null != listener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    listener.onListFragmentInteraction(holder);
                }
            }
        });
    }

    /**
     * This is where we must get the number of values. Good thing it's easy!
     */
    @Override public int getItemCount() {

        return items.size();
    }

    public void removeItem(Entry entry) {

        int position = items.indexOf(entry);
        items.remove(position);
        notifyItemRemoved(position);
    }

    public void enableHeader(boolean enable) {

        if (parentEntry != null) {
            parentEntry.headerRow = enable;
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
