package org.aisen.weibo.sina.sinasdk.http;

import android.text.TextUtils;

import com.squareup.okhttp.OkHttpClient;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.network.http.DefHttpUtility;
import org.aisen.android.network.task.TaskException;
import org.aisen.weibo.sina.service.OfflineService;
import org.aisen.weibo.sina.sinasdk.bean.BaseSinaBean;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpsUtility extends DefHttpUtility {

	private static OkHttpClient mOKHttpClient;

	public OkHttpClient getOkHttpClient() {
		if (mOKHttpClient == null) {
			try {
				mOKHttpClient = new OkHttpClient();
				mOKHttpClient.setConnectTimeout(GlobalContext.CONN_TIMEOUT, TimeUnit.MILLISECONDS);
				mOKHttpClient.setReadTimeout(GlobalContext.READ_TIMEOUT, TimeUnit.MILLISECONDS);

				TrustManager tm = new X509TrustManager() {

					public void checkClientTrusted(X509Certificate[] chain,
												   String authType) throws CertificateException {

					}

					public void checkServerTrusted(X509Certificate[] chain,
												   String authType) throws CertificateException {

					}

					public X509Certificate[] getAcceptedIssuers() {

						return null;

					}

				};

				SSLContext sslContext = SSLContext.getInstance("TLS");
				sslContext.init(null, new TrustManager[] { tm }, null);
				mOKHttpClient.setSslSocketFactory(sslContext.getSocketFactory());
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		return mOKHttpClient;
	}

	@Override
	protected <T> T parseResponse(String resultStr, Class<T> responseCls) throws TaskException {
		T result = super.parseResponse(resultStr, responseCls);

		if (result instanceof OfflineService.OfflineLength) {
			OfflineService.OfflineLength iLength = (OfflineService.OfflineLength) result;
			iLength.setLength(resultStr.length());
		}

		if (result instanceof BaseSinaBean) {
			BaseSinaBean sinaBean = (BaseSinaBean) result;
			if (sinaBean.getError_code() > 0 && !TextUtils.isEmpty(sinaBean.getError())) {
				throw new TaskException(String.valueOf(sinaBean.getError_code()), sinaBean.getError());
			}
		}
		return result;
	}

}
