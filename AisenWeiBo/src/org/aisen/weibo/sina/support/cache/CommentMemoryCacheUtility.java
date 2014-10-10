package org.aisen.weibo.sina.support.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aisen.weibo.sina.support.utils.AisenUtil;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.AppSettings;
import org.sina.android.bean.StatusComment;
import org.sina.android.bean.StatusComments;
import org.sina.android.bean.WeiBoUser;

import com.m.common.params.Params;
import com.m.common.settings.Setting;
import com.m.common.utils.Logger;
import com.m.support.cache.ICacheUtility;
import com.m.support.sqlite.property.Extra;
import com.m.support.task.TaskException;
import com.m.support.task.WorkTask;

public class CommentMemoryCacheUtility implements ICacheUtility {

	private static final String TAG = CommentMemoryCacheUtility.class.getSimpleName();

	public static Map<String, StatusComments> commentsMap = new HashMap<String, StatusComments>();
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> Cache<T> findCacheData(Setting action, Params params, Class<T> responseCls) {
		if (AppSettings.isDisableCache())
			return null;
		
		if (!AppContext.isLogedin())
			return null;

		Extra extra = CommentCacheUtility.getExtra(params, action, AppContext.getUser());

		String key = extra.getKey();
		key = AisenUtil.getUserKey(key, AppContext.getUser());
		extra.setKey(key);
		
		StatusComments comments = commentsMap.get(key);
		if (comments != null && comments.getComments().size() > 0) {

			Logger.d(
					TAG,
					String.format("load data success from CommentMemoryCacheUtility, key = %s", key));

			comments.setCache(true);
			return new Cache((T) comments, false);
		}

		return null;
	}

	@Override
	public void addCacheData(Setting action, Params params, Object responseObj) {
//		if (!AppContext.isLogedin())
//			return;
//		
//		StatusComments comments = (StatusComments) responseObj;
//
//		Extra extra = CommentCacheUtility.getExtra(params, action, AppContext.getUser());
//
//		String key = extra.getKey();
//		key = AisenUtil.getUserKey(key, AppContext.getUser());
//		extra.setKey(key);
//		
//		Logger.d(TAG, String.format("save data to CommentMemoryCacheUtility, key = %s", key));
//		
//		commentsMap.put(key, comments);
	}
	
	public static void remove(final StatusComment source) {
		new WorkTask<Void, Void, Void>() {

			@Override
			public Void workInBackground(Void... params) throws TaskException {
				Set<String> keySet = commentsMap.keySet();
				for (String key : keySet) {
					StatusComments comments = commentsMap.get(key);
					for (StatusComment comment : comments.getComments()) {
						if (source.getId().equals(comment.getId())) {
							comments.getComments().remove(comment);
							break;
						}
					}
				}
				return null;
			}
			
		}.execute();
	}
	
	public static void reset(final Params params, final Setting action, final List<StatusComment> datas, WeiBoUser user) {
		Extra extra = CommentCacheUtility.getExtra(params, action, user);

		String key = extra.getKey();
		key = AisenUtil.getUserKey(key, user);
		extra.setKey(key);
		
		commentsMap.put(key, new StatusComments(datas));
	}
	
}
