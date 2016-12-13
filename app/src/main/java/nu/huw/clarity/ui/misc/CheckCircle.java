package nu.huw.clarity.ui.misc;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;

import nu.huw.clarity.R;

/**
 * Custom class for the iconic OmniFocus Check Circle.
 * Includes special states for an indication of due soon / overdue, etc.
 * Credit http://stackoverflow.com/a/5806001
 */
public class CheckCircle extends CheckBox {

    private static final int[] STATE_FLAGGED  = {R.attr.state_flagged};
    private static final int[] STATE_OVERDUE  = {R.attr.state_overdue};
    private static final int[] STATE_DUE_SOON = {R.attr.state_due_soon};
    private boolean flagged;
    private boolean overdue;
    private boolean dueSoon;

    public CheckCircle(Context context, AttributeSet attrs) {

        super(context, attrs);
    }

    public void setFlagged(boolean flagged) {

        this.flagged = flagged;
    }

    public void setOverdue(boolean overdue) {

        this.overdue = overdue;
    }

    public void setDueSoon(boolean dueSoon) {

        this.dueSoon = dueSoon;
    }

    @Override protected int[] onCreateDrawableState(int extraSpace) {

        if (flagged) extraSpace++;
        if (overdue) extraSpace++;
        if (dueSoon) extraSpace++;

        int[] baseState = super.onCreateDrawableState(extraSpace);

        /*
         Android handles multiple states just fine, so no need to specify weird rules for multiple.
         */
        if (flagged) mergeDrawableStates(baseState, STATE_FLAGGED);
        if (overdue) mergeDrawableStates(baseState, STATE_OVERDUE);
        if (dueSoon) mergeDrawableStates(baseState, STATE_DUE_SOON);

        return baseState;
    }
}
