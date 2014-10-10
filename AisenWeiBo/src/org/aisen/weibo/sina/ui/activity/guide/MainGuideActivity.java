package org.aisen.weibo.sina.ui.activity.guide;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.ui.activity.main.MainActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.m.common.utils.ActivityHelper;
import com.m.support.Inject.ViewInject;
import com.m.ui.activity.BaseActivity;

/**
 * 首页引导页
 * 
 * @author Jeff.Wang
 *
 * @date 2014年9月11日
 */
public class MainGuideActivity extends BaseActivity {
	
	public static void launch(Activity from) {
		if (BaseActivity.getRunningActivity() != null && BaseActivity.getRunningActivity() instanceof MainActivity) {
			Intent intent = new Intent(from, MainGuideActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			from.startActivity(intent);
			
			ActivityHelper.getInstance().putBooleanShareData("org.aisen.weibo.sina.MAIN_CAN_GUIDE", false);
		}
	}
	
	public static boolean canGuide() {
		return ActivityHelper.getInstance().getBooleanShareData("org.aisen.weibo.sina.MAIN_CAN_GUIDE", true);
	}
	
	@ViewInject(id = R.id.root, click = "back")
	View root;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_main_guide);
	}
	
	void back(View v) {
		finish();
	}
	
}
