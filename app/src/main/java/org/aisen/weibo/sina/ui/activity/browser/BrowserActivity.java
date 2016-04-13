package org.aisen.weibo.sina.ui.activity.browser;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.aisen.android.common.utils.Utils;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.support.utils.ThemeUtils;
import org.aisen.weibo.sina.support.utils.UMengUtil;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

/**
 * 内置浏览器
 *
 * @author Jeff.Wang
 *
 * @date 2014年11月5日
 */
public class BrowserActivity extends BaseActivity {

	private final static String TAG = "Browser";

	@ViewInject(id = R.id.webview)
	WebView mWebView;
	@ViewInject(id = R.id.progress)
	SmoothProgressBar progressbar;

	@SuppressLint("SetJavaScriptEnabled") @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_browser);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setTitle("");

		progressbar.setIndeterminate(true);

		WebSettings setting = mWebView.getSettings();
		setting.setJavaScriptEnabled(true);
		mWebView.setWebViewClient(new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, final String url) {
				if (!url.startsWith("http://") && !url.startsWith("https://"))
					view.loadUrl("http://" + url);
				else
					view.loadUrl(url);

//				if (url.startsWith("http://weibo.com/"))

				return true;
			}

		});
		mWebView.setWebChromeClient(new WebChromeClient() {

			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				if (newProgress < 100) {
					progressbar.setVisibility(View.VISIBLE);
				} else if (newProgress == 100) {
					progressbar.setVisibility(View.GONE);
					invalidateOptionsMenu();
				}
				progressbar.setProgress(newProgress);

				super.onProgressChanged(view, newProgress);
			}

		});
		setting.setJavaScriptCanOpenWindowsAutomatically(true);

		if (savedInstanceState == null) {
			String url = null;
			String action = getIntent().getAction();
	        if (Intent.ACTION_VIEW.equalsIgnoreCase(action) && getIntent().getData() != null) {
	            url = getIntent().getData().toString();
	        } else {
	            url = getIntent().getStringExtra("url");
	        }
	        if (url.startsWith("aisen://"))
	        	url = url.replace("aisen://", "");
	        mWebView.loadUrl(url);
        }
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.removeGroup(R.id.browser);
		getMenuInflater().inflate(R.menu.menu_browser, menu);

        String shareContent = String.format("%s %s ", mWebView.getTitle() + "", mWebView.getUrl() + "");
        Intent shareIntent = Utils.getShareIntent(shareContent, "", null);

        MenuItem shareItem = menu.findItem(R.id.share);
        ShareActionProvider shareProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        shareProvider.setShareHistoryFileName("channe_share.xml");
        shareProvider.setShareIntent(shareIntent);

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.refresh) {
			mWebView.reload();
		}
		else if (item.getItemId() == R.id.copy) {
			AisenUtils.copyToClipboard(mWebView.getUrl());

			showMessage(R.string.msg_url_copyed);
		}
		else if (item.getItemId() == R.id.to_browser) {
			try {
				Intent intent = new Intent();
				intent.setAction("android.intent.action.VIEW");
				Uri content_url = Uri.parse(mWebView.getUrl());
				intent.setData(content_url);
				startActivity(intent);
			} catch (Exception e) {
			}
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onHomeClick() {
		return super.onBackClick();
	}

	@Override
	public boolean onBackClick() {
		if (mWebView.canGoBack()) {
			mWebView.goBack();

			return true;
		}

		return super.onBackClick();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		mWebView.destroy();
	}

	@Override
	public void onResume() {
		super.onResume();

		UMengUtil.onPageStart(this, "内置浏览器页");
	}

	@Override
	public void onPause() {
		super.onPause();

		UMengUtil.onPageEnd(this, "内置浏览器页");
	}

    @Override
    protected int configTheme() {
        return ThemeUtils.themeArr[AppSettings.getThemeColor()][0];
    }

}
