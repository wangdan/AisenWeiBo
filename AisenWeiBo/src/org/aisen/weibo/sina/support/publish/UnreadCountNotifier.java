package org.aisen.weibo.sina.support.publish;

import java.util.Calendar;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.utils.AppSettings;
import org.aisen.weibo.sina.ui.activity.main.MainActivity;
import org.sina.android.bean.UnreadCount;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.m.common.utils.DateUtils;
import com.m.common.utils.Logger;

public class UnreadCountNotifier extends Notifier {

	public UnreadCountNotifier(Context context) {
		super(context);
	}
	
	// 保存最后一次的数据，跟上一次匹配，当数据不同时且大于0时，发出通知提醒用户
	public static UnreadCount mCount = new UnreadCount();

	public void notinfyUnreadCount(UnreadCount count) {
		String fromAisen = context.getString(R.string.notifer_from_aisen);

		// 新粉丝
		if (count.getFollower() != 0 && mCount.getFollower() != count.getFollower()) {
			String contentTitle = String.format(context.getString(R.string.notifer_new_followers), count.getFollower());
			
			NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
			builder.setSmallIcon(R.drawable.statusbar_ic_follower_small).setContentTitle(contentTitle).setContentText(fromAisen);

			Intent intent = new Intent(context, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.setAction("showFollowers");
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
			builder.setContentIntent(contentIntent).setAutoCancel(true);
			builder.setTicker(contentTitle);
			
			notify(RemindUnreadForFollowers, builder);
		}
		mCount.setFollower(count.getFollower());
		if (count.getFollower() == 0)
			cancelNotification(RemindUnreadForFollowers);

		// 新评论
		if (count.getCmt() != 0 && mCount.getCmt() != count.getCmt()) {
			String contentTitle = String.format(context.getString(R.string.notifer_new_cmts), count.getCmt());
			
			NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
			builder.setSmallIcon(R.drawable.statusbar_ic_comment_small).setContentTitle(contentTitle).setContentText(fromAisen);
			builder.setTicker(contentTitle);

			Intent intent = new Intent(context, MainActivity.class);
			intent.setAction("showComments");
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
			builder.setContentIntent(contentIntent).setAutoCancel(true);

			notify(RemindUnreadComments, builder);
		}
		mCount.setCmt(count.getCmt());
		if (count.getCmt() == 0)
			cancelNotification(RemindUnreadComments);

		// 新提及我的微博数
		if (count.getMention_status() != 0 && mCount.getMention_status() != count.getMention_status()) {
			String contentTitle = String.format(context.getString(R.string.notifer_new_mention_status), count.getMention_status());
			
			NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
			builder.setSmallIcon(R.drawable.statusbar_ic_mention_small).setContentTitle(contentTitle)
					.setContentText(fromAisen);
			builder.setTicker(contentTitle);

			Intent intent = new Intent(context, MainActivity.class);
			intent.setAction("showMentionStatus");
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
			builder.setContentIntent(contentIntent).setAutoCancel(true);

			notify(RemindUnreadForMentionStatus, builder);
		}
		mCount.setMention_status(count.getMention_status());
		if (count.getMention_status() == 0)
			cancelNotification(RemindUnreadForMentionStatus);

		// 新提及我的评论数
		if (count.getMention_cmt() != 0 && mCount.getMention_cmt() != count.getMention_cmt()) {
			String contentTitle = String.format(context.getString(R.string.notifer_new_mention_cmt), count.getMention_cmt());
			
			NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
			builder.setSmallIcon(R.drawable.statusbar_ic_mention_small).setContentTitle(contentTitle)
					.setContentText(fromAisen);
			builder.setTicker(contentTitle);

			Intent intent = new Intent(context, MainActivity.class);
			intent.setAction("showMentionCmt");
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
			builder.setContentIntent(contentIntent).setAutoCancel(true);

			notify(RemindUnreadForMentionComments, builder);
		} 
		mCount.setMention_cmt(count.getMention_cmt());
		if (count.getMention_cmt() == 0)
			cancelNotification(RemindUnreadForMentionComments);

	}
	
	private void notify(int request, NotificationCompat.Builder builder) {
		Notification notification = builder.build();

		// 判断是否是夜间勿扰
		boolean feedback = true;
		if (AppSettings.isNotifyNightClose()) {
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(System.currentTimeMillis());

			Logger.d(String.format("现在时间是%s", DateUtils.formatDate(cal.getTimeInMillis(), DateUtils.TYPE_01)));
			if (cal.get(Calendar.HOUR_OF_DAY) >= 1 && cal.get(Calendar.HOUR_OF_DAY) <= 7) {
				feedback = false;
				
				Logger.d("夜间勿扰");
			}
		}
		
		if (feedback) {
			if (AppSettings.isNotifyLED()) {
				notification.defaults = notification.defaults | Notification.DEFAULT_LIGHTS | Notification.FLAG_SHOW_LIGHTS;
				notification.ledARGB = 0xff00ff00;
				notification.ledOffMS = 500;
				notification.ledOnMS = 500;
			}
			if (AppSettings.isNotifyVibrate()) {
				notification.defaults |= Notification.DEFAULT_VIBRATE;
			}
			if (AppSettings.isNotifySound()) {
				notification.defaults |= Notification.DEFAULT_SOUND;
			}
		}
		
		notify(request, notification);
	}

}
