package org.aisen.weibo.sina.ui.fragment.picture;

import java.io.File;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.utils.AppSettings;
import org.aisen.weibo.sina.ui.widget.PictureProgressView;
import org.android.loader.BitmapLoader;
import org.android.loader.core.BitmapDecoder;
import org.android.loader.core.ImageConfig;
import org.android.loader.download.DownloadProcess;
import org.sina.android.bean.PicUrls;

import uk.co.senab.photoview.PhotoView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Rect;
import android.opengl.GLES10;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ShareActionProvider;

import com.m.common.utils.BitmapUtil;
import com.m.common.utils.BitmapUtil.BitmapType;
import com.m.common.utils.FileUtility;
import com.m.common.utils.SystemUtility;
import com.m.common.utils.Utils;
import com.m.support.Inject.ViewInject;
import com.m.ui.fragment.ABaseFragment;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

/**
 * 部分代码参考自四次元
 * 
 * @author Jeff.Wang
 *
 * @date 2014年9月18日
 */
@SuppressLint("SdCardPath") public class PictureFragment extends ABaseFragment {

	public static ABaseFragment newInstance(PicUrls url) {
		PictureFragment fragment = new PictureFragment();
		
		Bundle args = new Bundle();
		args.putSerializable("url", url);
		fragment.setArguments(args);
		
		return fragment;
	}
	
	@ViewInject(idStr = "photoview")
	PhotoView photoView;
	@ViewInject(idStr = "webview")
	WebView mWebView;
	@ViewInject(idStr = "txtFailure", click = "loadPicture")
	View viewFailure;
	@ViewInject(id = R.id.viewProgress)
	PictureProgressView progressView;
	@ViewInject(id = R.id.progress)
	SmoothProgressBar progressBar;
	
	private PicUrls image;

	@Override
	protected int inflateContentView() {
		return R.layout.ui_picture;
	}
	
	@Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		super.layoutInit(inflater, savedInstanceSate);
		
		image = savedInstanceSate == null ? (PicUrls) getArguments().getSerializable("url")
										  : (PicUrls) savedInstanceSate.getSerializable("url");
		
		progressBar.setIndeterminate(true);
		
