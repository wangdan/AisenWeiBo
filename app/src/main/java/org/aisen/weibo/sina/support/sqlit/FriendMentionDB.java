package org.aisen.weibo.sina.support.sqlit;

import org.aisen.android.component.orm.extra.Extra;
import org.aisen.android.component.orm.utils.FieldUtils;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;

import java.util.List;

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
		SinaDB.getDB().insertOrReplace(new Extra(AppContext.getAccount().getUser().getIdstr(), "Mention"), friend);
	}
	
	public static List<WeiBoUser> query() {
		String selection = String.format(" %s = ? and %s = ? ", FieldUtils.OWNER, FieldUtils.KEY);
		String[] selectionArgs = new String[]{ AppContext.getAccount().getUser().getIdstr(), "Mention" };
		
		return SinaDB.getDB().select(WeiBoUser.class, selection, selectionArgs);
	}
	
	/**
	 * 获得最近提及的好友记录
	 * 
	 * @return
	 */
	public static List<WeiBoUser> getRecentMention(String size) {
		String selection = String.format(" %s = ? and %s = ? ", FieldUtils.OWNER, FieldUtils.KEY);
		String[] selectionArgs = new String[]{ AppContext.getAccount().getUser().getIdstr(), "Mention" };
		
		return SinaDB.getDB().select(WeiBoUser.class, selection, selectionArgs, null, null, String.format(" %s desc ", FieldUtils.CREATEAT), size);
	}
	
	/**
	 * 清理提及的好友记录
	 */
	public static void clear() {
		SinaDB.getDB().deleteAll(new Extra(AppContext.getAccount().getUser().getIdstr(), "Mention"), WeiBoUser.class);
	}
	
}

