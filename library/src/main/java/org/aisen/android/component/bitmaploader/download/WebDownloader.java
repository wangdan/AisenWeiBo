package org.aisen.android.component.bitmaploader.download;

import android.content.Context;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.component.bitmaploader.core.ImageConfig;
import org.aisen.android.network.task.TaskException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class WebDownloader implements Downloader {

	@Override
	public byte[] downloadBitmap(Context context, String url, ImageConfig config) throws Exception {
		Logger.v(url);

		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			DownloadProcess progress = config.getProgress();

			if (progress != null)
				progress.sendPrepareDownload(url);

			Request request = new Request.Builder().url(url).build();
//			httpGet.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:27.0) Gecko/20100101 Firefox/27.0");

			Response response = GlobalContext.getOkHttpClient().newCall(request).execute();
			if (!(response.code() == HttpURLConnection.HTTP_OK || response.code() == HttpURLConnection.HTTP_PARTIAL)) {
				throw new TaskException(String.valueOf(TaskException.TaskError.failIOError));
			}
			else {
				// 图片大小
				int length = 0;
				try {
					String header = response.header("Content-Length");
					length = Integer.parseInt(header);
				} catch (Exception e) {
				}

				if (progress != null) {
					progress.sendLength(length);
				}
				InputStream in = response.body().byteStream();

				// 获取图片数据
				byte[] buffer = new byte[1024 * 8];
				int readLen = -1;
				int readBytes = 0;
				while ((readLen = in.read(buffer)) != -1) {
					readBytes += readLen;
					if (progress != null)
						progress.sendProgress(readBytes);
					out.write(buffer, 0, readLen);
				}
				byte[] bs = out.toByteArray();

				// 如果图片没有下载完成，默认图片加载失败
				if (length != 0 && bs.length != length)
					return null;

				in.close();
				out.close();
				return bs;
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (config.getProgress() != null)
				config.getProgress().sendException(e);
			throw new Exception(e.getCause());
		}
	}

}
