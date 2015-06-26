package org.aisen.weibo.sina.support.db;

import java.util.List;

import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.support.utils.CacheTimeUtils;
import org.aisen.weibo.sina.sinasdk.bean.Friendship;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;

import org.aisen.android.common.setting.Setting;
import org.aisen.android.common.utils.ActivityHelper;
import org.aisen.android.network.cache.ICacheUtility;
import org.aisen.android.network.http.Params;
import org.aisen.orm.extra.Extra;
import org.aisen.orm.utils.FieldUtils;

/**
 * 保存当前登录用户的最多200个好友信息
 * 
 * @author wangdan
 *
 */
public class BilateralDB implements ICacheUtility {

	public static void insertFriends(List<WeiBoUser> friendList) {
		SinaDB.getSqlite().insert(new Extra(AppContext.getUser().getIdstr(), "Bilateral"), friendList);
	}
	
	public static List<WeiBoUser> selectAll() {
		String selection = String.format(" %s = ? and %s = ? ", FieldUtils.OWNER, FieldUtils.KEY);
		String[] selectionArgs = new String[]{ AppContext.getUser().getIdstr(), "Bilateral" };
		
		return SinaDB.getSqlite().select(WeiBoUser.class, selection, selectionArgs, null, null, null, "200");
	}
	
	public static void clear() {
		SinaDB.getSqlite().deleteAll(new Extra(AppContext.getUser().getIdstr(), "Bilateral"), WeiBoUser.class);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> Cache<T> findCacheData(Setting action, Params params, Class<T> responseCls) {
		List<WeiBoUser> userList = selectAll();
		if (userList.size() > 0) {
			Friendship users = new Friendship();
			users.setUsers(userList);
			users.setCache(true);
			users.setExpired(CacheTimeUtils.isExpired("Bilateral", AppContext.getUser()));
			users.setNext_cursor(ActivityHelper.getIntShareData("Bilateral" + AppContext.getUser().getIdstr(), 0));
			return new Cache((T) users, false);
		}
		
		return null;
	}

	@Override
	public void addCacheData(Setting action, Params params, Object responseObj) {
		boolean save = true;
		if (params.containsKey("uid")) {
			save = params.getParameter("uid").equals(AppContext.getUser().getIdstr());
		}
		else if (params.containsKey("screen_name")) {
			save = params.getParameter("screen_name").equals(AppContext.getUser().getScreen_name());
		}
		
		if (save) {
			Friendship users = (Friendship) responseObj;
			if (users.getUsers().size() > 0) {
				
				if (params.containsKey("cursor") && Integer.parseInt(params.getParameter("cursor")) == 0) {
					CacheTimeUtils.saveTime("Bilateral", AppContext.getUser());
					
					clear();
				}
				
				insertFriends(users.getUsers());
				ActivityHelper.putIntShareData("Bilateral" + AppContext.getUser().getIdstr(), users.getNext_cursor());
			}
		}
	}
	
}
