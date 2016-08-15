package nu.huw.clarity.ui.fragments;

import android.accounts.Account;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SyncInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.List;

import nu.huw.clarity.R;
import nu.huw.clarity.account.AccountManagerHelper;
import nu.huw.clarity.db.DataModelHelper;
import nu.huw.clarity.model.Entry;
import nu.huw.clarity.ui.adapters.ListAdapter;
import nu.huw.clarity.ui.misc.DividerItemDecoration;

/**
 * A fragment representing a list of entries (tasks, contexts, etc). This is the thing which gets
 * swapped around by the main activity—it holds a <i>full</i> list. The Adapter, then, will
 * populate this list with the appropriate subviews (or really, subfragments).
 */
public class ListFragment extends Fragment {

    private static final String TAG = ListFragment.class.getSimpleName();
    private OnListFragmentInteractionListener mListener;
    private RecyclerView.Adapter              mAdapter;
    private View                              view;
    private SwipeRefreshLayout                swipeLayout;
    // For receiving sync broadcasts
    private IntentFilter                      syncIntentFilter;
    private BroadcastReceiver syncReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {

            if (view == null || swipeLayout == null) {
                return;
            }

            create();

            fillContent(view);
            swipeLayout.setRefreshing(false);
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon
     * screen orientation changes).
     */
    public ListFragment() {}

    public static ListFragment newInstance(int menuID) {

        ListFragment fragment = new ListFragment();
        Bundle       args     = new Bundle();
        args.putInt("menuID", menuID);
        fragment.setArguments(args);
        return fragment;
    }

    public static ListFragment newInstance(int menuID, String parentID) {

        ListFragment fragment = new ListFragment();
        Bundle       args     = new Bundle();
        args.putInt("menuID", menuID);
        args.putString("parentID", parentID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (getContext() != null) {

            // Setup receiver

            syncIntentFilter =
                    new IntentFilter(getContext().getString(R.string.sync_broadcast_intent));
        }

        create();
    }

    private void create() {

        if (getArguments() != null) {
            int    menuID   = getArguments().getInt("menuID");
            String parentID = getArguments().getString("parentID");

            DataModelHelper dmHelper = new DataModelHelper(getContext());
            List<Entry>     items;

            switch (menuID) {
                case R.id.nav_inbox:
                    items = dmHelper.getTasksInInbox();
                    break;
                case R.id.nav_projects:
                    if (parentID == null) {
                        items = dmHelper.getTopLevelProjects();
                    } else {
                        items = dmHelper.getChildren(parentID);
                    }
                    break;
                case R.id.nav_contexts:
                    if (parentID == null) {
                        items = dmHelper.getTopLevelContexts();
                    } else if (parentID.equals("NO_CONTEXT")) {
                        items = dmHelper.getTasksWithNoContext();
                    } else {
                        items = dmHelper.getContextChildren(parentID);
                    }
                    break;
                case R.id.nav_flagged:
                    items = dmHelper.getFlagged();
                    break;
                default:
                    items = dmHelper.getTasks();
            }

            mAdapter = new ListAdapter(getContext(), items, mListener);
        }
    }

    @Override public void onResume() {

        super.onResume();

        LocalBroadcastManager.getInstance(getContext())
                             .registerReceiver(syncReceiver, syncIntentFilter);
    }

    @Override public void onPause() {

        super.onPause();

        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(syncReceiver);
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_entry_list, container, false);

        // Swipe to refresh

        AccountManagerHelper AMHelper = new AccountManagerHelper(getContext());
        if (AMHelper.doesAccountExist()) {

            swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.list_refresh);

            final Account account   = AMHelper.getAccount();
            final String  authority = getString(R.string.authority);

            TypedValue typedValue = new TypedValue();
            getContext().getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
            int color = typedValue.data;
            swipeLayout.setColorSchemeColors(color);

            swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override public void onRefresh() {

                    ContentResolver.requestSync(account, authority, new Bundle());
                }
            });
        }

        fillContent(view);

        return view;
    }

    private void fillContent(View view) {

        // Set the adapter
        RecyclerView   recyclerView   = (RecyclerView) view.findViewById(R.id.list);
        RelativeLayout relativeLayout = (RelativeLayout) view.findViewById(R.id.empty_state);

        if (mAdapter.getItemCount() > 0) {

            recyclerView.setVisibility(View.VISIBLE);
            relativeLayout.setVisibility(View.GONE);

            Context context = view.getContext();
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.invalidateItemDecorations();
            recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));
            recyclerView.setAdapter(mAdapter);

        } else {

            recyclerView.setVisibility(View.GONE);
            relativeLayout.setVisibility(View.VISIBLE);
        }

        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.list_refresh);

        if (new AccountManagerHelper(getContext()).doesAccountExist()) {

            // Update swipe layout spinner

            String  authority = getString(R.string.authority);
            boolean active    = false;

            for (SyncInfo syncInfo : ContentResolver.getCurrentSyncs()) {
                if (syncInfo.authority.equals(authority)) {
                    active = true;
                }
            }

            if (active) {

                // Weird bug with swipe layouts
                // See: http://stackoverflow.com/a/26910973

                swipeLayout.post(new Runnable() {
                    @Override public void run() {

                        swipeLayout.setRefreshing(true);
                    }
                });
            } else {

                swipeLayout.post(new Runnable() {
                    @Override public void run() {

                        swipeLayout.setRefreshing(false);
                    }
                });
            }
        }
    }

    public void showProgress() {

        if (swipeLayout != null) {

            swipeLayout.post(new Runnable() {
                @Override public void run() {

                    swipeLayout.setRefreshing(true);
                }
            });
        }
    }

    @Override public void onAttach(Context context) {

        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
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
