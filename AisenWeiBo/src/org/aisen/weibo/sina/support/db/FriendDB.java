package org.aisen.weibo.sina.support.db;

import java.util.List;

import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.CacheTimeUtils;
import org.sina.android.bean.Friendship;
import org.sina.android.bean.WeiBoUser;

import android.text.TextUtils;

import com.m.common.params.Params;
import com.m.common.settings.Setting;
import com.m.common.utils.ActivityHelper;
import com.m.support.cache.ICacheUtility;
import com.m.support.sqlite.property.Extra;
import com.m.support.sqlite.util.FieldUtils;

/**
 * 保存当前登录用户的最多200个好友信息
 * 
 * @author wangdan
 *
 */
public class FriendDB implements ICacheUtility {

	public static void insertFriends(List<WeiBoUser> friendList) {
		SinaDB.getSqlite().insertList(new Extra(AppContext.getUser().getIdstr(), "Friends"), friendList);
	}
	
	public static List<WeiBoUser> query(String q) {
		if (TextUtils.isEmpty(q))
			return FriendMentionDB.getRecentMention(null);
		
		q = "%" + q + "%";
		
		String selection = String.format(" %s = ? and %s = ? and ( %s like ? or %s like ? ) ", FieldUtils.OWNER, FieldUtils.KEY, "screen_name", "remark");
		String[] selectionArgs = new String[]{ AppContext.getUser().getIdstr(), "Friends", q, q };
		
		return SinaDB.getSqlite().selectAll(WeiBoUser.class, selection, selectionArgs, null, null);
	}

	public static List<WeiBoUser> selectAll() {
		String selection = String.format(" %s = ? and %s = ? ", FieldUtils.OWNER, FieldUtils.KEY);
		String[] selectionArgs = new String[]{ AppContext.getUser().getIdstr(), "Friends" };
		
		return SinaDB.getSqlite().selectAll(WeiBoUser.class, selection, selectionArgs, null, "200");
	}
	
	public static void clear() {
		SinaDB.getSqlite().deleteAll(new Extra(AppContext.getUser().getIdstr(), "Friends"), WeiBoUser.class);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> Cache<T> findCacheData(Setting action, Params params, Class<T> responseCls) {
		if (params.containsKey("uid")) {
			if (!params.getParameter("uid").equals(AppContext.getUser().getIdstr()))
				return null;
		}
		else if (params.containsKey("screen_name")) {
			if (!params.getParameter("screen_name").equals(AppContext.getUser().getScreen_name()))
				return null;
		}
		
		List<WeiBoUser> userList = selectAll();
		if (userList.size() > 0) {
			Friendship users = new Friendship();
			users.setUsers(userList);
			users.setCache(true);
			users.setExpired(CacheTimeUtils.isExpired("Friends", AppContext.getUser()));
			users.setNext_cursor(ActivityHelper.getInstance().getIntShareData("Friends" + AppContext.getUser().getIdstr(), 0));
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
					CacheTimeUtils.saveTime("Friends", AppContext.getUser());
					
					clear();
				}
				
				insertFriends(users.getUsers());
				
				ActivityHelper.getInstance().putIntShareData("Friends" + AppContext.getUser().getIdstr(), users.getNext_cursor());
			}
		}
	}
	
}
