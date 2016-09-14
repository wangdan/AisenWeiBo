package org.aisen.android.component.bitmaploader.core;

import android.graphics.Bitmap;

import java.io.File;

public interface IBitmapCompress {

	/**
	 * 压缩图片
	 * 
	 * @param bitmapBytes
	 *            图片的byte数据流
	 * @param file
	 *            图片存储的文件
	 * @param url
	 *            图片加载地址
	 * @param config
	 *            加载图片设置
	 * @param origW
	 *            图片宽度
	 * @param origH
	 *            图片高度
	 * @return null-不压缩
	 * @throws Exception
	 */
	public MyBitmap compress(byte[] bitmapBytes, File file, String url, ImageConfig config, int origW, int origH) throws Exception;

}
