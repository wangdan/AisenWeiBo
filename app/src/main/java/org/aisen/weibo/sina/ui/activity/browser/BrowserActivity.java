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
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.VideoView;

import org.aisen.android.common.utils.KeyGenerator;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.Utils;
import org.aisen.android.component.orm.extra.Extra;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.support.bean.WeipaiVideoBean;
import org.aisen.weibo.sina.support.sqlit.SinaDB;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.support.utils.ThemeUtils;
import org.aisen.weibo.sina.support.utils.UMengUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

/**
 * 内置浏览器
 *
 * @author Jeff.Wang
 *
 * @date 2014年11月5日
 */
public class BrowserActivity extends BaseActivity {

	private final static String TAG = "AisenBrowser";

	@ViewInject(id = R.id.webview)
	WebView mWebView;
	@ViewInject(id = R.id.progress)
	SmoothProgressBar progressbar;
	@ViewInject(id = R.id.layVideo)
	View layVideo;
	@ViewInject(id = R.id.video)
	VideoView videoView;

	private boolean loadVideo;
	private String url;

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
		// 注入JS回调HTML源码
		mWebView.addJavascriptInterface(new LoadHtmlJavaScriptInterface(), "loadhtmljs");
		String js = "javascript:(function() { \n";
		js += "        window.onload = function() {\n";
		js += "            loadhtmljs.setHtml(document.getElementsByTagName('html')[0].innerHTML);\n";
		js += "        };\n";
		js += "    })()\n";

		mWebView.loadUrl(js);

		mWebView.setWebViewClient(new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, final String url) {
				if (!url.startsWith("http://") && !url.startsWith("https://"))
					view.loadUrl("http://" + url);
				else
					view.loadUrl(url);

				Logger.d(TAG, url);

				if (url.startsWith("http://www.miaopai.com") || url.startsWith(" http://m.miaopai.com")) {
					loadVideo = true;
				}

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
			String action = getIntent().getAction();
	        if (Intent.ACTION_VIEW.equalsIgnoreCase(action) && getIntent().getData() != null) {
	            url = getIntent().getData().toString();
	        } else {
	            url = getIntent().getStringExtra("url");
	        }
	        if (url.startsWith("aisen://"))
	        	url = url.replace("aisen://", "");

			WeipaiVideoBean bean = SinaDB.getDB().selectById(new Extra(null, "History"), WeipaiVideoBean.class, KeyGenerator.generateMD5(url));
			if (bean != null) {
				playVideo(bean);
			}
			else {
				mWebView.loadUrl(url);
			}
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

	final class LoadHtmlJavaScriptInterface {

		public LoadHtmlJavaScriptInterface() {
		}

		@JavascriptInterface
		public void setHtml(String html) {
			if (loadVideo) {
				new ParseHtmlTask().execute(html);
			}
		}

	}

	class ParseHtmlTask extends WorkTask<String, Void, WeipaiVideoBean> {

		@Override
		public WeipaiVideoBean workInBackground(String... params) throws TaskException {
			Document dom = Jsoup.parse(params[0]);

			WeipaiVideoBean bean = new WeipaiVideoBean();

			bean.setIdStr(KeyGenerator.generateMD5(url));
			bean.setUrl(url);

			Elements divs = dom.select("div[class=video_img WscaleH]");
			if (divs != null && divs.size() > 0) {
				bean.setImage(divs.get(0).attr("data-url"));
			}
			divs = dom.select("video#video");
			if (divs != null && divs.size() > 0) {
				bean.setVideoUrl(divs.get(0).attr("src"));
			}

			Logger.d(TAG, bean);

			SinaDB.getDB().insertOrReplace(new Extra(null, "History"), bean);

			return bean;
		}

		@Override
		protected void onSuccess(final WeipaiVideoBean weipaiVideoBean) {
			super.onSuccess(weipaiVideoBean);

			playVideo(weipaiVideoBean);
		}
	}

	private void playVideo(final WeipaiVideoBean weipaiVideoBean) {
		layVideo.setVisibility(View.VISIBLE);
		videoView.setVideoURI(Uri.parse(weipaiVideoBean.getVideoUrl()));
		videoView.start();
	}

}
