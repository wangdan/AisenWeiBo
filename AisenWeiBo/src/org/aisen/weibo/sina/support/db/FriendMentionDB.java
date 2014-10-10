package org.aisen.weibo.sina.support.db;

import java.util.List;

import org.aisen.weibo.sina.support.utils.AppContext;
import org.sina.android.bean.WeiBoUser;

import com.m.support.sqlite.property.Extra;
import com.m.support.sqlite.util.FieldUtils;

/**
 * 保存最近提及的好友记录
 * 
 * @author wangdan
 *
 */
public class FriendMentionDB {
	
	/**
	 * 保存一个好友，如果已存在，则更新
	 * 
	 * @param friend
	 */
	public static void addFriend(WeiBoUser friend) {
		SinaDB.getSqlite().insert(new Extra(AppContext.getUser().getIdstr(), "Mention"), friend);
	}
	
	public static List<WeiBoUser> query() {
		String selection = String.format(" %s = ? and %s = ? ", FieldUtils.OWNER, FieldUtils.KEY);
		String[] selectionArgs = new String[]{ AppContext.getUser().getIdstr(), "Mention" };
		
		return SinaDB.getSqlite().selectAll(WeiBoUser.class, selection, selectionArgs);
	}
	
	/**
	 * 获得最近提及的好友记录
	 * 
	 * @return
	 */
	public static List<WeiBoUser> getRecentMention(String size) {
		String selection = String.format(" %s = ? and %s = ? ", FieldUtils.OWNER, FieldUtils.KEY);
		String[] selectionArgs = new String[]{ AppContext.getUser().getIdstr(), "Mention" };
		
		return SinaDB.getSqlite().selectAll(WeiBoUser.class, selection, selectionArgs, String.format(" %s desc ", FieldUtils.CREATEAT), size);
	}
	
	/**
	 * 清理提及的好友记录
	 */
	public static void clear() {
		SinaDB.getSqlite().deleteAll(new Extra(AppContext.getUser().getIdstr(), "Mention"), WeiBoUser.class);
	}
	
}

