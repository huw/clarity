package nu.huw.clarity.ui.dialog;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.util.Log;
import org.threeten.bp.LocalTime;

public class TimePickerDialogFragment extends DialogFragment {

  private static final String TAG = TimePickerDialogFragment.class.getSimpleName();
  private OnTimeSetListener listener;

  public TimePickerDialogFragment() {
  }

  public static TimePickerDialogFragment newInstance(OnTimeSetListener listener) {
    TimePickerDialogFragment timePickerDialogFragment = new TimePickerDialogFragment();
    timePickerDialogFragment.setRetainInstance(true);
    timePickerDialogFragment.setListener(listener);
    return timePickerDialogFragment;
  }

  public void setListener(OnTimeSetListener listener) {
    Log.d(TAG, "LISTENING");
    this.listener = listener;
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {

    // Use current date for calendar

    LocalTime time = LocalTime.now();
    int hour = time.getHour();
    int minute = time.getMinute();

    return new TimePickerDialog(getActivity(), listener, hour, minute,
        DateFormat.is24HourFormat(getActivity()));
  }

  /**
   * https://code.google.com/p/android/issues/detail?id=17423#c26
   */
  @Override
  public void onDestroyView() {
    if (getDialog() != null && getRetainInstance()) {
      getDialog().setDismissMessage(null);
    }
    super.onDestroyView();
  }
}
