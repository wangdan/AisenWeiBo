package org.aisen.android.common.utils;

/**
 * Created by wangdan on 15/8/29.
 */
public class CacheTimeUtils {

    public static void saveTime(String key) {
        String time = String.valueOf(System.currentTimeMillis() / 1000);

        ActivityHelper.putShareData(key, time);

        Logger.d("CacheTimeUtils", String.format("保存缓存 %s, saveTime = %s", key, time));
    }

    public static boolean isOutofdate(String key, long refreshInterval) {
        long saveTime = Long.parseLong(ActivityHelper.getShareData(key, "0"));

        boolean expired = (System.currentTimeMillis() / 1000 - saveTime) * 1000 >= refreshInterval;

        Logger.d("CacheTimeUtils", String.format("缓存有效性 %s, expired = %s", key, String.valueOf(expired)));

        return expired;
    }

}
