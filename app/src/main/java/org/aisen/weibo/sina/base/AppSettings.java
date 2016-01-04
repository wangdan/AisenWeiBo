package org.aisen.weibo.sina.base;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.ActivityHelper;
import org.aisen.android.common.utils.SystemUtils;

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

    /**
     * 微博加载数量
     *
     * @return
     */
    public static int getTimelineCount() {
//        if (true) return 5;
        final int[] countArr = { 20, 50, 100 };

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        int index = Integer.parseInt(prefs.getString("pTimelineCount", "0"));

        int count = countArr[0];
        if (index == 3) {
            if (SystemUtils.getNetworkType() == SystemUtils.NetWorkType.wifi)
                count = 100;
        }
        else {
            count = countArr[index];
        }

        return count;
    }

}
