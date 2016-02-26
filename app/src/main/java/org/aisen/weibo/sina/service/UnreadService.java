package org.aisen.weibo.sina.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.service.notifier.UnreadCountNotifier;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.UnreadCount;
import org.aisen.weibo.sina.support.sqlit.SinaDB;

import java.util.Calendar;

/**
 * 未读消息服务<br/>
 *
 * @author wangdan
 * 
 */
public class UnreadService extends Service {

	public static final String TAG = UnreadService.class.getSimpleName();
	public static final String ACTION_GET = "org.aisen.weibo.sina.ACTION_GET";
	public static final String ACTION_UPDATE = "org.aisen.weibo.sina.ACTION_UPDATE";
	public static final String ACTION_UNREAD_CHANGED = "org.aisen.weibo.sina.ACTION_UNREAD_CHANGED";

	public static void startService() {
		if (!AppSettings.isNotifyEnable())
			return;
		
		Intent intent = new Intent(GlobalContext.getInstance(), UnreadService.class);
		intent.setAction(ACTION_GET);
		GlobalContext.getInstance().startService(intent);
	}
	
	public static void stopService() {
		clearAlarm();

		if (AppContext.isLoggedIn()) {
			UnreadCountNotifier.mCount = new UnreadCount();
		}
		
		Intent intent = new Intent(GlobalContext.getInstance(), UnreadService.class);
		GlobalContext.getInstance().stopService(intent);
	}

	public static void updateAlarm() {
		Intent intent = new Intent(GlobalContext.getInstance(), UnreadService.class);
		intent.setAction(ACTION_UPDATE);
		GlobalContext.getInstance().startService(intent);
	}
	
	public UnreadCountNotifier unreadCountNotifier;

	private UnreadTask unreadTask;

	@Override
	public void onCreate() {
		super.onCreate();
		
		Logger.v(TAG, "服务初始化");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (unreadCountNotifier == null)
			unreadCountNotifier = new UnreadCountNotifier(this);


		if (!AppContext.isLoggedIn()) {
			stopSelf();
			return super.onStartCommand(intent, flags, startId);
		}
		
		String action = intent != null ? intent.getAction() : "";
		
		if (ACTION_GET.equals(action)) {
			resetTheTime();

			if (!AppContext.getAccount().getAccessToken().isExpired()) {
				new UnreadTask().execute();
			}
		}
		else if (ACTION_UPDATE.equals(action)) {
			Logger.v(TAG, "刷新时间");
			
			resetTheTime();
		}

		return super.onStartCommand(intent, flags, startId);
	}

	private static PendingIntent getOperation() {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		intent.setAction(ACTION_GET);
		PendingIntent sender = PendingIntent.getService(GlobalContext.getInstance().getBaseContext()
										, 1000, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		return sender;
	}

	private static void clearAlarm() {
		Logger.v(TAG, "clear unread block");

		AlarmManager am = (AlarmManager) GlobalContext.getInstance().getSystemService(ALARM_SERVICE);
		am.cancel(getOperation());
	}

	private void resetTheTime() {
		Logger.v(TAG, "reset unread block");

		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

		// 指定时间
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.add(Calendar.SECOND, AppSettings.getUnreadInterval());
		Logger.v(TAG, String.format("未读消息，在%s后还是读取", AppSettings.getUnreadInterval()));

		am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), getOperation());
	}

	class UnreadTask extends WorkTask<Void, Void, UnreadCount> {

		UnreadTask() {
			if (unreadTask != null)
				unreadTask.cancel(true);
			unreadTask = this;
			Logger.v(TAG, "execute UnreadTask");
		}
		
		@Override
		public UnreadCount workInBackground(Void... params) throws TaskException {
			if (!AppContext.isLoggedIn())
				return null;
			
			UnreadCount result = SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).remindUnread(AppContext.getAccount().getUser().getIdstr());
//			result.setMention_status(500);
//			result.setMention_cmt(100);
//			result.setCmt(20);
//			result.setFollower(10);

			AppContext.getAccount().setUnreadCount(new UnreadCount());
			if (AppSettings.isNotifyComment())
				AppContext.getAccount().getUnreadCount().setCmt(result.getCmt());
			if (AppSettings.isNotifyCommentMention())
				AppContext.getAccount().getUnreadCount().setMention_cmt(result.getMention_cmt());
			if (AppSettings.isNotifyStatusMention())
				AppContext.getAccount().getUnreadCount().setMention_status(result.getMention_status());
			if (AppSettings.isNotifyFollower())
				AppContext.getAccount().getUnreadCount().setFollower(result.getFollower());
			if (AppSettings.isNotifyDm())
				AppContext.getAccount().getUnreadCount().setDm(result.getDm());
			
			// 更新DB
			result.setId(AppContext.getAccount().getUser().getIdstr());
			SinaDB.getDB().insert(null, AppContext.getAccount().getUnreadCount());
			return result;
		}

		@Override
		protected void onSuccess(UnreadCount result) {
			super.onSuccess(result);

			// 通知消息
			unreadCountNotifier.notinfyUnreadCount(AppContext.getAccount().getUnreadCount());

			sendUnreadBroadcast();
		}

		@Override
		protected void onFinished() {
			super.onFinished();
			
			unreadTask = null;
		}

	}
	
	public static void sendUnreadBroadcast() {
		// 发出广播更新状态
		Intent intent = new Intent(ACTION_UNREAD_CHANGED);
		GlobalContext.getInstance().sendBroadcast(intent);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		Logger.v(TAG, "服务停止");
		
		if (unreadTask != null)
			unreadTask.cancel(true);
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public static UnreadCount getUnreadCount() {
		if (!AppContext.isLoggedIn())
			return null;
		
		return SinaDB.getDB().selectById(null, UnreadCount.class, AppContext.getAccount().getUser().getIdstr());
	}

}
