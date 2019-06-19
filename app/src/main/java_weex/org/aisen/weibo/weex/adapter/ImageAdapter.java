package org.aisen.weibo.weex.adapter;

import android.widget.ImageView;

import com.taobao.weex.adapter.IWXImgLoaderAdapter;
import com.taobao.weex.common.WXImageStrategy;
import com.taobao.weex.dom.WXImageQuality;

import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.weibo.sina.support.utils.ImageConfigUtils;

public class ImageAdapter implements IWXImgLoaderAdapter {

    @Override
    public void setImage(String url, ImageView imageView, WXImageQuality wxImageQuality, WXImageStrategy wxImageStrategy) {
        BitmapLoader.getInstance().display(null, url, imageView, ImageConfigUtils.getPhotoConfig());
    }

}
