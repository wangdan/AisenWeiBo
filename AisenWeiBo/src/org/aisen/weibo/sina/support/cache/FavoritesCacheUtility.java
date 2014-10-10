package org.aisen.weibo.sina.support.cache;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.AppSettings;
import org.aisen.weibo.sina.support.utils.CacheTimeUtils;
import org.sina.android.bean.Favorities;
import org.sina.android.bean.Favority;

import com.m.common.context.GlobalContext;
import com.m.common.params.Params;
import com.m.common.settings.Setting;
import com.m.common.utils.FileUtility;
import com.m.common.utils.Logger;
import com.m.support.bizlogic.ABaseBizlogic;
import com.m.support.cache.ICacheUtility;
import com.m.support.task.TaskException;
import com.m.support.task.WorkTask;


public class FavoritesCacheUtility implements ICacheUtility {

	static final String TAG = ABaseBizlogic.TAG;
	
	private Favorities getCache() throws Exception {
		File favoritesFile = getCacheFile();
		
		return FileUtility.readObject(favoritesFile, Favorities.class);
	}
	
	private File getCacheFile() {
		File extenrnalDir = new File(GlobalContext.getInstance().getDataPath());
		Logger.v(TAG, String.format("缓存目录 = %s", extenrnalDir.getAbsolutePath()));
		
		File favoritesFile = new File(String.format("%s%s%s-favorites.o", 
										extenrnalDir.getAbsolutePath(), 
										File.separator,
										AppContext.getUser().getIdstr()));
		
		return favoritesFile;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> Cache<T> findCacheData(Setting action, Params params, Class<T> responseCls) {
		if (AppSettings.isDisableCache())
			return null;
		
		if (!AppContext.isLogedin())
			return null;
		try {
			long time = System.currentTimeMillis();
			Favorities favorities = getCache();
			if (favorities != null) {
				Logger.w(TAG, String.format("读取收藏数据，共耗时%sms", String.valueOf(System.currentTimeMillis() - time)));
				
				favorities.setExpired(CacheTimeUtils.isExpired("Favorites", AppContext.getUser()));
				return new Cache((T) favorities, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public void addCacheData(Setting action, Params params, Object responseObj) {
		if (!AppContext.isLogedin())
			return;

		try {
			int page = Integer.parseInt(params.getParameter("page"));
			Favorities favorities = (Favorities) responseObj;
			// 如果是第一页，就重置数据，否则，将数据添加到集合末尾
			if (page > 1) {
				Cache<Favorities> c = findCacheData(action, params, Favorities.class); 
				Favorities cache = c.getT();
				if (cache != null) {
					Favorities cacheFavorities = cache;

					List<Favority> statusList = cacheFavorities.getFavorites();
					statusList.addAll(favorities.getFavorites());
					favorities.setFavorites(statusList);
				}
			}
			// 加载第一页的时候，保存缓存的时间
			else {
				CacheTimeUtils.saveTime("Favorites", AppContext.getUser());
			}
			
			favorities.setExpired(false);
			favorities.setCache(true);
			
			Logger.d(TAG, String.format("加载了%d条数据，总共%d条", favorities.getFavorites().size(), favorities.getTotal_number()));
			long time = System.currentTimeMillis();
			FileUtility.writeObject(getCacheFile(), (Serializable) responseObj);
			Logger.w(TAG, String.format("保存收藏数据，共耗时%sms", String.valueOf(System.currentTimeMillis() - time)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 删除一条收藏
	 * 
	 * @param statudId
	 */
	public static void destory(String statudId) {
		new WorkTask<String, Void, Void>() {

			@Override
			public Void workInBackground(String... params) throws TaskException {
				try {
					FavoritesCacheUtility cacheUtility = new FavoritesCacheUtility();
					Favorities favorities = cacheUtility.getCache();
					if (favorities != null) {
						for (Favority favority : favorities.getFavorites()) {
							if (favority.getStatus().getId().equals(params[0])) {
								favorities.getFavorites().remove(favority);
								int total = favorities.getTotal_number() - 1;
								favorities.setTotal_number(total <= 0 ? 0 : total);
								
								Logger.d(TAG, "删除一条收藏");
								break;
							}
						}
					}
					
					long time = System.currentTimeMillis();
					FileUtility.writeObject(cacheUtility.getCacheFile(), favorities);
					Logger.w(TAG, String.format("保存收藏数据，共耗时%sms", String.valueOf(System.currentTimeMillis() - time)));
				} catch (Exception e) {
				}
				return null;
			}
			
		}.executeOnSerialExecutor(statudId);
	}

}
