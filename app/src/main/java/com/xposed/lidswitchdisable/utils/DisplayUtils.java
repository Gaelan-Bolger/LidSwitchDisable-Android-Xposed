package com.xposed.lidswitchdisable.utils;

import android.content.res.Resources;

public class DisplayUtils {

    public static int dp2px(int dp) {
        return (int) (Resources.getSystem().getDisplayMetrics().density * dp);
    }

}
