package nu.huw.clarity.ui.misc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import nu.huw.clarity.R;

public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    private Drawable mDivider;

    public DividerItemDecoration(Context context) {

        mDivider = ResourcesCompat
                .getDrawable(context.getResources(), R.drawable.line_divider, context.getTheme());
    }

    @Override public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top    = child.getBottom() + params.bottomMargin;
            int bottom = top + mDivider.getIntrinsicHeight();
            int left   = params.leftMargin;
            int right  = child.getWidth() + left;

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }
}