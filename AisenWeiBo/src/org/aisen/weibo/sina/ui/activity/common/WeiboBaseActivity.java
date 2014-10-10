package org.aisen.weibo.sina.ui.activity.common;

import org.aisen.weibo.sina.support.utils.AppSettings;

import android.content.pm.ActivityInfo;

import com.m.ui.activity.BaseActivity;

public class WeiboBaseActivity extends BaseActivity {

	@Override
	protected void onResume() {
		super.onResume();
		
		setScreenOrientation();
	}
	
	private void setScreenOrientation() {
		// 开启屏幕旋转
		if (AppSettings.isScreenRotate()) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		}
		else {
			if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_SENSOR)
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
	}
	
}
