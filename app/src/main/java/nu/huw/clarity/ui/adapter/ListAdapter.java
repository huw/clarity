package nu.huw.clarity.ui.adapter;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import nu.huw.clarity.R;
import nu.huw.clarity.model.Context;
import nu.huw.clarity.model.Entry;
import nu.huw.clarity.model.Folder;
import nu.huw.clarity.model.Header;
import nu.huw.clarity.model.Perspective;
import nu.huw.clarity.model.Task;
import nu.huw.clarity.ui.fragment.ListFragment.OnListFragmentInteractionListener;
import nu.huw.clarity.ui.viewholder.CollationHeaderViewHolder;
import nu.huw.clarity.ui.viewholder.ContextViewHolder;
import nu.huw.clarity.ui.viewholder.EntryHeaderViewHolder;
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
  private final OnListFragmentInteractionListener listener;
  private final android.content.Context androidContext;
  private Perspective perspective;
  private Entry parentEntry;
  private List<Entry> entries = new ArrayList<>();
  private TreeMap<Header, TreeSet<? extends Entry>> entryMap = new TreeMap<>();

  public ListAdapter(android.content.Context androidContext,
      OnListFragmentInteractionListener listener) {
    this.androidContext = androidContext;
    this.listener = listener;
  }

  /**
   * This function flattens a TreeMap into a List<\Entry> suitable for display, including some nice
   * header rows. The Header keys will be displayed before the list of entries from the TreeSet,
   * with the parentEntry at the top.
   *
   * @param perspective The current OmniFocus perspective
   * @param parent An entry representing the parent of this view
   * @param entryMap A TreeMap of the given type (should make sense)
   */
  public void setData(@Nullable Perspective perspective, @Nullable Entry parent,
      @Nullable TreeMap<Header, TreeSet<? extends Entry>> entryMap) {

    this.perspective = perspective;
    this.parentEntry = parent;
    this.entryMap = entryMap;

    if (entryMap != null) {

      notifyItemRangeRemoved(0, getItemCount());
      List<Entry> entries = new ArrayList<>();

      // Add the parent at the top (if it exists)

      if (parent != null) entries.add(parent);

      // Add all keys and values

      for (Header header : entryMap.keySet()) {

        TreeSet<? extends Entry> treeSet = entryMap.get(header);
        if (treeSet == null) continue;

        // Only add header if there is more than 1 item in the TreeMap and the TreeSet is not empty

        if (entryMap.size() > 1 && treeSet.size() > 0) {
          entries.add(header);
        }
        entries.addAll(treeSet);
      }

      this.entries = entries;
      notifyItemRangeInserted(0, getItemCount());
    }
  }

  @Override
  public int getItemViewType(int position) {
    Entry entry = entries.get(position);
    if (entry == parentEntry) {
      return Entry.VT_HEADER_ENTRY;
    } else {
      return entry.getViewType(perspective);
    }
  }

  /**
   * Run every time a new row is created, returns the ViewHolder for that row, and inflates a
   * fragment.
   */
  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

    View view;

    switch (viewType) {
      case Entry.VT_TASK:
        view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_list_task, parent, false);
        return new TaskViewHolder(view, this);
      case Entry.VT_CONTEXT:
        view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_list_context, parent, false);
        return new ContextViewHolder(view, this);
      case Entry.VT_FOLDER:
        view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_list_folder, parent, false);
        return new FolderViewHolder(view, this);
      case Entry.VT_PROJECT:
        view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_list_project, parent, false);
        return new ProjectViewHolder(view, this);
      case Entry.VT_NESTED_TASK:
        view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_list_nestedtask, parent, false);
        return new NestedTaskViewHolder(view, this);
      case Entry.VT_HEADER_ENTRY:
        view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_listheader_entry, parent, false);
        return new EntryHeaderViewHolder(view, this);
      case Entry.VT_HEADER_COLLATION:
        view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_listheader_collation, parent, false);
        return new CollationHeaderViewHolder(view, this);
    }

    return new ViewHolder(new View(androidContext), this);
  }

  /**
   * Run every time the row's inner view is created. This is where the data should be populated.
   */
  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {

    int type = holder.getItemViewType();
    final Entry entry = entries.get(position);

    switch (type) {
      case Entry.VT_TASK:
        Task task = (Task) entry;
        ((TaskViewHolder) holder).bind(task, androidContext, perspective);
        break;
      case Entry.VT_CONTEXT:
        Context context = (Context) entry;
        ((ContextViewHolder) holder).bind(context, this.androidContext, perspective);
        break;
      case Entry.VT_FOLDER:
        Folder folder = (Folder) entry;
        ((FolderViewHolder) holder).bind(folder, this.androidContext, perspective);
        break;
      case Entry.VT_PROJECT:
        Task project = (Task) entry;
        ((ProjectViewHolder) holder).bind(project, this.androidContext, perspective);
        break;
      case Entry.VT_NESTED_TASK:
        Task nestedTask = (Task) entry;
        ((NestedTaskViewHolder) holder).bind(nestedTask, this.androidContext, perspective);
        break;
      case Entry.VT_HEADER_ENTRY:
        ((EntryHeaderViewHolder) holder).bind(entry, this.androidContext, perspective);
        break;
      case Entry.VT_HEADER_COLLATION:
        Header header = (Header) entry;
        ((CollationHeaderViewHolder) holder).bind(header, this.androidContext, perspective);
    }

    // If the item is a task or a header, then we should send the user straight to the detail view
    // on click. Otherwise, we want to further explore this item's contents, so send them through
    // the list again. See MainActivity.

    if (type == Entry.VT_TASK || type == Entry.VT_HEADER_ENTRY) {
      holder.view.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          if (listener != null) {
            listener.onItemDetailInteraction(entry, perspective);
          }
        }
      });
    } else if (type != Entry.VT_HEADER_COLLATION) {
      holder.view.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          if (listener != null) {
            listener.onItemListInteraction(entry, perspective);
          }
        }
      });
    }
  }

  /**
   * This is where we must get the number of values. Good thing it's easy!
   */
  @Override
  public int getItemCount() {
    return entries.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {

    protected final View view;
    protected final ListAdapter adapter;
    public Entry entry;

    public ViewHolder(View view, ListAdapter adapter) {

      super(view);
      this.view = view;
      this.adapter = adapter;
    }
  }
}
