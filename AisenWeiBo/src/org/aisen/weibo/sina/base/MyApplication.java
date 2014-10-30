package org.aisen.weibo.sina.base;

import java.io.File;
import java.util.List;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.bean.AccountBean;
import org.aisen.weibo.sina.support.bean.AppSettingsBean;
import org.aisen.weibo.sina.support.bean.PublishBean;
import org.aisen.weibo.sina.support.bean.WallpaperBean;
import org.aisen.weibo.sina.support.biz.BizLogic;
import org.aisen.weibo.sina.support.db.AccountDB;
import org.aisen.weibo.sina.support.db.EmotionsDB;
import org.aisen.weibo.sina.support.db.SinaDB;
import org.aisen.weibo.sina.support.publish.PublishDB;
import org.aisen.weibo.sina.support.utils.AisenUtil;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.AppSettings;
import org.aisen.weibo.sina.sys.receiver.TimingBroadcastReceiver;
import org.aisen.weibo.sina.sys.receiver.TimingIntent;
import org.aisen.weibo.sina.ui.fragment.account.AccountFragment;
import org.aisen.weibo.sina.ui.widget.WallpaperViewGroup;
import org.sina.android.bean.WeiBoUser;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.m.common.context.GlobalContext;
import com.m.common.settings.SettingUtility;
import com.m.common.utils.ActivityHelper;
import com.m.common.utils.CommSettings;
import com.m.common.utils.DateUtils;
import com.m.common.utils.Logger;
import com.m.support.sqlite.SqliteUtility;
import com.m.support.sqlite.util.FieldUtils;
import com.m.support.task.TaskException;
import com.m.support.task.WorkTask;
import com.m.ui.utils.MToast;

public class MyApplication extends GlobalContext {

	@Override
	public void onCreate() {
		super.onCreate();
		
		SettingUtility.addSettings("meizt_actions");
		
		MToast.type = 2;

		if (AppSettings.isCrashLogUpload()) 
			initBaiduAnalyze();
		
		// 设置登录账户
		AccountBean accountBean = AccountDB.getLogedinAccount();
		if (accountBean != null) 
			AppContext.login(accountBean.getUser(), accountBean.getGroups(), accountBean.getToken());
		
		// 检查表情
		try {
			EmotionsDB.checkEmotions();
		} catch (Exception e) {
		}
		
		// KitKat以下，禁用设置选项
		if (!WallpaperViewGroup.isKitKat()) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
			Editor editor = prefs.edit();
			editor.putBoolean("pTranslucent", false);
			editor.commit();
		}
		
		// 默认设置壁纸
		// 暂时不默认设置壁纸
		if (false && ActivityHelper.getInstance().getBooleanShareData("app_wallpaper_init", true)) {
			ActivityHelper.getInstance().putBooleanShareData("app_wallpaper_init", false);
			
			CommSettings.setAppTheme(R.style.BaseTheme_Translucent);
			
			AppContext.setWallpaper(AisenUtil.generaterDefaultWallpaper());
		}
		else {
			String selection = String.format(" %s = ? ", FieldUtils.KEY);
			String[] selectionArgs = new String[]{ "wallpaper_setting" };
			List<WallpaperBean> settingList = SinaDB.getSqlite().selectAll(WallpaperBean.class, selection, selectionArgs);
			if (settingList.size() > 0)
				AppContext.setWallpaper(settingList.get(0));
		}
		
