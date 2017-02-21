package nu.huw.clarity.ui.fragment;

import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;
import butterknife.BindView;
import butterknife.Unbinder;
import nu.huw.clarity.R;
import nu.huw.clarity.model.Context;
import nu.huw.clarity.model.Entry;
import nu.huw.clarity.model.Perspective;
import nu.huw.clarity.model.Task;
import nu.huw.clarity.ui.dialog.DatePickerDialogFragment;
import nu.huw.clarity.ui.dialog.TimePickerDialogFragment;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

/**
 * Fragment subclass for creating the 'list' of actionable items in the detail view. It chooses
 * and decides which layout XML to display depending on the type of entry passed to it, and then
 * populates it with the correct values.
 */
public class DetailInfoFragment extends Fragment {

  private static final String TAG = DetailInfoFragment.class.getSimpleName();
  private static final String TIME_PICKER_TAG = "TIMEPICKERDIALOGFRAGMENT_TAG";
  private static final String DATE_PICKER_TAG = "DATEPICKERDIALOGFRAGMENT_TAG";
  Entry entry;
  Perspective perspective;
  OnDetailInfoInteractionListener listener;
  Unbinder unbinder;
  @ColorInt
  int TEXT_COLOR_PRIMARY;
  @ColorInt
  int TEXT_COLOR_SECONDARY;
  @Nullable
  @BindView(R.id.relativelayout_detailitem_defer)
  RelativeLayout relativelayout_detailitem_defer;
  @Nullable
  @BindView(R.id.textview_detailitem_defervalue)
  TextView textview_detailitem_defervalue;
  private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMM d yyyy hh:mma");
  private OnDateSetListener onDeferDateSetListener;

  public DetailInfoFragment() {
  }

  public static DetailInfoFragment newInstance(Entry entry, Perspective perspective) {

    DetailInfoFragment fragment = new DetailInfoFragment();
    fragment.setRetainInstance(true);
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

    TypedValue typedValue = new TypedValue();
    getContext().getTheme().resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
    TEXT_COLOR_PRIMARY = typedValue.data;

    getContext().getTheme().resolveAttribute(android.R.attr.textColorSecondary, typedValue, true);
    TEXT_COLOR_SECONDARY = typedValue.data;
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
  void bindContextValue(Task task, RelativeLayout container, TextView textView,
      ImageButton imageButton) {
    if (task.contextID != null) {

      final Context context = task.getContext(getContext());
      textView.setText(context.name);
      textView.setTextColor(TEXT_COLOR_PRIMARY);

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
  void bindFlaggedValue(Task task, RelativeLayout container, ToggleButton toggleButton) {
    if (task.flagged || task.flaggedEffective) {
      toggleButton.setChecked(true);
    }
  }

  /**
   * Binds the "Estimated Duration" field
   */
  void bindDurationValue(Task task, RelativeLayout container, TextView textView) {
    if (task.estimatedTime != null) {
      String time =
          String.valueOf(task.estimatedTime.toMinutes()) + " " + getString(R.string.detail_minutes);
      textView.setText(time);
      textView.setTextColor(TEXT_COLOR_PRIMARY);
    }
  }

  /**
   * Binds the "Defer Until" field
   */
  void bindDeferValue(final Task task, final RelativeLayout container, final TextView textView) {
    if (task.dateDefer != null) {

      String date = task.dateDefer.format(dateTimeFormatter);

      textview_detailitem_defervalue.setText(date);
      textview_detailitem_defervalue.setTextColor(TEXT_COLOR_PRIMARY);
      textview_detailitem_defervalue.setTypeface(null, Typeface.NORMAL);

    } else if (task.dateDeferEffective != null) {

      String date = task.dateDeferEffective.format(dateTimeFormatter);

      // Set the text _and_ italicise it

      textview_detailitem_defervalue.setText(date);
      textview_detailitem_defervalue.setTextColor(TEXT_COLOR_PRIMARY);
      textview_detailitem_defervalue.setTypeface(null, Typeface.ITALIC);

    } else {

      // Remove style, set text to "None"

      textview_detailitem_defervalue.setText(getString(R.string.detail_none));
      textview_detailitem_defervalue.setTextColor(TEXT_COLOR_SECONDARY);
      textview_detailitem_defervalue.setTypeface(null, Typeface.NORMAL);
    }

    // Setup listener for changes

    onDeferDateSetListener = new OnDateSetListener() {
      @Override
      public void onDateSet(DatePicker datePicker, final int year, final int month,
          final int dayOfMonth) {

        final Task task = (Task) entry;
        if (year == 0 && month == 0 && dayOfMonth == 0) {

          // We cleared the date, reset the task's dateDefer

          task.dateDefer = null;
          bindDeferValue(task, container, textView);

        } else {

          // Open a time picker

          TimePickerDialogFragment timePickerDialogFragment = TimePickerDialogFragment.newInstance(
              new OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int hour, int minute) {

                  // Actual code starts here
                  // Save the new dateTime into the dateDefer field and rebind values

                  task.dateDefer = LocalDateTime.of(year, month, dayOfMonth, hour, minute);
                  bindDeferValue(task, container, textView);

                }
              });
          timePickerDialogFragment.show(getChildFragmentManager(), TIME_PICKER_TAG);
        }
      }
    };

    // Set click listener

    container.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {

        DatePickerDialogFragment datePickerDialogFragment = DatePickerDialogFragment
            .newInstance(onDeferDateSetListener, task.dateDefer != null);
        datePickerDialogFragment.show(getChildFragmentManager(), DATE_PICKER_TAG);

      }
    });
  }

  /**
   * Binds the "Due" field
   */
  void bindDueValue(Task task, RelativeLayout container, TextView textView) {
    if (task.dateDue != null) {

      String date = task.dateDue.format(dateTimeFormatter);
      textView.setText(date);
      textView.setTextColor(TEXT_COLOR_PRIMARY);

    } else if (task.dateDueEffective != null) {

      String date = task.dateDueEffective.format(dateTimeFormatter);
      textView.setText(date);
      textView.setTextColor(TEXT_COLOR_PRIMARY);
      textView.setTypeface(null, Typeface.ITALIC);
    }
  }

  /**
   * Binds the "Repeat" field
   */
  void bindRepeatValue(Task task, RelativeLayout container, TextView textView) {
    if (task.repetitionRule != null) {
      textView.setText(getString(R.string.detail_repeating));
      textView.setTextColor(TEXT_COLOR_PRIMARY);
    }
  }

  public interface OnDetailInfoInteractionListener {

    void onContextClick(Entry entry, Perspective perspective);

    void onProjectClick(Entry entry, Perspective perspective);
  }
}