		loadPicture(viewFailure);
		
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putSerializable("url", image);
	}
	
	private String getImage() {
		return image.getThumbnail_pic().replace("thumbnail", "bmiddle");
	}
	
	void loadPicture(View v) {
		File file = BitmapLoader.getInstance().getCacheFile(getImage());
		if (!file.exists()) {
			progressBar.setVisibility(View.VISIBLE);			
			progressView.setVisibility(View.VISIBLE);
		}
//		try {
//			file.delete();
//		} catch (Exception e) {
//		}
		
		viewFailure.setVisibility(View.GONE);

		ImageView imgView = new ImageView(getActivity());
		
		ImageConfig config = new ImageConfig();
		config.setId("Picture");
		config.setProgress(new PictureDownloadProgress());
		BitmapLoader.getInstance().display(null, getImage(), imgView, config);
	}

	class PictureDownloadProgress extends DownloadProcess {

		private long length;

		@Override
		public void receiveLength(long length) {
			this.length = length;
		}

		@Override
		public void receiveProgress(long progressed) {
			if (getActivity() == null)
				return;
			
			if (progressBar.getVisibility() == View.VISIBLE)
				progressBar.setVisibility(View.GONE);
			progressView.setProgress(Math.round(progressed * 100.0f / length));
		}

		@Override
		public void prepareDownload(String url) {

		}

		@Override
		public void finishedDownload(byte[] bytes) {
			if (getActivity() == null)
				return;
			
			getActivity().invalidateOptionsMenu();
			progressView.setVisibility(View.INVISIBLE);
			
			Options opts = new Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);

			// gif
			if (BitmapUtil.getType(bytes) == BitmapType.gif) {
				readGifPicture(mWebView, bytes);
			}
			// 图片太大
			else if (opts.outWidth > 1024 || opts.outHeight > 1024) {
				readLargePicture(mWebView, bytes);
			}
			// 解析图片
			else {
				readPicture(bytes, photoView);
			}
			
			getActivity().invalidateOptionsMenu();
		}

		@Override
		public void downloadFailed(Exception e) {
			if (getActivity() == null)
				return;
			
			progressView.setVisibility(View.INVISIBLE);
			
			viewFailure.setVisibility(View.VISIBLE);
		}

	}
	
	@SuppressLint("SetJavaScriptEnabled")
	private void readGifPicture(final WebView webview, byte[] bytes) {
		File file = BitmapLoader.getInstance().getCacheFile(getImage());
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

		int picWidth = options.outWidth;
		int picHeight = options.outHeight;
		int availableWidth = SystemUtility.getScreenWidth() - getResources().getDimensionPixelOffset(R.dimen.normal_gif_webview_margin_left)
				- getResources().getDimensionPixelOffset(R.dimen.normal_gif_webview_margin_right);
		int availableHeight = getAppHeight(getActivity());

		int maxPossibleResizeHeight = availableWidth * availableHeight / picWidth;

		if (picWidth >= availableWidth || picHeight >= availableHeight || maxPossibleResizeHeight >= availableHeight) {
			readLargePicture(webview, bytes);
			return;
		}

		webview.getSettings().setJavaScriptEnabled(true);
		webview.getSettings().setUseWideViewPort(true);
		webview.getSettings().setLoadWithOverviewMode(true);
		webview.getSettings().setBuiltInZoomControls(false);
		webview.getSettings().setDisplayZoomControls(false);
		webview.getSettings().setSupportZoom(false);

		webview.setVerticalScrollBarEnabled(false);
		webview.setHorizontalScrollBarEnabled(false);

		String str1 = "file://" + file.getAbsolutePath().replace("/mnt/sdcard/", "/sdcard/");
		String str2 = "<html>\n<head>\n     <style>\n          html,body{background:#3b3b3b;margin:0;padding:0;}          *{-webkit-tap-highlight-color:rgba(0, 0, 0, 0);}\n     </style>\n     <script type=\"text/javascript\">\n     var imgUrl = \""
				+ str1
				+ "\";"
				+ "     var objImage = new Image();\n"
				+ "     var realWidth = 0;\n"
				+ "     var realHeight = 0;\n"
				+ "\n"
				+ "     function onLoad() {\n"
				+ "          objImage.onload = function() {\n"
				+ "               realWidth = objImage.width;\n"
				+ "               realHeight = objImage.height;\n"
				+ "\n"
				+ "               document.gagImg.src = imgUrl;\n"
				+ "               onResize();\n"
				+ "          }\n"
				+ "          objImage.src = imgUrl;\n"
				+ "     }\n"
				+ "\n"
				+ "     function onResize() {\n"
				+ "          var scale = 1;\n"
				+ "          var newWidth = document.gagImg.width;\n"
				+ "          if (realWidth > newWidth) {\n"
				+ "               scale = realWidth / newWidth;\n"
				+ "          } else {\n"
				+ "               scale = newWidth / realWidth;\n"
				+ "          }\n"
				+ "\n"
				+ "          hiddenHeight = Math.ceil(30 * scale);\n"
				+ "          document.getElementById('hiddenBar').style.height = hiddenHeight + \"px\";\n"
				+ "          document.getElementById('hiddenBar').style.marginTop = -hiddenHeight + \"px\";\n"
				+ "     }\n"
				+ "     </script>\n"
				+ "</head>\n"
				+ "<body onload=\"onLoad()\" onresize=\"onResize()\" onclick=\"Android.toggleOverlayDisplay();\">\n"
				+ "     <table style=\"width: 100%;height:100%;\">\n"
				+ "          <tr style=\"width: 100%;\">\n"
				+ "               <td valign=\"middle\" align=\"center\" style=\"width: 100%;\">\n"
				+ "                    <div style=\"display:block\">\n"
				+ "                         <img name=\"gagImg\" src=\"\" width=\"100%\" style=\"\" />\n"
				+ "                    </div>\n"
				+ "                    <div id=\"hiddenBar\" style=\"position:absolute; width: 100%; background: #3b3b3b;\"></div>\n"
				+ "               </td>\n" + "          </tr>\n" + "     </table>\n" + "</body>\n" + "</html>";
		webview.loadDataWithBaseURL("file:///android_asset/", str2, "text/html", "utf-8", null);

		webview.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				webview.setVisibility(View.VISIBLE);
			}
		}, 500);
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void readLargePicture(final WebView large, byte[] bytes) {
		File file = BitmapLoader.getInstance().getCacheFile(getImage());

		large.getSettings().setJavaScriptEnabled(true);
		large.getSettings().setUseWideViewPort(true);
		large.getSettings().setLoadWithOverviewMode(true);
		large.getSettings().setBuiltInZoomControls(true);
		large.getSettings().setDisplayZoomControls(false);

		large.setVerticalScrollBarEnabled(false);
		large.setHorizontalScrollBarEnabled(false);

		String str1 = "file://" + file.getAbsolutePath().replace("/mnt/sdcard/", "/sdcard/");
		String str2 = "<html>\n<head>\n     <style>\n          html,body{background:#3b3b3b;margin:0;padding:0;}          *{-webkit-tap-highlight-color:rgba(0, 0, 0, 0);}\n     </style>\n     <script type=\"text/javascript\">\n     var imgUrl = \""
				+ str1
				+ "\";"
				+ "     var objImage = new Image();\n"
				+ "     var realWidth = 0;\n"
				+ "     var realHeight = 0;\n"
				+ "\n"
				+ "     function onLoad() {\n"
				+ "          objImage.onload = function() {\n"
				+ "               realWidth = objImage.width;\n"
				+ "               realHeight = objImage.height;\n"
				+ "\n"
				+ "               document.gagImg.src = imgUrl;\n"
				+ "               onResize();\n"
				+ "          }\n"
				+ "          objImage.src = imgUrl;\n"
				+ "     }\n"
				+ "\n"
				+ "     function onResize() {\n"
				+ "          var scale = 1;\n"
				+ "          var newWidth = document.gagImg.width;\n"
				+ "          if (realWidth > newWidth) {\n"
				+ "               scale = realWidth / newWidth;\n"
				+ "          } else {\n"
				+ "               scale = newWidth / realWidth;\n"
				+ "          }\n"
				+ "\n"
				+ "          hiddenHeight = Math.ceil(30 * scale);\n"
				+ "          document.getElementById('hiddenBar').style.height = hiddenHeight + \"px\";\n"
				+ "          document.getElementById('hiddenBar').style.marginTop = -hiddenHeight + \"px\";\n"
				+ "     }\n"
				+ "     </script>\n"
				+ "</head>\n"
				+ "<body onload=\"onLoad()\" onresize=\"onResize()\" onclick=\"Android.toggleOverlayDisplay();\">\n"
				+ "     <table style=\"width: 100%;height:100%;\">\n"
				+ "          <tr style=\"width: 100%;\">\n"
				+ "               <td valign=\"middle\" align=\"center\" style=\"width: 100%;\">\n"
				+ "                    <div style=\"display:block\">\n"
				+ "                         <img name=\"gagImg\" src=\"\" width=\"100%\" style=\"\" />\n"
				+ "                    </div>\n"
				+ "                    <div id=\"hiddenBar\" style=\"position:absolute; width: 100%; background: #3b3b3b;\"></div>\n"
				+ "               </td>\n" + "          </tr>\n" + "     </table>\n" + "</body>\n" + "</html>";
		large.loadDataWithBaseURL("file:///android_asset/", str2, "text/html", "utf-8", null);

		large.setTag(new Object());
		large.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				large.setVisibility(View.VISIBLE);
			}
		}, 500);
	}

	private void readPicture(byte[] bytes, ImageView imageView) {
		Bitmap bitmap = BitmapDecoder.decodeSampledBitmapFromByte(bytes);
		imageView.setImageBitmap(bitmap);
		imageView.setVisibility(View.VISIBLE);
	}
	
	public static int getBitmapMaxWidthAndMaxHeight() {
		// 2014-08-26 最大高度改小一点
        int[] maxSizeArray = new int[1];
        GLES10.glGetIntegerv(GL10.GL_MAX_TEXTURE_SIZE, maxSizeArray, 0);

        if (maxSizeArray[0] == 0) {
            GLES10.glGetIntegerv(GL11.GL_MAX_TEXTURE_SIZE, maxSizeArray, 0);
        }
        
        if (maxSizeArray[0] > 0)
        	return maxSizeArray[0];
        
        return 1280;
    }
	
	public static int getAppHeight(Activity paramActivity) {
        Rect localRect = new Rect();
        paramActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
        return localRect.height();
    }
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.picture, menu);
		
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		File file = BitmapLoader.getInstance().getCacheFile(getImage());
		menu.findItem(R.id.savePicture).setVisible(file.exists());
		menu.findItem(R.id.share).setVisible(file.exists());
		
		Intent shareIntent = Utils.getShareIntent("", "", getImage());

		MenuItem shareItem = menu.findItem(R.id.share);
		ShareActionProvider shareProvider = (ShareActionProvider) shareItem.getActionProvider();
		shareProvider.setShareHistoryFileName("channe_share.xml");
		shareProvider.setShareIntent(shareIntent);
		
		super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// 下载
		if (item.getItemId() == R.id.savePicture) {
			downloadImage();
		}
		// 分享
		else if (item.getItemId() == R.id.share) {
			
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private void downloadImage() {
		File file = BitmapLoader.getInstance().getCacheFile(getImage());

		String path = SystemUtility.getSdcardPath() + File.separator + AppSettings.getImageSavePath() + File.separator + file.getName();
		File newFile = new File(path);
		if (!newFile.exists()) {
			if (!newFile.getParentFile().exists())
				newFile.getParentFile().mkdirs();
			try {
				FileUtility.copyFile(file, newFile);
			} catch (Exception e) {
			}
		}
		showMessage(R.string.msg_save_pic_success);
	}
	
}
