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

import org.aisen.android.common.utils.KeyGenerator;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.Utils;
import org.aisen.android.component.orm.extra.Extra;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.downloader.DownloadController;
import org.aisen.downloader.DownloadManager;
import org.aisen.downloader.IDownloadObserver;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.UrlsBean;
import org.aisen.weibo.sina.support.bean.VideoBean;
import org.aisen.weibo.sina.support.sqlit.SinaDB;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.support.utils.ThemeUtils;
import org.aisen.weibo.sina.support.utils.UMengUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

/**
 * 内置浏览器
 *
 * @author Jeff.Wang
 *
 * @date 2014年11月5日
 */
public class BrowserActivity extends BaseActivity implements IDownloadObserver {

	private final static String TAG = "AisenBrowser";

	@ViewInject(id = R.id.webview)
	WebView mWebView;
	@ViewInject(id = R.id.progress)
	SmoothProgressBar progressbar;

	private boolean loadVideo;
	private String url;
	private String directUrl = "";
	private VideoBean videoBean;

	@SuppressLint("SetJavaScriptEnabled") @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_browser);

		new WorkTask<Void, Void, Void>() {

			@Override
			public Void workInBackground(Void... params) throws TaskException {
				UrlsBean beans = SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).shortUrlExpand("http://t.cn/R5DqmqO", "http://t.cn/Rt7g8kQ");
				Logger.d(TAG, beans);
				return null;
			}

		}.execute();

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

				directUrl = url;

				Logger.d(TAG, directUrl);

				if (isWeipai() || isSinaVideo()) {
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

					Logger.w(TAG, "progress = 100, url = " + directUrl);

					String js = "javascript:(function() { \n";
					js += "            loadhtmljs.parseHtml(document.domain, document.title, document.URL, document.body.innerHTML);\n";
					js += "    })()\n";
					Logger.d(TAG, "load js");
					mWebView.loadUrl(js);
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

//			WeipaiVideoBean bean = SinaDB.getDB().selectById(new Extra(null, "History"), WeipaiVideoBean.class, KeyGenerator.generateMD5(url));
//			if (bean != null) {
//				videoBean = bean;
//
//				downloadVideo(videoBean);
//
//				preparePlayVideo();
//
//				videoSetup(bean);
//			}
//			else {
				mWebView.loadUrl(url);
//			}
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

	@Override
	public String downloadURI() {
		return videoBean == null ? "" : videoBean.getVideoUrl();
	}

	@Override
	public void onDownloadInit() {

	}

	@Override
	public void onDownloadChanged(DownloadController.DownloadStatus status) {

	}

	final class LoadHtmlJavaScriptInterface {

		public LoadHtmlJavaScriptInterface() {
		}

		@JavascriptInterface
		public void setHtml(String html) {
			Logger.v(TAG, "setHtml() : " + html);

			if (loadVideo) {
				new ParseHtmlTask().execute(html);
			}
		}

		@JavascriptInterface
		public void parseHtml(String domain, String title, String url, String html) {
			Logger.v(TAG, "domain = %s, title = %s, url = %s, html = %s", domain + "", title + "", url + "", html + "");
		}

	}

	private boolean isWeipai() {
		if (directUrl.startsWith("http://www.miaopai.com") ||
				directUrl.startsWith("http://m.miaopai.com")) {
			return true;
		}

		return false;
	}

	private boolean isSinaVideo() {
		if (directUrl.startsWith("http://video.weibo.com")) {
			return true;
		}

		return false;
	}

	class ParseHtmlTask extends WorkTask<String, Void, VideoBean> {

		@Override
		protected void onPrepare() {
			super.onPrepare();

			preparePlayVideo();
		}

		@Override
		public VideoBean workInBackground(String... params) throws TaskException {
			VideoBean bean = null;

			if (isWeipai()) {
				bean = parseWeipai(params[0]);
			}
			else if (isSinaVideo()) {
				bean = parseSinaVideo(params[0]);
			}

			if (bean != null) {
				Logger.d(TAG, bean);

				SinaDB.getDB().insertOrReplace(new Extra(null, "History"), bean);
			}

			return bean;
		}

		private VideoBean parseWeipai(String html) {
			Document dom = Jsoup.parse(html);

			VideoBean bean = new VideoBean();

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

			return bean;
		}

		private VideoBean parseSinaVideo(String html) {
			Document dom = Jsoup.parse(html);

			VideoBean bean = new VideoBean();

			bean.setIdStr(KeyGenerator.generateMD5(url));
			bean.setUrl(url);

			Elements divs = dom.select("video.video");
			if (divs != null && divs.size() > 0) {
				String src = divs.get(0).attr("src");
				src = src.replace("amp;", "");

				bean.setVideoUrl(src);
			}
			divs = dom.select("img.poster");
			if (divs != null && divs.size() > 0) {
				bean.setImage(divs.attr("src"));
			}

			return bean;
		}

		@Override
		protected void onSuccess(final VideoBean weipaiVideoBean) {
			super.onSuccess(weipaiVideoBean);

			if (weipaiVideoBean != null) {
				videoBean = weipaiVideoBean;

				videoSetup(weipaiVideoBean);
			}
			else {
				playError();
			}
		}
	}

	private void preparePlayVideo() {
//		layVideo.setVisibility(View.VISIBLE);
	}

	private void playError() {
//		layVideo.setVisibility(View.GONE);
	}

	private void videoSetup(final VideoBean weipaiVideoBean) {
		Logger.w(TAG, "setup url = " + weipaiVideoBean.getVideoUrl());

		mWebView.loadUrl(weipaiVideoBean.getVideoUrl());

		downloadVideo(weipaiVideoBean);

//		mWebView.destroy();

//		videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
//
//			@Override
//			public boolean onError(MediaPlayer mp, int what, int extra) {
//				SinaDB.getDB().deleteById(new Extra(null, "History"), WeipaiVideoBean.class, KeyGenerator.generateMD5(url));
//
//				mWebView.loadUrl(weipaiVideoBean.getVideoUrl());
//				layVideo.setVisibility(View.GONE);
//
//				return false;
//			}
//
//		});
//		videoView.setVideoURI(Uri.parse(weipaiVideoBean.getVideoUrl()));
//		videoView.start();
	}

	private void downloadVideo(VideoBean bean) {
//		if (true) return;

		Logger.d(TAG, "download video : " + bean.getVideoUrl());
		DownloadManager.Request request = new DownloadManager.Request(Uri.parse(bean.getVideoUrl()));
		// http://us.sinaimg.cn/002fMJ9hjx073lW7TzgI05040100115A0k01.mp4?KID=unistore,video&Expires=1469011590&ssig=DPCNzOndpE
//		DownloadManager.Request request = new DownloadManager.Request(Uri.parse("http://us.sinaimg.cn/002fMJ9hjx073lW7TzgI05040100115A0k01.mp4?KID=unistore,video&Expires=1469016944&ssig=GOC8a3RyZp"));
		request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
		request.setTitle("测试" + ".mp4");
		File file = new File(getExternalFilesDir("video") + File.separator + "测试视频下载" + ".mp4");
		Logger.d(TAG, "下载文件 ： " + file.getAbsolutePath());
		request.setDestinationUri(Uri.fromFile(file));
		DownloadManager.getInstance().enqueue(request);
	}

}
