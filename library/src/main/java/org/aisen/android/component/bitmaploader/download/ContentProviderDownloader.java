package org.aisen.android.component.bitmaploader.download;

import android.content.Context;
import android.net.Uri;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.FileUtils;
import org.aisen.android.component.bitmaploader.core.ImageConfig;

import java.io.InputStream;

public class ContentProviderDownloader implements Downloader {

	@Override
	public byte[] downloadBitmap(Context context, String url, ImageConfig config) throws Exception {
		
		try {
			InputStream is = context.getContentResolver().openInputStream(Uri.parse(url));
			byte[] datas = FileUtils.readStreamToBytes(is);
			return datas;
		} catch (Exception e) {
			if (config.getProgress() != null)
				config.getProgress().downloadFailed(e);
			e.printStackTrace();
			throw e;
		}
	}

}
