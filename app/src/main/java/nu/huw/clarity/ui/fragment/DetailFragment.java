package nu.huw.clarity.ui.fragment;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;
import java.text.DateFormat;
import nu.huw.clarity.R;
import nu.huw.clarity.model.Context;
import nu.huw.clarity.model.Entry;
import nu.huw.clarity.model.Folder;
import nu.huw.clarity.model.Task;

/**
 * Fragment subclass for creating the 'list' of actionable items in the detail view. It chooses
 * and decides which layout XML to display depending on the type of entry passed to it, and then
 * populates it with the correct values.
 */
public class DetailFragment extends Fragment {

    private Entry                       entry;
    private OnDetailInteractionListener mListener;
    private int                         PRIMARY_TEXT;

    public DetailFragment() {}

    public static DetailFragment newInstance(Entry entry) {

        DetailFragment fragment = new DetailFragment();
        Bundle         args     = new Bundle();
        args.putParcelable("ENTRY", entry);
        fragment.setArguments(args);
        return fragment;
    }

    @Override public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            entry = getArguments().getParcelable("ENTRY");
        }

        PRIMARY_TEXT = ContextCompat.getColor(getContext(), R.color.primary_text_light);
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {

        if (entry == null) {
            throw new NullPointerException("Entry cannot be null");
        }

        if (entry instanceof Task) {
            if (((Task) entry).isProject) {
                return bindProjectDetails(R.layout.fragment_project_detail, inflater, container);
            } else {
                return bindTaskDetails(R.layout.fragment_task_detail, inflater, container);
            }
        } else if (entry instanceof Context) {
            return bindContextDetails(R.layout.fragment_context_detail, inflater, container);
        } else if (entry instanceof Folder) {
            return bindFolderDetails(R.layout.fragment_folder_detail, inflater, container);
        }

        throw new NullPointerException("Entry is of unknown type");
    }

    @Override public void onAttach(android.content.Context context) {

        super.onAttach(context);
        if (context instanceof OnDetailInteractionListener) {
            mListener = (OnDetailInteractionListener) context;
        } else {
            throw new RuntimeException(
                    context.toString() + " must implement OnDetailInteractionListener");
        }
    }

    @Override public void onDetach() {

        super.onDetach();
        mListener = null;
    }

    private View bindProjectDetails(int fragmentID, LayoutInflater inflater, ViewGroup container) {

        View       view  = inflater.inflate(fragmentID, container, false);
        Task       entry = (Task) this.entry;
        DateFormat df    = android.text.format.DateFormat.getMediumDateFormat(getContext());

        // Project type field
        int typeStringID = R.string.project_sequential;
        if (entry.type.equals("single")) {
            typeStringID = R.string.project_single_actions;
        } else if (entry.type.equals("parallel")) {
            typeStringID = R.string.project_parallel;
        }

        ((TextView) view.findViewById(R.id.detail_project_type_value)).setText(typeStringID);

        // Project status field
        if (entry.dropped) {
            ((TextView) view.findViewById(R.id.detail_status_value))
                    .setText(R.string.status_dropped);
        }

        // 'Complete when completing last action'
        if (entry.completeWithChildren) {
            ((SwitchCompat) view.findViewById(R.id.detail_project_complete_switch))
                    .setChecked(true);
        }

        // Context field
        if (entry.contextID != null) {
            final Context context = entry.getContext(getContext());

            TextView tv = (TextView) view.findViewById(R.id.detail_context_name);
            tv.setText(context.name);
            tv.setTextColor(PRIMARY_TEXT);

            ImageButton ib = (ImageButton) view.findViewById(R.id.detail_context_button);
            ib.setVisibility(View.VISIBLE);
            ib.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {

                    mListener.onContextClick(context.id);
                }
            });
        }

        // Flagged icon
        if (entry.flagged || entry.flaggedEffective) {
            ((ToggleButton) view.findViewById(R.id.detail_flag_button)).setChecked(true);
        }

        // Estimated duration
        if (entry.estimatedTime > 0) {
            String   time = String.valueOf(entry.estimatedTime) + " " + getString(R.string.minutes);
            TextView tv   = (TextView) view.findViewById(R.id.detail_duration_value);

            tv.setText(time);
            tv.setTextColor(PRIMARY_TEXT);
        }

        // Defer
        if (entry.dateDefer != null) {
            String   date = df.format(entry.dateDefer);
            TextView tv   = (TextView) view.findViewById(R.id.detail_defer_value);

            tv.setText(date);
            tv.setTextColor(PRIMARY_TEXT);
        }
        if (entry.dateDeferEffective != null) {

            String   date = df.format(entry.dateDeferEffective);
            TextView tv   = ((TextView) view.findViewById(R.id.detail_defer_value));

            // Set the text _and_ italicise it
            tv.setText(date);
            tv.setTextColor(PRIMARY_TEXT);
            tv.setTypeface(null, Typeface.ITALIC);
        }

        // Due
        if (entry.dateDue != null) {
            String   date = df.format(entry.dateDue);
            TextView tv   = (TextView) view.findViewById(R.id.detail_due_value);
            tv.setText(date);
            tv.setTextColor(PRIMARY_TEXT);
        } else if (entry.dateDueEffective != null) {
            String   date = df.format(entry.dateDueEffective);
            TextView tv   = ((TextView) view.findViewById(R.id.detail_due_value));
            tv.setText(date);
            tv.setTextColor(PRIMARY_TEXT);
            tv.setTypeface(null, Typeface.ITALIC);
        }

        // Repeat
        if (entry.repetitionRule != null) {
            TextView tv = (TextView) view.findViewById(R.id.detail_repeat_value);
            tv.setText(R.string.repeating);
            tv.setTextColor(PRIMARY_TEXT);
        }

        return view;
    }

    private View bindTaskDetails(int fragmentID, LayoutInflater inflater, ViewGroup container) {

        View       view  = inflater.inflate(fragmentID, container, false);
        Task       entry = (Task) this.entry;
        DateFormat df    = android.text.format.DateFormat.getMediumDateFormat(getContext());

        // Project field
        if (entry.projectID != null) {
            final Task project = entry.getProject(getContext());

            TextView tv = (TextView) view.findViewById(R.id.detail_project_name);
            tv.setText(project.name);
            tv.setTextColor(PRIMARY_TEXT);

            ImageButton ib = (ImageButton) view.findViewById(R.id.detail_project_button);
            ib.setVisibility(View.VISIBLE);
            ib.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {

                    mListener.onProjectClick(project.id);
                }
            });
        }

        // Context field
        if (entry.contextID != null) {

            final Context context = entry.getContext(getContext());

            TextView tv = (TextView) view.findViewById(R.id.detail_context_name);
            tv.setText(context.name);
            tv.setTextColor(PRIMARY_TEXT);

            ImageButton ib = (ImageButton) view.findViewById(R.id.detail_context_button);
            ib.setVisibility(View.VISIBLE);
            ib.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {

                    mListener.onContextClick(context.id);
                }
            });
        }

        // Flagged icon
        if (entry.flagged || entry.flaggedEffective) {
            ((ToggleButton) view.findViewById(R.id.detail_flag_button)).setChecked(true);
        }

        // Estimated duration
        if (entry.estimatedTime > 0) {
            String   time = String.valueOf(entry.estimatedTime) + " " + getString(R.string.minutes);
            TextView tv   = (TextView) view.findViewById(R.id.detail_duration_value);

            tv.setText(time);
            tv.setTextColor(PRIMARY_TEXT);
        }

        // Defer
        if (entry.dateDefer != null) {
            String   date = df.format(entry.dateDefer);
            TextView tv   = (TextView) view.findViewById(R.id.detail_defer_value);

            tv.setText(date);
            tv.setTextColor(PRIMARY_TEXT);
        } else if (entry.dateDeferEffective != null) {

            String   date = df.format(entry.dateDeferEffective);
            TextView tv   = ((TextView) view.findViewById(R.id.detail_defer_value));

            // Set the text _and_ italicise it
            tv.setText(date);
            tv.setTextColor(PRIMARY_TEXT);
            tv.setTypeface(null, Typeface.ITALIC);
        }

        // Due
        if (entry.dateDue != null) {
            String   date = df.format(entry.dateDue);
            TextView tv   = (TextView) view.findViewById(R.id.detail_due_value);
            tv.setText(date);
            tv.setTextColor(PRIMARY_TEXT);
        } else if (entry.dateDueEffective != null) {
            String   date = df.format(entry.dateDueEffective);
            TextView tv   = ((TextView) view.findViewById(R.id.detail_due_value));
            tv.setText(date);
            tv.setTextColor(PRIMARY_TEXT);
            tv.setTypeface(null, Typeface.ITALIC);
        }

        // Repeat
        if (entry.repetitionRule != null) {
            TextView tv = (TextView) view.findViewById(R.id.detail_repeat_value);
            tv.setText(R.string.repeating);
            tv.setTextColor(PRIMARY_TEXT);
        }

        return view;
    }

    private View bindContextDetails(int fragmentID, LayoutInflater inflater, ViewGroup container) {

        View view = inflater.inflate(fragmentID, container, false);

        // Status
        if (((Context) entry).droppedEffective) {
            ((TextView) view.findViewById(R.id.detail_status_value))
                    .setText(R.string.status_dropped);
        }

        return view;
    }

    private View bindFolderDetails(int fragmentID, LayoutInflater inflater, ViewGroup container) {

        View view = inflater.inflate(fragmentID, container, false);

        return view;
    }

    public interface OnDetailInteractionListener {

        void onContextClick(String id);

        void onProjectClick(String id);
    }
}
