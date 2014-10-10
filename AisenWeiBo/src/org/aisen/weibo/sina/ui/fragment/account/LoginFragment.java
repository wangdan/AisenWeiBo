package org.aisen.weibo.sina.ui.fragment.account;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.bean.AccountBean;
import org.aisen.weibo.sina.support.db.AccountDB;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.BaiduAnalyzeUtils;
import org.aisen.weibo.sina.ui.activity.common.FragmentContainerActivity;
import org.sina.android.SinaSDK;
import org.sina.android.bean.AccessToken;
import org.sina.android.bean.Groups;
import org.sina.android.bean.WeiBoUser;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.m.common.params.Params;
import com.m.common.params.ParamsUtil;
import com.m.common.settings.SettingUtility;
import com.m.common.utils.Logger;
import com.m.support.Inject.ViewInject;
import com.m.support.task.TaskException;
import com.m.support.task.WorkTask;
import com.m.ui.fragment.ABaseFragment;
import com.m.ui.utils.ViewUtils;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

/**
 * 授权页面
 * 
 * @author wangdan
 *
 */
public class LoginFragment extends ABaseFragment {

	public static void launch(ABaseFragment from, int requestCode) {
		FragmentContainerActivity.launchForResult(from, LoginFragment.class, null, requestCode);
	}
	
	private static final String TAG = "Login";
	
	@ViewInject(id = R.id.webview)
	WebView webView;
	@ViewInject(id = R.id.progress)
	SmoothProgressBar progressBar;
	
	private AccountTask accountTask;
	
	@Override
	protected int inflateContentView() {
		return R.layout.ui_login;
	}
	
	@SuppressLint("SetJavaScriptEnabled") @Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		super.layoutInit(inflater, savedInstanceSate);
		
		ActionBar actionBar = getActivity().getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(R.string.title_login);
		
		progressBar.setIndeterminate(true);
		
		WebSettings setting = webView.getSettings();
		setting.setJavaScriptEnabled(true);
		setting.setDomStorageEnabled(true); 
		setting.setAppCacheEnabled(true);
		setting.setDefaultTextEncodingName("utf-8") ;
		webView.setWebViewClient(new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, final String url) {
				view.loadUrl(url);
				
				return true;
			}

		});
		webView.setWebChromeClient(new WebChromeClient() {

			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				if (newProgress < 100) {
					progressBar.setVisibility(View.VISIBLE);
				} else if (newProgress == 100) {
					progressBar.setVisibility(View.GONE);
				}
				
				if (newProgress < 100) {
					
				} else if (newProgress == 100) {
					Logger.d(TAG, String.format("progress = 100 , url = %s", view.getUrl()));
					
					// 是否授权成功
					if (accountTask == null && webView.getUrl() != null && webView.getUrl().startsWith(SettingUtility.getStringSetting("callback_url"))) {
						Params params = ParamsUtil.deCodeUrl(webView.getUrl());
						String code = params.getParameter("code");
						
						Logger.d(TAG, "授权成功, code = " + code);
						
						accountTask = new AccountTask();
						accountTask.execute(code);
					}
				}
				
				if (webView.getUrl() != null && webView.getUrl().startsWith(SettingUtility.getStringSetting("callback_url"))) {
					webView.setVisibility(View.GONE);
				}
				super.onProgressChanged(view, newProgress);
			}

		});
	}
	
	@Override
	public void requestData() {
		super.requestData();
		
		webView.setVisibility(View.VISIBLE);
		// 请求授权
		SinaSDK.getInstance(null).doWebRequest(webView);
	}
	
	/**
	 * 1、加载授权信息
	 * 2、加载用户信息
	 * 
	 * @author wangdan
	 *
	 */
	class AccountTask extends WorkTask<String, Integer, WeiBoUser> {

		@Override
		protected void onPrepare() {
			super.onPrepare();
			
			ViewUtils.createNormalProgressDialog(getActivity(), getString(R.string.account_load_auth)).show();
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			
			if (getActivity() != null)
				ViewUtils.updateNormalProgressDialog(getString(values[0]));
		}
		
		@Override
		public WeiBoUser workInBackground(String... params) throws TaskException {
			// 1、请求授权
			AccessToken accessToken = SinaSDK.getInstance(null).getAccessToken(params[0]);
			AccountBean account = new AccountBean(); 
			account.set_token(accessToken.getToken());
			account.setSecret(accessToken.getSecret());
			
			// 2、加载用户信息
			publishProgress(R.string.account_load_userinfo);
			WeiBoUser user = SinaSDK.getInstance(accessToken).userShow(accessToken.getUid(), null);
			
			// 3、加载分组信息
			publishProgress(R.string.account_load_groups);
			Groups groups = SinaSDK.getInstance(accessToken).friendshipGroups();
			
			// 4、更新新账户到DB
			account.setUser(user);
			account.setGroups(groups);
			account.setUserId(user.getIdstr());
			// 2014-09-18 移除了所有账户信息，包括LoggedIn，所以需要调用AccountFragment.login()
			AccountDB.remove(account.getUserId());
			AccountDB.newAccount(account);
			
			if (AppContext.isLogedin() && 
					// 2014-09-18 仅更新登录用户数据
					AppContext.getUser().getIdstr().equals(user.getIdstr())) {
				AccountFragment.login(account, false);
			}
			
			return user;
		}
		
		@Override
		protected void onSuccess(WeiBoUser result) {
			super.onSuccess(result);
			
			Logger.d(TAG, "授权成功");
			
			BaiduAnalyzeUtils.onEvent("add_account", "添加授权账号");
			
			showMessage(R.string.account_auth_success);
			
			if (getActivity() != null) {
				getActivity().setResult(Activity.RESULT_OK);
				getActivity().finish();
			}
		}
		
		@Override
		protected void onFailure(TaskException exception) {
			super.onFailure(exception);
			
			if ("21324".equals(exception.getErrorCode()) && getActivity() != null) {
				new AlertDialog.Builder(getActivity()).setTitle(R.string.remind)
									.setMessage(R.string.account_illegal_app)
									.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int which) {
											getActivity().finish();
										}
									})
									.show();
			}
			else {
				showMessage(exception.getErrorMsg());
				
				if (getActivity() != null) 
					requestData();
			}
		}
		
		@Override
		protected void onFinished() {
			super.onFinished();
			
			ViewUtils.dismissNormalProgressDialog();
			
			accountTask = null;
		}
		
	}

}
