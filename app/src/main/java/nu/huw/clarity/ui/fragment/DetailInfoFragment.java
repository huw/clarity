package nu.huw.clarity.ui.fragment;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;
import butterknife.Unbinder;
import nu.huw.clarity.R;
import nu.huw.clarity.model.Context;
import nu.huw.clarity.model.Entry;
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
  Entry entry;
  Perspective perspective;
  OnDetailInfoInteractionListener listener;
  Unbinder unbinder;
  @ColorInt
  int PRIMARY_TEXT_COLOR;
  private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMM d yyyy hh:mma");

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
    onAttachToParentFragment(getParentFragment());
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
    throw new NullPointerException(
        "OnCreateView must be overridden by a child class of DetailInfoFragment");
  }

  void onAttachToParentFragment(Fragment fragment) {
    if (fragment instanceof OnDetailInfoInteractionListener) {
      listener = (OnDetailInfoInteractionListener) fragment;
    } else if (fragment != null) {
      throw new ClassCastException(
          fragment.toString() + " must implement OnDetailInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    listener = null;
  }

  /**
   * Binds the "Context" field
   */
  void bindContextValue(Task task, TextView textView, ImageButton imageButton) {
    if (task.contextID != null) {

      final Context context = task.getContext(getContext());
      textView.setText(context.name);
      textView.setTextColor(PRIMARY_TEXT_COLOR);

      imageButton.setVisibility(View.VISIBLE);
      imageButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {

          listener.onContextClick(context, perspective);
        }
      });
    }
  }

  /**
   * Binds the "Flagged" field
   */
  void bindFlaggedValue(Task task, ToggleButton toggleButton) {
    if (task.flagged || task.flaggedEffective) {
      toggleButton.setChecked(true);
    }
  }

  /**
   * Binds the "Estimated Duration" field
   */
  void bindDurationValue(Task task, TextView textView) {
    if (task.estimatedTime != null) {
      String time =
          String.valueOf(task.estimatedTime.toMinutes()) + " " + getString(R.string.detail_minutes);
      textView.setText(time);
      textView.setTextColor(PRIMARY_TEXT_COLOR);
    }
  }

  /**
   * Binds the "Defer Until" field
   */
  void bindDeferValue(Task task, TextView textView) {
    if (task.dateDefer != null) {

      String date = task.dateDefer.format(dateTimeFormatter);

      textView.setText(date);
      textView.setTextColor(PRIMARY_TEXT_COLOR);

    } else if (task.dateDeferEffective != null) {

      String date = task.dateDeferEffective.format(dateTimeFormatter);

      // Set the text _and_ italicise it

      textView.setText(date);
      textView.setTextColor(PRIMARY_TEXT_COLOR);
      textView.setTypeface(null, Typeface.ITALIC);
    }
  }

  /**
   * Binds the "Due" field
   */
  void bindDueValue(Task task, TextView textView) {
    if (task.dateDue != null) {

      String date = task.dateDue.format(dateTimeFormatter);
      textView.setText(date);
      textView.setTextColor(PRIMARY_TEXT_COLOR);

    } else if (task.dateDueEffective != null) {

      String date = task.dateDueEffective.format(dateTimeFormatter);
      textView.setText(date);
      textView.setTextColor(PRIMARY_TEXT_COLOR);
      textView.setTypeface(null, Typeface.ITALIC);
    }
  }

  /**
   * Binds the "Repeat" field
   */
  void bindRepeatValue(Task task, TextView textView) {
    if (task.repetitionRule != null) {
      textView.setText(getString(R.string.detail_repeating));
      textView.setTextColor(PRIMARY_TEXT_COLOR);
    }
  }

  public interface OnDetailInfoInteractionListener {

    void onContextClick(Entry entry, Perspective perspective);

    void onProjectClick(Entry entry, Perspective perspective);
  }
}
