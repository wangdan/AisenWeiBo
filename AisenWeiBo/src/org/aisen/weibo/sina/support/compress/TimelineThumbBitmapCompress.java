package org.aisen.weibo.sina.support.compress;

import java.io.File;

import org.android.loader.core.IBitmapCompress;
import org.android.loader.core.ImageConfig;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class TimelineThumbBitmapCompress implements IBitmapCompress {

	@Override
	public Bitmap compress(byte[] bitmapBytes, File file, String url, ImageConfig config, int origW, int origH) throws Exception {
		return BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
	}

}
