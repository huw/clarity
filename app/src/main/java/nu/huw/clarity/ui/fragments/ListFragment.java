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
import nu.huw.clarity.model.Task;
import nu.huw.clarity.ui.adapters.TaskAdapter;

/**
 * A fragment representing a list of entries (tasks, contexts, etc). This is the thing which gets
 * swapped around by the main activity—it holds a <i>full</i> list. The Adapter, then, will
 * populate this list with the appropriate subviews (or really, subfragments).
 */
public class ListFragment extends Fragment {

    private static final String TAG = ListFragment.class.getSimpleName();
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon
     * screen orientation changes).
     */
    public ListFragment() {}

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused") public static ListFragment newInstance() {

        ListFragment fragment = new ListFragment();
        Bundle       args     = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
        }
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_entry_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            DataModelHelper dmHelper = new DataModelHelper();
            List<Task>      tasks    = dmHelper.getTasks();

            Context      context      = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(new TaskAdapter(tasks, mListener));
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
