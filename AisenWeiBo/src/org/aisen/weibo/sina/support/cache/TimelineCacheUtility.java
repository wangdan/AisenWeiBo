package org.aisen.weibo.sina.support.cache;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.aisen.weibo.sina.support.bean.DestoryedStatusesBean;
import org.aisen.weibo.sina.support.db.SinaDB;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.AppSettings;
import org.aisen.weibo.sina.support.utils.CacheTimeUtils;
import org.sina.android.bean.StatusContent;
import org.sina.android.bean.StatusContents;

import android.text.TextUtils;

import com.m.common.context.GlobalContext;
import com.m.common.params.Params;
import com.m.common.settings.Setting;
import com.m.common.utils.FileUtility;
import com.m.common.utils.KeyGenerator;
import com.m.common.utils.Logger;
import com.m.support.bizlogic.ABaseBizlogic;
import com.m.support.cache.ICacheUtility;
import com.m.support.sqlite.util.FieldUtils;

public class TimelineCacheUtility implements ICacheUtility {

	static final String TAG = ABaseBizlogic.TAG;
	
	private String getCacheKey(Setting action, Params params) {
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
		
		key += AppContext.getUser().getIdstr();
		
		return KeyGenerator.generateMD5(key);
	}
	
	private File getCacheFile(Setting action, Params params) {
		File extenrnalDir = new File(GlobalContext.getInstance().getDataPath());
		Logger.v(TAG, String.format("缓存目录 = %s", extenrnalDir.getAbsolutePath()));
		
		File favoritesFile = new File(String.format("%s%s%s-%s.o", 
										extenrnalDir.getAbsolutePath(), 
										File.separator,
										AppContext.getUser().getIdstr(),
										getCacheKey(action, params)));
		
		return favoritesFile;
	}
	
	private StatusContents getCache(File cacheFile) {
		try {
			long time = System.currentTimeMillis();
			StatusContents dbStatusContents = (StatusContents) FileUtility.readObject(cacheFile, StatusContents.class);
			Logger.w(TAG, String.format("读取微博数据，共耗时%sms", String.valueOf(System.currentTimeMillis() - time)));
			
			return dbStatusContents;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
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
		
		try {
			File cacheFile = getCacheFile(action, params);
			
			StatusContents statusContents = getCache(cacheFile);
			statusContents.setCache(true);
			statusContents.setExpired(CacheTimeUtils.isExpired(getCacheKey(action, params), AppContext.getUser()));
			
			// 同时清除超过设定的缓存时间的标记数据
			String whereClause = String.format(" %s < ?", FieldUtils.CREATEAT);
			String[] whereArgs = new String[]{ String.valueOf((System.currentTimeMillis() - AppSettings.getRefreshInterval()) / 1000) };
			SinaDB.getSqlite().delete(DestoryedStatusesBean.class, whereClause, whereArgs);
			// 将缓存中的数据，对比已经被删除的数据，剔除掉
			long time = System.currentTimeMillis();
			List<DestoryedStatusesBean> destoryedBeans = SinaDB.getSqlite().selectAll(DestoryedStatusesBean.class);
			if (destoryedBeans != null && destoryedBeans.size() > 0) {
				List<StatusContent> newList = new ArrayList<StatusContent>();
				for (StatusContent status : statusContents.getStatuses()) {
					boolean destoryed = false;
					for (DestoryedStatusesBean destoryedStatus : destoryedBeans) {
						if (destoryedStatus.getStatusId().equals(status.getId()))
							destoryed = true;
					}
					if (!destoryed)
						newList.add(status);
				}
				
				statusContents.setStatuses(newList);
			}
			Logger.w(TAG, String.format("排除已删除数据耗时%sms", String.valueOf(System.currentTimeMillis() - time)));
			Logger.d(TAG, String.format("返回微博数据%d条, expired = %s", statusContents.getStatuses().size(), String.valueOf(statusContents.expired())));
			
			return new Cache((T) statusContents, false);
		} catch (Exception e) {
		}
		
		return null;
	}

	@Override
	public void addCacheData(Setting action, Params params, Object responseObj) {
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
				
		try {
			File cacheFile = getCacheFile(action, params);

			StatusContents statusContents = (StatusContents) responseObj;
			
			List<StatusContent> newList = new ArrayList<StatusContent>();
			
			// 刷新
			if (!TextUtils.isEmpty(params.getParameter("since_id"))) {
				StatusContents dbStatusContents = getCache(cacheFile);
				newList.addAll(statusContents.getStatuses());
				if (dbStatusContents != null)
					newList.addAll(dbStatusContents.getStatuses());
			}
			// 加载更多
			else if (!TextUtils.isEmpty(params.getParameter("max_id"))) {
				StatusContents dbStatusContents = getCache(cacheFile);
				if (dbStatusContents != null)
					newList.addAll(dbStatusContents.getStatuses());
				newList.addAll(statusContents.getStatuses());
			}
			// 重置
			else {
				newList.addAll(statusContents.getStatuses());
			}
			
			statusContents.setStatuses(newList);
			
			long time = System.currentTimeMillis();
			FileUtility.writeObject(cacheFile, statusContents);
			Logger.w(TAG, String.format("写入微博数据，共%d条，共耗时%sms", newList.size(), String.valueOf(System.currentTimeMillis() - time)));
			
			// 如果是重置数据，刷新缓存时间
			if (!params.containsKey("max_id")) {
				CacheTimeUtils.saveTime(getCacheKey(action, params), AppContext.getUser());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
