package org.aisen.android.component.bitmaploader.download;

import org.aisen.android.component.bitmaploader.core.ImageConfig;
import org.aisen.android.network.task.TaskException;

public interface Downloader {

	public byte[] downloadBitmap(String url, ImageConfig config) throws Exception;

}
