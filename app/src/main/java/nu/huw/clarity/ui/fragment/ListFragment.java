package nu.huw.clarity.ui.fragment;

import android.accounts.Account;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SyncInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.util.List;

import nu.huw.clarity.R;
import nu.huw.clarity.account.AccountManagerHelper;
import nu.huw.clarity.db.model.ListLoader;
import nu.huw.clarity.model.Entry;
import nu.huw.clarity.model.Perspective;
import nu.huw.clarity.ui.adapter.ListAdapter;
import nu.huw.clarity.ui.misc.DividerItemDecoration;

/**
 * A fragment representing a list of entries (tasks, contexts, etc). It's built to be really
 * flexible, so it'll hold any type of entry and display them differently using the ListAdapter
 * and various ViewHolders.
 */
public class ListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Entry>> {

    private static final String TAG = ListFragment.class.getSimpleName();
    private ListFragment.OnListFragmentInteractionListener mListener;
    private Bundle                                         mArgs;
    private ListAdapter                                    mAdapter;
    private View                                           mView;
    private SwipeRefreshLayout                             mSwipeLayout;
    private RecyclerView                                   mRecyclerView;
    private RelativeLayout                                 mEmptyState;
    private ProgressBar                                    mSpinner;
    private Entry                                          mParent;
    private Perspective                                    mPerspective;
    private boolean mLoaded = false;
    private IntentFilter syncIntentFilter;
    private BroadcastReceiver syncReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {

            // Get objects

            if (mView == null) return;
            if (mArgs == null) mArgs = getArguments();

            // Set swipe layout

            checkForSyncs();

            // Request a reload

            if (mArgs != null) {
                getLoaderManager().initLoader(0, mArgs, ListFragment.this);
            }
        }
    };

    public ListFragment() {}

    /**
     * Creates a new ListFragment
     *
     * @param perspective Perspective to determine filtering
     * @param parent      Parent Entry, can be null
     */
    public static ListFragment newInstance(Perspective perspective, @Nullable Entry parent) {

        ListFragment fragment = new ListFragment();
        Bundle       args     = new Bundle();
        args.putParcelable("perspective", perspective);
        args.putParcelable("parent", parent);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called when creating the fragment object (not view)
     * Includes general instantiation stuff
     */
    @Override public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Context context = getContext();
        mArgs = getArguments();

        // Setup sync receiver

        if (context != null) {
            syncIntentFilter = new IntentFilter(context.getString(R.string.sync_broadcast_intent));
        }

        // Setup loader

        if (mArgs != null) {
            getLoaderManager().initLoader(0, mArgs, this);
        }

        // Setup list adapter

        mAdapter = new ListAdapter(getContext(), mListener);
    }

    /**
     * Called when creating the view
     * Sets up the view & the swipe spinner
     */
    @Nullable @Override public View onCreateView(LayoutInflater inflater,
                                                 @Nullable ViewGroup container,
                                                 @Nullable Bundle savedInstanceState) {

        Context context = getContext();

        // Inflate view

        mView = inflater.inflate(R.layout.fragment_list, container, false);

        // Setup swipe-to-refresh

        AccountManagerHelper AMHelper = new AccountManagerHelper(getContext());
        if (AMHelper.doesAccountExist()) {

            // Get view & account

            mSwipeLayout = (SwipeRefreshLayout) mView.findViewById(R.id.list_refresh);
            final Account account   = AMHelper.getAccount();
            final String  authority = getString(R.string.authority);

            // Set refresh ring colours

            TypedValue typedValue = new TypedValue();
            getContext().getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
            int color = typedValue.data;
            mSwipeLayout.setColorSchemeColors(color);

            // Set listener

            mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override public void onRefresh() {

                    ContentResolver.requestSync(account, authority, new Bundle());
                }
            });

            // Check for current syncs

            checkForSyncs();
        }

        // Setup recycler view & adapter

        mRecyclerView = (RecyclerView) mView.findViewById(R.id.list);
        mEmptyState = (RelativeLayout) mView.findViewById(R.id.empty_state);
        mSpinner = (ProgressBar) mView.findViewById(R.id.list_progress);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerView.invalidateItemDecorations();
        mRecyclerView.addItemDecoration(new DividerItemDecoration(context));
        mRecyclerView.setItemAnimator(null); // otherwise new items fade in (huge annoyance)

        // Set adapter & refresh views

        mRecyclerView.setAdapter(mAdapter);
        refreshAdapterViews();

        return mView;
    }

    /**
     * Given an adapter, appropriately set it on the recycler view and display it to the user if it
     * has any items.
     */
    private void refreshAdapterViews() {

        if (mView == null || mAdapter == null) return;
        if (mRecyclerView == null) mRecyclerView = (RecyclerView) mView.findViewById(R.id.list);
        if (mEmptyState == null) {
            mEmptyState = (RelativeLayout) mView.findViewById(R.id.empty_state);
        }
        if (mSpinner == null) mSpinner = (ProgressBar) mView.findViewById(R.id.list_progress);

        // Show the spinner if we're loading something, the empty state if we loaded nothing, and
        // the recycler view if we loaded something.

        if (mAdapter.getItemCount() > 0) {

            // Show the recycler view if the adapter has items

            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyState.setVisibility(View.GONE);
            mSpinner.setVisibility(View.GONE);
        } else if (mLoaded) {

            // Show the empty state if the adapter is empty and we've tried to load something

            mRecyclerView.setVisibility(View.GONE);
            mEmptyState.setVisibility(View.VISIBLE);
            mSpinner.setVisibility(View.GONE);
        } else {

            // Show nothing

            mRecyclerView.setVisibility(View.GONE);
            mEmptyState.setVisibility(View.GONE);
            mSpinner.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Checks for any running syncs and appropriately sets the swipe layout's spinner
     */
    public boolean checkForSyncs() {

        String authority = getString(R.string.authority);

        boolean active = false;
        for (SyncInfo syncInfo : ContentResolver.getCurrentSyncs()) {
            if (syncInfo.authority.equals(authority)) {
                active = true;
            }
        }

        if (mView != null) {
            if (mSwipeLayout != null) {
                mSwipeLayout = (SwipeRefreshLayout) mView.findViewById(R.id.list_refresh);
            }

            if (mSwipeLayout == null) return active;

            if (active) {

                // Weird bug with swipe layouts
                // See: http://stackoverflow.com/a/26910973

                mSwipeLayout.post(new Runnable() {
                    @Override public void run() {

                        mSwipeLayout.setRefreshing(true);
                    }
                });
            } else {
                mSwipeLayout.post(new Runnable() {
                    @Override public void run() {

                        mSwipeLayout.setRefreshing(false);
                    }
                });
            }
        }

        return active;
    }

    /**
     * Return an appropriate loader given the arguments
     *
     * @param id   ID of requested loader (currently irrelevant)
     * @param args A bundle including arguments to retrieve a loder with
     *
     * @return A ListLoader
     */
    @Override public Loader<List<Entry>> onCreateLoader(int id, Bundle args) {

        if (args != null && args.containsKey("perspective")) {

            mPerspective = args.getParcelable("perspective");
            mParent = args.getParcelable("parent");

            return new ListLoader(getContext(), mPerspective, mParent);
        }

        throw new IllegalArgumentException("Argument bundle requires 'perspective' key");
    }

    /**
     * Once the load is done, refresh the adapter and view
     */
    @Override public void onLoadFinished(Loader<List<Entry>> loader, List<Entry> data) {

        mAdapter.setData(mParent, data);

        Log.i(TAG, "Load finished (ListFragment)");

        checkForSyncs();
        refreshAdapterViews();

        mLoaded = true;
    }

    /**
     * Nothing needs to be cleaned up
     */
    @Override public void onLoaderReset(Loader<List<Entry>> loader) {}

    /**
     * Register and deregister the sync receiver when this fragment is no longer showing
     */
    @Override public void onPause() {

        super.onPause();

        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(syncReceiver);
        checkForSyncs();
    }

    @Override public void onResume() {

        super.onResume();

        LocalBroadcastManager.getInstance(getContext())
                             .registerReceiver(syncReceiver, syncIntentFilter);
        checkForSyncs();
    }

    /**
     * Register and deregister the fragment interaction listener
     */
    @Override public void onAttach(Context context) {

        super.onAttach(context);
        if (context instanceof ListFragment.OnListFragmentInteractionListener) {
            mListener = (ListFragment.OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(
                    context.toString() + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override public void onDetach() {

        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnListFragmentInteractionListener {

        void onListFragmentInteraction(ListAdapter.ViewHolder holder);
    }
}
