package org.aisen.weibo.sina.support.cache;

import java.util.HashMap;

import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.AppSettings;
import org.aisen.weibo.sina.support.utils.CacheTimeUtils;
import org.sina.android.bean.StatusContents;

import com.m.common.params.Params;
import com.m.common.settings.Setting;
import com.m.common.utils.Logger;
import com.m.support.cache.ICacheUtility;

public class TimelineMemoryCacheUtility implements ICacheUtility {

	private static HashMap<String, StatusContents> statusMap = new HashMap<String, StatusContents>();
	
	public static boolean isEmpty() {
		return statusMap.size() == 0;
	}
	
	public static void clear() {
		Logger.w(TimelineCacheUtility.TAG, "清理内存数据");
		statusMap.clear();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> Cache<T> findCacheData(Setting action, Params params, Class<T> responseCls) {
		if (AppSettings.isDisableCache())
			return null;
		
		if (!AppContext.isLogedin())
			return null;
		
		// 如果是用户微博，只返回用户的微博
		if (action.getValue().equals("statuses/user_timeline.json")) {
			// 是当前登录用户
			if (params.containsKey("user_id") && params.getParameter("user_id").equals(AppContext.getUser().getIdstr())) {
			}
			else if (params.containsKey("screen_name") && params.getParameter("screen_name").equals(AppContext.getUser().getScreen_name())) {
			}
			else {
				return null;
			}
		}
		
		String key = TimelineCacheUtility.getCacheKey(action, params);
		
		StatusContents statusContents = statusMap.get(key);
		if (statusContents != null) {
			statusContents.setCache(true);
			statusContents.setExpired(CacheTimeUtils.isExpired(TimelineCacheUtility.getCacheKey(action, params), AppContext.getUser()));
			
			Logger.w(TimelineCacheUtility.TAG, String.format("返回内存微博数据，共%d条", statusContents.getStatuses().size()));
			
			return new Cache((T) statusContents, false);
		}
		
		return null;
	}

	@Override
	public void addCacheData(Setting action, Params params, Object responseObj) {
		StatusContents statusContents = (StatusContents) responseObj;

		if (statusContents.getStatuses().size() == 0)
			return;
		
		// 如果是用户微博
		if (action.getValue().equals("statuses/user_timeline.json")) {
			// 是当前登录用户
			if (params.containsKey("user_id") && params.getParameter("user_id").equals(AppContext.getUser().getIdstr())) {
			}
			else if (params.containsKey("screen_name") && params.getParameter("screen_name").equals(AppContext.getUser().getScreen_name())) {
			}
			else {
				return;
			}
		}
		
		Logger.w(TimelineCacheUtility.TAG, String.format("写入内存微博数据，共%d条", statusContents.getStatuses().size()));
		
		String key = TimelineCacheUtility.getCacheKey(action, params);
		
		statusMap.put(key, statusContents);
	}

}
