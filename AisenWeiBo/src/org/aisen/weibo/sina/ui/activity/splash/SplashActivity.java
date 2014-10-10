package org.aisen.weibo.sina.ui.activity.splash;

import java.util.Timer;
import java.util.TimerTask;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.MyApplication;
import org.aisen.weibo.sina.support.publish.UnreadCountNotifier;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.CheckChangedUtils;
import org.aisen.weibo.sina.ui.activity.main.MainActivity;
import org.aisen.weibo.sina.ui.fragment.account.AccountFragment;
import org.aisen.weibo.sina.ui.fragment.settings.VersionSettingsFragment;
import org.sina.android.bean.UnreadCount;

import android.os.Bundle;

import com.m.ui.activity.BaseActivity;

public class SplashActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_splash);
		
		// 清空未读消息记录
		UnreadCountNotifier.mCount = new UnreadCount();
		
		// 检查更新
		VersionSettingsFragment.checkVersion();
		
		// 刷新配置信息
		MyApplication.refreshSettings();
		
		// 刷新用户信息
		if (AppContext.isLogedin())
			CheckChangedUtils.check(AppContext.getUser(), AppContext.getToken());
		
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
