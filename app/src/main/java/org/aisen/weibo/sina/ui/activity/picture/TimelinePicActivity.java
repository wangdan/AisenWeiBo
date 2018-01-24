package org.aisen.weibo.sina.ui.activity.picture;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Browser;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.aisen.android.common.utils.KeyGenerator;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.common.utils.ViewUtils;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.action.IAction;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.service.VideoService;
import org.aisen.weibo.sina.sinasdk.bean.PicUrls;
import org.aisen.weibo.sina.support.action.WebLoginAction;
import org.aisen.weibo.sina.support.bean.VideoBean;
import org.aisen.weibo.sina.support.sqlit.SinaDB;
import org.aisen.weibo.sina.support.utils.ThemeUtils;
import org.aisen.weibo.sina.support.utils.UMengUtil;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;
import org.aisen.weibo.sina.ui.fragment.picture.PictureFragment;

/**
 * Created by wangdan on 16/8/7.
 */
public class TimelinePicActivity extends BaseActivity {

    @ViewInject(id = R.id.layToolbar)
    ViewGroup layToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_timeline_pic);

        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        getToolbar().setBackgroundColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= 19) {
            layToolbar.setPadding(layToolbar.getPaddingLeft(),
                    layToolbar.getPaddingTop() + SystemUtils.getStatusBarHeight(this),
                    layToolbar.getPaddingRight(),
                    layToolbar.getPaddingBottom());
        }

        if (savedInstanceState == null) {
            BizFragment.createBizFragment(this);

            mHandler.sendEmptyMessageDelayed(1, 200);
        }
        else {
            finish();
        }
    }

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (BizFragment.createBizFragment(TimelinePicActivity.this).getActivity() == null) {
                mHandler.sendEmptyMessageDelayed(1, 200);
            }
            else {
                String url;
                String action = getIntent().getAction();
                if (Intent.ACTION_VIEW.equalsIgnoreCase(action) && getIntent().getData() != null) {
                    url = getIntent().getData().toString();
                } else {
                    url = getIntent().getStringExtra("url");
                }
                if (url.startsWith("timeline_pic://"))
                    url = url.replace("timeline_pic://", "");

                parseShortUrl(url);
            }
        }

    };

    private void parseShortUrl(final String url) {
        while (BizFragment.createBizFragment(this).getActivity() == null) {

        }

        // 检测Cookie
        new IAction(this, new WebLoginAction(this, BizFragment.createBizFragment(this))) {

            @Override
            public void doAction() {
                new WorkTask<Void, Integer, String>() {

                    @Override
                    protected void onProgressUpdate(Integer... values) {
                        super.onProgressUpdate(values);

                        if (values != null && values[0] == 100) {
                            ViewUtils.createProgressDialog(TimelinePicActivity.this, "正在解析图片", ThemeUtils.getThemeColor()).show();
                        }
                    }

                    @Override
                    public String workInBackground(Void... params) throws TaskException {
                        String id = KeyGenerator.generateMD5(url);

                        VideoBean videoBean = SinaDB.getDB().selectById(null, VideoBean.class, id);

                        if (videoBean == null) {
                            videoBean = new VideoBean();
                            videoBean.setIdStr(id);
                            videoBean.setShortUrl(url);
                        }

                        // 根据短链接解析
                        if (TextUtils.isEmpty(videoBean.getImage())) {
                            try {
                                publishProgress(100);

                                VideoService.getPicture(videoBean);
                            } catch (Exception e) {
                                throw new TaskException("");
                            }
                        }

                        if (TextUtils.isEmpty(videoBean.getImage())) {
                            throw new TaskException("");
                        }

                        SinaDB.getDB().update(null, videoBean);

                        return videoBean.getImage();
                    }

                    @Override
                    protected void onFailure(TaskException exception) {
                        super.onFailure(exception);

                        if (isDestory())
                            return;

                        new MaterialDialog.Builder(TimelinePicActivity.this)
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
                                        loadPicture(url);
                                    }

                                })
                                .show();
                    }

                    @Override
                    protected void onSuccess(String s) {
                        super.onSuccess(s);
                        if (isDestory())
                            return;

                        loadPicture(s);
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

    private void loadPicture(String pic) {
        PicUrls picUrls = new PicUrls();
        picUrls.setThumbnail_pic(pic.replace("large", "bmiddle").replace("small", "bmiddle"));
        ABaseFragment pictureFragment = PictureFragment.newInstance(picUrls);
        getFragmentManager().beginTransaction().replace(R.id.layContainer, pictureFragment, "PicFragment").commit();
    }

    @Override
    protected int configTheme() {
        return R.style.AppTheme_Pics;
    }

    @Override
    public void onResume() {
        super.onResume();

        UMengUtil.onPageStart(this, "图片预览页");
    }

    @Override
    public void onPause() {
        super.onPause();

        UMengUtil.onPageEnd(this, "图片预览页");
    }

}
