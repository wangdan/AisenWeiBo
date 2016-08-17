package org.aisen.weibo.sina.ui.activity.browser;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.aisen.android.common.utils.KeyGenerator;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.action.IAction;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.download.DownloadManager;
import org.aisen.download.DownloadMsg;
import org.aisen.download.DownloadProxy;
import org.aisen.download.IDownloadObserver;
import org.aisen.download.Request;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.service.VideoService;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.UrlBean;
import org.aisen.weibo.sina.sinasdk.bean.UrlsBean;
import org.aisen.weibo.sina.support.bean.VideoBean;
import org.aisen.weibo.sina.support.permissions.SdcardPermissionAction;
import org.aisen.weibo.sina.support.sqlit.SinaDB;
import org.aisen.weibo.sina.support.utils.UMengUtil;

import java.io.File;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class VideoPlayerActivity extends BaseActivity implements View.OnClickListener, IDownloadObserver {

    public static void launchByShort(Context from, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("videoshort://%s", url)));
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, from.getPackageName());
        from.startActivity(intent);
    }

    public static void launchByVideo(Context from, String url, VideoBean videoBean) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("videopath://%s", url)));
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, from.getPackageName());
        intent.putExtra("bean", videoBean);
        from.startActivity(intent);
    }

    private static final String TAG = "VideoBrower";

    @ViewInject(id = R.id.progressbar)
    ProgressBar progressBar;
    @ViewInject(id = R.id.layoutContent)
    View layoutContent;

    private VideoView mVideoPlayer;

    private Uri shortUri;
    private Uri videoUri;
    private VideoBean videoBean;
    private DownloadProxy mDownloadProxy = new DownloadProxy();
    private DownloadMsg mDownloadMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ui_video_player);

        mVideoPlayer = (VideoView) findViewById(R.id.videoView);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setTitle("");

        if (getResources().getConfiguration().orientation == 2) {
            getToolbar().setVisibility(View.GONE);
        }

        mPlayingWhenPaused = true;

        String action = getIntent().getAction();
        if (Intent.ACTION_VIEW.equalsIgnoreCase(action) && getIntent().getData() != null) {
            Uri uri = getIntent().getData();
            if ("videoshort".equals(uri.getScheme())) {
                playWithShort(uri.toString().replace("videoshort://", ""));
            }
            else if ("videopath".equals(uri.getScheme())) {
                VideoBean bean = (VideoBean) getIntent().getSerializableExtra("bean");

                playWithVideo(uri.toString().replace("videopath://", ""), bean);
            }
        }
        else {
            finish();
        }
    }

    MediaController mediaController;
    private void initPlayer(Uri uri) {
        videoUri = uri;

        mediaController = new MediaController(this);
        mediaController.setAnchorView(mVideoPlayer);
        mVideoPlayer.setMediaController(mediaController);
        mVideoPlayer.requestFocus();

        mVideoPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
//                if (mPlayingWhenPaused)
                mVideoPlayer.start();

                layoutContent.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);

                invalidateOptionsMenu();
            }
        });

        mVideoPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Logger.w(TAG, "onError what=%d, extra=%d", what, extra);
                String err = getResources().getString(R.string.toast_video_play_error, what);
                showMessage(err);
                return false;
            }
        });

        mVideoPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                Logger.w(TAG, "onCompletion");
                mVideoPlayer.seekTo(1);
            }
        });

        mVideoPlayer.setVideoURI(uri);

        invalidateOptionsMenu();

        mDownloadProxy.attach(this);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onBackPressed() {
        mVideoPlayer.stopPlayback();
        super.onBackPressed();
    }

    int mPositionWhenPaused = -1;
    boolean mPlayingWhenPaused = false;
    @Override
    protected void onPause() {
        super.onPause();
        if (mVideoPlayer.isPlaying()) {
            mVideoPlayer.pause();
            mPlayingWhenPaused = true;
        } else {
            mPlayingWhenPaused = false;
        }

        mPositionWhenPaused = mVideoPlayer.getCurrentPosition();

        UMengUtil.onPageEnd(this, "视频播放页");

        if (DownloadManager.getInstance() != null) {
            DownloadManager.getInstance().getController().unregister(mDownloadProxy);
        }
    }

    @Override
    protected int configTheme() {
        return R.style.AppTheme_Pics;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPositionWhenPaused >= 0) {
            mVideoPlayer.seekTo(mPositionWhenPaused);
            mPositionWhenPaused = -1;
        }

        UMengUtil.onPageStart(this, "视频播放页");

        if (DownloadManager.getInstance() != null) {
            DownloadManager.getInstance().getController().register(mDownloadProxy);
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mDownloadProxy.detach(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    final static String LAST_PLAYED_TIME = "last_play_position";
    final static String LAST_PLAYED_STATE = "last_play_state";
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(LAST_PLAYED_TIME, mPositionWhenPaused);
        outState.putBoolean(LAST_PLAYED_STATE, mPlayingWhenPaused);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mPositionWhenPaused = savedInstanceState.getInt(LAST_PLAYED_TIME);
        mPlayingWhenPaused = savedInstanceState.getBoolean(LAST_PLAYED_STATE);
    }

    private void playWithShort(final String url) {
        shortUri = Uri.parse(url);
        
        Logger.d(TAG, "short : " + url);

        new WorkTask<Void, Void, VideoBean>() {

            @Override
            protected void onPrepare() {
                super.onPrepare();

                layoutContent.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public VideoBean workInBackground(Void... params) throws TaskException {
                String id = KeyGenerator.generateMD5(url);
//                VideoBean videoBean = SinaDB.getDB().selectById(null, VideoBean.class, id);
//                if (videoBean != null && !TextUtils.isEmpty(videoBean.getImage()) && !TextUtils.isEmpty(videoBean.getVideoUrl())) {
//                    return videoBean;
//                }

                UrlsBean urlsBean = SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).urlShort2Long(url);
                if (urlsBean != null && urlsBean.getUrls() != null && urlsBean.getUrls().size() > 0) {
                    UrlBean urlBean = urlsBean.getUrls().get(0);

                    VideoBean videoBean = new VideoBean();
                    videoBean.setIdStr(KeyGenerator.generateMD5(url));
                    videoBean.setShortUrl(urlBean.getUrl_short());
                    videoBean.setLongUrl(urlBean.getUrl_long());
                    int repeat = 8;
                    while (repeat-- > 0) {
                        try {
                            if (VideoService.isSinaVideo(urlBean.getUrl_long())) {
                                videoBean.setType(VideoService.TYPE_VIDEO_SINA);

                                videoBean = VideoService.getVideoFromSinaVideo(videoBean);
                            }
                            else if (VideoService.isWeipai(urlBean.getUrl_long())) {
                                videoBean.setType(VideoService.TYPE_VIDEO_WEIPAI);

                                videoBean = VideoService.getVideoFromWeipai(videoBean);
                            }
                            else if (VideoService.isMeipai(urlBean.getUrl_long())) {
                                videoBean.setType(VideoService.TYPE_VIDEO_MEIPAI);

                                videoBean = VideoService.getVideoFromMeipai(videoBean);
                            }

                            repeat = 0;

                            if (!TextUtils.isEmpty(videoBean.getVideoUrl())) {
                                if (!TextUtils.isEmpty(videoBean.getImage())) {
                                    SinaDB.getDB().update(null, videoBean);
                                }

                                return videoBean;
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                }

                throw new TaskException("解析短链接失败");
            }

            @Override
            protected void onSuccess(VideoBean videoBean) {
                super.onSuccess(videoBean);

                playWithVideo(url, videoBean);
            }

            @Override
            protected void onFailure(TaskException exception) {
                super.onFailure(exception);

                if (isDestory())
                    return;

                new MaterialDialog.Builder(VideoPlayerActivity.this)
                        .forceStacking(true)
                        .content(R.string.video_short_faild)
                        .positiveText(R.string.video2browser)
                        .negativeText(R.string.video_again)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {

                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
                                toBrowser(url);
                            }

                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {

                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
                                playWithShort(url);
                            }

                        })
                        .show();
            }

        }.execute();
    }

    private void toBrowser(String url) {
        if (AppSettings.isInnerBrower()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("aisen://" + url));
            intent.putExtra(Browser.EXTRA_APPLICATION_ID, getPackageName());
            startActivity(intent);
        }
        else {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            intent.setData(Uri.parse(url));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        finish();
    }

    private void playWithVideo(String url, VideoBean bean) {
        Logger.d(TAG, "videopath : " + url);
        Logger.d(TAG, bean);

        videoBean = bean;

        layoutContent.setVisibility(View.INVISIBLE);

        mPlayingWhenPaused = true;
        initPlayer(Uri.parse(bean.getVideoUrl()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_video, menu);
        menu.findItem(R.id.download).setVisible(false);
        menu.findItem(R.id.progress).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    private boolean isRunning(int status) {
        switch (status) {
        case DownloadManager.STATUS_PENDING:
        case DownloadManager.STATUS_WAITING:
        case DownloadManager.STATUS_RUNNING:
            return true;
        default:
            return false;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (videoUri == null) {
            menu.findItem(R.id.download).setVisible(false);
            menu.findItem(R.id.progress).setVisible(false);
        }
        else {
            File file = getVideoFile();
            if ((file != null && file.exists())) {
                menu.findItem(R.id.download).setVisible(false);
                menu.findItem(R.id.progress).setVisible(false);
            }
            else {
                if (mDownloadMsg != null) {
                    MenuItem progressItem = menu.findItem(R.id.progress);
                    // 正在下载
                    if (isRunning(mDownloadMsg.getStatus())) {
                        progressItem.setVisible(true);

                        long total = mDownloadMsg.getTotal() == 0 ? 1 : mDownloadMsg.getTotal();
                        long progress = mDownloadMsg.getCurrent() == 0 ? 0 : mDownloadMsg.getCurrent();
                        String progressed = String.valueOf(Math.round(progress * 100.0f / total));
                        progressItem.setTitle(progressed + "%");
                    }


                }

                // 下载失败、未下载
                if (mDownloadMsg == null || mDownloadMsg.getStatus() == DownloadManager.STATUS_FAILED ||
                        mDownloadMsg.isNull()) {
                    menu.findItem(R.id.download).setVisible(true);
                }
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    private void download() {
        new IAction(this, new SdcardPermissionAction(this, null)) {

            @Override
            public void doAction() {
                File file = getVideoFile();
                Uri uri = Uri.parse(videoUri.toString());
                Request request = new Request(uri, Uri.fromFile(file));
                request.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setTitle(KeyGenerator.generateMD5(videoUri.toString()) + ".mp4");
                DownloadManager.getInstance().enqueue(request);
            }

        }.run();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.download) {
            if (mDownloadMsg == null ||
                    mDownloadMsg.isNull()) {
                download();
            }
            else if (mDownloadMsg.getStatus() == DownloadManager.STATUS_FAILED) {
                DownloadManager.getInstance().resume(mDownloadMsg.getKey());
            }

            invalidateOptionsMenu();
        }
        else if (item.getItemId() == R.id.browser) {
            try {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = videoUri;
                intent.setData(content_url);
                startActivity(intent);
            } catch (Exception e) {
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private File getVideoFile() {
        if (shortUri != null) {
            return new File(SystemUtils.getSdcardPath() + File.separator + AppSettings.getVideoSavePath() + File.separator + KeyGenerator.generateMD5(shortUri.toString()) + ".mp4");
        }

        return null;
    }

    @Override
    public Uri downloadURI() {
        return videoBean != null ? Uri.parse(videoBean.getVideoUrl()) : null;
    }

    @Override
    public Uri downloadFileURI() {
        return Uri.fromFile(getVideoFile());
    }

    @Override
    public void onPublish(DownloadMsg downloadMsg) {
        mDownloadMsg = downloadMsg;
        int status = downloadMsg.getStatus();

        if (downloadMsg.isNull()) {

        }
        // 失败
        else if (status == DownloadManager.STATUS_FAILED) {
            showMessage("下载视频失败");
        }
        // 成功
        else if (status == DownloadManager.STATUS_SUCCESSFUL) {
            showMessage(String.format(getString(R.string.msg_save_video_success), getVideoFile().getParentFile().getAbsolutePath()));

            if (getVideoFile() != null)
                SystemUtils.scanPhoto(this, getVideoFile());
        }
        // 暂停
        else if (status == DownloadManager.STATUS_PAUSED) {
        }
        // 等待
        else if (status == DownloadManager.STATUS_PENDING ||
                status == DownloadManager.STATUS_WAITING) {
        }
        // 下载中
        else if (status == DownloadManager.STATUS_RUNNING) {
        }

        invalidateOptionsMenu();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == 2) {
            getToolbar().setVisibility(View.GONE);
        }
        else {
            getToolbar().setVisibility(View.VISIBLE);
        }
    }

}
