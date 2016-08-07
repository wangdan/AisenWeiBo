package org.aisen.weibo.sina.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import org.aisen.android.common.utils.KeyGenerator;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.component.bitmaploader.core.ImageConfig;
import org.aisen.android.component.bitmaploader.core.MyBitmap;
import org.aisen.android.component.bitmaploader.display.DefaultDisplayer;
import org.aisen.android.support.action.IAction;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.sinasdk.bean.PicUrls;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.support.action.WebLoginAction;
import org.aisen.weibo.sina.support.bean.VideoBean;
import org.aisen.weibo.sina.support.sqlit.SinaDB;
import org.aisen.weibo.sina.ui.activity.picture.PicsActivity;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;
import org.aisen.weibo.sina.ui.widget.videoimage.PictureDownloader;

/**
 * Created by wangdan on 16/8/7.
 */
public class CommentPictureView extends ImageView implements View.OnClickListener {

    public CommentPictureView(Context context) {
        super(context);
    }

    public CommentPictureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CommentPictureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);

//        setLayoutParams(new RelativeLayout.LayoutParams(drawable.getBounds().right - drawable.getBounds().left,
//                                                            drawable.getBounds().bottom - drawable.getBounds().top));

        setOnClickListener(this);
    }

    public void display(String url) {
        setTag(url);
        ImageConfig config = new ImageConfig();
        config.setCompressCacheEnable(false);
        config.setDownloaderClass(PictureDownloader.class);
        config.setLoadingRes(R.drawable.bg_timeline_loading);
        config.setDisplayer(new DefaultDisplayer());

        MyBitmap myBitmap = BitmapLoader.getInstance().getMyBitmapFromMemory(url, config);
        // 内存缓存存在图片，且未释放
        if (myBitmap != null) {
            setImageDrawable(new BitmapDrawable(myBitmap.getBitmap()));
        }
        else {
            BitmapLoader.getInstance().display(null, url, this, config);
        }
    }

    @Override
    public void onClick(View v) {
        launch(getContext(), v.getTag().toString());
    }

    public static boolean launch(final Context context, String shortUrl) {
        String id = KeyGenerator.generateMD5(shortUrl);

        final VideoBean videoBean = SinaDB.getDB().selectById(null, VideoBean.class, id);

        if (videoBean == null) {
            return false;
        }

        if (videoBean.getImage() == null) {
            return false;
        }

        Activity from = BaseActivity.getRunningActivity();
        if (from == null && context instanceof Activity) {
            from = (Activity) context;
        }
        if (from != null) {
            final Activity from_ = from;
            new IAction(from, new WebLoginAction(from, BizFragment.createBizFragment(from))) {

                @Override
                public void doAction() {
                    StatusContent bean = new StatusContent();
                    bean.setPic_urls(new PicUrls[1]);
                    bean.getPic_urls()[0] = new PicUrls();
                    if (SystemUtils.getNetworkType(context) == SystemUtils.NetWorkType.wifi) {
                        bean.getPic_urls()[0].setThumbnail_pic(videoBean.getImage().replace("small", "bmiddle"));
                    }
                    else {
                        bean.getPic_urls()[0].setThumbnail_pic(videoBean.getImage().replace("small", "large"));
                    }
                    PicsActivity.launch(from_, bean, 0);
                }

            }.run();

            return true;
        }

        return false;
    }

}
