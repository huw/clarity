package nu.huw.clarity.ui.fragments;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import nu.huw.clarity.R;
import nu.huw.clarity.db.DatabaseContract;
import nu.huw.clarity.db.DatabaseCursorLoader;
import nu.huw.clarity.ui.MyEntryListRecyclerViewAdapter;

/**
 * This class holds the full-screen list container fragment. We initialise our own RecyclerView
 * using fragment_entrylist_list, and populate it with fragment_entrylist
 *
 * TODO: Make name more appropriate
 */
public class EntryListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private ListInteractionListener        mListener;
    private MyEntryListRecyclerViewAdapter mAdapter;
    private String   TABLE_NAME     = DatabaseContract.Tasks.TABLE_NAME;
    private String[] COLUMNS        =
            {DatabaseContract.Tasks.COLUMN_ID.name, // Must always get the ID column!
             DatabaseContract.Tasks.COLUMN_NAME.name, DatabaseContract.Tasks.COLUMN_DATE_DUE.name};
    private String   SELECTION      = "(" + DatabaseContract.Tasks.COLUMN_DUE_SOON.name + "=1 OR " +
                                      DatabaseContract.Tasks.COLUMN_FLAGGED + "=1) AND " +
                                      DatabaseContract.Tasks.COLUMN_DATE_COMPLETED + " IS NULL";
    private String[] SELECTION_ARGS = {};

    /**
     * Mandatory empty constructor
     */
    public EntryListFragment() {}

    @Override public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_entrylist_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context      context      = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            // Initialise the adapter with an empty cursor.
            // This is because we need to get the data with
            // a loader, and we can swap it in once loaded.

            mAdapter = new MyEntryListRecyclerViewAdapter(mListener, null);

            recyclerView.setAdapter(mAdapter);

            // Now go load the cursor
            getLoaderManager().initLoader(0, null, this);
        }
        return view;
    }

    /**
     * Ensure that the context implements ListInteractionListener
     */
    @Override public void onAttach(Context context) {

        super.onAttach(context);
        if (context instanceof ListInteractionListener) {
            mListener = (ListInteractionListener) context;
        } else {
            throw new RuntimeException(
                    context.toString() + " must implement ListInteractionListener");
        }
    }

    @Override public void onDetach() {

        super.onDetach();
        mListener = null;
    }

    /**
     * Called once the system is ready to create a Loader. We pass the system a CursorLoader with
     * the selection we want, and move on.
     */
    @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new DatabaseCursorLoader(this.getActivity(), TABLE_NAME, COLUMNS, SELECTION,
                                        SELECTION_ARGS);
    }

    /**
     * Once the cursor is ready, we can pass it to the Adapter.
     */
    @Override public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        mAdapter.swapCursor(cursor);
    }

    /**
     * I'm just trusting Google on this one. Their code.
     */
    @Override public void onLoaderReset(Loader<Cursor> loader) {

        mAdapter.swapCursor(null);
    }

    /**
     * Any activity which contains this fragment needs to implement this interface so it can receive
     * interactions and appropriately act on them, as per the correct fragment lifecycle.
     */
    public interface ListInteractionListener {

        // TODO: Implement all this somewhere useful
        void onListInteraction();
    }
}
