package nu.huw.clarity.ui.fragment;

import android.accounts.Account;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SyncInfo;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import java.util.TreeMap;
import java.util.TreeSet;
import nu.huw.clarity.R;
import nu.huw.clarity.account.AccountManagerHelper;
import nu.huw.clarity.db.model.ListLoader;
import nu.huw.clarity.model.Entry;
import nu.huw.clarity.model.Header;
import nu.huw.clarity.model.Perspective;
import nu.huw.clarity.ui.adapter.ListAdapter;
import nu.huw.clarity.ui.misc.DividerItemDecoration;

/**
 * A fragment representing a list of entries (tasks, contexts, etc). It's built to be really
 * flexible, so it'll hold any type of entry and display them differently using the ListAdapter
 * and various ViewHolders.
 */
public class ListFragment extends Fragment implements
    LoaderManager.LoaderCallbacks<TreeMap<Header, TreeSet<? extends Entry>>> {

  private static final String TAG = ListFragment.class.getSimpleName();
  @BindView(R.id.swiperefreshlayout_list)
  SwipeRefreshLayout swiperefreshlayout_list;
  @BindView(R.id.recyclerview_list)
  RecyclerView recyclerview_list;
  @BindView(R.id.relativelayout_list_empty)
  RelativeLayout relativelayout_list_empty;
  @BindView(R.id.progressbar_list_spinner)
  ProgressBar progressbar_list_spinner;
  private ListFragment.OnListFragmentInteractionListener fragmentInteractionListener;
  private Bundle args;
  private ListAdapter adapter;
  private View view;
  private Unbinder unbinder;
  private Entry parentEntry;
  private Perspective perspective;
  private boolean loaded = false;
  private IntentFilter syncIntentFilter;
  private BroadcastReceiver syncReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {

      // Get objects

      if (view == null) return;
      if (args == null) args = getArguments();

      // Set swipe layout

      checkForSyncs();

      // Request a reload

      if (args != null) {
        getLoaderManager().initLoader(0, args, ListFragment.this);
      }
    }
  };

  public ListFragment() {
  }

  /**
   * Creates a new ListFragment
   *
   * @param perspective Perspective to determine filtering
   * @param parent Parent Entry, can be null
   */
  public static ListFragment newInstance(Perspective perspective, @Nullable Entry parent) {

    ListFragment fragment = new ListFragment();
    Bundle args = new Bundle();
    args.putParcelable("PERSPECTIVE", perspective);
    args.putParcelable("PARENT", parent);
    fragment.setArguments(args);
    return fragment;
  }

  /**
   * Called when creating the fragment object (not view)
   * Includes general instantiation stuff
   */
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    Context context = getContext();
    args = getArguments();

    // Setup sync receiver

    if (context != null) {
      syncIntentFilter = new IntentFilter(context.getString(R.string.sync_broadcastintent));
    }

    // Setup loader

    if (args != null) {
      getLoaderManager().initLoader(0, args, this);
    }

    // Setup list adapter

    adapter = new ListAdapter(getContext(), fragmentInteractionListener);
  }

  /**
   * Called when creating the view
   * Sets up the view & the swipe spinner
   */
  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {

    Context context = getContext();

    if (perspective == null) {
      if (args == null) args = getArguments();
      perspective = args.getParcelable("PERSPECTIVE");
      parentEntry = args.getParcelable("ENTRY");
    }

    // Inflate view

    view = inflater.inflate(R.layout.fragment_list, container, false);
    unbinder = ButterKnife.bind(this, view);

    // Setup swipe-to-refresh

    int spinnerColor = ContextCompat.getColor(getContext(), perspective.color);

    AccountManagerHelper AMHelper = new AccountManagerHelper(getContext());
    if (AMHelper.doesAccountExist()) {

      // Get view & account

      final Account account = AMHelper.getAccount();
      final String authority = getString(R.string.sync_authority);

      // Set refresh ring colours

      swiperefreshlayout_list.setColorSchemeColors(spinnerColor);

      // Set listener

      swiperefreshlayout_list.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {

          ContentResolver.requestSync(account, authority, new Bundle());
        }
      });

      // Check for current syncs

      checkForSyncs();
    }

    // Set the spinner colour to the current perspective
    // http://stackoverflow.com/a/36828947/3208895

    progressbar_list_spinner.getIndeterminateDrawable().setColorFilter(spinnerColor, Mode.SRC_IN);

    // Setup recycler view & adapter

    LinearLayoutManager layoutManager = new LinearLayoutManager(context);
    DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context,
        layoutManager.getOrientation());

    recyclerview_list.setLayoutManager(layoutManager);
    recyclerview_list.invalidateItemDecorations();
    recyclerview_list.addItemDecoration(dividerItemDecoration);
    recyclerview_list.setItemAnimator(null); // otherwise new items fade in (huge annoyance)
    recyclerview_list.setNestedScrollingEnabled(true); // smooth scrolling

    // Set adapter & refresh views

    recyclerview_list.setAdapter(adapter);
    refreshAdapterViews();

    return view;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
  }

  /**
   * Given an adapter, appropriately set it on the recycler view and display it to the user if it
   * has any items.
   */
  private void refreshAdapterViews() {

    if (view == null || adapter == null) return;

    // Show the spinner if we're loading something, the empty state if we loaded nothing, and
    // the recycler view if we loaded something.

    if (adapter.getItemCount() > 0) {

      // Show the recycler view if the adapter has items

      recyclerview_list.setVisibility(View.VISIBLE);
      relativelayout_list_empty.setVisibility(View.GONE);
      progressbar_list_spinner.setVisibility(View.GONE);
    } else if (loaded) {

      // Show the empty state if the adapter is empty and we've tried to load something

      recyclerview_list.setVisibility(View.GONE);
      relativelayout_list_empty.setVisibility(View.VISIBLE);
      progressbar_list_spinner.setVisibility(View.GONE);
    } else {

      // Show nothing

      recyclerview_list.setVisibility(View.GONE);
      relativelayout_list_empty.setVisibility(View.GONE);
      progressbar_list_spinner.setVisibility(View.VISIBLE);
    }
  }

  /**
   * Checks for any running syncs and appropriately sets the swipe layout's spinner
   */
  public boolean checkForSyncs() {

    String authority = getString(R.string.sync_authority);

    boolean active = false;
    for (SyncInfo syncInfo : ContentResolver.getCurrentSyncs()) {
      if (syncInfo.authority.equals(authority)) {
        active = true;
      }
    }

    if (swiperefreshlayout_list != null) {
      if (active) {

        // Weird bug with swipe layouts
        // See: http://stackoverflow.com/a/26910973

        swiperefreshlayout_list.post(new Runnable() {
          @Override
          public void run() {

            if (swiperefreshlayout_list != null) {
              swiperefreshlayout_list.setRefreshing(true);
            }
          }
        });
      } else {
        swiperefreshlayout_list.post(new Runnable() {
          @Override
          public void run() {

            if (swiperefreshlayout_list != null) {
              swiperefreshlayout_list.setRefreshing(false);
            }
          }
        });
      }
    }

    return active;
  }

  /**
   * Return an appropriate loader given the arguments
   *
   * @param id ID of requested loader (currently irrelevant)
   * @param args A bundle including arguments to retrieve a loder with
   * @return A ListLoader
   */
  @Override
  public Loader<TreeMap<Header, TreeSet<? extends Entry>>> onCreateLoader(int id, Bundle args) {

    if (args != null && args.containsKey("PERSPECTIVE")) {

      perspective = args.getParcelable("PERSPECTIVE");
      parentEntry = args.getParcelable("PARENT");

      return new ListLoader(getContext(), perspective, parentEntry);
    }

    throw new IllegalArgumentException("Argument bundle requires 'PERSPECTIVE' key");
  }

  /**
   * Once the load is done, refresh the adapter and view
   */
  @Override
  public void onLoadFinished(Loader<TreeMap<Header, TreeSet<? extends Entry>>> loader,
      TreeMap<Header, TreeSet<? extends Entry>> data) {

    loaded = true;
    adapter.setData(perspective, parentEntry, data);

    checkForSyncs();
    refreshAdapterViews();
  }

  /**
   * Nothing needs to be cleaned up
   */
  @Override
  public void onLoaderReset(Loader<TreeMap<Header, TreeSet<? extends Entry>>> loader) {
  }

  /**
   * Register and deregister the sync receiver when this fragment is no longer showing
   */
  @Override
  public void onPause() {

    super.onPause();

    LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(syncReceiver);
    checkForSyncs();
  }

  @Override
  public void onResume() {

    super.onResume();

    LocalBroadcastManager.getInstance(getContext())
        .registerReceiver(syncReceiver, syncIntentFilter);
    checkForSyncs();
  }

  /**
   * Register and deregister the fragment interaction listener
   */
  @Override
  public void onAttach(Context context) {

    super.onAttach(context);
    if (context instanceof ListFragment.OnListFragmentInteractionListener) {
      fragmentInteractionListener = (ListFragment.OnListFragmentInteractionListener) context;
    } else {
      throw new RuntimeException(
          context.toString() + " must implement OnListFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {

    super.onDetach();
    fragmentInteractionListener = null;
  }

  /**
   * This interface must be implemented by activities that contain this
   * fragment to allow an interaction in this fragment to be communicated
   * to the activity and potentially other fragments contained in that
   * activity.
   */
  public interface OnListFragmentInteractionListener {

    void onItemDetailInteraction(@NonNull Entry entry, @Nullable Perspective perspective);

    void onItemListInteraction(@NonNull Entry entry, @Nullable Perspective perspective);
  }
}
