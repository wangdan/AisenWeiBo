package org.aisen.weibo.sina.base;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.m.common.context.GlobalContext;
import com.m.common.utils.ActivityHelper;

import org.aisen.weibo.sina.R;

/**
 * Created by wangdan on 15/4/12.
 */
public class AppSettings {

    /**
     * 网络请求延迟
     *
     * @return
     */
    public static boolean isNetworkDelay() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pNetworkDelay", false);
    }

    /**
     * 提醒设置
     *
     * @return
     */
    public static boolean isNotifyEnable() {
        return ActivityHelper.getBooleanShareData("org.aisen.weibo.sina.NOTIFICATION", true);
    }

    /**
     * 提及评论提醒
     *
     * @return
     */
    public static boolean isNotifyCommentMention() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pCommentMention", true);
    }

    /**
     * 提及微博提醒
     *
     * @return
     */
    public static boolean isNotifyStatusMention() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pStatusMention", true);
    }

    /**
     * 粉丝提醒
     *
     * @return
     */
    public static boolean isNotifyFollower() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pFollower", true);
    }

    /**
     * 评论提醒
     *
     * @return
     */
    public static boolean isNotifyComment() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pComment", true);
    }

    /**
     * 私信提醒
     *
     * @return
     */
    public static boolean isNotifyDm() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pDm", true);
    }

    /**
     * 声音提醒
     *
     * @return
     */
    public static boolean isNotifySound() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pNotifySound", true);
    }

    /**
     * 振动提醒
     *
     * @return
     */
    public static boolean isNotifyVibrate() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pNotifyVibrate", true);
    }

    /**
     * LED提醒
     *
     * @return
     */
    public static boolean isNotifyLED() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pNotifyLED", true);
    }

    /**
     * 夜间不扰
     *
     * @return
     */
    public static boolean isNotifyNightClose() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pNightClose", true);
    }

    /**
     * 发送成功后的震动反馈
     *
     * @return
     */
    public static boolean isSendVibrate() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pSendVibrate", true);
    }

    /**
     * 未读小时间隔时间
     *
     * @return
     */
    public static int getUnreadInterval() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        int value = Integer.parseInt(prefs.getString("pInterval", "0"));
        String[] valueArr = GlobalContext.getInstance().getResources().getStringArray(R.array.prefValues);

        int interval = 60;

        switch (Integer.parseInt(valueArr[value])) {
            case 0:
                interval = 60;
                break;
            case 1:
                interval = 60 * 5;
                break;
            case 2:
                interval = 60 * 15;
                break;
            case 3:
                interval = 60 * 60;
                break;
        }

        return interval;
    }

    /**
     * 上传图片质量设置
     *
     * @return
     */
    public static int getUploadSetting() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        int value = Integer.parseInt(prefs.getString("pUploadSetting", "0"));
        return value;
    }

}
