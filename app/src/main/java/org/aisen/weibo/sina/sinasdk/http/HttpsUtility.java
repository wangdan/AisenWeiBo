package org.aisen.weibo.sina.sinasdk.http;

import org.aisen.android.common.setting.Setting;
import org.aisen.android.network.http.HttpConfig;
import org.aisen.android.network.http.IHttpUtility;
import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;

import java.io.File;

public class HttpsUtility implements IHttpUtility {

	private int connectTimeout = 15 * 1000;

	private int soTimeout = 15 * 1000;

	@Override
	public <T> T doGet(HttpConfig config, Setting action, Params params, Class<T> responseCls) throws TaskException {
		return null;
	}

	@Override
	public <T> T doPost(HttpConfig config, Setting action, Params params, Class<T> responseCls, Object requestObj) throws TaskException {
		return null;
	}

	@Override
	public <T> T uploadFile(HttpConfig config, Setting action, Params params, File file, Params headers, Class<T> responseClass) throws TaskException {
		return null;
	}

}
