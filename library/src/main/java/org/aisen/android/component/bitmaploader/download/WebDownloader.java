package org.aisen.android.component.bitmaploader.download;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.component.bitmaploader.core.ImageConfig;

public class WebDownloader implements Downloader {

	private DefaultHttpClient httpClient;

	public WebDownloader() {
		BasicHttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 10 * 1000);
		HttpConnectionParams.setSoTimeout(httpParameters, 20 * 1000);
		httpClient = new DefaultHttpClient(httpParameters);
		// 设置网络代理
		HttpHost proxy = SystemUtils.getProxy();
		if (proxy != null)
			httpClient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);
	}

	@Override
	public byte[] downloadBitmap(String url, ImageConfig config) throws Exception {
		Logger.v(url);

		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			DownloadProcess progress = config.getProgress();

			if (progress != null)
				progress.sendPrepareDownload(url);

			HttpGet httpGet = new HttpGet(url);
//			httpGet.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:27.0) Gecko/20100101 Firefox/27.0");

			HttpResponse response = httpClient.execute(httpGet);
			if (200 == response.getStatusLine().getStatusCode()) {
				// 图片大小
				int length = 0;
				try {
					Header header = response.getFirstHeader("Content-Length");
					length = Integer.parseInt(header.getValue());
				} catch (Exception e) {
				}

				if (progress != null) {
					progress.sendLength(length);
				}
				InputStream in = response.getEntity().getContent();

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
		return null;
	}

}
