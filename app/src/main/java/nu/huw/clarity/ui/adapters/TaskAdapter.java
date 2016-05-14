package nu.huw.clarity.ui.adapters;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import nu.huw.clarity.R;
import nu.huw.clarity.db.DataModelHelper;
import nu.huw.clarity.model.Task;
import nu.huw.clarity.ui.fragments.ListFragment.OnListFragmentInteractionListener;

/**
 * This class handles the display of the data in the RecyclerView—it's passed a data set and
 * deals with the way that the set's items are distributed amongst the appropriate child views.
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    private static final String TAG = TaskAdapter.class.getSimpleName();
    private final OnListFragmentInteractionListener mListener;
    private final List<Task>                        mValues;
    private final Map<String, String>               mContextNameMap;

    public TaskAdapter(List<Task> items, OnListFragmentInteractionListener listener) {

        mValues = items;
        mListener = listener;
        DataModelHelper DMHelper = new DataModelHelper();
        mContextNameMap = DMHelper.getContextNameMap();
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

        String     date        = "";
        DateFormat localFormat = SimpleDateFormat.getDateInstance();

        // Navigate between date due and effective date due, and also set the date due view to
        // italics if it's an effective due date.
        if (holder.mItem.dateDue != null) {

            date = "Due " + localFormat.format(holder.mItem.dateDue);
        } else if (holder.mItem.dateDueEffective != null) {

            date = "Due " + localFormat.format(holder.mItem.dateDueEffective);
            holder.mDateView.setTypeface(null, Typeface.ITALIC);
        }

        String context = mContextNameMap.get(holder.mItem.context);

        if (context == null) context = "";

        holder.mNameView.setText(holder.mItem.name);
        holder.mDateView.setText(date);
        holder.mContextView.setText(context);

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
        public final TextView mNameView;
        public final TextView mContextView;
        public final TextView mDateView;
        public       Task     mItem;

        public ViewHolder(View view) {

            super(view);
            mView = view;
            mNameView = (TextView) view.findViewById(R.id.name);
            mContextView = (TextView) view.findViewById(R.id.context);
            mDateView = (TextView) view.findViewById(R.id.date);
        }

        @Override public String toString() {

            return super.toString() + " '" + mNameView.getText() + "'";
        }
    }
}
