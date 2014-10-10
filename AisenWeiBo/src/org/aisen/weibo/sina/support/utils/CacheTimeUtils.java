package org.aisen.weibo.sina.support.utils;

import org.sina.android.bean.WeiBoUser;

import com.m.common.utils.ActivityHelper;
import com.m.common.utils.Logger;

public class CacheTimeUtils {

	public static void saveTime(String key, WeiBoUser owner) {
		if (owner != null)
			key = String.format("%s-%s", key, owner.getIdstr());
		
		String time = String.valueOf(System.currentTimeMillis() / 1000);
		
		ActivityHelper.getInstance().putShareData(key, time);
		
		Logger.d("CacheTimeUtils", String.format("保存缓存 %s, saveTime = %s", key, time));
	}
	
	public static boolean isExpired(String key, WeiBoUser owner) {
		if (owner != null)
			key = String.format("%s-%s", key, owner.getIdstr());
			
		long saveTime = Long.parseLong(ActivityHelper.getInstance().getShareData(key, "0"));
		
		boolean expired = (System.currentTimeMillis() / 1000 - saveTime) * 1000 >= AppSettings.getRefreshInterval();
		
		Logger.d("CacheTimeUtils", String.format("缓存有效性 %s, expired = %s", key, String.valueOf(expired)));
		
		return expired;
	}
	
}
