package nu.huw.clarity.ui.dialog;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import nu.huw.clarity.R;
import org.threeten.bp.LocalDate;

public class DatePickerDialogFragment extends DialogFragment {

  private static final String TAG = DatePickerDialogFragment.class.getSimpleName();
  private OnDateSetListener listener;
  private boolean enableClear;

  public DatePickerDialogFragment() {
  }

  public static DatePickerDialogFragment newInstance(OnDateSetListener listener,
      boolean enableClear) {
    DatePickerDialogFragment datePickerDialogFragment = new DatePickerDialogFragment();
    datePickerDialogFragment.setRetainInstance(true);
    datePickerDialogFragment.setListener(listener);
    datePickerDialogFragment.enableClear = enableClear;
    return datePickerDialogFragment;
  }

  public void setListener(OnDateSetListener listener) {
    Log.d(TAG, "LISTENING");
    this.listener = listener;
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {

    // Use current date for calendar

    LocalDate date = LocalDate.now();
    int year = date.getYear();
    int month = date.getMonthValue();
    int dayOfMonth = date.getDayOfMonth();

    final DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), listener, year,
        month, dayOfMonth);

    // Create a clear button if enabled which sets the date to zeros

    if (enableClear) {
      datePickerDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.detail_clear),
          new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
              listener.onDateSet(datePickerDialog.getDatePicker(), 0, 0, 0);
            }
          });
    }

    return datePickerDialog;
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
