package org.aisen.android.component.bitmaploader.core;

import org.aisen.android.common.utils.KeyGenerator;
import org.aisen.android.component.bitmaploader.BitmapLoader;


public class BitmapCache {
	private LruMemoryCache<String, MyBitmap> mMemoryCache;

	public BitmapCache(int memCacheSize) {
		init(memCacheSize);
	}

	private void init(int memCacheSize) {
		mMemoryCache = new LruMemoryCache<String, MyBitmap>(memCacheSize) {
			@Override
			protected int sizeOf(String key, MyBitmap bitmap) {
				return BitmapCommonUtils.getBitmapSize(bitmap.getBitmap()) * 4;
			}
		};

	}

	public void addBitmapToMemCache(String url, ImageConfig config, MyBitmap bitmap) {
		if (url == null || bitmap == null) {
			return;
		}

		if (mMemoryCache != null) {
			mMemoryCache.put(KeyGenerator.generateMD5(BitmapLoader.getKeyByConfig(url, config)), bitmap);
		}
	}

	public MyBitmap getBitmapFromMemCache(String url, ImageConfig config) {
		if (mMemoryCache != null) {
			final MyBitmap memBitmap = mMemoryCache.get(KeyGenerator.generateMD5(BitmapLoader.getKeyByConfig(url, config)));
			if (memBitmap != null) {
				return memBitmap;
			}
		}
		return null;
	}

	public void clearMemCache() {
		if (mMemoryCache != null) {
			mMemoryCache.evictAll();
		}
	}

	public void clearMemHalfCache() {
		if (mMemoryCache != null) {
			mMemoryCache.evictHalf();
		}
	}
}
