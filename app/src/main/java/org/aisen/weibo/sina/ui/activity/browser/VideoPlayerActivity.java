package org.aisen.weibo.sina.ui.activity.browser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Browser;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.aisen.android.common.utils.KeyGenerator;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.SystemBarUtils;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
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

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private VideoView mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

            mControlsView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            getToolbar().setVisibility(View.VISIBLE);
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    private Uri videoUri;
    private VideoBean videoBean;
    private DownloadProxy mDownloadProxy = new DownloadProxy();
    private DownloadMsg mDownloadMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// 强制为横屏

//        if (this.getResources().getConfiguration().orientation ==
//                Configuration.ORIENTATION_LANDSCAPE) {

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);


//        }

        setContentView(R.layout.ui_video_player);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = (VideoView) findViewById(R.id.videoView);
        TextView tv = (TextView) findViewById(R.id.video_title_text);

        findViewById(R.id.layControlls).setPadding(0, 0, 0, SystemUtils.getNavigationBarHeight(this));

        tv.setVisibility(View.INVISIBLE);
//        tv.setText(getIntent().getStringExtra("title"));
        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    toggle();
                }

                return false;
            }
        });

        setSupportActionBar(getToolbar());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setTitle("");
        getToolbar().setVisibility(View.GONE);

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        tv.setOnTouchListener(mDelayHideTouchListener);
        SystemBarUtils.hideSystemBars(getWindow());

        mPlayingWhenPaused = true;
//        initPlayer(videoUri);

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

    private void stop() {
        if (mContentView.isPlaying())
            mContentView.stopPlayback();
    }

    private void exit() {
        finish();
    }

    MediaController mediaController;
    private void initPlayer(Uri uri) {
        videoUri = uri;

        mediaController = new MediaController(this);
        mediaController.setAnchorView(mContentView);
        mContentView.setMediaController(mediaController);
        mContentView.requestFocus();

        mContentView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Logger.w(TAG, "onPrepared");
//                if (mPlayingWhenPaused)
                mContentView.start();

                layoutContent.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        });

        mContentView.setOnErrorListener(new MediaPlayer.OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Logger.w(TAG, "onError what=%d, extra=%d", what, extra);
                String err = getResources().getString(R.string.toast_video_play_error, what);
                showMessage(err);
                return false;
            }
        });

        mContentView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                Logger.w(TAG, "onCompletion");
                mContentView.seekTo(1);
            }
        });

        mContentView.setVideoURI(uri);

        invalidateOptionsMenu();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        getToolbar().setVisibility(View.GONE);

        SystemBarUtils.hideSystemBars(getWindow());
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        mControlsView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
        mHideHandler.postDelayed(mHideRunnable, AUTO_HIDE_DELAY_MILLIS);
    }

    @SuppressLint("InlinedApi")
    private void showAlways() {
        if (mVisible) {
            mHideHandler.removeCallbacks(mHideRunnable);
            mHideHandler.removeCallbacks(mHidePart2Runnable);
            return;
        }
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        mControlsView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onBackPressed() {
        mContentView.stopPlayback();
        super.onBackPressed();
    }

    int mPositionWhenPaused = -1;
    boolean mPlayingWhenPaused = false;
    @Override
    protected void onPause() {
        super.onPause();
        if (mContentView.isPlaying()) {
            mContentView.pause();
            mPlayingWhenPaused = true;
        } else {
            mPlayingWhenPaused = false;
        }

        mPositionWhenPaused = mContentView.getCurrentPosition();

        UMengUtil.onPageEnd(this, "视频播放页");

        if (DownloadManager.getInstance() != null) {
            DownloadManager.getInstance().getController().unregister(mDownloadProxy);
        }
        mDownloadProxy.detach(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mPositionWhenPaused >= 0) {
            mContentView.seekTo(mPositionWhenPaused);
            mPositionWhenPaused = -1;
        }

        UMengUtil.onPageStart(this, "视频播放页");

        if (DownloadManager.getInstance() != null) {
            DownloadManager.getInstance().getController().register(mDownloadProxy);
        }
        mDownloadProxy.attach(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
                VideoBean videoBean = SinaDB.getDB().selectById(null, VideoBean.class, id);
                if (videoBean != null && !TextUtils.isEmpty(videoBean.getImage()) && !TextUtils.isEmpty(videoBean.getVideoUrl())) {
                    return videoBean;
                }

                UrlsBean urlsBean = SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).shortUrlExpand(url);
                if (urlsBean != null && urlsBean.getUrls() != null && urlsBean.getUrls().size() > 0) {
                    UrlBean urlBean = urlsBean.getUrls().get(0);

                    videoBean = new VideoBean();
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

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        File file = getVideoFile(videoUri);
        if (mDownloadMsg != null) {
            int status = mDownloadMsg.getStatus();
            // 失败
            if (status == DownloadManager.STATUS_FAILED) {
                menu.findItem(R.id.download).setVisible(true);
            }
            // 成功
            else if (status == DownloadManager.STATUS_SUCCESSFUL) {
                menu.findItem(R.id.download).setVisible(false);
            }
            // 暂停
            else if (status == DownloadManager.STATUS_PAUSED) {
                menu.findItem(R.id.download).setVisible(true);
            }
            // 等待
            else if (status == DownloadManager.STATUS_PENDING ||
                    status == DownloadManager.STATUS_WAITING) {
                menu.findItem(R.id.download).setVisible(false);
            }
            // 下载中
            else if (status == DownloadManager.STATUS_RUNNING) {
                menu.findItem(R.id.download).setVisible(false);
            }
        }
        else {
            menu.findItem(R.id.download).setVisible(file == null || !file.exists());
        }

        return super.onPrepareOptionsMenu(menu);
    }

    private void download() {
        File file = getVideoFile(videoUri);
        Uri uri = Uri.parse(videoUri.toString());
        Request request = new Request(uri, Uri.fromFile(file));
        request.setNotificationVisibility(Request.VISIBILITY_VISIBLE);
        request.setTitle(KeyGenerator.generateMD5(videoUri.toString()) + ".mp4");
        DownloadManager.getInstance().enqueue(request);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.download) {
            if (mDownloadMsg != null) {
                // 失败
                if (mDownloadMsg.getStatus() == DownloadManager.STATUS_FAILED) {
                    DownloadManager.getInstance().resume(mDownloadMsg.getKey());
                }
            }
            else {
                download();
            }

            invalidateOptionsMenu();
        }

        return super.onOptionsItemSelected(item);
    }

    private File getVideoFile(Uri uri) {
        if (uri != null) {
            return new File(SystemUtils.getSdcardPath() + File.separator + AppSettings.getVideoSavePath() + File.separator + KeyGenerator.generateMD5(uri.toString()) + ".mp4");
        }

        return null;
    }

    @Override
    public Uri downloadURI() {
        return videoBean != null ? Uri.parse(videoBean.getVideoUrl()) : null;
    }

    @Override
    public Uri downloadFileURI() {
        return Uri.fromFile(getVideoFile(videoUri));
    }

    @Override
    public void onPublish(DownloadMsg downloadMsg) {
        this.mDownloadMsg = downloadMsg;

        int status = downloadMsg.getStatus();

        // 失败
        if (status == DownloadManager.STATUS_FAILED) {
            showMessage("下载视频失败");
        }
        // 成功
        else if (status == DownloadManager.STATUS_SUCCESSFUL) {
            showMessage(String.format(getString(R.string.msg_save_video_success), getVideoFile(videoUri).getParentFile().getAbsolutePath()));
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
    }

}
