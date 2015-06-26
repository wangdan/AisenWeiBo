package org.aisen.weibo.sina.support.biz;

import android.net.Proxy;

import org.aisen.android.common.setting.Setting;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.network.http.HttpConfig;
import org.aisen.android.network.http.IHttpUtility;
import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;

import org.aisen.weibo.sina.support.bean.PictureSize;
import org.aisen.weibo.sina.support.db.SinaDB;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;

/**
 * Created by wangdan on 15/5/2.
 */
public class PictureSizeHttpUtility implements IHttpUtility {

    static final String TAG = "PictureSizeHttpUtility";

    @Override
    public <T> T doGet(HttpConfig config, Setting action, Params params, Class<T> responseCls) throws TaskException {
        if (SystemUtils.getNetworkType() == SystemUtils.NetWorkType.none)
            return null;

        String url = params.getParameter("path");

        PictureSize size = new PictureSize();
        size.setUrl(url);

        DefaultHttpClient httpClient = new DefaultHttpClient();
        String host = Proxy.getDefaultHost();
        if (host != null) {
            httpClient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, new HttpHost(host, Proxy.getDefaultPort()));
        }
        try {
            HttpGet get = new HttpGet(url);
            HttpResponse response = httpClient.execute(get);
            if (200 == response.getStatusLine().getStatusCode()) {
                Header lengthHeader = response.getLastHeader("Content-Length");
                if (lengthHeader != null) {
                    size.setSize(Long.parseLong(lengthHeader.getValue()));
                    SinaDB.getSqlite().insert(null, size);
                    Logger.d(TAG, String.format("图片大小 %s", String.valueOf(size.getSize())));
                }
                get.abort();
            }
        } catch (Exception e) {
        }

        return (T) size;
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
