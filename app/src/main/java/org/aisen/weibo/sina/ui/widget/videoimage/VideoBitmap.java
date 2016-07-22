package org.aisen.weibo.sina.ui.widget.videoimage;

import android.graphics.Bitmap;

import org.aisen.android.component.bitmaploader.core.MyBitmap;
import org.aisen.weibo.sina.support.bean.VideoBean;

/**
 * Created by wangdan on 16/7/21.
 */
public class VideoBitmap extends MyBitmap {

    private VideoBean videoBean;

    public VideoBitmap(Bitmap bitmap, String url) {
        super(bitmap, url);
    }

    public VideoBitmap(Bitmap bitmap, String url, VideoBean videoBean) {
        super(bitmap, url);

        this.videoBean = videoBean;
    }

    public VideoBean getVideoBean() {
        return videoBean;
    }

    public void setVideoBean(VideoBean videoBean) {
        this.videoBean = videoBean;
    }

}
