package nu.huw.clarity.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import nu.huw.clarity.R;
import nu.huw.clarity.model.Task;
import nu.huw.clarity.ui.fragments.EntryFragment.OnListFragmentInteractionListener;

/**
 * This class handles the display of the data in the RecyclerView—it's passed a data set and
 * deals with the way that the set's items are distributed amongst the appropriate child views.
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    private final OnListFragmentInteractionListener mListener;
    private final List<Task>                        mValues;

    public TaskAdapter(List<Task> items, OnListFragmentInteractionListener listener) {

        mValues = items;
        mListener = listener;
    }

    /**
     * Run every time a new row is created, returns the ViewHolder for that row, and inflates a
     * fragment.
     */
    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.fragment_entry, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Run every time the row's inner view is created. This is where the data should be populated.
     */
    @Override public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).id);
        holder.mContentView.setText(mValues.get(position).name);

        holder.mView.setOnClickListener(new View.OnClickListener() {
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

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final View     mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public       Task     mItem;

        public ViewHolder(View view) {

            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override public String toString() {

            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
