package com.snailstudio2010.libutils;

import android.app.Activity;
import android.util.DisplayMetrics;

/**
 * ScreenInfo.java Use this class to get the information of the screen.
 * <p>
 * Created by xuqiqiang on 2016/05/17.
 */
public class ScreenUtils {
    private static boolean initialized;
    private static int widthPixels;
    private static int heightPixels;

    public static void initialize(Activity context) {
        DisplayMetrics dm = new DisplayMetrics();

        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        ScreenUtils.widthPixels = dm.widthPixels;
        ScreenUtils.heightPixels = dm.heightPixels;
        initialized = true;
    }

    /**
     * @return the number of pixel in the width of the screen.
     */
    public static int getWidthPixels() {
        if (!initialized) throw new RuntimeException("Not initialized");
        return widthPixels;
    }

    /**
     * @return the number of pixel in the height of the screen.
     */
    public static int getHeightPixels() {
        if (!initialized) throw new RuntimeException("Not initialized");
        return heightPixels;
    }

    public static String getSize() {
        if (!initialized) throw new RuntimeException("Not initialized");
        return widthPixels + "×" + heightPixels;
    }
}