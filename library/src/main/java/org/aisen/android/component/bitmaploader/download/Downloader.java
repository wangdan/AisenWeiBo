package org.aisen.android.component.bitmaploader.download;

import org.aisen.android.component.bitmaploader.core.ImageConfig;

public interface Downloader {

	public byte[] downloadBitmap(String url, ImageConfig config) throws Exception;

}
