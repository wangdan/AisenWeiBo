package org.aisen.weibo.sina.ui.widget.videoimage;

import android.content.Context;
import android.text.TextUtils;

import org.aisen.android.common.utils.KeyGenerator;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.component.bitmaploader.core.ImageConfig;
import org.aisen.android.component.bitmaploader.download.Downloader;
import org.aisen.android.component.bitmaploader.download.WebDownloader;
import org.aisen.weibo.sina.service.VideoService;
import org.aisen.weibo.sina.support.bean.VideoBean;
import org.aisen.weibo.sina.support.sqlit.SinaDB;

/**
 * Created by wangdan on 16/7/21.
 */
public class PictureDownloader implements Downloader {

    @Override
    public byte[] downloadBitmap(Context context, String url, ImageConfig config) throws Exception {
        WebDownloader webDownloader = new WebDownloader();

        String id = KeyGenerator.generateMD5(url);

        VideoBean videoBean = SinaDB.getDB().selectById(null, VideoBean.class, id);
        if (videoBean != null) {
            if (TextUtils.isEmpty(videoBean.getImage())) {
                int repeat = 8;
                while (repeat-- > 0) {
                    try {
                        VideoService.getPicture(videoBean);

                        Logger.d("VidewDownloader", "video = " + videoBean.getVideoUrl() + ", long = " + videoBean.getLongUrl() + ", short = " + videoBean.getShortUrl() + ", image = " + videoBean.getImage());

                        SinaDB.getDB().update(null, videoBean);

                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Logger.d(videoBean);
            }

            if (!TextUtils.isEmpty(videoBean.getImage())) {
                return webDownloader.downloadBitmap(context, videoBean.getImage().replaceAll("large", "thumbnail"), config);
            }
        }

        return null;
    }

}
