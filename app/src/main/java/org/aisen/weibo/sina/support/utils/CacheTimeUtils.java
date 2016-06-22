package org.aisen.weibo.sina.support.utils;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.ActivityHelper;
import org.aisen.android.common.utils.Logger;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;

public class CacheTimeUtils {

	public static void saveTime(String key, WeiBoUser owner) {
		if (owner != null)
			key = String.format("%s-%s", key, owner.getIdstr());
		
		String time = String.valueOf(System.currentTimeMillis() / 1000);
		
		ActivityHelper.putShareData(GlobalContext.getInstance(), key, time);
		
		Logger.d("CacheTimeUtils", String.format("保存缓存 %s, saveTime = %s", key, time));
	}
	
	public static boolean isOutofdate(String key, WeiBoUser owner) {
		if (owner != null)
			key = String.format("%s-%s", key, owner.getIdstr());
			
		long saveTime = Long.parseLong(ActivityHelper.getShareData(GlobalContext.getInstance(), key, "0"));
		
		boolean expired = (System.currentTimeMillis() / 1000 - saveTime) * 1000 >= AppSettings.getRefreshInterval();
		
		Logger.d("CacheTimeUtils", String.format("缓存有效性 %s, expired = %s", key, String.valueOf(expired)));
		
		return expired;
	}
	
}
