package org.aisen.android.network.http;

import java.io.File;

import org.aisen.android.common.setting.Setting;
import org.aisen.android.network.task.TaskException;

public interface IHttpUtility {

	public <T> T doGet(HttpConfig config, Setting action, Params params, Class<T> responseCls) throws TaskException;

	public <T> T doPost(HttpConfig config, Setting action, Params params, Class<T> responseCls, Object requestObj) throws TaskException;

	public <T> T uploadFile(HttpConfig config, Setting action, Params params, File file, Params headers, Class<T> responseClass) throws TaskException;

}
