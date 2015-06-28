package org.aisen.android.component.bitmaploader.download;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.aisen.android.common.utils.BitmapUtil;
import org.aisen.android.component.bitmaploader.core.ImageConfig;

public class DrawableDownloader implements Downloader {

	@Override
	public byte[] downloadBitmap(String url, ImageConfig config) throws Exception {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			if (config.getProgress() != null)
				config.getProgress().sendPrepareDownload(url);

			InputStream in = null;

			try {
//			in = GlobalContext.getInstance().getResources().openRawResource(id)
				in = BitmapUtil.getFromDrawableAsStream(url);
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			}

			if (in == null)
				return null;

			if (config.getProgress() != null)
				config.getProgress().receiveLength(in.available());

			byte[] buffer = new byte[1024 * 128];
			int readLen = -1;
			int readBytes = 0;
			while ((readLen = in.read(buffer)) != -1) {
				readBytes += readLen;
				if (config.getProgress() != null)
					config.getProgress().sendProgress(readBytes);
				out.write(buffer, 0, readLen);
			}

			byte[] bs = out.toByteArray();
			in.close();
			out.close();
			return bs;
		} catch (Exception e) {
			if(config.getProgress()!=null)
				config.getProgress().sendException(e);
			throw new Exception(e.getCause());
		}
	}

}
