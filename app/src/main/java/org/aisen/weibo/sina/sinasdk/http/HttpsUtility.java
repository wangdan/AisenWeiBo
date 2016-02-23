package org.aisen.weibo.sina.sinasdk.http;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.setting.Setting;
import org.aisen.android.common.setting.SettingUtility;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.network.biz.ABizLogic;
import org.aisen.android.network.http.HttpConfig;
import org.aisen.android.network.http.IHttpUtility;
import org.aisen.android.network.http.Params;
import org.aisen.android.network.http.ParamsUtil;
import org.aisen.android.network.task.TaskException;
import org.aisen.weibo.sina.service.OfflineService;
import org.aisen.weibo.sina.sinasdk.bean.BaseSinaBean;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpsUtility implements IHttpUtility {

	private static final String TAG = "Http-Https";

	private static OkHttpClient mOKHttpClient;

	@Override
	public <T> T doGet(HttpConfig config, Setting action, Params params, Class<T> responseCls) throws TaskException {
		Request.Builder builder = createRequestBuilder(config, action, params);

		String url = (config.baseUrl + action.getValue() + (params == null ? "" : "?" + ParamsUtil.encodeToURLParams(params))).replaceAll(" ", "");
		builder.url(url);

		Logger.v(TAG, "GET url = %s", url);

		Request request = builder.build();

		return executeRequest(request, responseCls);
	}

	@Override
	public <T> T doPost(HttpConfig config, Setting action, Params params, Class<T> responseCls, Object requestObj) throws TaskException {
		Request.Builder builder = createRequestBuilder(config, action, params);
		builder.url(config.baseUrl + action.getValue());

		Logger.v(TAG, "POST url = %s%s", config.baseUrl, action.getValue());

		if (requestObj != null) {
			RequestBody requestBody = null;

			String requestBodyStr = null;
			if (requestObj instanceof Params) {
				Params p = (Params) requestObj;
				requestBodyStr = ParamsUtil.encodeToURLParams(p);

				Logger.d(TAG, "ParamsBody = %s", requestBodyStr);

				requestBody = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded;charset=UTF-8"), requestBodyStr);
			}
			else {
				requestBodyStr = JSON.toJSONString(requestObj);

				Logger.d(TAG, "ParamsBody = %s", requestBodyStr);

				requestBody = RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), requestBodyStr);
			}

			builder.post(requestBody);
		}
		else if (params != null) {
			String requestBodyStr = ParamsUtil.encodeToURLParams(params);

			Logger.d(TAG, "ParamsBody = %s", requestBodyStr);

			RequestBody requestBody = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded;charset=UTF-8"), requestBodyStr);

			builder.post(requestBody);
		}

		return executeRequest(builder.build(), responseCls);
	}

	private Request.Builder createRequestBuilder(HttpConfig config, Setting action, Params params) throws TaskException {
		// 是否有网络连接
		if (SystemUtils.getNetworkType() == SystemUtils.NetWorkType.none)
			throw new TaskException(TaskException.TaskError.noneNetwork.toString());

		Request.Builder builder = new Request.Builder();

		// add Cookie
		if (!TextUtils.isEmpty(config.cookie)) {
			builder.header("Cookie", config.cookie);
		}
		// add header
		if (config.headerMap.size() > 0) {
			Set<String> keySet = config.headerMap.keySet();
			for (String key : keySet) {
				builder.addHeader(key, config.headerMap.get(key));
			}
		}

		return builder;
	}

	@Override
	public <T> T uploadFile(HttpConfig config, Setting action, Params params, MultipartFile[] files, Params headers, Class<T> responseClazz) throws TaskException {
		Request.Builder builder = createRequestBuilder(config, action, params);
		builder.url(config.baseUrl + action.getValue());

		Logger.v(TAG, "UploadFile url = %s%s", config.baseUrl, action.getValue());

		MultipartBuilder multipartBuilder = new MultipartBuilder();
		multipartBuilder.type(MultipartBuilder.FORM);

		// 处理参数
		if (params != null) {
			for (String key : params.getKeys()) {
				multipartBuilder.addFormDataPart(key, params.getParameter(key));
			}
		}

		// 处理文件数据
		if (files != null && files.length > 0) {
			for (MultipartFile file : files) {
				// 普通字节流
				if (file.getBytes() != null) {
					multipartBuilder.addFormDataPart(file.getKey(), file.getKey(), RequestBody.create(MediaType.parse("application/octet-stream"), file.getBytes()));
				}
				// 文件
				else if (file.getFile() != null) {
					multipartBuilder.addFormDataPart(file.getKey(), file.getFile().getName(), RequestBody.create(MediaType.parse(file.getContentType()), file.getFile()));
				}
			}

		}

		RequestBody requestBody = multipartBuilder.build();
		builder.post(requestBody);
		return executeRequest(builder.build(), responseClazz);
	}

	private <T> T executeRequest(Request request, Class<T> responseCls) throws TaskException {
		try {
			if (SettingUtility.getPermanentSettingAsInt("http_delay") > 0) {
				Thread.sleep(SettingUtility.getPermanentSettingAsInt("http_delay"));
			}
		} catch (Throwable e) {
		}

		try {
			Response response = getOkHttpClient().newCall(request).execute();

			if (!(response.code() == HttpURLConnection.HTTP_OK || response.code() == HttpURLConnection.HTTP_PARTIAL)) {
				Logger.e(ABizLogic.TAG, String.format("请求Http失败，状态码 : %d", response.code()));

				String responseStr = response.body().string();

				if (Logger.DEBUG) {
					Logger.w(ABizLogic.TAG, responseStr);
				}

				TaskException.checkResponse(responseStr);

				throw new TaskException(TaskException.TaskError.timeout.toString());
			} else {
				String responseStr = response.body().string();

				if (responseCls != null) {
					Logger.d(TAG, "Response class = %s", responseCls.getSimpleName());
				}
				Logger.v(TAG, "Response = %s", responseStr);

				try {
					if (responseCls.getSimpleName().equals("String"))
						return (T) responseStr;

					T result = JSON.parseObject(responseStr, responseCls);

					if (result instanceof OfflineService.OfflineLength) {
						OfflineService.OfflineLength iLength = (OfflineService.OfflineLength) result;
						iLength.setLength(responseStr.length());
					}

					if (result instanceof BaseSinaBean) {
						BaseSinaBean sinaBean = (BaseSinaBean) result;
						if (sinaBean.getError_code() > 0 && !TextUtils.isEmpty(sinaBean.getError())) {
							throw new TaskException(String.valueOf(sinaBean.getError_code()), sinaBean.getError());
						}
					}

					return result;
				} catch (Exception e) {
					Logger.printExc(HttpsUtility.class, e);

					if (e instanceof TaskException) {
						throw e;
					}

					throw new TaskException(TaskException.TaskError.resultIllegal.toString());
				}
			}
		} catch (SocketTimeoutException e) {
			Logger.printExc(HttpsUtility.class, e);

			throw new TaskException(TaskException.TaskError.timeout.toString());
		} catch (IOException e) {
			Logger.printExc(HttpsUtility.class, e);

			throw new TaskException(TaskException.TaskError.timeout.toString());
		}
	}

	public synchronized static OkHttpClient getOkHttpClient() {
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

}
