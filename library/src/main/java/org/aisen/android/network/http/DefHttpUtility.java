package org.aisen.android.network.http;

import android.net.Proxy;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import org.aisen.android.common.setting.Setting;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.network.biz.ABizLogic;
import org.aisen.android.network.task.TaskException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class DefHttpUtility implements IHttpUtility {

	private static final String TAG = DefHttpUtility.class.getSimpleName();

	@Override
	public <T> T doGet(HttpConfig config, Setting action, Params params, Class<T> responseCls) throws TaskException {
		// 是否有网络连接
		if (SystemUtils.getNetworkType() == SystemUtils.NetWorkType.none)
			throw new TaskException(TaskException.TaskError.noneNetwork.toString());

		String url = (config.baseUrl + action.getValue() + (params == null ? "" : "?" + ParamsUtil.encodeToURLParams(params))).replaceAll(" ", "");
		Logger.v(TAG, url);

		HttpGet httpGet = new HttpGet(url);
		configHttpHeader(httpGet, config);

		return executeClient(httpGet, responseCls);
	}

	@Override
	public <T> T doPost(HttpConfig config, Setting action, Params params, Class<T> responseCls, Object requestObj) throws TaskException {
		// 是否有网络连接
		if (SystemUtils.getNetworkType() == SystemUtils.NetWorkType.none)
			throw new TaskException(TaskException.TaskError.noneNetwork.toString());

		String url = (config.baseUrl + action.getValue() + (params == null ? "" : "?" + ParamsUtil.encodeToURLParams(params))).replaceAll(" ", "");
		Logger.v(TAG, url);

		HttpPost httpPost = new HttpPost(url);
		configHttpHeader(httpPost, config);

		if (requestObj != null) {
			String requestBodyStr = null;
			if (requestObj instanceof Params) {
				Params p = (Params) requestObj;
				requestBodyStr = ParamsUtil.encodeToURLParams(p);
			}
			else {
				requestBodyStr = JSON.toJSONString(requestObj);
			}
			
			ByteArrayEntity entity = new ByteArrayEntity(requestBodyStr.getBytes());
			httpPost.setEntity(entity);
		}

		return executeClient(httpPost, responseCls);
	}

	public <T> T uploadFile(HttpConfig config, Setting action, Params params, File file, Params headers, Class<T> responseClazz) throws TaskException {
//		PostMethod postMethod = new PostMethod((config.baseUrl + action.getValue() + (params == null ? "" : "?"
//				+ ParamsUtil.encodeToURLParams(params))).replaceAll(" ", ""));
//
//		StringPart sp = new StringPart(" TEXT ", " testValue ");
//		FilePart fp = null;
//		try {
//			fp = new FilePart("file", file.getName(), file);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
//
//		MultipartRequestEntity mrp = new MultipartRequestEntity(new Part[] { sp, fp }, postMethod.getParams());
//		postMethod.setRequestEntity(mrp);
//		postMethod.addRequestHeader("cookie", config.cookie);
//
//		if (headers != null)
//			for (String key : headers.getKeys())
//				postMethod.addRequestHeader(key, headers.getParameter(key));
//
//		// 执行postMethod
//		org.apache.commons.httpclient.HttpClient httpClient = new org.apache.commons.httpclient.HttpClient();
//		try {
//			httpClient.executeMethod(postMethod);
//			Logger.v(ABaseBizlogic.TAG, String.format("upload file's response body = %s", postMethod.getResponseBodyAsString()));
////			T result = new ObjectMapper().readValue(postMethod.getResponseBodyAsString(), responseClazz);
//			T result = JSON.parseObject(postMethod.getResponseBodyAsString(), responseClazz);
//			return result;
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		return null;
	}

	@SuppressWarnings("unchecked")
	private <T> T executeClient(HttpUriRequest request, Class<T> responseCls) throws TaskException {
		try {
			HttpClient httpClient = generateHttpClient();

			HttpResponse httpResponse = httpClient.execute(request);
			if (httpResponse.getStatusLine().getStatusCode() / 100 == 2) {
				String responseStr = readResponse(httpResponse);
				try {
					if (responseCls.getSimpleName().equals("String"))
						return (T) responseStr;
					
					return JSON.parseObject(responseStr, responseCls);
				} catch (Exception e) {
					e.printStackTrace();
					throw new TaskException(TaskException.TaskError.resultIllegal.toString());
				}
			} else {
				Logger.e(ABizLogic.TAG,
                        String.format("Access to the server error, statusCode = %d", httpResponse.getStatusLine().getStatusCode()));
				Logger.w(ABizLogic.TAG, readResponse(httpResponse));
				throw new TaskException(TaskException.TaskError.timeout.toString());
			}
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
			throw new TaskException(TaskException.TaskError.timeout.toString());
		} catch (ConnectTimeoutException e) {
			e.printStackTrace();
			throw new TaskException(TaskException.TaskError.timeout.toString());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			throw new TaskException(TaskException.TaskError.timeout.toString());
		} catch (UnknownHostException e) {
			e.printStackTrace();
			throw new TaskException(TaskException.TaskError.timeout.toString());
		} catch (IOException e) {
			e.printStackTrace();
			throw new TaskException(TaskException.TaskError.timeout.toString());
		}
	}

	private void configHttpHeader(HttpUriRequest request, HttpConfig config) {
		request.addHeader("Cookie", config.cookie);
		request.addHeader("Accept-Charset", "utf-8");
		if (!TextUtils.isEmpty(config.contentType))
			request.addHeader("Content-Type", config.contentType);
		else
			request.addHeader("Content-Type", "application/json");
	}

	private HttpClient generateHttpClient() {
		BasicHttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 8 * 1000);
		HttpConnectionParams.setSoTimeout(httpParameters, 8 * 1000);
		DefaultHttpClient client = new DefaultHttpClient(httpParameters);

		String host = Proxy.getDefaultHost();
		if (host != null) {
			int port = Proxy.getDefaultPort();
			client.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, new HttpHost(host, port));
		}
		return client;
	}

	private String readResponse(HttpResponse response) throws IllegalStateException, IOException {
		String result = "";
		HttpEntity entity = response.getEntity();
		InputStream inputStream = entity.getContent();

		ByteArrayOutputStream content = new ByteArrayOutputStream();

		int readBytes = 0;
		byte[] sBuffer = new byte[1024 * 8];
		while ((readBytes = inputStream.read(sBuffer)) != -1) {
			content.write(sBuffer, 0, readBytes);
		}
		result = new String(content.toByteArray());
		
		Logger.d(ABizLogic.TAG, String.format("response = %s", result));
		
		return result;
	}

}
