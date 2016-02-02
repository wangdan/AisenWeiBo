package org.aisen.weibo.sina.support.biz;

import android.net.Proxy;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.setting.Setting;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.network.http.HttpConfig;
import org.aisen.android.network.http.IHttpUtility;
import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;

import org.aisen.weibo.sina.support.bean.PictureSize;
import org.aisen.weibo.sina.support.db.SinaDB;

import java.io.File;
import java.net.HttpURLConnection;

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

        Request request = new Request.Builder().url(url).build();
//			httpGet.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:27.0) Gecko/20100101 Firefox/27.0");

        try {
            Response response = GlobalContext.getInstance().getOkHttpClient().newCall(request).execute();
            if (!(response.code() == HttpURLConnection.HTTP_OK || response.code() == HttpURLConnection.HTTP_PARTIAL)) {
                throw new TaskException(String.valueOf(TaskException.TaskError.failIOError));
            }
            else {
                // 图片大小
                String header = response.header("Content-Length");
                int length = Integer.parseInt(header);

                size.setSize(length);
                SinaDB.getSqlite().insert(null, size);
                Logger.d(TAG, String.format("图片大小 %s", String.valueOf(size.getSize())));
            }
        } catch (Exception e) {
            throw new TaskException(String.valueOf(TaskException.TaskError.failIOError));
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
