package nu.huw.clarity.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import nu.huw.clarity.R;
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

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon
     * screen orientation changes).
     */
    public ListFragment() {}

    // TODO: Customize parameter initialization
    public static ListFragment newInstance(int menuID) {

        ListFragment fragment = new ListFragment();
        Bundle       args     = new Bundle();
        args.putInt("menuID", menuID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            int menuID = getArguments().getInt("menuID");

            DataModelHelper dmHelper = new DataModelHelper(getContext());
            List<Entry>     items;

            switch (menuID) {
                case R.id.nav_inbox:
                    items = dmHelper.getTasksInInbox();
                    break;
                case R.id.nav_projects:
                    items = dmHelper.getTopLevelProjects();
                    break;
                case R.id.nav_contexts:
                    items = dmHelper.getTopLevelContexts();
                    break;
                case R.id.nav_flagged:
                    items = dmHelper.getFlagged();
                    break;
                default:
                    items = dmHelper.getTasks();
            }

            mAdapter = new ListAdapter(this.getContext(), items, mListener);
        }
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_entry_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {

            Context      context      = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));
            recyclerView.setAdapter(mAdapter);
        }
        return view;
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
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {

        // TODO: Update argument type and name
        //void onListFragmentInteraction();
    }
}
