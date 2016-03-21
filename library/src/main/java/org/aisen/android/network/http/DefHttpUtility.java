package org.aisen.android.network.http;

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
import org.aisen.android.network.task.TaskException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.util.Set;

public class DefHttpUtility implements IHttpUtility {

	static String getTag(Setting action, String append) {
		return ABizLogic.getTag(action, append);
	}

	@Override
	public <T> T doGet(HttpConfig config, Setting action, Params urlParams, Class<T> responseCls) throws TaskException {
		Request.Builder builder = createRequestBuilder(config, action, urlParams, "Get");

		Request request = builder.build();

		return executeRequest(request, responseCls, action, "Get");
	}

	@Override
	public <T> T doPost(HttpConfig config, Setting action, Params urlParams, Params bodyParams, Object requestObj, Class<T> responseCls) throws TaskException {
		Request.Builder builder = createRequestBuilder(config, action, urlParams, "Post");

		if (bodyParams != null) {
			Params p = (Params) requestObj;
			String requestBodyStr = ParamsUtil.encodeToURLParams(p);

			Logger.d(getTag(action, "Post"), requestBodyStr);

			builder.post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded;charset=UTF-8"), requestBodyStr));
		}
		else if (requestObj != null) {
			String requestBodyStr = JSON.toJSONString(requestObj);

			Logger.d(getTag(action, "Post"), requestBodyStr);

			builder.post(RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), requestBodyStr));
		}

		return executeRequest(builder.build(), responseCls, action, "Post");
	}

	@Override
	public <T> T doPostFiles(HttpConfig config, Setting action, Params urlParams, Params bodyParams, MultipartFile[] files, Class<T> responseCls) throws TaskException {
		String method = "doPostFiles";

		Request.Builder builder = createRequestBuilder(config, action, urlParams, method);

		MultipartBuilder multipartBuilder = new MultipartBuilder();
		multipartBuilder.type(MultipartBuilder.FORM);

		// 处理Body参数
		if (bodyParams != null && bodyParams.getKeys().size() > 0) {
			for (String key : bodyParams.getKeys()) {
				String value = bodyParams.getParameter(key);
				multipartBuilder.addFormDataPart(key, value);

				Logger.v(getTag(action, method), "BodyParam[%s, %s]", key, value);
			}
		}

		// 处理文件数据
		if (files != null && files.length > 0) {
			for (MultipartFile file : files) {
				// 普通字节流
				if (file.getBytes() != null) {
					multipartBuilder.addFormDataPart(file.getKey(), file.getKey(), RequestBody.create(MediaType.parse("application/octet-stream"), file.getBytes()));

					Logger.v(getTag(action, method), "Multipart bytes, length = " + file.getBytes().length);
				}
				// 文件
				else if (file.getFile() != null) {
					multipartBuilder.addFormDataPart(file.getKey(), file.getFile().getName(), RequestBody.create(MediaType.parse(file.getContentType()), file.getFile()));

					Logger.v(getTag(action, method), "Multipart file, name = %s, path = %s", file.getFile().getName(), file.getFile().getAbsolutePath());
				}
			}

		}

		RequestBody requestBody = multipartBuilder.build();
		builder.post(requestBody);
		return executeRequest(builder.build(), responseCls, action, method);
	}

	private Request.Builder createRequestBuilder(HttpConfig config, Setting action, Params urlParams, String method) throws TaskException {
		// 是否有网络连接
		if (SystemUtils.getNetworkType() == SystemUtils.NetWorkType.none) {
			Logger.w(getTag(action, method), "没有网络连接");

			throw new TaskException(TaskException.TaskError.noneNetwork.toString());
		}

		String url = (config.baseUrl + action.getValue() + (urlParams == null ? "" : "?" + ParamsUtil.encodeToURLParams(urlParams))).replaceAll(" ", "");
		Logger.v(getTag(action, method), url);

		Request.Builder builder = new Request.Builder();
		builder.url(url);

		// add Cookie
		if (!TextUtils.isEmpty(config.cookie)) {
			builder.header("Cookie", config.cookie);

			Logger.v(getTag(action, method), "Cookie = " + config.cookie);
		}
		// add header
		if (config.headerMap.size() > 0) {
			Set<String> keySet = config.headerMap.keySet();
			for (String key : keySet) {
				builder.addHeader(key, config.headerMap.get(key));

				Logger.v(getTag(action, method), "Header[%s, %s]", key, config.headerMap.get(key));
			}
		}

		return builder;
	}

	private <T> T executeRequest(Request request, Class<T> responseCls, Setting action, String method) throws TaskException {
		try {
			if (SettingUtility.getPermanentSettingAsInt("http_delay") > 0) {
				Thread.sleep(SettingUtility.getPermanentSettingAsInt("http_delay"));
			}
		} catch (Throwable e) {
		}

		try {
			Response response = getOkHttpClient().newCall(request).execute();

			Logger.w(getTag(action, method), "Http-code = %d", response.code());
			if (!(response.code() == HttpURLConnection.HTTP_OK || response.code() == HttpURLConnection.HTTP_PARTIAL)) {
				String responseStr = response.body().string();

				if (Logger.DEBUG) {
					Logger.w(getTag(action, method), responseStr);
				}

				TaskException.checkResponse(responseStr);

				throw new TaskException(TaskException.TaskError.timeout.toString());
			} else {
				String responseStr = response.body().string();

				Logger.v(getTag(action, method), "Response = %s", responseStr);

				return parseResponse(responseStr, responseCls);
			}
		} catch (SocketTimeoutException e) {
			Logger.printExc(DefHttpUtility.class, e);
			Logger.w(getTag(action, method), e + "");

			throw new TaskException(TaskException.TaskError.timeout.toString());
		} catch (IOException e) {
			Logger.printExc(DefHttpUtility.class, e);
			Logger.w(getTag(action, method), e + "");

			throw new TaskException(TaskException.TaskError.timeout.toString());
		} catch (TaskException e) {
			Logger.printExc(DefHttpUtility.class, e);
			Logger.w(getTag(action, method), e + "");

			throw e;
		} catch (Exception e) {
			Logger.printExc(DefHttpUtility.class, e);
			Logger.w(getTag(action, method), e + "");

			throw new TaskException(TaskException.TaskError.resultIllegal.toString());
		}
	}

	protected <T> T parseResponse(String resultStr, Class<T> responseCls) throws TaskException  {
		if (responseCls.getSimpleName().equals("String"))
			return (T) resultStr;

		T result = JSON.parseObject(resultStr, responseCls);
		return result;
	}

	public synchronized OkHttpClient getOkHttpClient() {
		return GlobalContext.getInstance().getOkHttpClient();
	}

}
