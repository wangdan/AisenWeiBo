package org.aisen.weibo.sina.ui.fragment.settings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.aisen.android.common.setting.SettingUtility;
import org.aisen.android.ui.activity.container.FragmentArgs;
import org.aisen.weibo.sina.ui.activity.base.SinaCommonActivity;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.ui.activity.browser.BrowserActivity;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

/**
 * 关于Aisen微博
 * 
 * @author wangdan
 *
 */
public class AboutWebFragment extends ABaseFragment {

	public static void launchAbout(Activity from) {
		FragmentArgs args = new FragmentArgs();
		args.add("type", 0);

//		SinaCommonActivity.launch(from, AboutWebFragment.class, args);
		Intent intent = new Intent(from, BrowserActivity.class);
		intent.setAction(Intent.ACTION_VIEW);
		intent.putExtra("url", SettingUtility.getStringSetting("about_url"));
		from.startActivity(intent);
	}
	
	public static void launchHelp(Activity from) {
//		FragmentArgs args = new FragmentArgs();
//		args.add("type", 1);
//
//		SinaCommonActivity.launch(from, AboutWebFragment.class, args);

        Intent intent = new Intent(from, BrowserActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra("url", SettingUtility.getStringSetting("help_url"));
        from.startActivity(intent);
	}
	
	public static void launchOpensource(Activity from) {
		FragmentArgs args = new FragmentArgs();
		args.add("type", 2);

		SinaCommonActivity.launch(from, AboutWebFragment.class, args);
	}
	
	@ViewInject(id = R.id.webView)
	WebView webView;
	@ViewInject(id = R.id.progress)
	SmoothProgressBar progressbar;
	
	private int type;
	
	@Override
	public int inflateContentView() {
		return R.layout.ui_about_web;
	}
	
	@SuppressLint("SetJavaScriptEnabled") @Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		super.layoutInit(inflater, savedInstanceSate);
		
		type = savedInstanceSate == null ? getArguments().getInt("type") : savedInstanceSate.getInt("type");

        BaseActivity activity = (BaseActivity) getActivity();
		activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		activity.getSupportActionBar().setTitle(getPageTitle(type));
		
		progressbar.setVisibility(View.GONE);
		progressbar.setIndeterminate(true);
		
		WebSettings setting = webView.getSettings();
		setting.setJavaScriptEnabled(true);
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
					progressbar.setVisibility(View.VISIBLE);
				} else if (newProgress == 100) {
					progressbar.setVisibility(View.GONE);
				}
				progressbar.setProgress(newProgress);
				
				super.onProgressChanged(view, newProgress);
			}

		});
		setting.setJavaScriptCanOpenWindowsAutomatically(true);
		
		if (type == 0)
			webView.loadUrl(SettingUtility.getStringSetting("getSupportActionBar"));
//		else if (type == 1)
//			webView.loadUrl(AppSettings.getSettingExtra().getHelpURL());
		else if (type == 2)
			webView.loadUrl("file:///android_asset/licenses.html");
		
		if (type == 0 || type == 1)
			setHasOptionsMenu(true);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putInt("type", type);
	}

	@Override
	public boolean onHomeClick() {
		return super.onBackClick();
	}
	
	@Override
	public boolean onBackClick() {
		if (webView.canGoBack()) {
			webView.goBack();
			
			return true;
		}
		
		return super.onBackClick();
	}

	String getPageTitle(int type) {
		switch (type) {
		case 0:
			return getString(R.string.title_about);
//		case 1:
//			return getString(R.string.title_help);
		case 2:
			return getString(R.string.title_opensource);
		default:
			return getString(R.string.title_about);
		}
	}

}
