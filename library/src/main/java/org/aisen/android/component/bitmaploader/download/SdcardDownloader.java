package org.aisen.android.component.bitmaploader.download;

import android.content.Context;
import android.graphics.Bitmap;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.component.bitmaploader.core.BitmapDecoder;
import org.aisen.android.component.bitmaploader.core.ImageConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class SdcardDownloader implements Downloader {

	@Override
	public byte[] downloadBitmap(Context context, String url, ImageConfig config) throws Exception {
		try {
			File imgFile = new File(url);
			if (imgFile.exists()) {
				DownloadProcess process = config.getProgress();

				if (process != null)
					process.prepareDownload(url);

                // 如果图片需要压缩，直接解析成bitmap
                if (config.getMaxHeight() > 0 || config.getMaxWidth() > 0) {
                    Bitmap bitmap = BitmapDecoder.decodeSampledBitmapFromFile(imgFile.getAbsolutePath(), config.getMaxWidth(), config.getMaxHeight());

                    ByteArrayOutputStream out = new ByteArrayOutputStream();

                    boolean isPng = url.toLowerCase().endsWith("png") ? true : false;
                    bitmap.compress(isPng ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 100, out);
                    byte[] result = out.toByteArray();
                    out.close();

                    if (process != null) {
                        process.sendLength(result.length);
                        process.sendProgress(result.length);
                        process.sendFinishedDownload(result);
                    }

                    Logger.w("直接解析sd卡图片，压缩尺寸");

                    return result;
                }
                else {
                    InputStream in = new FileInputStream(new File(url));

                    if (process != null)
                        process.sendLength(in.available());

                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] buffer = new byte[8 * 1024];
                    int length = -1;
                    long readBytes = 0;
                    while ((length = in.read(buffer)) != -1) {
                        readBytes += length;
                        if (process != null)
                            process.sendProgress(readBytes);
                        out.write(buffer, 0, length);
                    }
                    out.flush();
                    byte[] result = out.toByteArray();
                    in.close();
                    out.close();

                    if (process != null)
                        process.sendFinishedDownload(result);

                    return result;
                }
			}
			
			if (config.getProgress() != null)
				config.getProgress().downloadFailed(null);
			throw new Exception("");
		} catch (Exception e) {
            e.printStackTrace();
			if(config.getProgress()!=null)
				config.getProgress().sendException(e);
			throw new Exception(e.getCause());
		}
	}

}
