package org.aisen.weibo.sina.ui.activity.picture;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.umeng.analytics.MobclickAgent;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.FileUtils;
import org.aisen.android.common.utils.KeyGenerator;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.component.bitmaploader.core.ImageConfig;
import org.aisen.android.component.bitmaploader.display.DefaultDisplayer;
import org.aisen.android.component.bitmaploader.download.SdcardDownloader;
import org.aisen.android.support.action.IAction;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.permissions.SdcardPermissionAction;
import org.aisen.weibo.sina.support.utils.UMengUtil;
import org.aisen.weibo.sina.ui.fragment.secondgroups.WallpaperDownloadTask;
import org.aisen.weibo.sina.ui.widget.WallpaperViewer;
import org.aisen.weibo.sina.ui.widget.WaveView;
import org.aisen.weibo.sina.ui.widget.photoview.AttacherInterface;
import org.aisen.weibo.sina.ui.widget.photoview.PhotoView;

import java.io.File;

/**
 * 壁纸设置
 *
 * Created by wangdan on 16/3/23.
 */
public class WallpaperSettingActivity extends BaseActivity implements WallpaperViewer.WallpaperViewerLisenter, WallpaperDownloadTask.OnProgressCallback {

    public static void launch(Activity from, File file, String origURL) {
        if (!file.exists())
            return;

        Uri uri = Uri.fromFile(file);
        Intent intent = new Intent(from, WallpaperSettingActivity.class);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra("origURL", origURL);
        from.startActivity(intent);
    }

    @ViewInject(id = R.id.layWallpaperRoot)
    View layWallpaperRoot;
    @ViewInject(id = R.id.settingView)
    WaveView setting;
    @ViewInject(id = R.id.viewer)
    WallpaperViewer viewer;
    @ViewInject(id = R.id.photoview)
    PhotoView photoView;
    @ViewInject(id = R.id.viewFinish)
    View viewFinish;

