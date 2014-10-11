package org.sina.android.http;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.security.KeyStore;

import org.aisen.weibo.sina.support.utils.AppSettings;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.sina.android.core.SinaErrorMsgUtil;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.m.common.params.Params;
import com.m.common.params.ParamsUtil;
import com.m.common.settings.Setting;
import com.m.common.utils.Logger;
import com.m.common.utils.SystemUtility;
import com.m.common.utils.SystemUtility.NetWorkType;
import com.m.support.bizlogic.ABaseBizlogic;
import com.m.support.network.HttpConfig;
import com.m.support.network.HttpUtility;
import com.m.support.task.TaskException;

public class HttpsUtility implements HttpUtility {

	private static final int connectTimeout = 8 * 1000;

	private static final int soTimeout = 8 * 1000;

	protected HttpClient getHttpsClient() throws TaskException {
		BasicHttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, connectTimeout);
		HttpConnectionParams.setSoTimeout(httpParameters, soTimeout);

		HttpClient client = null;
		try {
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(null, null);

			// 如果不设置这里，会报no peer certificate错误
			SSLSocketFactory sf = new MSSLSocketFactory(keyStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("https", sf, 443));
			schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
			SingleClientConnManager clientManager = new SingleClientConnManager(params, schemeRegistry);
			client = new DefaultHttpClient(clientManager, httpParameters);
		} catch (Exception e) {
			e.printStackTrace();
			throw new TaskException(TaskException.TaskError.timeout.toString());
		}

