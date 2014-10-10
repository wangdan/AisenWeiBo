package org.aisen.weibo.sina.ui.fragment.settings;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.utils.AppSettings;
import org.aisen.weibo.sina.support.utils.BaiduAnalyzeUtils;
import org.aisen.weibo.sina.ui.activity.common.FragmentContainerActivity;
import org.aisen.weibo.sina.ui.activity.publish.PublishActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.m.support.Inject.ViewInject;
import com.m.ui.fragment.ABaseFragment;
import com.m.ui.utils.FragmentArgs;

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

		FragmentContainerActivity.launch(from, AboutWebFragment.class, args);		
	}
	
	public static void launchHelp(Activity from) {
		FragmentArgs args = new FragmentArgs();
		args.add("type", 1);

		FragmentContainerActivity.launch(from, AboutWebFragment.class, args);
	}
	
	public static void launchOpensource(Activity from) {
		FragmentArgs args = new FragmentArgs();
		args.add("type", 2);

		FragmentContainerActivity.launch(from, AboutWebFragment.class, args);
	}
	
	@ViewInject(id = R.id.webView)
	WebView webView;
	@ViewInject(id = R.id.progress)
	SmoothProgressBar progressbar;
	
	private int type;
	
	@Override
	protected int inflateContentView() {
		return R.layout.ui_about_web;
	}
	
	@SuppressLint("SetJavaScriptEnabled") @Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		super.layoutInit(inflater, savedInstanceSate);
		
		type = savedInstanceSate == null ? getArguments().getInt("type") : savedInstanceSate.getInt("type");
		
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		getActivity().getActionBar().setTitle(getPageTitle(type));
		
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
			webView.loadUrl(AppSettings.getSettingExtra().getAboutURL());
		else if (type == 1)
			webView.loadUrl(AppSettings.getSettingExtra().getHelpURL());
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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		
		inflater.inflate(R.menu.share, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.share)
			PublishActivity.publishRecommend(getActivity());
		
		return super.onOptionsItemSelected(item);
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
	
	@Override
	public void onResume() {
		super.onResume();
		
		BaiduAnalyzeUtils.onPageStart(getPageTitle(type));
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		BaiduAnalyzeUtils.onPageEnd(getPageTitle(type));
	}
	
	String getPageTitle(int type) {
		switch (type) {
		case 0:
			return getString(R.string.title_about);
		case 1:
			return getString(R.string.title_help);
		case 2:
			return getString(R.string.title_opensource);
		default:
			return getString(R.string.title_about);
		}
	}

}
