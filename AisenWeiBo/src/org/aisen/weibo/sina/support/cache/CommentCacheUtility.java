package org.aisen.weibo.sina.support.cache;

import java.util.List;

import org.aisen.weibo.sina.support.db.SinaDB;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.AppSettings;
import org.aisen.weibo.sina.support.utils.CacheTimeUtils;
import org.sina.android.bean.StatusComment;
import org.sina.android.bean.StatusComments;
import org.sina.android.bean.WeiBoUser;

import com.m.common.params.Params;
import com.m.common.settings.Setting;
import com.m.common.utils.Logger;
import com.m.support.cache.ICacheUtility;
import com.m.support.sqlite.property.Extra;
import com.m.support.sqlite.util.FieldUtils;
import com.m.support.task.TaskException;
import com.m.support.task.WorkTask;

public class CommentCacheUtility implements ICacheUtility {

	private static final String TAG = CommentCacheUtility.class.getSimpleName();

	public static Extra getExtra(Params params, Setting action, WeiBoUser user) {
		String key = null;
		// 提及的评论
		if (action.getValue().equals("comments/mentions.json"))
			key = action.getDescription() + ":" + action.getValue() + ":" + params.getParameter("filter_by_author");
		// 我发出的、我收到的
		else
			key = action.getDescription() + ":" + action.getValue() + ":all";

		Extra extra = new Extra(user.getIdstr(), key);
		
		return extra;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> Cache<T> findCacheData(Setting action, Params params, Class<T> responseCls) {
		if (AppSettings.isDisableCache())
			return null;
		
		if (!AppContext.isLogedin())
			return null;

		Extra extra = getExtra(params, action, AppContext.getUser());
		String key = extra.getKey();

		String selection = String.format(" %s = ? and %s = ? ", FieldUtils.KEY, FieldUtils.OWNER);

		String[] selectionArgs = new String[] { key, AppContext.getUser().getIdstr() };

		List<StatusComment> comments = SinaDB.getSqlite().selectAll(StatusComment.class, selection, selectionArgs, " id desc ", null);
		if (comments != null && comments.size() > 0) {

			Logger.d(
					TAG,
					String.format("load data success from CommentCacheUtility, key = %s", key));

			StatusComments c = new StatusComments(comments);
			c.setCache(true);
			c.setExpired(CacheTimeUtils.isExpired(key, AppContext.getUser()));
			return new Cache((T) c, false);
		}

		return null;
	}

	@Override
	public void addCacheData(Setting action, Params params, Object responseObj) {
//		if (!AppContext.isLogedin())
//			return;
//		
//		addCacheData(action, params, responseObj, AppContext.getUser());
	}
	
	private void addCacheData(Setting action, Params params, Object responseObj, WeiBoUser user) {
		if (!AppContext.isLogedin())
			return;
		
		List<StatusComment> comments = ((StatusComments) responseObj).getComments();

		Extra extra = getExtra(params, action, AppContext.getUser());
		String key = extra.getKey();

		if (!params.containsKey("since_id") && !params.containsKey("max_id")) {
			CacheTimeUtils.saveTime(key, AppContext.getUser());
		}
		
		Logger.d(TAG, String.format("save data to CommentCacheUtility, key = %s", key));

		SinaDB.getSqlite().insertList(extra, comments);
	}
	
	public static void reset(final Params params, final Setting action, final List<StatusComment> datas, final WeiBoUser user) {
		new WorkTask<Void, Void, Void>() {

			@Override
			public Void workInBackground(Void... p) throws TaskException {
				Extra extra = getExtra(params, action, user);
				
				SinaDB.getSqlite().deleteAll(extra, StatusComment.class);

				CommentMemoryCacheUtility.reset(params, action, datas, user);
				
				StatusComments comments = new StatusComments(datas);
				new CommentCacheUtility().addCacheData(action, params, comments, user);
				return null;
			}
			
		}.execute();
	}

}