		HttpHost proxy = SystemUtility.getProxy();
		if (proxy != null)
			client.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);

		return client;
	}

	@Override
	public <T> T doGet(HttpConfig config, Setting action, Params params, Class<T> responseCls) throws TaskException {
		// 是否有网络连接
		if (SystemUtility.getNetworkType() == NetWorkType.none)
			throw new TaskException(TaskException.TaskError.noneNetwork.toString());

		String url = config.baseUrl + action.getValue();
		if (params != null && params.getVaules().size() > 0) {
			url += "?";
			url += ParamsUtil.encodeToURLParams(params);
		}

		Logger.i(ABaseBizlogic.TAG, String.format("url ---> %s", url));

		HttpGet httpGet = null;
		URL uRL = null;
		try {
			uRL = new URL(url);
			URI uri = new URI(uRL.getProtocol(), uRL.getHost(), uRL.getPath(), uRL.getQuery(), null);
			httpGet = new HttpGet(uri);
		} catch (Exception e) {
			e.printStackTrace();
			httpGet = new HttpGet(url);
		}

		configHttpHeader(httpGet, config);

		return executeClient(httpGet, responseCls);
	}

	@Override
	public <T> T doPost(HttpConfig config, Setting action, Params params, Class<T> responseCls, Object requestObj) throws TaskException {
		// 是否有网络连接
		if (SystemUtility.getNetworkType() == NetWorkType.none)
			throw new TaskException(TaskException.TaskError.noneNetwork.toString());

		String url = config.baseUrl + action.getValue();

		Logger.i(ABaseBizlogic.TAG, String.format("url ---> %s", url));

		HttpPost httpPost = new HttpPost(url);

		String postStringEntity = null;
		if (requestObj != null) {
			if (requestObj instanceof Params)
				postStringEntity = encodeNetParamsByContentType((Params) requestObj, config.contentType);
			else 
				postStringEntity = JSON.toJSONString(postStringEntity);
		}
		else if (params != null && params.size() != 0) {
			postStringEntity = encodeNetParamsByContentType(params, config.contentType);
		}
		Logger.d(ABaseBizlogic.TAG, String.format("post entity --->%s", postStringEntity));
		
		ByteArrayEntity entity;
		try {
			entity = new ByteArrayEntity(postStringEntity.getBytes());
			httpPost.setEntity(entity);
		} catch (Exception e) {
			e.printStackTrace();
			throw new TaskException(TaskException.TaskError.resultIllegal.toString());
		}

		configHttpHeader(httpPost, config);

		return executeClient(httpPost, responseCls);
	}

	public <T> T uploadFile(HttpConfig config, Setting action, Params params, File file, Params headers, Class<T> responseClazz) throws TaskException {
		try {
			PostMethod postMethod = new PostMethod((config.baseUrl + action.getValue()));

			FilePart fp = null;
			try {
				fp = new FilePart("pic", file.getName(), file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			postMethod.addRequestHeader("Authorization", config.authrization);

			int index = 0;
			Part[] part = new Part[params.size() + 1];
			for (String key : params.getKeys())
				part[index++] = new StringPart(key, ParamsUtil.encode(params.getParameter(key)));
			part[index] = fp;

			MultipartRequestEntity mrp = new MultipartRequestEntity(part, postMethod.getParams());
			postMethod.setRequestEntity(mrp);

			if (headers != null)
				for (String key : headers.getKeys())
					postMethod.addRequestHeader(key, ParamsUtil.encode(headers.getParameter(key)));

			// 执行postMethod
			org.apache.commons.httpclient.HttpClient httpClient = new org.apache.commons.httpclient.HttpClient();
			httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(connectTimeout);
			postMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, soTimeout);
			postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");
			httpClient.executeMethod(postMethod);
			Logger.v(ABaseBizlogic.TAG, String.format("upload file's response body = %s", postMethod.getResponseBodyAsString()));

			T result = null;
			try {
				result = JSON.parseObject(postMethod.getResponseBodyAsString(), responseClazz);
			} catch (Exception e) {
				e.printStackTrace();
				throw new TaskException(TaskException.TaskError.timeout.toString());
			}
			return result;

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

	@SuppressWarnings("deprecation")
	private <T> T executeClient(HttpUriRequest request, Class<T> responseCls) throws TaskException {
		try {
			HttpResponse httpResponse = getHttpsClient().execute(request);

			String responseStr = readResponse(httpResponse);
			
			if (!TextUtils.isEmpty(responseStr) && AppSettings.isTraditional()) {
				// 太耗时，取消这里的统一转换
//				long time = System.currentTimeMillis();
//				int length = responseStr.length();
//				ZHConverter converter = ZHConverter.getInstance(ZHConverter.TRADITIONAL);
//				responseStr = converter.convert(responseStr);
//				Logger.w(String.format("文字长度%s,简繁体转换耗时%sms", String.valueOf(length), String.valueOf(System.currentTimeMillis() - time)));
			}

			if (httpResponse.getStatusLine().getStatusCode() / 100 == 2) {
				T result = null;
				try {
					result = JSON.parseObject(responseStr, responseCls);
				} catch (Exception e) {
					e.printStackTrace();
					throw new TaskException(TaskException.TaskError.timeout.toString());
				}
				return result;
			} else {
				responseStr = URLDecoder.decode(responseStr);
				throw SinaErrorMsgUtil.transToException(responseStr);
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

	@SuppressWarnings("unused")
	private HttpClient getHttpClient() {
		BasicHttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, connectTimeout);
		HttpConnectionParams.setSoTimeout(httpParameters, soTimeout);
		HttpClient client = new DefaultHttpClient(httpParameters);

		HttpHost proxy = SystemUtility.getProxy();
		if (proxy != null)
			client.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);
		return client;
	}

	private void configHttpHeader(HttpUriRequest request, HttpConfig config) {
		if (!TextUtils.isEmpty(config.contentType))
			request.setHeader("Content-Type", config.contentType);
		if (!TextUtils.isEmpty(config.authrization))
			request.setHeader("Authorization", config.authrization);
	}

	private static String readResponse(HttpResponse response) throws IllegalStateException, IOException {
		String result = "";
		HttpEntity entity = response.getEntity();
		InputStream inputStream;

		inputStream = entity.getContent();
		ByteArrayOutputStream content = new ByteArrayOutputStream();

		int len = 0;
		byte[] sBuffer = new byte[256 * 1024];
		while ((len = inputStream.read(sBuffer)) != -1) {
			content.write(sBuffer, 0, len);
		}
		result = new String(content.toByteArray());

		content.close();
		
		Logger.v(ABaseBizlogic.TAG, result);
		return result;
	}

	/**
	 * 根据contentType类型组织网络请求参数列表
	 * 
	 * @param params
	 * @param contentType
	 * @return
	 */
	private static String encodeNetParamsByContentType(Params params, String contentType) {
		if ("application/x-www-form-urlencoded".equals(contentType)) {
			return ParamsUtil.encodeToURLParams(params);
		} else if ("application/json".equals(contentType)) {
			return ParamsUtil.encodeParamsToJson(params);
		}
		return null;
	}

	/**
	 * 将上传文件的参数放到post实体中
	 * <p>
	 * 发送一个文件，格式详情具体可以参照,在Key与value之间，必须\r\n\r\n，否则system error<br>
	 * <a href="http://www.w3.org/TR/html401/interact/forms.html#h-17.13.4.1">
	 * <p>
	 * <a href="http://yefeng.iteye.com/blog/315847">
	 * <p>
	 * 
	 * @param out
	 * @param params
	 */
	private static final String boundary = "4a5b6c7d8e9f";

	@SuppressWarnings("unused")
	private static void parseFileUploadParams(OutputStream out, Params params, byte[] file) throws IOException {
		StringBuffer buffer = new StringBuffer();

		for (String key : params.getKeys()) {
			buffer.append("--").append(boundary);
			buffer.append("\r\n");
			buffer.append("Content-Disposition: form-data; name=" + "\"" + key + "\"");
			buffer.append("\r\n\r\n");// 每一行必须以\r\n结尾,包括最后一行
			buffer.append(ParamsUtil.encode(params.getParameter(key)));
			buffer.append("\r\n");
		}

		String imageKey = "pic";
		if (params.containsKey("imageKey")) {
			imageKey = params.getParameter("imageKey");
		}

		buffer.append("--" + boundary);
		buffer.append("\r\n");
		buffer.append("Content-Disposition: form-data; name=").append("\"").append(imageKey).append("\"").append(";").append("filename=")
				.append("\"").append("lovesong.jpg").append("\"");
		buffer.append("Content-Type: " + "image/jpge");
		buffer.append("\r\n\r\n");
		out.write(buffer.toString().getBytes());
		// Logger.d(TAG, "form-data = " + buffer.toString());
		out.write(file);
		out.write(("\r\n--" + boundary + "--\r\n").getBytes());
	}

}
