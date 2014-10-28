package org.aisen.weibo.sina.ui.activity.splash;

import java.util.Timer;
import java.util.TimerTask;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.MyApplication;
import org.aisen.weibo.sina.support.cache.TimelineMemoryCacheUtility;
import org.aisen.weibo.sina.support.publish.UnreadCountNotifier;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.AppSettings;
import org.aisen.weibo.sina.support.utils.CheckChangedUtils;
import org.aisen.weibo.sina.ui.activity.main.MainActivity;
import org.aisen.weibo.sina.ui.fragment.account.AccountFragment;
import org.aisen.weibo.sina.ui.fragment.base.ActivityHelper;
import org.aisen.weibo.sina.ui.fragment.settings.VersionSettingsFragment;
import org.sina.android.SinaSDK;
import org.sina.android.bean.Group;
import org.sina.android.bean.UnreadCount;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.m.common.params.Params;
import com.m.support.bizlogic.ABaseBizlogic.CacheMode;
import com.m.support.task.TaskException;
import com.m.support.task.WorkTask;
import com.m.ui.activity.BaseActivity;

public class SplashActivity extends BaseActivity {

	@Override
	protected int configTheme() {
		return R.style.BaseTheme_Wallpaper_Translucent_Splash;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ActivityHelper activityHelper = (ActivityHelper) getActivityHelper();
		activityHelper.blur = false;
		
		super.onCreate(savedInstanceState);
		if (AppContext.getWallpaper() == null) {
			View contentView = View.inflate(this, R.layout.ui_splash, null);
			
			setContentView(contentView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		}
		else {
			setContentView(R.layout.ui_splash);
		}
		
		// 清空未读消息记录
		UnreadCountNotifier.mCount = new UnreadCount();
		
		// 检查更新
		VersionSettingsFragment.checkVersion();
		
		// 刷新配置信息
		MyApplication.refreshSettings();
		
		// 刷新用户信息
		if (AppContext.isLogedin())
			CheckChangedUtils.check(AppContext.getUser(), AppContext.getToken());
		
		if (TimelineMemoryCacheUtility.isEmpty() && AppContext.isLogedin()) {
			new LoadCacheTask().execute();
		}
		else {
			new Timer().schedule(new TimerTask() {
				
				@Override
				public void run() {
					// 已有用户授权
					if (AppContext.isLogedin()) {
						MainActivity.login();
					}
					// 没有授权，跳转至账号页面，添加授权
					else {
						AccountFragment.launch(SplashActivity.this);
					}
					
					finish();
				}
			}, 700);
		}
		
	}
	
	class LoadCacheTask extends WorkTask<Void, Void, Boolean> {

		@Override
		public Boolean workInBackground(Void... p) throws TaskException {
			
			if (false && AppContext.getGroups() != null) {
				for (Group group : AppContext.getGroups().getLists()) {
					Params params = new Params();
					params.addParameter("list_id", group.getIdstr());
					params.addParameter("count", String.valueOf(AppSettings.getTimelineCount()));
					
					SinaSDK.getInstance(AppContext.getToken(), CacheMode.cachePriority).friendshipGroupsTimeline(params);
				}
			}
			return true;
		}
		
		@Override
		protected void onFinished() {
			super.onFinished();
			
			MainActivity.login();
			finish();
		}
		
	}
	
}
