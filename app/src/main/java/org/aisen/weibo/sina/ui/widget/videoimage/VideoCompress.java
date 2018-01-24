package org.aisen.weibo.sina.ui.widget.videoimage;

import org.aisen.android.common.utils.KeyGenerator;
import org.aisen.android.component.bitmaploader.core.BitmapCompress;
import org.aisen.android.component.bitmaploader.core.IBitmapCompress;
import org.aisen.android.component.bitmaploader.core.ImageConfig;
import org.aisen.android.component.bitmaploader.core.MyBitmap;
import org.aisen.weibo.sina.support.bean.VideoBean;
import org.aisen.weibo.sina.support.sqlit.SinaDB;

import java.io.File;

/**
 * Created by wangdan on 16/7/21.
 */
public class VideoCompress implements IBitmapCompress {

    @Override
    public MyBitmap compress(byte[] bitmapBytes, File file, String url, ImageConfig config, int origW, int origH) throws Exception {
        String id = KeyGenerator.generateMD5(url);

        VideoBean videoBean = SinaDB.getDB().selectById(null, VideoBean.class, id);
        if (videoBean != null) {
            MyBitmap myBitmap = new BitmapCompress().compress(bitmapBytes, file, url, config, origW, origH);
            if (myBitmap != null && myBitmap.getBitmap() != null) {
                return new VideoBitmap(myBitmap.getBitmap(), url, videoBean);
            }
        }

        return null;
    }

}
