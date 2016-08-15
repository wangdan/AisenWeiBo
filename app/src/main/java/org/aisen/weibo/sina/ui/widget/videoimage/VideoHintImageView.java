package org.aisen.weibo.sina.ui.widget.videoimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.component.bitmaploader.core.BitmapOwner;
import org.aisen.android.component.bitmaploader.core.ImageConfig;
import org.aisen.android.component.bitmaploader.core.MyBitmap;
import org.aisen.android.component.bitmaploader.view.MyDrawable;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.sinasdk.bean.UrlBean;
import org.aisen.weibo.sina.support.bean.VideoBean;
import org.aisen.weibo.sina.ui.activity.browser.VideoPlayerActivity;

/**
 * Created by wangdan on 16/7/21.
 */
public class VideoHintImageView extends ImageView implements View.OnClickListener {

    private static Bitmap bitmapPlay;
    private Paint paint;

    private VideoBean videoBean;
    private UrlBean urlBean;
    private int minHeight;

    public VideoHintImageView(Context context) {
        super(context);
    }

    public VideoHintImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setup(attrs);
    }

    public VideoHintImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setup(attrs);
    }

    private void setup(AttributeSet attributeSet) {
        minHeight = getContext().getResources().getDimensionPixelSize(R.dimen.video_min_height);

        paint = new Paint();

        if (bitmapPlay == null) {
            bitmapPlay = BitmapFactory.decodeResource(getResources(), R.drawable.multimedia_videocard_play);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (getWidth() > 0 && getHeight() > 0) {
            canvas.drawBitmap(bitmapPlay, (getWidth() - bitmapPlay.getWidth()) / 2, (getHeight() - bitmapPlay.getHeight()) / 2, paint);
        }
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);

        MyDrawable myDrawable = null;
        if (drawable instanceof MyDrawable) {
            myDrawable = (MyDrawable) drawable;
        }
        else if (drawable instanceof TransitionDrawable) {
            myDrawable = (MyDrawable) ((TransitionDrawable) drawable).getDrawable(1);
        }

        if (myDrawable != null) {
            MyBitmap myBitmap = myDrawable.getMyBitmap();
            if (myBitmap instanceof VideoBitmap) {
                setOnClickListener(this);

                videoBean = ((VideoBitmap) myBitmap).getVideoBean();

                if (getWidth() > 0) {
                    int width = myBitmap.getBitmap().getWidth();
                    int height = myBitmap.getBitmap().getHeight();

                    float scale = getWidth() * 1.0f / width;
                    height = (int) (height * scale);
                    if (height > getWidth() * 4 / 5) {
                        height = getWidth() * 4 / 5;
                    }

                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) getLayoutParams();
                    if (params != null) {
                        params.height = height;
                    }
                    else {
                        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height);
                    }
                    setLayoutParams(params);
                }

                Logger.d(VideoHintImageView.class.getSimpleName(), videoBean);
            }
        }
    }

    @Override
    public void onClick(View v) {
//        if (videoBean == null || TextUtils.isEmpty(videoBean.getVideoUrl())) {
            VideoPlayerActivity.launchByShort(getContext(), urlBean.getUrl_short());
//        }
//        else {
//            VideoPlayerActivity.launchByVideo(getContext(), videoBean.getVideoUrl(), videoBean);
//        }
    }

    public void release() {
        videoBean = null;
    }

    public void display(BitmapOwner owner, UrlBean urlBean) {
        setOnClickListener(null);

        this.urlBean = urlBean;
        if (videoBean != null && !TextUtils.isEmpty(videoBean.getShortUrl()) && videoBean.getShortUrl().equals(urlBean.getUrl_short())) {
        }
        else {
            videoBean = null;
        }

        ImageConfig config = new ImageConfig();
        config.setCompressCacheEnable(false);
        config.setDownloaderClass(VideoDownloader.class);
        config.setBitmapCompress(VideoCompress.class);
        config.setLoadingRes(R.drawable.bg_timeline_loading);

        MyBitmap myBitmap = BitmapLoader.getInstance().getMyBitmapFromMemory(urlBean.getUrl_short(), config);
        // 内存缓存存在图片，且未释放
        if (myBitmap != null) {
            setImageDrawable(new MyDrawable(getResources(), myBitmap, config, null));
        }
        else {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) getLayoutParams();
            if (params != null && params.height != minHeight) {
                params.height = minHeight;
            }
            setLayoutParams(params);

            BitmapLoader.getInstance().display(owner, urlBean.getUrl_short(), this, config);
        }
    }

}
