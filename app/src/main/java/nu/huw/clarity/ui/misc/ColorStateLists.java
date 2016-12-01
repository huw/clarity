package nu.huw.clarity.ui.misc;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;

import nu.huw.clarity.R;

/**
 * Holder for colorStateLists so I don't have them clogging up my main code line.
 */
public class ColorStateLists {

    private static int[][] states =
            new int[][]{new int[]{android.R.attr.state_checked},   // Checked
                        new int[]{}                               // Default
            };
    public ColorStateList red;
    public ColorStateList blueGrey;
    public ColorStateList blue;
    public ColorStateList purple;
    public ColorStateList orange;
    public ColorStateList green;

    public ColorStateLists(Context context) {

        int[] redArray = new int[]{ContextCompat.getColor(context, R.color.primary_red),
                                   ContextCompat.getColor(context,
                                                          android.R.color.primary_text_light)};
        int[] blueGreyArray = new int[]{ContextCompat.getColor(context, R.color.primary_blue_grey),
                                        ContextCompat.getColor(context,
                                                               android.R.color.primary_text_light)};
        int[] blueArray = new int[]{ContextCompat.getColor(context, R.color.primary_blue),
                                    ContextCompat.getColor(context,
                                                           android.R.color.primary_text_light)};
        int[] purpleArray = new int[]{ContextCompat.getColor(context, R.color.primary),
                                      ContextCompat.getColor(context,
                                                             android.R.color.primary_text_light)};
        int[] orangeArray = new int[]{ContextCompat.getColor(context, R.color.primary_orange),
                                      ContextCompat.getColor(context,
                                                             android.R.color.primary_text_light)};
        int[] greenArray = new int[]{ContextCompat.getColor(context, R.color.primary_green),
                                     ContextCompat.getColor(context,
                                                            android.R.color.primary_text_light)};

        red = new ColorStateList(states, redArray);
        blueGrey = new ColorStateList(states, blueGreyArray);
        blue = new ColorStateList(states, blueArray);
        purple = new ColorStateList(states, purpleArray);
        orange = new ColorStateList(states, orangeArray);
        green = new ColorStateList(states, greenArray);
    }
}
