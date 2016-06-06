package org.aisen.weibo.sina.base;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.Environment;

import com.tencent.bugly.crashreport.CrashReport;
import com.umeng.analytics.MobclickAgent;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.setting.SettingUtility;
import org.aisen.android.common.utils.CrashHandler;
import org.aisen.android.common.utils.DateUtils;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.weibo.sina.BuildConfig;
import org.aisen.weibo.sina.receiver.TimingBroadcastReceiver;
import org.aisen.weibo.sina.receiver.TimingIntent;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;
import org.aisen.weibo.sina.sinasdk.core.SinaErrorMsgUtil;
import org.aisen.weibo.sina.support.bean.AccountBean;
import org.aisen.weibo.sina.support.bean.PublishBean;
import org.aisen.weibo.sina.support.sqlit.EmotionsDB;
import org.aisen.weibo.sina.support.sqlit.PublishDB;
import org.aisen.weibo.sina.support.sqlit.SinaDB;
import org.aisen.weibo.sina.support.utils.AccountUtils;
import org.aisen.weibo.sina.support.utils.UMengUtil;
import org.aisen.weibo.sina.ui.fragment.account.AccountFragment;

import java.io.File;
import java.util.List;
import java.util.Random;

/**
 * Created by wangdan on 15/12/13.
 */
public class MyApplication extends GlobalContext {

    @Override
    public void onCreate() {
        super.onCreate();

        // 添加一些配置项
        SettingUtility.addSettings(this, "actions");
        SettingUtility.addSettings(this, "settings");
        // 初始化一个颜色主题
        setupTheme();
        // 打开Debug日志
        Logger.DEBUG = BuildConfig.LOG_DEBUG;
        setupCrash();
        // 初始化图片加载
        BitmapLoader.newInstance(this, getImagePath());
        // 配置异常处理类
        TaskException.config(new SinaErrorMsgUtil());
        // 初始化数据库
        SinaDB.setInitDB();
        // 检查表情
        try {
            EmotionsDB.checkEmotions();
        } catch (Exception e) {
        }
        // 设置登录账号
        AppContext.setAccount(AccountUtils.getLogedinAccount());
        if (AppContext.isLoggedIn())
            AppContext.login(AppContext.getAccount());
    }

    public static String getImagePath() {
        return GlobalContext.getInstance().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.separator;
    }

    private void setupTheme() {
        int position = AppSettings.getThemeColor();
        if (position == -1) {
            // 一些我喜欢的颜色
            int[] initIndex = new int[]{ 0, 1, 4, 8, 15, 16, 18 };
            position = initIndex[new Random().nextInt(initIndex.length)];

            AppSettings.setThemeColor(position);
        }
    }

    public void setupCrash() {
        if (BuildConfig.LOG_DEBUG) {
            CrashHandler.setupCrashHandler(this);
        }
        // UMENG统计设置
        MobclickAgent.setDebugMode(Logger.DEBUG);
//        AnalyticsConfig.setAppkey(this, BuildConfig.UMENG_APP_ID);
        MobclickAgent.setCatchUncaughtExceptions(false);
        MobclickAgent.openActivityDurationTrack(false);
        if (BuildConfig.LOG_DEBUG) {
            Logger.d("Device_info", UMengUtil.getDeviceInfo(this));
        }
        // BUGLY日志上报
        CrashReport.initCrashReport(this, BuildConfig.BUGLY_APP_ID, Logger.DEBUG);
    }

    // 刷新定时发布任务
    public static void refreshPublishAlarm() {
        new WorkTask<Void, Void, Void>() {

            @Override
            public Void workInBackground(Void... params) throws TaskException {
                List<PublishBean> beans = PublishDB.getPublishList(AppContext.getAccount().getUser());


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
        }.execute(AppContext.getAccount().getUser());
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
//            FIR.addCustomizeValue("uid", account.getUserId());
//            FIR.addCustomizeValue("screen_name", URLEncoder.encode(account.getUser().getScreen_name(), "utf-8"));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
