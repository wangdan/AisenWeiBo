package org.aisen.weibo.sina.base;

import org.aisen.android.common.utils.ActivityHelper;

/**
 * Created by wangdan on 15/12/31.
 */
public class AppSettings {

    public static int getThemeColor() {
        return ActivityHelper.getIntShareData("Theme_index", 8);
    }

    public static void setThemeColor(int theme) {
        ActivityHelper.putIntShareData("Theme_index", theme);
    }

}