		if (ActivityHelper.getInstance().getBooleanShareData("theme_init", true)) {
			ActivityHelper.getInstance().putBooleanShareData("theme_init", false);
			
			if (WallpaperViewGroup.isKitKat())
				CommSettings.setAppTheme(R.style.BaseTheme_Translucent);
		}
	}
	
	@Override
	public String getDataPath() {
		// 读取SD卡文件速度慢很多
		return getCacheDir().getAbsolutePath();
//		return super.getDataPath();
	}
	
	@Override
	public String getImagePath() {
		return super.getAppPath() + SettingUtility.getPermanentSettingAsStr("com_m_common_image", "image") + File.separator;
	}
	
	private void initBaiduAnalyze() {
		com.baidu.mobstat.StatService.setAppChannel(this, SettingUtility.getStringSetting("app_channel"), true);
		com.baidu.mobstat.StatService.setSessionTimeOut(5 * 60);
//		com.baidu.mobstat.StatService.setSessionTimeOut(10);
		// 打开崩溃错误收集
		com.baidu.mobstat.StatService.setOn(this, com.baidu.mobstat.StatService.EXCEPTION_LOG);
		com.baidu.mobstat.StatService.setDebugOn(true);
	}
	
	public static MyApplication getInstance() {
		return (MyApplication) GlobalContext.getInstance();
	}
	
	// 刷新定时发布任务
	public static void refreshPublishAlarm() {
		new WorkTask<Void, Void, Void>() {

			@Override
			public Void workInBackground(Void... params) throws TaskException {
				List<PublishBean> beans = PublishDB.getPublishList(AppContext.getUser());
				
				
				AlarmManager am = (AlarmManager) GlobalContext.getInstance().getSystemService(ALARM_SERVICE);

				for (PublishBean bean : beans) {
//					PendingIntent sender = PendingIntent.getService(getInstance(), (int) (bean.getTiming() - System.currentTimeMillis()), intent, PendingIntent.FLAG_CANCEL_CURRENT);
					if (bean.getTiming() > System.currentTimeMillis()) {
						TimingIntent intent = new TimingIntent(bean.getTiming());
						String timingStr = bean.getTiming() / 1000 + "";
						int requectCode = Integer.parseInt(timingStr.substring(timingStr.length() - 6, timingStr.length()));
						PendingIntent sender = PendingIntent.getBroadcast(GlobalContext.getInstance(), requectCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
						Logger.d(TimingBroadcastReceiver.TAG, "添加一个定时任务到系统时钟, request = " + requectCode);
						Logger.d(TimingBroadcastReceiver.TAG, DateUtils.formatDate(bean.getTiming(), DateUtils.TYPE_01));
						Logger.d(AccountFragment.TAG, "添加一个定时任务到系统时钟, request = " + requectCode);
						Logger.d(AccountFragment.TAG, DateUtils.formatDate(bean.getTiming(), DateUtils.TYPE_01));
						
						am.set(AlarmManager.RTC_WAKEUP, bean.getTiming(), sender);
					}
					else {
						Logger.d(TimingBroadcastReceiver.TAG, "定时任务已过期");
					}
				}
				
				return null;
			}
		}.executeOnSerialExecutor();
	}
	
	public static void removeAllPublishAlarm() {
		new WorkTask<WeiBoUser, Void, Void>() {

			@Override
			public Void workInBackground(WeiBoUser... params) throws TaskException {
				List<PublishBean> beans = PublishDB.getPublishList(params[0]);
				
				for (PublishBean bean : beans) {
					if (bean.getTiming() > System.currentTimeMillis()) {
						Logger.d(AccountFragment.TAG, "清理所有定时任务");
						Logger.d(TimingBroadcastReceiver.TAG, "清理所有定时任务");
						removePublishAlarm(bean);
					}
				}
				
				return null;
			}
		}.execute(AppContext.getUser());
	}
	
	public static void removePublishAlarm(PublishBean bean) {
		TimingIntent intent = new TimingIntent(bean.getTiming());
		String timingStr = bean.getTiming() / 1000 + "";
		int requectCode = Integer.parseInt(timingStr.substring(timingStr.length() - 6, timingStr.length()));
		PendingIntent sender = PendingIntent.getBroadcast(GlobalContext.getInstance(), requectCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		
		Logger.d(AccountFragment.TAG, "从系统时钟移除一个定时任务, request = " + requectCode);
		Logger.d(TimingBroadcastReceiver.TAG, "从系统时钟移除一个定时任务, request = " + requectCode);
		AlarmManager am = (AlarmManager) GlobalContext.getInstance().getSystemService(ALARM_SERVICE);
		am.cancel(sender);
	}
	
	public static void refreshSettings() {
		// 更新程序配置信息
		new UpdateSettings().execute();
	}
	
	static class UpdateSettings extends WorkTask<Void, Void, AppSettingsBean> {
		
		@Override
		public AppSettingsBean workInBackground(Void... params) throws TaskException {
			AppSettingsBean bean = BizLogic.newInstance().getSettings();
			
			if (bean != null)
				SqliteUtility.getInstance().insert(null, bean);
			
			AppSettings.setSettings(bean);
			
			return null;
		}
		
	}
	
}
