package org.aisen.weibo.sina.support.utils;

import com.m.common.utils.Logger;
import com.m.ui.activity.BaseActivity;

public class BaiduAnalyzeUtils {

	static final String TAG = "BaiduAnalyze";
	
	private static String lastPageTitleString;
	
	public static void onPageEnd(String pageName) {
		Logger.d(TAG, String.format("page %s end", pageName));
		
		if (BaseActivity.getRunningActivity() != null && !pageName.equals(lastPageTitleString)) { 
			com.baidu.mobstat.StatService.onPageEnd(BaseActivity.getRunningActivity(), pageName);
			
//			StatService.trackEndPage(BaseActivity.getRunningActivity(), pageName);
			
			lastPageTitleString = pageName;
		}
	}
	
	public static void onPageStart(String pageName) {
		Logger.d(TAG, String.format("page %s start", pageName));
		
		if (BaseActivity.getRunningActivity() != null && !pageName.equals(lastPageTitleString)) { 
			com.baidu.mobstat.StatService.onPageStart(BaseActivity.getRunningActivity(), pageName);
			
//			StatService.trackBeginPage(BaseActivity.getRunningActivity(), pageName);
		}
	}
	
	public static void onEvent(String eventId, String tag) {
		Logger.d(TAG, String.format("onEvent[id=%s, tag=%s]", eventId, tag));
		
		if (BaseActivity.getRunningActivity() != null) {
			com.baidu.mobstat.StatService.onEvent(BaseActivity.getRunningActivity(), eventId, tag);
		}
	}
	
}
