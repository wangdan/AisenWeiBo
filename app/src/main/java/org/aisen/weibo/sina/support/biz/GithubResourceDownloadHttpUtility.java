package org.aisen.weibo.sina.support.biz;

import android.net.Proxy;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.setting.Setting;
import org.aisen.android.common.setting.SettingUtility;
import org.aisen.android.common.utils.FileUtils;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.network.http.HttpConfig;
import org.aisen.android.network.http.IHttpUtility;
import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Github资源下载器
 * 
 * @author Jeff.Wang
 *
 * @date 2014年10月24日
 */
public class GithubResourceDownloadHttpUtility implements IHttpUtility {

	public static final String TAG = "GithubResDownload";
	
	@Override
	public <T> T doGet(HttpConfig config, Setting action, Params params, Class<T> responseCls) throws TaskException {
		String url = config.baseUrl + action.getValue();
		String dir = params.getParameter("dir");
		String fileName = params.getParameter("fileName");
		
		Logger.d(TAG, String.format("下载地址 = %s, fileName = %s, 保存路径 dir = %s",
                url, fileName, dir));
		
		String dataPath = GlobalContext.getInstance().getAppPath() + "temp";
		File tempFile = new File(dataPath + File.separator + fileName);
		if (!tempFile.getParentFile().exists())
			tempFile.getParentFile().mkdirs();
		File targetFile = new File(dir + File.separator + fileName);
		if (targetFile.exists())
            targetFile.delete();
        else if (!targetFile.getParentFile().exists())
            targetFile.getParentFile().mkdirs();

		DefaultHttpClient httpClient = new DefaultHttpClient();
        String host = Proxy.getDefaultHost();
        if (host != null) {
            httpClient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, new HttpHost(host, Proxy.getDefaultPort()));
        }
        try {
            HttpResponse response = httpClient.execute(new HttpGet(url + fileName));
            if (200 == response.getStatusLine().getStatusCode()) {
                HttpEntity entity = response.getEntity();
                // 获取输入流
                InputStream in = entity.getContent();
                File tmpFile = tempFile;
                FileOutputStream out = new FileOutputStream(tmpFile);
                int i;
                byte bs[] = new byte[1024 * 8];
                while ((i = in.read(bs)) > 0) {
                    out.write(bs, 0, i);
                }
                out.flush();
                out.close();
                in.close();
                
                FileUtils.copyFile(tempFile, targetFile);
                boolean result = targetFile.exists();
                Logger.d(TAG, "下载 result = " + result);
                return (T) new Boolean(result);
            }
        } catch (Exception e) {
        	e.printStackTrace();
        }
		
		return (T) new Boolean(false);
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
