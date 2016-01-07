package org.aisen.weibo.sina.support.compress;

import android.graphics.Bitmap;

import org.aisen.android.common.utils.BitmapUtil;
import org.aisen.android.component.bitmaploader.core.BitmapCompress;
import org.aisen.android.component.bitmaploader.core.ImageConfig;
import org.aisen.weibo.sina.ui.widget.TimelinePicsView;

import java.io.File;

public class TimelineThumbBitmapCompress extends BitmapCompress {

    public static final int maxHeight = 1000;
    public static final int cutWidth = 550;
    public static final int cutHeight = 900;

	@Override
	public Bitmap compress(byte[] bitmapBytes, File file, String url, ImageConfig config, int origW, int origH) throws Exception {
        boolean isGif = url.toLowerCase().endsWith("gif");

        if (config instanceof TimelinePicsView.TimelineImageConfig) {
            TimelinePicsView.TimelineImageConfig timelineImageConfig = (TimelinePicsView.TimelineImageConfig) config;

            if (timelineImageConfig.getSize() > 1) {
                if (isGif) {
                }
                else {
                    float maxRadio = 6 * 1.0f / 16;
                    // 图片的宽高比过小，不截gif图
                    if (origW * 1.0f / origH < maxRadio) {
//                        Logger.v(String.format("原始尺寸, width = %d, height = %d", origW, origH));

                        // 根据比例截取图片
                        int width = origW;
                        int height = width * (timelineImageConfig.getShowHeight() / timelineImageConfig.getShowWidth());

                        Bitmap bitmap = BitmapUtil.decodeRegion(bitmapBytes, width, height);
//                        if (bitmap != null)
//                            Logger.v(String.format("截取后的尺寸, width = %d, height = %d", bitmap.getWidth(), bitmap.getHeight()));
                        return bitmap;
                    }
                }
            }

        }

        // 高度比较高时，截图部分显示
        if (!isGif && origW <= 440 && origH > maxHeight) {
            float outHeight = origW * 1.0f * (cutHeight * 1.0f / cutWidth);
            return BitmapUtil.decodeRegion(bitmapBytes, origW, Math.round(outHeight));
        }

        Bitmap bitmap = super.compress(bitmapBytes, file, url, config, origW, origH);;
//        if (bitmap != null)
//            Logger.w(String.format("原始尺寸, width = %d, height = %d, 解析后, width = %d, height = %d", origW, origH, bitmap.getWidth(), bitmap.getHeight()));
        return bitmap;
    }

}
