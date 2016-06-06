package org.aisen.weibo.sina.ui.fragment.picture;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Rect;
import android.opengl.GLES10;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.ImageView;

import com.umeng.analytics.MobclickAgent;

import org.aisen.android.common.utils.BitmapUtil;
import org.aisen.android.common.utils.BitmapUtil.BitmapType;
import org.aisen.android.common.utils.FileUtils;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.common.utils.Utils;
import org.aisen.android.common.utils.ViewUtils;
import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.component.bitmaploader.core.BitmapDecoder;
import org.aisen.android.component.bitmaploader.core.ImageConfig;
import org.aisen.android.component.bitmaploader.download.DownloadProcess;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.action.IAction;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.android.ui.fragment.ATabsFragment;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.sinasdk.bean.PicUrls;
import org.aisen.weibo.sina.support.bean.PictureSize;
import org.aisen.weibo.sina.support.permissions.SdcardPermissionAction;
import org.aisen.weibo.sina.support.sdk.SDK;
import org.aisen.weibo.sina.support.sqlit.SinaDB;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.support.utils.ThemeUtils;
import org.aisen.weibo.sina.ui.activity.picture.PhotosActivity;
import org.aisen.weibo.sina.ui.activity.picture.PicsActivity;
import org.aisen.weibo.sina.ui.widget.PictureProgressView;

import java.io.File;
import java.text.DecimalFormat;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * 部分代码参考自四次元
 * 
 * @author Jeff.Wang
 *
 * @date 2014年9月18日
 */
@SuppressLint("SdCardPath") public class PictureFragment extends ABaseFragment implements ATabsFragment.ITabInitData {

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

	private PicUrls image;

    private File origFile = null;

    private PictureSize pictureSize;

    public enum PictureStatus {
        wait, downloading, success, faild
    }

    private PictureStatus mStatus;

	@Override
	public int inflateContentView() {
		return R.layout.ui_picture;
	}
	
	@Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		super.layoutInit(inflater, savedInstanceSate);

        mStatus = PictureStatus.wait;

		image = savedInstanceSate == null ? (PicUrls) getArguments().getSerializable("url")
										  : (PicUrls) savedInstanceSate.getSerializable("url");
		
        origFile = BitmapLoader.getInstance().getCacheFile(getOrigImage());

		photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {

			@Override
			public void onPhotoTap(View view, float v, float v1) {
				getActivity().finish();
			}

		});
		mWebView.addJavascriptInterface(new PictureJavaScriptInterface(), "picturejs");

		loadPicture(viewFailure);
		
		setHasOptionsMenu(true);
	}

	final class PictureJavaScriptInterface {

		public PictureJavaScriptInterface() {

		}

		@JavascriptInterface
		public void onClick() {
			getActivity().finish();
		}

	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putSerializable("url", image);
	}
	
	private String getImage() {
        if (AppSettings.isLoadOrigPic())
            return getOrigImage();

		return image.getThumbnail_pic().replace("thumbnail", "bmiddle");
	}

    private String getOrigImage() {
        return image.getThumbnail_pic().replace("thumbnail", "large");
    }
	
	void loadPicture(View v) {
        String url = null;// 下载路径

        ImageConfig config = new ImageConfig();

        File file = origFile;
        // 原图存在，就下载原图
        if (file.exists()) {
            url = getOrigImage();
        }
        // 原图不存在，就下载中图
        else {
            file = BitmapLoader.getInstance().getCacheFile(getImage());
            config.setId("Picture");

            url = getImage();
            pictureSize = SinaDB.getDB().selectById(null, PictureSize.class, getOrigImage());
            if (pictureSize == null)
                new LoadPictureSizeTask().execute();
        }
        if (!file.exists()) {
			progressView.setVisibility(View.VISIBLE);

            // 如果网络不是WIFI，且原图和中图都没缓存，那么只加载当前看的那个图片
            if (SystemUtils.NetWorkType.wifi != SystemUtils.getNetworkType(getActivity())) {
                if (getActivity() instanceof PicsActivity) {
                    PicsActivity picsActivity = (PicsActivity) getActivity();
                    if (!picsActivity.getCurrent().getThumbnail_pic().equalsIgnoreCase(image.getThumbnail_pic()))
                        return;
                }
                else if (getActivity() instanceof PhotosActivity) {
                    PhotosActivity picsActivity = (PhotosActivity) getActivity();
                    if (!picsActivity.getCurrent().getThumbnail_pic().equalsIgnoreCase(image.getThumbnail_pic()))
                        return;
                }
            }
		}
		
		viewFailure.setVisibility(View.GONE);

		ImageView imgView = new ImageView(getActivity());
        config.setProgress(new PictureDownloadProgress(file));
		BitmapLoader.getInstance().display(null, url, imgView, config);
	}

	class PictureDownloadProgress extends DownloadProcess {

		private long length;
        private File file;

        PictureDownloadProgress(File file) {
            this.file = file;
        }

		@Override
		public void receiveLength(long length) {
			this.length = length;
		}

		@Override
		public void receiveProgress(long progressed) {
			if (getActivity() == null)
				return;
			
			progressView.setProgress(Math.round(progressed * 100.0f / length));
		}

		@Override
		public void prepareDownload(String url) {
            mStatus = PictureStatus.downloading;
		}

		@Override
		public void finishedDownload(byte[] bytes) {
			onDownloadPicture(bytes, file);

            mStatus = PictureStatus.success;
		}

		@Override
		public void downloadFailed(Exception e) {
			if (getActivity() == null)
				return;

            mStatus = PictureStatus.faild;
			
			progressView.setVisibility(View.INVISIBLE);
			
			viewFailure.setVisibility(View.VISIBLE);
		}

	}

    private void onDownloadPicture(byte[] bytes, File file) {
        if (getActivity() == null)
            return;

        getActivity().invalidateOptionsMenu();
        progressView.setVisibility(View.INVISIBLE);

        Options opts = new Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);

        // gif
        if (BitmapUtil.getType(bytes) == BitmapType.gif) {
            readGifPicture(mWebView, bytes, file);
        }
        // 图片太大
        else if (opts.outWidth > 1024 || opts.outHeight > 1024) {
            readLargePicture(mWebView, file);
        }
        // 解析图片
        else {
            readPicture(bytes, file, photoView);
        }

        getActivity().invalidateOptionsMenu();
    }
	
	@SuppressLint("SetJavaScriptEnabled")
	private void readGifPicture(final WebView webview, byte[] bytes, File file) {
		Options options = new Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

		int picWidth = options.outWidth;
		int picHeight = options.outHeight;
		int availableWidth = SystemUtils.getScreenWidth(getActivity()) - getResources().getDimensionPixelOffset(R.dimen.normal_gif_webview_margin_left)
				- getResources().getDimensionPixelOffset(R.dimen.normal_gif_webview_margin_right);
		int availableHeight = getAppHeight(getActivity());

		int maxPossibleResizeHeight = availableWidth * availableHeight / picWidth;

		if (picWidth >= availableWidth || picHeight >= availableHeight || maxPossibleResizeHeight >= availableHeight) {
			readLargePicture(webview, file);
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
				+ "     function imgOnClick() {\n"
				+ "			window.picturejs.onClick();"
				+ "     }\n"
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
				+ "                         <img name=\"gagImg\" src=\"\" width=\"100%\" style=\"\" onclick=\"imgOnClick()\" />\n"
				+ "                    </div>\n"
				+ "                    <div id=\"hiddenBar\" style=\"position:absolute; width: 0%; background: #3b3b3b;\"></div>\n"
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
	private void readLargePicture(final WebView large, File file) {
        photoView.setImageDrawable(getResources().getDrawable(R.drawable.bg_timeline_loading));

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
				+ "     function imgOnClick() {\n"
				+ "			window.picturejs.onClick();"
				+ "     }\n"
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
				+ "                         <img name=\"gagImg\" src=\"\" width=\"100%\" style=\"\" onclick=\"imgOnClick()\" />\n"
				+ "                    </div>\n"
				+ "                    <div id=\"hiddenBar\" style=\"position:absolute; width: 0%; background: #3b3b3b;\"></div>\n"
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

	private void readPicture(byte[] bytes, File file, ImageView imageView) {
		try {
			Bitmap bitmap = BitmapDecoder.decodeSampledBitmapFromByte(getActivity(), bytes);
			imageView.setImageBitmap(bitmap);
			imageView.setVisibility(View.VISIBLE);
		} catch (OutOfMemoryError e) {
			readLargePicture(mWebView, file);
		}
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
		inflater.inflate(R.menu.menu_picture, menu);
		
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		if (getActivity() != null) {
			File file = BitmapLoader.getInstance().getCacheFile(getImage());
			menu.findItem(R.id.savePicture).setVisible(file.exists());
			menu.findItem(R.id.share).setVisible(file.exists());

			MenuItem origItem = menu.findItem(R.id.origPicture);
			if (file.exists())
				origItem.setVisible(!origFile.exists());
			else
				origItem.setVisible(false);

			if (origItem.isVisible()) {
				if (downloadOrigPicture != null) {
					String progressed = String.valueOf(Math.round(progress[0] * 100.0f / progress[1]));
					origItem.setTitle(String.format("%s(%s", getString(R.string.orig_pic), progressed) + "%)");
				}
				else {
					if (pictureSize != null) {
						String sizeStr;
						if (pictureSize.getSize() * 1.0f / 1024 / 1024 > 1)
							sizeStr = String.format("%s M", new DecimalFormat("#.00").format(pictureSize.getSize() * 1.0d / 1024 / 1024));
						else
							sizeStr = String.format("%d Kb", pictureSize.getSize() / 1024);
						origItem.setTitle(getString(R.string.orig_pic) +
								String.format("(%s)", sizeStr));
					}
					else {
						origItem.setTitle(R.string.orig_pic);
					}
				}
			}

//			Intent shareIntent = Utils.getShareIntent("", "", getImage());

//			MenuItem shareItem = menu.findItem(R.id.share);
//			ShareActionProvider shareProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
//			shareProvider.setShareHistoryFileName("channe_share.xml");
//			shareProvider.setShareIntent(shareIntent);
		}

		super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// 下载
		if (item.getItemId() == R.id.savePicture) {
			MobclickAgent.onEvent(getActivity(), "save_picture");

			downloadImage();
		}
		// 分享
		else if (item.getItemId() == R.id.share) {
			MobclickAgent.onEvent(getActivity(), "share_picture");

			Intent shareIntent = Utils.getShareIntent("", "", getImage());
			startActivity(shareIntent);
		}
		// 复制链接
		else if (item.getItemId() == R.id.copy) {
			AisenUtils.copyToClipboard(getImage());
			
			ViewUtils.showMessage(getActivity(), R.string.msg_image_copyed);
		}
        // 下载原图
        else if (item.getItemId() == R.id.origPicture) {
            if (downloadOrigPicture == null)
                new DownloadOrigPicture(origFile).execute();

            getActivity().invalidateOptionsMenu();
        }
        // 重新下载
        else if (item.getItemId() == R.id.downloadAgain) {
            origFile.delete();
            BitmapLoader.getInstance().getCacheFile(getImage()).delete();
            loadPicture(null);
        }
		
		return super.onOptionsItemSelected(item);
	}

	private void downloadImage() {
		new IAction(getActivity(), new SdcardPermissionAction((BaseActivity) getActivity(), null)) {

			@Override
			public void doAction() {
				new WorkTask<Void, Void, String>() {

					@Override
					protected void onPrepare() {
						super.onPrepare();

						ViewUtils.createProgressDialog(getActivity(), getString(R.string.msg_save_pic_loading), ThemeUtils.getThemeColor()).show();
					}

					private void notifyFileSys(File file) {
						SystemUtils.scanPhoto(getActivity(), file);
					}

					@Override
					public String workInBackground(Void... params) throws TaskException {
						File file = origFile;
						if (!file.exists())
							file = BitmapLoader.getInstance().getCacheFile(getImage());

						String path = SystemUtils.getSdcardPath() + File.separator + AppSettings.getImageSavePath() + File.separator + file.getName();
						File newFile = new File(path);
						if (!newFile.exists()) {
							if (!newFile.getParentFile().exists())
								newFile.getParentFile().mkdirs();
							try {
								FileUtils.copyFile(file, newFile);

								notifyFileSys(newFile);

								return newFile.getParentFile().getAbsolutePath();
							} catch (Exception e) {

							}
						}
						else {
							notifyFileSys(newFile);

							return newFile.getParentFile().getAbsolutePath();
						}
						return null;
					}

					@Override
					protected void onSuccess(String aBoolean) {
						super.onSuccess(aBoolean);

						if (!TextUtils.isEmpty(aBoolean)) {
							showMessage(String.format(getString(R.string.msg_save_pic_success), aBoolean));
						}
						else {
							showMessage(R.string.msg_save_pic_faild);
						}
					}

					@Override
					protected void onFinished() {
						super.onFinished();

						ViewUtils.dismissProgressDialog();
					}

				}.execute();
			}

		}.run();
	}

    DownloadOrigPicture downloadOrigPicture;
    class DownloadOrigPicture extends WorkTask<Void, Void, Void> {

        ImageConfig config = new ImageConfig();

        DownloadOrigPicture(File file) {
            downloadOrigPicture = this;
            config.setProgress(new OrigPictureProgress(file));
        }

        @Override
        public Void workInBackground(Void... params) throws TaskException {
            try {
                BitmapLoader.getInstance().doDownload(getOrigImage(), config);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onFinished() {
            super.onFinished();

            downloadOrigPicture = null;
        }
    }

    private long[] progress = new long[2];
    class OrigPictureProgress extends DownloadProcess {

        private File file;

        OrigPictureProgress(File file) {
            this.file = file;
        }

        @Override
        public void receiveProgress(long progressed) {
            super.receiveProgress(progressed);

            progress[0] = progressed;
            if (getActivity() != null)
                getActivity().invalidateOptionsMenu();
        }

        @Override
        public void prepareDownload(String url) {
            super.prepareDownload(url);

            progress = new long[2];
            if (getActivity() != null)
                getActivity().invalidateOptionsMenu();
        }

        @Override
        public void finishedDownload(byte[] bytes) {
            super.finishedDownload(bytes);

            if (getActivity() != null) {
                getActivity().invalidateOptionsMenu();

                onDownloadPicture(bytes, file);
            }
        }

        @Override
        public void downloadFailed(Exception e) {
            super.downloadFailed(e);

            if (getActivity() != null)
                showMessage(R.string.msg_save_orig_faild);
        }

        @Override
        public void receiveLength(long length) {
            super.receiveLength(length);

            progress[1] = length;
            if (getActivity() != null)
                getActivity().invalidateOptionsMenu();
        }

    }

    class LoadPictureSizeTask extends WorkTask<Void, Void, PictureSize> {

        @Override
        public PictureSize workInBackground(Void... params) throws TaskException {
            PictureSize size = SDK.newInstance().getPictureSize(getOrigImage());
            return size;
        }

        @Override
        protected void onSuccess(PictureSize pictureSize) {
            super.onSuccess(pictureSize);

            if (getActivity() != null)
                getActivity().invalidateOptionsMenu();
            PictureFragment.this.pictureSize = pictureSize;
        }
    }

    @Override
    public void onTabRequestData() {
        if (mStatus == PictureStatus.wait || mStatus == PictureStatus.faild) {
            loadPicture(viewFailure);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (downloadOrigPicture != null)
            downloadOrigPicture.cancel(true);
    }
}
