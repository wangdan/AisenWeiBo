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
import org.aisen.weibo.sina.sinasdk.bean.StatusComment;
import org.aisen.weibo.sina.sinasdk.bean.StatusComments;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;
import org.aisen.weibo.sina.support.sqlit.SinaDB;
import org.aisen.weibo.sina.support.utils.CacheTimeUtils;

import java.util.ArrayList;
import java.util.List;

public class CommentCacheUtility implements ICacheUtility {

	private static final String TAG = ABizLogic.class.getSimpleName();

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
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public IResult findCacheData(Setting action, Params params) {
		if (AppSettings.isDisableCache())
			return null;
		
		if (!AppContext.isLoggedIn())
			return null;

		try {
            long time = System.currentTimeMillis();
            Extra extra = getExtra(params, action, AppContext.getAccount().getUser());
            List<StatusComment> cmtList = SinaDB.getTimelineDB().select(extra, StatusComment.class);
            if (cmtList.size() > 0) {
                StatusComments cmts = new StatusComments();
                cmts.setFromCache(true);
                cmts.setOutofdate(CacheTimeUtils.isOutofdate(getExtra(params, action, AppContext.getAccount().getUser()).getKey(), AppContext.getAccount().getUser()));
                cmts.setComments(cmtList);

                Logger.w(TAG, String.format("读取缓存耗时%sms", String.valueOf(System.currentTimeMillis() - time)));
                Logger.d(TAG, String.format("返回评论数据%d条, expired = %s", cmts.getComments().size(), String.valueOf(cmts.outofdate())));

                return cmts;
            }

		} catch (Exception e) {
		}

		return null;
	}

	@Override
	public void addCacheData(Setting action, Params params, IResult result) {
		if (!AppContext.isLoggedIn())
			return;
		
		try {
			StatusComments cmts = (StatusComments) result;
			
			List<StatusComment> newList = new ArrayList<StatusComment>();

            boolean clear = false;
			// 刷新
			if (!TextUtils.isEmpty(params.getParameter("since_id"))) {
                int diff = Math.abs(cmts.getComments().size() - AppSettings.getCommentCount());
                clear = diff <= 3;
			}
			// 加载更多
			else if (!TextUtils.isEmpty(params.getParameter("max_id"))) {
			}
			// 重置
			else {
                clear = true;
			}

            Extra extra = getExtra(params, action, AppContext.getAccount().getUser());
			long time = System.currentTimeMillis();
            if (clear) {
                SinaDB.getTimelineDB().deleteAll(extra, StatusComment.class);
                Logger.d(TAG, "清理数据");
            }
            SinaDB.getTimelineDB().insert(extra, cmts.getComments());
			Logger.w(TAG, String.format("写入评论数据，共%d条，共耗时%sms", newList.size(), String.valueOf(System.currentTimeMillis() - time)));
			
			// 如果是重置数据，刷新缓存时间
			if (!params.containsKey("max_id")) {
				CacheTimeUtils.saveTime(getExtra(params, action, AppContext.getAccount().getUser()).getKey(), AppContext.getAccount().getUser());
			}
		} catch (Exception e) {
		}
	}
	
}
