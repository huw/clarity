package nu.huw.clarity.ui.misc;

import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;

import nu.huw.clarity.R;
import nu.huw.clarity.ui.MainActivity;

/**
 * Holder for colorStateLists so I don't have them clogging up my main code line.
 */
public class ColorStateLists {

    private static int[][]        states        =
            new int[][]{new int[]{android.R.attr.state_checked},   // Checked
                        new int[]{}                               // Default
            };
    private static int[]          redArray      =
            new int[]{ContextCompat.getColor(MainActivity.context, R.color.primary_red),
                      ContextCompat.getColor(MainActivity.context,
                                             android.R.color.tertiary_text_light)};
    public static  ColorStateList red           = new ColorStateList(states, redArray);
    private static int[]          blueGreyArray =
            new int[]{ContextCompat.getColor(MainActivity.context, R.color.primary_blue_grey),
                      ContextCompat.getColor(MainActivity.context,
                                             android.R.color.tertiary_text_light)};
    public static  ColorStateList blueGrey      = new ColorStateList(states, blueGreyArray);
    private static int[]          blueArray     =
            new int[]{ContextCompat.getColor(MainActivity.context, R.color.primary_blue),
                      ContextCompat.getColor(MainActivity.context,
                                             android.R.color.tertiary_text_light)};
    public static  ColorStateList blue          = new ColorStateList(states, blueArray);
    private static int[]          purpleArray   =
            new int[]{ContextCompat.getColor(MainActivity.context, R.color.primary),
                      ContextCompat.getColor(MainActivity.context,
                                             android.R.color.tertiary_text_light)};
    public static  ColorStateList purple        = new ColorStateList(states, purpleArray);
    private static int[]          orangeArray   =
            new int[]{ContextCompat.getColor(MainActivity.context, R.color.primary_orange),
                      ContextCompat.getColor(MainActivity.context,
                                             android.R.color.tertiary_text_light)};
    public static  ColorStateList orange        = new ColorStateList(states, orangeArray);
    private static int[]          greenArray    =
            new int[]{ContextCompat.getColor(MainActivity.context, R.color.primary_green),
                      ContextCompat.getColor(MainActivity.context,
                                             android.R.color.tertiary_text_light)};
    public static  ColorStateList green         = new ColorStateList(states, greenArray);
}
