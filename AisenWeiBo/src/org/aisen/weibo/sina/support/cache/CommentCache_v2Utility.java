package org.aisen.weibo.sina.support.cache;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.aisen.weibo.sina.support.bean.DestoryedCommentsBean;
import org.aisen.weibo.sina.support.db.SinaDB;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.AppSettings;
import org.aisen.weibo.sina.support.utils.CacheTimeUtils;
import org.sina.android.bean.StatusComment;
import org.sina.android.bean.StatusComments;
import org.sina.android.bean.WeiBoUser;

import android.text.TextUtils;

import com.m.common.context.GlobalContext;
import com.m.common.params.Params;
import com.m.common.settings.Setting;
import com.m.common.utils.FileUtility;
import com.m.common.utils.KeyGenerator;
import com.m.common.utils.Logger;
import com.m.support.bizlogic.ABaseBizlogic;
import com.m.support.cache.ICacheUtility;
import com.m.support.sqlite.property.Extra;
import com.m.support.sqlite.util.FieldUtils;

public class CommentCache_v2Utility implements ICacheUtility {

	private static final String TAG = ABaseBizlogic.class.getSimpleName();

	public static Extra getExtra(Params params, Setting action, WeiBoUser user) {
		String key = null;
		// 提及的评论
		if (action.getValue().equals("comments/mentions.json"))
			key = action.getDescription() + ":" + action.getValue() + ":" + params.getParameter("filter_by_author");
		// 我发出的、我收到的
		else
			key = action.getDescription() + ":" + action.getValue() + ":all";

		Extra extra = new Extra(user.getIdstr(), KeyGenerator.generateMD5(key));
		
		return extra;
	}
	
	private File getCacheFile(Setting action, Params params, WeiBoUser user) {
		File extenrnalDir = new File(GlobalContext.getInstance().getDataPath());;
		Logger.v(TAG, String.format("缓存目录 = %s", extenrnalDir.getAbsolutePath()));
		
		File favoritesFile = new File(String.format("%s%s%s-%s-comment.o", 
										extenrnalDir.getAbsolutePath(), 
										File.separator,
										AppContext.getUser().getIdstr(),
										getExtra(params, action, user).getKey()));
		
		return favoritesFile;
	}
	
	private StatusComments getCache(File cacheFile) {
		try {
			long time = System.currentTimeMillis();
			StatusComments cmts = (StatusComments) FileUtility.readObject(cacheFile, StatusComments.class);
			Logger.w(TAG, String.format("读取评论数据，共耗时%sms", String.valueOf(System.currentTimeMillis() - time)));
			
			return cmts;
		} catch (Exception e) {
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

		try {
			File cacheFile = getCacheFile(action, params, AppContext.getUser());
			
			StatusComments cmts = getCache(cacheFile);
			cmts.setCache(true);
			cmts.setExpired(CacheTimeUtils.isExpired(getExtra(params, action, AppContext.getUser()).getKey(), AppContext.getUser()));
			
			// 同时清除超过设定的缓存时间的标记数据
			String whereClause = String.format(" %s < ?", FieldUtils.CREATEAT);
			String[] whereArgs = new String[]{ String.valueOf((System.currentTimeMillis() - AppSettings.getRefreshInterval()) / 1000) };
			SinaDB.getSqlite().delete(DestoryedCommentsBean.class, whereClause, whereArgs);
			// 将缓存中的数据，对比已经被删除的数据，剔除掉
			long time = System.currentTimeMillis();
			List<DestoryedCommentsBean> destoryedBeans = SinaDB.getSqlite().selectAll(DestoryedCommentsBean.class);
			if (destoryedBeans != null && destoryedBeans.size() > 0) {
				List<StatusComment> newList = new ArrayList<StatusComment>();
				for (StatusComment cmt : cmts.getComments()) {
					boolean destoryed = false;
					for (DestoryedCommentsBean destoryedCmt : destoryedBeans) {
						if (destoryedCmt.getCmtId().equals(cmt.getId()))
							destoryed = true;
					}
					if (!destoryed)
						newList.add(cmt);
				}
				
				cmts.setComments(newList);
			}
			Logger.w(TAG, String.format("排除已删除数据耗时%sms", String.valueOf(System.currentTimeMillis() - time)));
			Logger.d(TAG, String.format("返回评论数据%d条, expired = %s", cmts.getComments().size(), String.valueOf(cmts.expired())));
			
			return new Cache((T) cmts, false);
		} catch (Exception e) {
		}

		return null;
	}

	@Override
	public void addCacheData(Setting action, Params params, Object responseObj) {
		if (!AppContext.isLogedin())
			return;
		
		try {
			File cacheFile = getCacheFile(action, params, AppContext.getUser());

			StatusComments cmts = (StatusComments) responseObj;
			
			List<StatusComment> newList = new ArrayList<StatusComment>();
			
			// 刷新
			if (!TextUtils.isEmpty(params.getParameter("since_id"))) {
				StatusComments dbStatusContents = getCache(cacheFile);
				newList.addAll(cmts.getComments());
				if (dbStatusContents != null)
					newList.addAll(dbStatusContents.getComments());
			}
			// 加载更多
			else if (!TextUtils.isEmpty(params.getParameter("max_id"))) {
				StatusComments dbStatusContents = getCache(cacheFile);
				if (dbStatusContents != null)
					newList.addAll(dbStatusContents.getComments());
				newList.addAll(cmts.getComments());
			}
			// 重置
			else {
				newList.addAll(cmts.getComments());
			}
			
			cmts.setComments(newList);
			
			long time = System.currentTimeMillis();
			FileUtility.writeObject(cacheFile, cmts);
			Logger.w(TAG, String.format("写入评论数据，共%d条，共耗时%sms", newList.size(), String.valueOf(System.currentTimeMillis() - time)));
			
			// 如果是重置数据，刷新缓存时间
			if (!params.containsKey("max_id")) {
				CacheTimeUtils.saveTime(getExtra(params, action, AppContext.getUser()).getKey(), AppContext.getUser());
			}
		} catch (Exception e) {
		}
	}
	
}
