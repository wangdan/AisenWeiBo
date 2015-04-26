package org.aisen.weibo.sina.ui.activity.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.view.View;
import android.widget.TextView;

import com.m.common.utils.ViewUtils;
import com.m.component.container.FragmentContainerActivity;
import com.m.network.task.TaskException;
import com.m.network.task.WorkTask;
import com.m.support.inject.ViewInject;
import com.m.ui.activity.basic.BaseActivity;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.ui.fragment.profile.UserProfilePagerFragment;
import org.sina.android.SinaSDK;
import org.sina.android.bean.AccessToken;
import org.sina.android.bean.Token;
import org.sina.android.bean.WeiBoUser;

/**
 * 用户搜索界面
 * 
 * @author wangdan
 *
 */
public class UserProfileActivity extends BaseActivity {

	public static void launch(Activity from, String screenName) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("org.aisen.weibo.sina.userinfo://@%s", screenName)));
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, from.getPackageName());
        from.startActivity(intent);
	}
	
	@ViewInject(id = R.id.layContent)
	View layoutContent;
	@ViewInject(id = R.id.layoutLoadFailed)
	View layoutLoadFailed;
	@ViewInject(id = R.id.txtLoadFailed)
	TextView txtLoadFailed;
	@ViewInject(id = R.id.layoutReload, click = "reload")
	View layoutReload;
	
	private boolean searchFailed = false;
	
	private String screenName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.as_ui_profile_activity);
		
		searchFailed = savedInstanceState == null ? false : savedInstanceState.getBoolean("searchFailed");
		screenName = savedInstanceState == null ? null : savedInstanceState.getString("screenName");
		
		txtLoadFailed.setText(R.string.error_pic_load_faild);
		
		if (savedInstanceState == null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_user_profile);
			
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
		 
		if (searchFailed) {
			reload(null);
		}

        getSupportActionBar().setDisplayShowHomeEnabled(false);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putString("screenName", screenName);
		outState.putBoolean("searchFailed", searchFailed);
	}
	
	void reload(View v) {
		new UserShowTask().execute();
	}
	
	class UserShowTask extends WorkTask<Void, Void, WeiBoUser> {

		@Override
		protected void onPrepare() {
			super.onPrepare();
			
			layoutContent.setVisibility(View.VISIBLE);
			layoutLoadFailed.setVisibility(View.GONE);
			
			ViewUtils.createProgressDialog(UserProfileActivity.this, getString(R.string.msg_load_profile)).show();
		}
		
		@Override
		public WeiBoUser workInBackground(Void... params) throws TaskException {
			Token token  = AppContext.getToken();
			if (AppContext.getAdvancedToken() != null) {
				AccessToken accessToken = AppContext.getAdvancedToken();
				
				token = new Token();
				token.setToken(accessToken.getToken());
				token.setSecret(accessToken.getSecret());
			}
			
			return SinaSDK.getInstance(token).userShow(null, screenName);
		}
		
		@Override
		protected void onFailure(TaskException exception) {
			super.onFailure(exception);
			
			showMessage(exception.getMessage());
			
			layoutContent.setVisibility(View.GONE);
			layoutLoadFailed.setVisibility(View.VISIBLE);
			
			searchFailed = true;
		}
		
		@Override
		protected void onFinished() {
			super.onFinished();
			
			ViewUtils.dismissProgressDialog();
		}
		
		@Override
		protected void onSuccess(WeiBoUser result) {
			super.onSuccess(result);
			
			searchFailed = false;
			
			layoutContent.setVisibility(View.VISIBLE);
			layoutLoadFailed.setVisibility(View.GONE);
			
			getFragmentManager().beginTransaction()
									.add(R.id.layContent, UserProfilePagerFragment.newInstance(result), FragmentContainerActivity.FRAGMENT_TAG)
									.commit();
		}
		
	}
	
}
