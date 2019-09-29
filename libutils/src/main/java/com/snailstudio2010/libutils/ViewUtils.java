package com.snailstudio2010.libutils;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by xuqiqiang on 2016/05/17.
 */
public class ViewUtils {

    public static void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    public static void setMargins(View v, int value) {
        setMargins(v, value, value, value, value);
    }
}