package org.aisen.weibo.sina.support.compress;

import java.io.File;

import org.android.loader.core.ImageConfig;

import android.graphics.Bitmap;

import com.m.common.utils.BitmapUtil;
import com.m.common.utils.Logger;
import com.m.common.utils.SystemUtility;

public class Timeline9ggBitmapCompress extends TimelineBitmapCompress {

	@Override
	public Bitmap compress(byte[] bitmapBytes, File file, String url, ImageConfig config, int origW, int origH) throws Exception {
		Logger.v("ATimeline", "压缩小图片");
		
		Bitmap bitmap = super.compress(bitmapBytes, file, url, config, origW, origH);

		int maxWidth = config.getMaxWidth() == 0 ? SystemUtility.getScreenWidth() : config.getMaxWidth();

		if (bitmap.getWidth() != maxWidth)
			bitmap = BitmapUtil.zoomBitmap(bitmap, maxWidth);
		
		return bitmap;
	}

}
