package org.aisen.weibo.sina.support.cache;

import android.os.Environment;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.setting.Setting;
import org.aisen.android.common.utils.FileUtils;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.network.biz.ABizLogic;
import org.aisen.android.network.biz.IResult;
import org.aisen.android.network.cache.ICacheUtility;
import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.sinasdk.bean.Favorities;
import org.aisen.weibo.sina.sinasdk.bean.Favority;
import org.aisen.weibo.sina.support.utils.CacheTimeUtils;

import java.io.File;
import java.io.Serializable;
import java.util.List;


public class FavoritesCacheUtility implements ICacheUtility {

	static final String TAG = ABizLogic.TAG;
	
	private Favorities getCache() throws Exception {
		File favoritesFile = getCacheFile();
		
		return FileUtils.readObject(favoritesFile, Favorities.class);
	}
	
	private File getCacheFile() {
		File extenrnalDir = new File(GlobalContext.getInstance().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath());
		Logger.v(TAG, String.format("缓存目录 = %s", extenrnalDir.getAbsolutePath()));
		
		File favoritesFile = new File(String.format("%s%s%s-favorites.o", 
										extenrnalDir.getAbsolutePath(), 
										File.separator,
										AppContext.getAccount().getUser().getIdstr()));
		
		return favoritesFile;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public IResult findCacheData(Setting action, Params params) {
		if (AppSettings.isDisableCache())
			return null;
		
		if (!AppContext.isLoggedIn())
			return null;
		try {
			long time = System.currentTimeMillis();
			Favorities favorities = getCache();
			if (favorities != null) {
				Logger.w(TAG, String.format("读取收藏数据，共耗时%sms", String.valueOf(System.currentTimeMillis() - time)));
				
				favorities.setEndPaging(CacheTimeUtils.isOutofdate("Favorites", AppContext.getAccount().getUser()));
				return favorities;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public void addCacheData(Setting action, Params params, IResult responseObj) {
		if (!AppContext.isLoggedIn())
			return;

		try {
			int page = Integer.parseInt(params.getParameter("page"));
			Favorities favorities = (Favorities) responseObj;
			// 如果是第一页，就重置数据，否则，将数据添加到集合末尾
			if (page > 1) {
				Favorities cache = (Favorities) findCacheData(action, params);
				if (cache != null) {
					Favorities cacheFavorities = cache;

					List<Favority> statusList = cacheFavorities.getFavorites();
					statusList.addAll(favorities.getFavorites());
					favorities.setFavorites(statusList);
				}
			}
			// 加载第一页的时候，保存缓存的时间
			else {
				CacheTimeUtils.saveTime("Favorites", AppContext.getAccount().getUser());
			}
			
			favorities.setEndPaging(false);
			favorities.setFromCache(true);
			
			Logger.d(TAG, String.format("加载了%d条数据，总共%d条", favorities.getFavorites().size(), favorities.getTotal_number()));
			long time = System.currentTimeMillis();
			FileUtils.writeObject(getCacheFile(), (Serializable) responseObj);
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
							if (String.valueOf(favority.getStatus().getId()).equals(params[0])) {
								favorities.getFavorites().remove(favority);
								int total = favorities.getTotal_number() - 1;
								favorities.setTotal_number(total <= 0 ? 0 : total);
								
								Logger.d(TAG, "删除一条收藏");
								break;
							}
						}
					}
					
					long time = System.currentTimeMillis();
					FileUtils.writeObject(cacheUtility.getCacheFile(), favorities);
					Logger.w(TAG, String.format("保存收藏数据，共耗时%sms", String.valueOf(System.currentTimeMillis() - time)));
				} catch (Exception e) {
				}
				return null;
			}
			
		}.executeOnSerialExecutor(statudId);
	}

}
