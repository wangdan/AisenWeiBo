package org.aisen.weibo.sina.ui.activity.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Browser;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.aisen.android.common.utils.SystemBarUtils;
import org.aisen.android.common.utils.ViewUtils;
import org.aisen.android.component.container.FragmentContainerActivity;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.support.utils.ThemeUtils;
import org.aisen.weibo.sina.ui.activity.basic.AisenActivityHelper;
import org.aisen.weibo.sina.ui.fragment.profile.UserProfilePagerFragment;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.AccessToken;
import org.aisen.weibo.sina.sinasdk.bean.Token;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;

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
	
	@ViewInject(id = R.id.layContent)
	View layoutContent;
	@ViewInject(id = R.id.layoutLoadFailed)
	View layoutLoadFailed;
	@ViewInject(id = R.id.txtLoadFailed)
	TextView txtLoadFailed;
	@ViewInject(id = R.id.layoutReload, click = "reload")
	View layoutReload;
    @ViewInject(id = R.id.layToolbar)
    ViewGroup layToolbar;

    @ViewInject(id = R.id.viewToolbar)
    View viewToolbar;
	
	private boolean searchFailed = false;
	
	private String screenName;

    private WeiBoUser mUser;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
        AisenUtils.setStatusBar(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.as_ui_profile_activity);

        getSupportActionBar().setDisplayShowHomeEnabled(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            layToolbar.setPadding(layToolbar.getPaddingLeft(),
                                        layToolbar.getPaddingTop() + SystemBarUtils.getStatusBarHeight(this),
                                        layToolbar.getPaddingRight(),
                                        layToolbar.getPaddingBottom());

            viewToolbar.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                    getResources().getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material) + SystemBarUtils.getStatusBarHeight(this)));
        }

        if (savedInstanceState == null && getIntent() != null) {
            mUser = (WeiBoUser) getIntent().getSerializableExtra("user");
        }
        else {
            if (savedInstanceState != null)
                mUser = (WeiBoUser) savedInstanceState.getSerializable("user");
        }

        if (mUser != null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.layContent, UserProfilePagerFragment.newInstance(mUser), FragmentContainerActivity.FRAGMENT_TAG)
                    .commit();
            return;
        }

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
			
			ViewUtils.createProgressDialog(UserProfileActivity.this, getString(R.string.msg_load_profile), ThemeUtils.getThemeColor()).show();
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
			
//			showMessage(exception.getMessage());

            if (!TextUtils.isEmpty(exception.getMessage()))
                txtLoadFailed.setText(exception.getMessage());
			
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

    @Override
    protected int configTheme() {
        return ThemeUtils.themeArr[AppSettings.getThemeColor()][2];
    }

    @Override
    public boolean canSwipe() {
        return true;
    }

}
