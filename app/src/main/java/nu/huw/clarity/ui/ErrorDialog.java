package nu.huw.clarity.ui;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;

import nu.huw.clarity.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ErrorDialog extends DialogFragment {

    private static final String TAG = ErrorDialog.class.getSimpleName();

    public ErrorDialog() {
        // Required empty public constructor
    }

    public interface onErrorDismissListener {
        void onErrorDismiss(int resultCode);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        Bundle args = getArguments();
        String message = args.getString("MESSAGE", "");
        String positiveString = args.getString("BUTTON_STRING", getString(R.string.try_again));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message);

        builder.setPositiveButton(positiveString, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // (The code up to here explains itself)
                // These two onClick responses pass a result back to the Activity
                // that called up the dialog. The positive button gives an 'ok',
                // and the negative gives a 'cancelled'.
                //
                // They do this because the activity implements a listener function
                // which we then pass back to. If it doesn't (and we get an error),
                // then we dismiss the message.

                try {

                    onErrorDismissListener activity = (onErrorDismissListener) getActivity();
                    activity.onErrorDismiss(Activity.RESULT_OK);

                } catch (ClassCastException e) {
                    Log.e(
                            TAG,
                            getActivity().getPackageName() +
                                    " should implement onErrorDismiss to receive results"
                    );
                }
            }
        });

        // Only build a 'cancel' button if there's not another action.
        // I.E. If the string on the positive button is 'Got it', then
        // the negative button is doing exactly the same thing. So
        // there's no need for duplicate buttons

        if (!positiveString.equals(getString(R.string.got_it))) {

            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        onErrorDismissListener activity = (onErrorDismissListener) getActivity();
                        activity.onErrorDismiss(Activity.RESULT_CANCELED);
                    } catch (ClassCastException e) {
                        Log.e(
                                TAG,
                                getActivity().getPackageName() +
                                        " should implement onErrorDismiss to receive results"
                        );
                    }
                }
            });

        }

        return builder.create();
    }

}
