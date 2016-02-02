package org.aisen.android.network.http;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.squareup.okhttp.MediaType;
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

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.util.Set;

public class DefHttpUtility implements IHttpUtility {

	private static final String TAG = "BizLogic-Http-Def";

	@Override
	public <T> T doGet(HttpConfig config, Setting action, Params params, Class<T> responseCls) throws TaskException {
		Request request = createRequestBuilder(config, action, params).build();

		return executeRequest(request, responseCls);
	}

	@Override
	public <T> T doPost(HttpConfig config, Setting action, Params params, Class<T> responseCls, Object requestObj) throws TaskException {
		Request.Builder builder = createRequestBuilder(config, action, params);

		if (requestObj != null) {
			RequestBody requestBody = null;

			String requestBodyStr = null;
			if (requestObj instanceof Params) {
				Params p = (Params) requestObj;
				requestBodyStr = ParamsUtil.encodeToURLParams(p);

				requestBody = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded;charset=UTF-8"), requestBodyStr);
			}
			else {
				requestBodyStr = JSON.toJSONString(requestObj);

				requestBody = RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), requestBodyStr);
			}

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

		String url = (config.baseUrl + action.getValue() + (params == null ? "" : "?" + ParamsUtil.encodeToURLParams(params))).replaceAll(" ", "");
		Logger.v(TAG, url);
		builder.url(url);

		return builder;
	}

	public <T> T uploadFile(HttpConfig config, Setting action, Params params, File file, Params headers, Class<T> responseClazz) throws TaskException {
		return null;
	}

	private <T> T executeRequest(Request request, Class<T> responseCls) throws TaskException {
		try {
			if (SettingUtility.getIntSetting("http_delay") > 0) {
				Thread.sleep(SettingUtility.getIntSetting("http_delay"));
			}
		} catch (Throwable e) {
		}

		try {
			Response response = GlobalContext.getInstance().getOkHttpClient().newCall(request).execute();

			if (!(response.code() == HttpURLConnection.HTTP_OK || response.code() == HttpURLConnection.HTTP_PARTIAL)) {
				Logger.e(ABizLogic.TAG, String.format("请求Http失败，状态码 : %d", response.code()));

				if (Logger.DEBUG) {
					Logger.w(ABizLogic.TAG, response.body().toString());
				}

				throw new TaskException(TaskException.TaskError.timeout.toString());
			} else {
				String responseStr = response.body().string();
				try {
					if (responseCls.getSimpleName().equals("String"))
						return (T) responseStr;

					return JSON.parseObject(responseStr, responseCls);
				} catch (Exception e) {
					e.printStackTrace();
					throw new TaskException(TaskException.TaskError.resultIllegal.toString());
				}
			}
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
			throw new TaskException(TaskException.TaskError.timeout.toString());
		} catch (IOException e) {
			e.printStackTrace();
			throw new TaskException(TaskException.TaskError.timeout.toString());
		}
	}

}
