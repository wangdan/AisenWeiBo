package org.aisen.android.component.bitmaploader.download;

import android.content.Context;

import org.aisen.android.component.bitmaploader.core.ImageConfig;
import org.aisen.android.network.task.TaskException;

public interface Downloader {

	public byte[] downloadBitmap(Context context, String url, ImageConfig config) throws Exception;

}
