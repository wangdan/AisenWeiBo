package org.aisen.weibo.sina.ui.activity.browser;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.text.TextUtils;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.aisen.android.common.utils.KeyGenerator;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.service.VideoService;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.UrlBean;
import org.aisen.weibo.sina.sinasdk.bean.UrlsBean;
import org.aisen.weibo.sina.support.bean.VideoBean;
import org.aisen.weibo.sina.support.sqlit.SinaDB;
import org.aisen.weibo.sina.support.utils.ThemeUtils;
import org.aisen.weibo.sina.support.utils.UMengUtil;

/**
 * Created by wangdan on 16/7/22.
 */
public class VideoBrowserActivity extends BaseActivity {

    private static final String TAG = "VideoBrower";

    @ViewInject(id = R.id.progressbar)
    ProgressBar progressBar;
    @ViewInject(id = R.id.videoView)
    VideoView videoView;
    @ViewInject(id = R.id.mediaController)
    MediaController mediaController;
    @ViewInject(id = R.id.layoutContent)
    View layoutContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ui_video);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setTitle("");

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

                new MaterialDialog.Builder(VideoBrowserActivity.this)
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

        layoutContent.setVisibility(View.VISIBLE);

        mediaController.setAnchorView(videoView);
        mediaController.setPrevNextListeners(null, null);
        videoView.setVideoURI(Uri.parse(bean.getVideoUrl()));
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                progressBar.setVisibility(View.GONE);
            }

        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {

            }

        });
        videoView.setMediaController(mediaController);
        videoView.start();
    }

    @Override
    public void onResume() {
        super.onResume();

        UMengUtil.onPageStart(this, "视频播放页");
    }

    @Override
    public void onPause() {
        super.onPause();

        UMengUtil.onPageEnd(this, "视频播放页");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (videoView.isPlaying())
            videoView.stopPlayback();
    }

    @Override
    protected int configTheme() {
        return ThemeUtils.themeArr[AppSettings.getThemeColor()][0];
    }



}
