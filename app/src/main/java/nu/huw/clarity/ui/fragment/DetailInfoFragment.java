package nu.huw.clarity.ui.fragment;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;
import nu.huw.clarity.R;
import nu.huw.clarity.model.Context;
import nu.huw.clarity.model.Entry;
import nu.huw.clarity.model.Folder;
import nu.huw.clarity.model.Perspective;
import nu.huw.clarity.model.Task;
import org.threeten.bp.format.DateTimeFormatter;

/**
 * Fragment subclass for creating the 'list' of actionable items in the detail view. It chooses
 * and decides which layout XML to display depending on the type of entry passed to it, and then
 * populates it with the correct values.
 */
public class DetailInfoFragment extends Fragment {

  private static final String TAG = DetailInfoFragment.class.getSimpleName();
  private Entry entry;
  private Perspective perspective;
  private OnDetailInfoInteractionListener mListener;
  @ColorInt
  private int PRIMARY_TEXT_COLOR;

  public DetailInfoFragment() {
  }

  public static DetailInfoFragment newInstance(Entry entry, Perspective perspective) {

    DetailInfoFragment fragment = new DetailInfoFragment();
    Bundle args = new Bundle();
    args.putParcelable("ENTRY", entry);
    args.putParcelable("PERSPECTIVE", perspective);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    Bundle args = getArguments();
    if (args != null) {
      entry = args.getParcelable("ENTRY");
      perspective = args.getParcelable("PERSPECTIVE");
    } else {
      throw new IllegalArgumentException("DetailFragment requires argument bundle");
    }

    PRIMARY_TEXT_COLOR = ContextCompat.getColor(getContext(), R.color.primary_text_light);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    if (entry != null) {
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
    }

    throw new NullPointerException("Entry is of unknown type");
  }

  @Override
  public void onAttach(android.content.Context context) {
    super.onAttach(context);
    if (context instanceof OnDetailInfoInteractionListener) {
      mListener = (OnDetailInfoInteractionListener) context;
    } else {
      throw new RuntimeException(
          context.toString() + " must implement OnDetailInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  private View bindProjectDetails(int fragmentID, LayoutInflater inflater, ViewGroup container) {

    View view = inflater.inflate(fragmentID, container, false);
    Task entry = (Task) this.entry;
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy");

    // Project type field
    @StringRes int typeStringID = R.string.detail_projectsequential;
    if (entry.type.equals("single")) {
      typeStringID = R.string.detail_projectsingleactions;
    } else if (entry.type.equals("parallel")) {
      typeStringID = R.string.detail_projectparallel;
    }

    ((TextView) view.findViewById(R.id.textview_detailitem_projecttypevalue)).setText(typeStringID);

    // Project status field
    if (entry.dropped) {
      ((TextView) view.findViewById(R.id.textview_detailitem_statusvalue))
          .setText(R.string.detail_dropped);
    }

    // 'Complete when completing last action'
    if (entry.completeWithChildren) {
      ((SwitchCompat) view.findViewById(R.id.switch_detailitem_projectcomplete)).setChecked(true);
    }

    // Context field
    if (entry.contextID != null) {
      final Context context = entry.getContext(getContext());

      TextView tv = (TextView) view.findViewById(R.id.textview_detailitem_contextvalue);
      tv.setText(context.name);
      tv.setTextColor(PRIMARY_TEXT_COLOR);

      ImageButton ib = (ImageButton) view.findViewById(R.id.imagebutton_detailitem_context);
      ib.setVisibility(View.VISIBLE);
      ib.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {

          mListener.onContextClick(context, perspective);
        }
      });
    }

    // Flagged icon
    if (entry.flagged || entry.flaggedEffective) {
      ((ToggleButton) view.findViewById(R.id.textview_detailitem_flagvalue)).setChecked(true);
    }

    // Estimated duration
    if (entry.estimatedTime != null) {
      String time =
          String.valueOf(entry.estimatedTime.toMinutes()) + " " + getString(
              R.string.detail_minutes);
      TextView tv = (TextView) view.findViewById(R.id.textview_detailitem_durationvalue);

      tv.setText(time);
      tv.setTextColor(PRIMARY_TEXT_COLOR);
    }

    // Defer
    if (entry.dateDefer != null) {
      String date = entry.dateDefer.format(dateTimeFormatter);
      TextView tv = (TextView) view.findViewById(R.id.textview_detailitem_defervalue);

      tv.setText(date);
      tv.setTextColor(PRIMARY_TEXT_COLOR);
    }
    if (entry.dateDeferEffective != null) {

      String date = entry.dateDeferEffective.format(dateTimeFormatter);
      TextView tv = ((TextView) view.findViewById(R.id.textview_detailitem_defervalue));

      // Set the text _and_ italicise it
      tv.setText(date);
      tv.setTextColor(PRIMARY_TEXT_COLOR);
      tv.setTypeface(null, Typeface.ITALIC);
    }

    // Due
    if (entry.dateDue != null) {
      String date = entry.dateDue.format(dateTimeFormatter);
      TextView tv = (TextView) view.findViewById(R.id.textview_detailitem_duevalue);
      tv.setText(date);
      tv.setTextColor(PRIMARY_TEXT_COLOR);
    } else if (entry.dateDueEffective != null) {
      String date = entry.dateDueEffective.format(dateTimeFormatter);
      TextView tv = ((TextView) view.findViewById(R.id.textview_detailitem_duevalue));
      tv.setText(date);
      tv.setTextColor(PRIMARY_TEXT_COLOR);
      tv.setTypeface(null, Typeface.ITALIC);
    }

    // Repeat
    if (entry.repetitionRule != null) {
      TextView tv = (TextView) view.findViewById(R.id.textview_detailitem_repeatvalue);
      tv.setText(R.string.detail_repeating);
      tv.setTextColor(PRIMARY_TEXT_COLOR);
    }

    return view;
  }

