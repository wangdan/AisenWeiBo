package org.aisen.weibo.sina.base;

import android.app.AlarmManager;
import android.app.PendingIntent;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.setting.SettingUtility;
import org.aisen.android.common.utils.DateUtils;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;
import org.aisen.weibo.sina.sinasdk.core.SinaErrorMsgUtil;
import org.aisen.weibo.sina.support.bean.AccountBean;
import org.aisen.weibo.sina.support.bean.PublishBean;
import org.aisen.weibo.sina.support.db.AccountDB;
import org.aisen.weibo.sina.support.db.EmotionsDB;
import org.aisen.weibo.sina.support.db.PublishDB;
import org.aisen.weibo.sina.sys.receiver.TimingBroadcastReceiver;
import org.aisen.weibo.sina.sys.receiver.TimingIntent;
import org.aisen.weibo.sina.ui.fragment.account.AccountFragment;

import java.net.URLEncoder;
import java.util.List;

import im.fir.sdk.FIR;

public class MyApplication extends GlobalContext {
	
	@Override
	public void onCreate() {
        // 初始化
        if (AppSettings.isCrashLogUpload()) {
            FIR.init(this);
        }
		super.onCreate();

		// 初始化图片加载
		BitmapLoader.newInstance(this, getImagePath());
        // 配置异常处理类
        TaskException.config(new SinaErrorMsgUtil());
        // 设置登录账户
        AccountBean accountBean = AccountDB.getLogedinAccount();
        SettingUtility.addSettings("meizt_actions");
        if (accountBean != null)
            AppContext.login(accountBean);
        // 检查表情
        try {
            EmotionsDB.checkEmotions();
        } catch (Exception e) {
        }
        // 打开Debug日志
        Logger.DEBUG = true;
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

    public static void setDebugAccount(AccountBean account) {
        try {
            FIR.addCustomizeValue("uid", account.getUserId());
            FIR.addCustomizeValue("screen_name", URLEncoder.encode(account.getUser().getScreen_name(), "utf-8"));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
