package org.aisen.weibo.sina.ui.activity.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.view.View;

import org.aisen.android.common.utils.ViewUtils;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.Token;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.support.utils.ThemeUtils;
import org.aisen.weibo.sina.ui.activity.base.AisenActivityHelper;
import org.aisen.weibo.sina.ui.activity.base.SinaCommonActivity;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;
import org.aisen.weibo.sina.ui.fragment.profile.ProfilePagerFragment;

/**
 * 用户搜索界面
 * 
 * @author wangdan
 *
 */
public class UserProfileActivity extends BaseActivity implements AisenActivityHelper.EnableSwipeback {

	public static void launch(Activity from, String screenName) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("org.aisen.weibo.sina.userinfo://@%s", screenName)));
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, from.getPackageName());
        from.startActivity(intent);
	}

    public static void launch(Activity from, WeiBoUser user) {
        Intent intent = new Intent(from, UserProfileActivity.class);
        intent.putExtra("user", user);
        from.startActivity(intent);
    }
	
	private String screenName;

	private ProfilePagerFragment fragment;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
        AisenUtils.setStatusBar(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_profile_pager);

		getSupportActionBar().setTitle("");

		BizFragment.createBizFragment(this);

		fragment = ProfilePagerFragment.newInstance();
		getFragmentManager().beginTransaction().add(fragment, SinaCommonActivity.FRAGMENT_TAG).commit();

		Uri data = getIntent().getData();
		if (data != null) {
			String d = data.toString();
			int index = d.lastIndexOf("/");
			String userName = d.substring(index + 1);
			if (userName.indexOf("@") == 0)
				userName = userName.substring(1);

			screenName = userName;

			reload(null);
		} else {
			finish();
			return;
		}
	}

	void reload(View v) {
		BizFragment.createBizFragment(this).checkProfile(new BizFragment.CheckProfileCallback() {

			@Override
			public void onCheckProfileSuccess() {
				new UserShowTask().execute();
			}

			@Override
			public void onCheckProfileFaild() {
				finish();
			}

		});
	}
	
	class UserShowTask extends WorkTask<Void, Void, WeiBoUser> {

		@Override
		protected void onPrepare() {
			super.onPrepare();
			
			ViewUtils.createProgressDialog(UserProfileActivity.this, getString(R.string.msg_load_profile), ThemeUtils.getThemeColor()).show();
		}
		
		@Override
		public WeiBoUser workInBackground(Void... params) throws TaskException {
			Token token  = AppContext.getAccount().getAdvancedToken();

			return SinaSDK.getInstance(token).userShow(null, screenName);
		}
		
		@Override
		protected void onFailure(TaskException exception) {
			super.onFailure(exception);

			showMessage(exception.getMessage() + "");

			finish();
		}
		
		@Override
		protected void onFinished() {
			super.onFinished();
			
			ViewUtils.dismissProgressDialog();
		}
		
		@Override
		protected void onSuccess(WeiBoUser result) {
			super.onSuccess(result);

			fragment.setUser(result);
		}
		
	}

    @Override
    protected int configTheme() {
        return ThemeUtils.themeArr[AppSettings.getThemeColor()][2];
    }

    @Override
    public boolean canSwipe() {
        return true;
    }

}
