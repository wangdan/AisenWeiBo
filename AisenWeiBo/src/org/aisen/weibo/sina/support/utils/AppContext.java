package org.aisen.weibo.sina.support.utils;

import org.aisen.weibo.sina.base.MyApplication;
import org.aisen.weibo.sina.sys.service.UnreadService;
import org.sina.android.bean.Groups;
import org.sina.android.bean.Token;
import org.sina.android.bean.UnreadCount;
import org.sina.android.bean.WeiBoUser;

/**
 * 应用上下文
 * 
 * @author wangdan
 *
 */
public class AppContext {

	private static WeiBoUser mUser;
	
	private static Groups mGroups;
	
	private static Token mToken;

	private static UnreadCount unreadCount;
	
	public static boolean isLogedin() {
		return mUser != null && mToken != null;
	}
	
	public static void refresh(WeiBoUser user, Groups groups) {
		AppContext.mUser = user;
		AppContext.mGroups = groups;
	}
	
	public static void login(WeiBoUser user, Groups groups, Token token) {
		boolean startUnreadService = mUser == null || !mUser.getIdstr().equals(user.getIdstr());
		
		mUser = user;
		mGroups = groups;
		mToken = token;
		
		// 未读消息重置
		if (AppContext.getUnreadCount() == null || startUnreadService) {
			AppContext.unreadCount = UnreadService.getUnreadCount();
		}
		if (AppContext.unreadCount == null)
			AppContext.unreadCount = new UnreadCount();
		
		// 开启未读服务
		if (startUnreadService)
			UnreadService.startService();
		
		// 检查更新变化
		CheckChangedUtils.check(AppContext.getUser(), AppContext.getToken());
		
		// 刷新定时任务
		MyApplication.refreshPublishAlarm();
	}
	
	public static void logout() {
		// 停止未读服务
		UnreadService.stopService();
		// 移除定时任务
		MyApplication.removeAllPublishAlarm();
		// 退出账号
		mUser = null;
		mToken = null;
		mGroups = null;
	}
	
	public static Token getToken() {
		return mToken;
	}
	
	public static WeiBoUser getUser() {
		return mUser;
	}
	
	public static Groups getGroups() {
		return mGroups;
	}
	
	public static void setUnreadCount(UnreadCount unreadCount) {
		AppContext.unreadCount = unreadCount;
	}
	
	public static UnreadCount getUnreadCount() {
		return AppContext.unreadCount;
	}
	
}