  private View bindTaskDetails(int fragmentID, LayoutInflater inflater, ViewGroup container) {

    View view = inflater.inflate(fragmentID, container, false);
    Task entry = (Task) this.entry;
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy");

    // Project field
    if (entry.projectID != null) {
      final Task project = entry.getProject(getContext());

      TextView tv = (TextView) view.findViewById(R.id.textview_detailitem_projectvalue);
      tv.setText(project.name);
      tv.setTextColor(PRIMARY_TEXT_COLOR);

      ImageButton ib = (ImageButton) view.findViewById(R.id.imagebutton_detailitem_project);
      ib.setVisibility(View.VISIBLE);
      ib.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {

          mListener.onProjectClick(project, perspective);
        }
      });
    }

    // Context field
    if (entry.contextID != null) {

      final Context context = entry.getContext(getContext());

      TextView tv = (TextView) view.findViewById(R.id.textview_detailitem_contextvalue);
      tv.setText(context.name);
      tv.setTextColor(PRIMARY_TEXT_COLOR);

      ImageButton ib = (ImageButton) view.findViewById(R.id.imagebutton_detailitem_context);
      ib.setVisibility(View.VISIBLE);
      ib.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {

          mListener.onContextClick(context, perspective);
        }
      });
    }

    // Flagged icon
    if (entry.flagged || entry.flaggedEffective) {
      ((ToggleButton) view.findViewById(R.id.textview_detailitem_flagvalue)).setChecked(true);
    }

    // Estimated duration
    if (entry.estimatedTime != null) {
      String time =
          String.valueOf(entry.estimatedTime.toMinutes()) + " " + getString(
              R.string.detail_minutes);
      TextView tv = (TextView) view.findViewById(R.id.textview_detailitem_durationvalue);

      tv.setText(time);
      tv.setTextColor(PRIMARY_TEXT_COLOR);
    }

    // Defer
    if (entry.dateDefer != null) {
      String date = entry.dateDefer.format(dateTimeFormatter);
      TextView tv = (TextView) view.findViewById(R.id.textview_detailitem_defervalue);

      tv.setText(date);
      tv.setTextColor(PRIMARY_TEXT_COLOR);
    } else if (entry.dateDeferEffective != null) {

      String date = entry.dateDeferEffective.format(dateTimeFormatter);
      TextView tv = ((TextView) view.findViewById(R.id.textview_detailitem_defervalue));

      // Set the text _and_ italicise it
      tv.setText(date);
      tv.setTextColor(PRIMARY_TEXT_COLOR);
      tv.setTypeface(null, Typeface.ITALIC);
    }

    // Due
    if (entry.dateDue != null) {
      String date = entry.dateDue.format(dateTimeFormatter);
      TextView tv = (TextView) view.findViewById(R.id.textview_detailitem_duevalue);
      tv.setText(date);
      tv.setTextColor(PRIMARY_TEXT_COLOR);
    } else if (entry.dateDueEffective != null) {
      String date = entry.dateDueEffective.format(dateTimeFormatter);
      TextView tv = ((TextView) view.findViewById(R.id.textview_detailitem_duevalue));
      tv.setText(date);
      tv.setTextColor(PRIMARY_TEXT_COLOR);
      tv.setTypeface(null, Typeface.ITALIC);
    }

    // Repeat
    if (entry.repetitionRule != null) {
      TextView tv = (TextView) view.findViewById(R.id.textview_detailitem_repeatvalue);
      tv.setText(R.string.detail_repeating);
      tv.setTextColor(PRIMARY_TEXT_COLOR);
    }

    return view;
  }

  private View bindContextDetails(int fragmentID, LayoutInflater inflater, ViewGroup container) {

    View view = inflater.inflate(fragmentID, container, false);

    // Status
    if (((Context) entry).droppedEffective) {
      ((TextView) view.findViewById(R.id.textview_detailitem_statusvalue))
          .setText(R.string.detail_dropped);
    }

    return view;
  }

  private View bindFolderDetails(int fragmentID, LayoutInflater inflater, ViewGroup container) {

    View view = inflater.inflate(fragmentID, container, false);

    return view;
  }

  public interface OnDetailInfoInteractionListener {

    void onContextClick(Entry entry, Perspective perspective);

    void onProjectClick(Entry entry, Perspective perspective);
  }
}
