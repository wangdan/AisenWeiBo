package org.aisen.weibo.sina.support.cache;

import android.text.TextUtils;

import org.aisen.android.common.setting.Setting;
import org.aisen.android.common.utils.KeyGenerator;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.component.orm.extra.Extra;
import org.aisen.android.network.biz.ABizLogic;
import org.aisen.android.network.biz.IResult;
import org.aisen.android.network.cache.ICacheUtility;
import org.aisen.android.network.http.Params;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;
import org.aisen.weibo.sina.support.sqlit.SinaDB;
import org.aisen.weibo.sina.support.utils.CacheTimeUtils;

import java.util.List;

public class TimelineCacheUtility implements ICacheUtility {

	static final String TAG = ABizLogic.TAG;

	public static String getCacheKey(Setting action, Params params) {
		String key = null;
		
		// 提及的微博
		if (params.containsKey("filter_by_author"))
			key = action.getDescription() + ":" + action.getValue() + ":" + params.getParameter("filter_by_author");
		else if (params.containsKey("filter_by_type"))
			key = action.getDescription() + ":" + action.getValue() + ":" + params.getParameter("filter_by_type");
		// 好友分组微博
		else if (params.containsKey("list_id"))
			key = action.getDescription() + ":" + action.getValue() + ":" + params.getParameter("list_id");
		// 转发微博
		else if (params.containsKey("id"))
			key = action.getDescription() + ":" + action.getValue() + ":" + params.getParameter("id");
		// 默认分组微博
		else
			key = action.getDescription() + ":" + action.getValue();
		
//		key += AppContext.getUser().getIdstr();

        if (params.containsKey("feature"))
            key += params.getParameter("feature");

		return KeyGenerator.generateMD5(key);
	}
	

	@Override
	public IResult findCacheData(Setting action, Params params) {
		if (AppSettings.isDisableCache())
			return null;
		
		if (!AppContext.isLoggedIn())
			return null;


		// 如果是用户微博，只返回用户的微博
		if (action.getValue().equals("statuses/user_timeline.json")) {
//			if (params.containsKey("feature")) {
//				return null;
//			}
			// 是当前登录用户
			if (params.containsKey("user_id") && params.getParameter("user_id").equals(AppContext.getAccount().getUser().getIdstr())) {
			}
			else if (params.containsKey("screen_name") && params.getParameter("screen_name").equals(AppContext.getAccount().getUser().getScreen_name())) {
			}
			else {
				return null;
			}
		}
		
		try {
            long time = System.currentTimeMillis();
            Extra extra = new Extra(AppContext.getAccount().getUser().getId(), getCacheKey(action, params));
            List<StatusContent> statusList = SinaDB.getTimelineDB().select(extra, StatusContent.class);

            if (statusList.size() > 0) {
                StatusContents statusContents = new StatusContents();
                statusContents.setFromCache(true);
                statusContents.setOutofdate(CacheTimeUtils.isOutofdate(getCacheKey(action, params), AppContext.getAccount().getUser()));
                statusContents.setStatuses(statusList);

                Logger.w(TAG, String.format("读取缓存耗时%sms", String.valueOf(System.currentTimeMillis() - time)));
                Logger.d(TAG, String.format("返回微博数据%d条, expired = %s", statusContents.getStatuses().size(), String.valueOf(statusContents.isOutofdate())));

                return statusContents;
            }
		} catch (Exception e) {
		}
		
		return null;
	}

	@Override
	public void addCacheData(Setting action, Params params, IResult responseObj) {
        // 如果是离线请求，忽略数据缓存
        if (action.getExtras() != null && action.getExtras().containsKey("offline_action"))
            return;

		// 如果是用户微博
		if (action.getValue().equals("statuses/user_timeline.json")) {
//			if (params.containsKey("feature")) {
//				return;
//			}
			// 是当前登录用户
			if (params.containsKey("user_id") && params.getParameter("user_id").equals(AppContext.getAccount().getUser().getIdstr())) {
			}
			else if (params.containsKey("screen_name") && params.getParameter("screen_name").equals(AppContext.getAccount().getUser().getScreen_name())) {
			}
			else {
				return;
			}
		}
				
		try {
			StatusContents statusContents = (StatusContents) responseObj;
			if (statusContents.getStatuses().size() == 0)
				return;

            boolean clear = false;
            // 刷新
            if (!TextUtils.isEmpty(params.getParameter("since_id"))) {
                int diff = Math.abs(statusContents.getStatuses().size() - AppSettings.getTimelineCount());
                clear = diff <= 3;
            }
            // 加载更多
            else if (!TextUtils.isEmpty(params.getParameter("max_id"))) {
            }
            // 重置
            else {
                clear = true;
            }

            Extra extra = new Extra(AppContext.getAccount().getUser().getId(), getCacheKey(action, params));
            if (clear) {
                SinaDB.getTimelineDB().deleteAll(extra, StatusContent.class);
                Logger.d(TAG, "清理数据");
            }
            long time = System.currentTimeMillis();
            SinaDB.getTimelineDB().insert(extra, statusContents.getStatuses());
			Logger.w(TAG, String.format("写入微博数据，共%d条，共耗时%sms", statusContents.getStatuses().size(), String.valueOf(System.currentTimeMillis() - time)));

            CacheTimeUtils.saveTime(getCacheKey(action, params), AppContext.getAccount().getUser());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
