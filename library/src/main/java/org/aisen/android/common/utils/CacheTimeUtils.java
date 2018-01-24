package org.aisen.android.common.utils;

import android.content.Context;
import android.text.TextUtils;

/**
 * Created by wangdan on 15/8/29.
 */
public class CacheTimeUtils {

    public static void saveTime(Context context, String key) {
        String time = String.valueOf(System.currentTimeMillis() / 1000);

        ActivityHelper.putShareData(context, key, time);

        Logger.d("CacheTimeUtils", String.format("保存缓存 %s, saveTime = %s", key, time));
    }

    public static long getSaveTime(Context context, String key) {
        String time = ActivityHelper.getShareData(context, key, "");
        long saveTime = Long.parseLong(TextUtils.isEmpty(time) ? "0" : time);
        return saveTime;
    }

    public static boolean isOutofdate(Context context, String key, long refreshInterval) {
        try {
            long saveTime = getSaveTime(context, key);

            boolean expired = Math.abs((System.currentTimeMillis() / 1000 - saveTime) * 1000) >= refreshInterval;

            Logger.d("CacheTimeUtils", String.format("缓存有效性 %s, expired = %s", key, String.valueOf(expired)));

            return expired;
        } catch (Throwable e) {
            Logger.printExc(CacheTimeUtils.class, e);

            return true;
        }
    }

}