    private File thumbFile;
    private File origFile;
    private String origURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_wallpaper_setting);

        viewer.init();
        viewer.setWallpaperViewerLisenter(this);

        setting.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!cancelIfRunning()) {
                    setWallpaper();
                }
            }

        });
        viewFinish.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }

        });

        setupPhotoView(savedInstanceState);

        findViewById(R.id.layNavigation).setPadding(0, 0, 0, SystemUtils.getNavigationBarHeight(this));
    }

    private void setupPhotoView(Bundle savedInstanceState) {
        photoView.setOnPhotoTapListener(new AttacherInterface.OnPhotoTapListener() {

            @Override
            public void onPhotoTap(View view, float x, float y) {
                finish();
            }

        });
        if (getIntent().getExtras().containsKey(Intent.EXTRA_STREAM)) {
            thumbFile = new File(getPath((Uri) getIntent().getParcelableExtra(Intent.EXTRA_STREAM)));
            origURL = getIntent().getExtras().getString("origURL");
            if (!TextUtils.isEmpty(origURL)) {
                Logger.d("Wallpaper", origURL);

                origFile = BitmapLoader.getInstance().getCacheFile(origURL);
                WallpaperDownloadTask.bindWallpaper(this, origURL, KeyGenerator.generateMD5(origURL), this);
            }
            if (getFile().exists()) {
                ImageConfig config = new ImageConfig();
                config.setLoadfaildRes(R.drawable.bg_timeline_loading);
                config.setLoadingRes(R.drawable.bg_timeline_loading);
                config.setDownloaderClass(SdcardDownloader.class);
                config.setDisplayer(new DefaultDisplayer());
                BitmapLoader.getInstance().display(null, getFile().getAbsolutePath(), photoView, config);

                return;
            }
        }
        finish();
    }

    private File getFile() {
        if (origFile != null && origFile.exists()) {
            return origFile;
        }

        return thumbFile;
    }

    private String getPath(Uri uri) {
        if (uri.toString().startsWith("content://")) {
            return FileUtils.getPath(GlobalContext.getInstance(), uri);
        }
        else {
            return uri.toString().replace("file://", "");
        }
    }

    @Override
    public void onWallpaperViewerScroll(float percent) {
        photoView.getAttacher().onMove(percent);
    }

    @Override
    public void onProgressUpdate(String image, long progress, long total, int flag) {
        boolean isSetting = (flag & 0x02) > 0;

        if (!TextUtils.isEmpty(image) && image.equals(origURL)) {
            // 下载完成
            if (total > 0 && progress > 0 && total == progress) {
                setWaveBackground(setting, true, R.drawable.ic_set_wallpaper_normal, 0);
            }
            // 正在下载
            else if (total > 0 && progress > 0) {
                int p = (int) (progress * 100 / total);

                setWaveBackground(setting, !isSetting, R.drawable.ic_set_wallpaper_normal, p);
            }
            // 等待下载
            else if (total < 1 && progress < 1) {
                setWaveBackground(setting, !isSetting, R.drawable.ic_set_wallpaper_normal, 0);
            }
        }
    }

    private void setWaveBackground(WaveView waveView, boolean setBk, int bkRes, int progress) {
        if (setBk) {
            waveView.setBackgroundResource(bkRes);
            waveView.setProgress0();
        } else {
            waveView.setBackground(null);
            if (progress == 0)
                waveView.setProgress0();
            else
                waveView.setProgress(progress);
        }
    }

    @Override
    public void onSetWallpaper(boolean success) {
        setWaveBackground(setting, true, R.drawable.ic_set_wallpaper_normal, 0);

        if (success) {
            ImageConfig config = new ImageConfig();
            config.setLoadfaildRes(R.drawable.bg_timeline_loading);
            config.setLoadingRes(R.drawable.bg_timeline_loading);
            config.setDownloaderClass(SdcardDownloader.class);
            config.setDisplayer(new DefaultDisplayer());
            BitmapLoader.getInstance().display(null, origFile.getAbsolutePath(), photoView, config);
        }
    }

    @Override
    public void showMessage(String image, String text) {
        showMessage(text);
    }

    @Override
    public void onCanceled(String image) {
        if (!TextUtils.isEmpty(image) && image.equals(origURL)) {
            setInit(image);
            showMessage(R.string.wallpaper_download_cancel);
        }
    }

    @Override
    public void setInit(String image) {
        if (!TextUtils.isEmpty(image) && image.equals(origURL)) {
            setting.setBackgroundResource(R.drawable.ic_set_wallpaper_normal);
            setting.setProgress0();
        }
    }

    private boolean cancelIfRunning() {
        if (setting.isRunning()) {
            WallpaperDownloadTask.cancelTask(this, origURL, KeyGenerator.generateMD5(origURL), this);

            return true;
        }

        return false;
    }

    private void setWallpaper() {
        new IAction(this, new SdcardPermissionAction(this, null)) {

            @Override
            public void doAction() {
                // 如果该壁纸已保存，直接弹框提示，然后再设置壁纸
                // 在idol3上设置时常耗时800ms以下，在idol5手机100ms以下
                final File file = WallpaperDownloadTask.getWallpaperSaveFile(origURL);
                if (file.exists()) {
                    MobclickAgent.onEvent(WallpaperSettingActivity.this, "wallpaper_setting");

                    showMessage(R.string.txt_set_wallpaper_suc);
                    // AsyncTask的execute()方法，默认会在队列执行
                    new AsyncTask<Void, Void, Boolean>() {

                        @Override
                        protected Boolean doInBackground(Void... params) {

                            try {
                                WallpaperDownloadTask.setWallpaper(WallpaperSettingActivity.this, file, null);
                            } catch (Throwable e) {
                                return false;
                            }

                            return true;
                        }

                        @Override
                        protected void onPostExecute(Boolean aBoolean) {
                            super.onPostExecute(aBoolean);

                            if (!aBoolean) {
                                showMessage(R.string.txt_set_wallpaper_fail);
                            }
                        }

                    }.execute();
                } else {
                    if (SystemUtils.getNetworkType(WallpaperSettingActivity.this) != SystemUtils.NetWorkType.none) {
                        WallpaperDownloadTask.settingWallpaper(WallpaperSettingActivity.this, origURL, KeyGenerator.generateMD5(origURL), WallpaperSettingActivity.this);
                    }else {
                        showMessage(R.string.txt_network_offline);
                    }

                }
            }

        }.run();
    }

    @Override
    protected int configTheme() {
        return R.style.AppTheme_Pics;
    }

    @Override
    protected void onResume() {
        super.onResume();

        UMengUtil.onPageStart(this, "壁纸预览页");
    }

    @Override
    protected void onPause() {
        super.onPause();

        UMengUtil.onPageEnd(this, "壁纸预览页");
    }

}
