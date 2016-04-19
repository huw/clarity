package nu.huw.clarity.ui;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import nu.huw.clarity.R;
import nu.huw.clarity.db.DatabaseContract;
import nu.huw.clarity.ui.fragments.EntryListFragment;

/**
 * Get items from the database as appropriate, and put them into views which then go into the
 * EntryListFragment.
 *
 * TODO: Make name more appropriate
 */
public class MyEntryListRecyclerViewAdapter
        extends CursorRecyclerAdapter<MyEntryListRecyclerViewAdapter.ViewHolder> {

    private final EntryListFragment.ListInteractionListener mListener;

    public MyEntryListRecyclerViewAdapter(EntryListFragment.ListInteractionListener listener,
                                          Cursor cursor) {

        super(cursor);

        mListener = listener;
    }

    /**
     * Create the View Holder for our entry. A ViewHolder (as far as I can tell) is a special view
     * class for lists and the like which stores a reference or something for the view, which can
     * then be accessed without slowing everything down.
     *
     * It's basically a view.
     */
    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.fragment_entrylist, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Once we've bound the holder to the view somewhere, then we get the values for the view from
     * the stored array of values, and set them. This is where all of the important view changing
     * around goes.
     *
     * Every time this is called, the cursor has been moved to the appropriate position by the
     * superclass. So we're all good.
     */
    @Override public void onBindViewHolder(final ViewHolder holder, Cursor cursor) {

        holder.mIdView.setText(
                cursor.getString(cursor.getColumnIndex(DatabaseContract.Tasks.COLUMN_NAME.name)));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {

                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListInteraction();
                }
            }
        });
    }

    /**
     * This inner class is the ViewHolder for the RecyclerView, and it stores some properties about
     * the view so we can edit them later.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {

        public final View     mView;
        public final TextView mIdView;
        public final TextView mContentView;

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
