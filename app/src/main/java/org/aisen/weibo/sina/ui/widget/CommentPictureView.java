package org.aisen.weibo.sina.ui.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Browser;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.component.bitmaploader.core.ImageConfig;
import org.aisen.android.component.bitmaploader.core.MyBitmap;
import org.aisen.android.component.bitmaploader.display.DefaultDisplayer;
import org.aisen.weibo.sina.R;
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
        Uri uri = Uri.parse("timeline_pic://" + v.getTag());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, getContext().getPackageName());
        getContext().startActivity(intent);
    }

}
