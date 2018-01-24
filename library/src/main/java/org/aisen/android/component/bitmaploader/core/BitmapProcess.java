/**
 * Copyright (c) 2012-2013, Michael Yang 杨福海 (www.yangfuhai.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.aisen.android.component.bitmaploader.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import org.aisen.android.common.utils.BitmapUtil;
import org.aisen.android.common.utils.BitmapUtil.BitmapType;
import org.aisen.android.common.utils.KeyGenerator;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.component.bitmaploader.BitmapLoader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public class BitmapProcess {
	private static final String TAG = "BitmapCache";

	private FileDisk compFielDisk;// 保存压缩或者缩放后的图片
	private FileDisk origFileDisk;// 保存原始下载

	public BitmapProcess(String imageCache) {
		compFielDisk = new FileDisk(imageCache + File.separator + "compression");
		origFileDisk = new FileDisk(imageCache + File.separator + "originate");
	}

	private byte[] getBitmapFromDiskCache(String url, String key, FileDisk fileDisk, ImageConfig config) throws Exception {
		InputStream inputStream = fileDisk.getInputStream(url, key);

		if (inputStream == null)
			return null;

		if (config.getProgress() != null)
			config.getProgress().sendLength(inputStream.available());

		byte[] buffer = new byte[8 * 1024];
		int readLen = -1;
		int readBytes = 0;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		while ((readLen = inputStream.read(buffer)) != -1) {
			readBytes += readLen;
			if (config.getProgress() != null)
				config.getProgress().sendProgress(readBytes);
			outputStream.write(buffer, 0, readLen);
		}
		return outputStream.toByteArray();
	}

	public File getOirgFile(String url) {
		String key = KeyGenerator.generateMD5(BitmapLoader.getKeyByConfig(url, null));

		return origFileDisk.getFile(url, key);
	}
	
	public File getCompressFile(String url, String imageId) {
		ImageConfig config = null;
		if (!TextUtils.isEmpty(imageId)) {
			config = new ImageConfig();
			config.setId(imageId);
		}
		String key = KeyGenerator.generateMD5(BitmapLoader.getKeyByConfig(url, config));

		return compFielDisk.getFile(url, key);
	}

	public void deleteFile(String url, ImageConfig config) {
		String key = KeyGenerator.generateMD5(BitmapLoader.getKeyByConfig(url, config));

		compFielDisk.deleteFile(url, key);
		origFileDisk.deleteFile(url, key);
	}

	/**
	 * 将数据写入原始缓存
	 * 
	 * @param bs
	 */
	public void writeBytesToOrigDisk(byte[] bs, String url) throws Exception {
		String key = KeyGenerator.generateMD5(BitmapLoader.getKeyByConfig(url, null));
		OutputStream out = origFileDisk.getOutputStream(url, key);

		ByteArrayInputStream in = new ByteArrayInputStream(bs);
		byte[] buffer = new byte[8 * 1024];
		int len = -1;
		while ((len = in.read(buffer)) != -1)
			out.write(buffer, 0, len);

		out.flush();
		in.close();
		out.close();
		origFileDisk.renameFile(url, key);
	}
	
	public void writeBytesToCompressDisk(String url, String key, byte[] bytes) throws Exception {
		OutputStream out = compFielDisk.getOutputStream(url, key);

		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		byte[] buffer = new byte[8 * 1024];
		int len = -1;
		while ((len = in.read(buffer)) != -1)
			out.write(buffer, 0, len);

		out.flush();
		in.close();
		out.close();
		compFielDisk.renameFile(url, key);
	}

	/**
	 * 从二级缓存获取位图数据
	 * 
	 * @param url
	 * @param config
	 * @return
	 * @throws Exception
	 */
	public byte[] getBitmapFromCompDiskCache(String url, ImageConfig config) throws Exception {
		String key = KeyGenerator.generateMD5(BitmapLoader.getKeyByConfig(url, config));

		return getBitmapFromDiskCache(url, key, compFielDisk, config);
	}

	/**
	 * 从原始缓存获取位图数据
	 * 
	 * @param url
	 * @param config
	 * @return
	 * @throws Exception
	 */
	public byte[] getBitmapFromOrigDiskCache(String url, ImageConfig config) throws Exception {
		String key = KeyGenerator.generateMD5(BitmapLoader.getKeyByConfig(url, null));

		return getBitmapFromDiskCache(url, key, origFileDisk, config);
	}

	/**
	 * 图片做二级缓存处理
	 * 
	 * @param bitmapBytes
	 * @param url
	 * @param config
	 * @return
	 * @throws Exception
	 */
	public MyBitmap compressBitmap(Context context, byte[] bitmapBytes, String url, int flag, ImageConfig config) throws Exception {
		boolean writeToComp = config.getCorner() > 0;

		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length, options);

		BitmapType bitmapType = BitmapUtil.getType(bitmapBytes);

		MyBitmap myBitmap = null;

		// 如果图片取自压缩目录，则不再对图片做压缩或者其他处理，直接返回
		if ((flag & 0x01) != 0) {
			myBitmap = new MyBitmap(BitmapDecoder.decodeSampledBitmapFromByte(context, bitmapBytes), url);
			return myBitmap;
		}

		// 判断是否需要压缩图片
		IBitmapCompress bitmapCompress = config.getBitmapCompress().newInstance();
		myBitmap = bitmapCompress.compress(bitmapBytes, getOirgFile(url), url, config, options.outWidth, options.outHeight);
		Bitmap bitmap = myBitmap.getBitmap();
		if (bitmap == null) {
			// 如果没压缩，就原始解析图片
			bitmap = BitmapDecoder.decodeSampledBitmapFromByte(context, bitmapBytes);
		} else {
			// 如果图片做了压缩处理，则需要写入二级缓存
			writeToComp = true;
		}

		// 对图片做圆角处理
		if (bitmapType != BitmapType.gif && config.getCorner() > 0) {
			bitmap = BitmapUtil.setImageCorner(bitmap, config.getCorner());
			bitmapType = BitmapType.png;
		}

		// GIF图片，进行压缩
		if (bitmapType == BitmapType.gif)
			writeToComp = true;

		// 当图片做了圆角、压缩处理后，将图片放置二级缓存
		if (writeToComp && config.isCompressCacheEnable()) {
			String key = KeyGenerator.generateMD5(BitmapLoader.getKeyByConfig(url, config));

			// PNG以外其他格式，都压缩成JPG格式
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			bitmap.compress(BitmapType.png == bitmapType ? CompressFormat.PNG : CompressFormat.JPEG, 100, out);
			byte[] bytes = out.toByteArray();
			writeBytesToCompressDisk(url, key, bytes);

			// 如果是GIF图片，无论如何，返回压缩格式图片
			if (bitmapType == BitmapType.gif) {
				Logger.v(TAG, String.format("parse gif image[url=%s,key=%s]", url, key));
				bitmap.recycle();
				bitmap = BitmapDecoder.decodeSampledBitmapFromByte(context, bytes);
			}
		}

		myBitmap.setBitmap(bitmap);
		return myBitmap;
	}

}
