package org.aisen.weibo.sina.base;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.ActivityHelper;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.weibo.sina.R;

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
     * 是否使用内置浏览器
     *
     * @return
     */
    public static boolean isInnerBrower() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pInnerBrowser", true);
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

    /**
     * 图片加载模式
     *
     * @return
     */
    public static int getPictureMode() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        int value = Integer.parseInt(prefs.getString("pPicMode", "2"));
        return value;
    }

    /**
     * 默认加载原图
     *
     * @return
     */
    public static boolean isLoadOrigPic() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pLoadOrigPic", false);
    }

    /**
     * 开启高清图已下载提示
     *
     * @return
     */
    public static boolean midPicHint() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pMidPicHint", true);
    }

    /**
     * 正文字体大小
     *
     * @return
     */
    private static int[] txtSizeResArr = new int[]{ R.dimen.sp_12, R.dimen.sp_13, R.dimen.sp_14, R.dimen.sp_15,
            R.dimen.sp_16, R.dimen.sp_17, R.dimen.sp_18, R.dimen.sp_19,
            R.dimen.sp_20 };
    public static int getTextSize() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        int value = Integer.parseInt(prefs.getString("pTextSize", "4"));
        return GlobalContext.getInstance().getResources().getDimensionPixelSize(txtSizeResArr[value]);
    }

    /**
     * 无图模式
     *
     * @return
     */
    public static boolean isPicNone() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pNonePic", false);
    }

    /**
     * 是否显示备注
     *
     * @return
     */
    public static boolean isShowRemark() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pShowRemark", true);
    }

    /**
     * 显示高清头像
     *
     * @return
     */
    public static boolean isLargePhoto() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pLargePhoto", true);
    }

    /**
     * 评论加载数量
     *
     * @return
     */
    public static int getCommentCount() {
        final int[] countArr = { 20, 50, 100 };

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        int index = Integer.parseInt(prefs.getString("pCommentCount", "0"));

        int count = 50;
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
