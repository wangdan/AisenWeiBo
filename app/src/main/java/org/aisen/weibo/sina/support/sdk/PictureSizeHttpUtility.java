package org.aisen.weibo.sina.support.sdk;

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
import org.aisen.weibo.sina.support.sqlit.SinaDB;

import java.net.HttpURLConnection;

/**
 * Created by wangdan on 16/2/20.
 */
public class PictureSizeHttpUtility implements IHttpUtility {

    private static final String TAG = PictureSizeHttpUtility.class.getSimpleName();

    @Override
    public <T> T doGet(HttpConfig config, Setting action, Params urlParams, Class<T> responseCls) throws TaskException {
        if (GlobalContext.getInstance() == null || SystemUtils.getNetworkType(GlobalContext.getInstance()) == SystemUtils.NetWorkType.none)
            return null;

        String url = urlParams.getParameter("path");

        PictureSize size = new PictureSize();
        size.setUrl(url);

        Request request = new Request.Builder().url(url).build();

        try {
            Response response = GlobalContext.getOkHttpClient().newCall(request).execute();
            if (!(response.code() == HttpURLConnection.HTTP_OK || response.code() == HttpURLConnection.HTTP_PARTIAL)) {
                throw new TaskException(String.valueOf(TaskException.TaskError.failIOError));
            }
            else {
                // 图片大小
                String header = response.header("Content-Length");
                int length = Integer.parseInt(header);

                size.setSize(length);
                SinaDB.getDB().insert(null, size);
                Logger.d(TAG, String.format("图片大小 %s", String.valueOf(size.getSize())));
            }
        } catch (Exception e) {
            throw new TaskException(String.valueOf(TaskException.TaskError.failIOError));
        }

        return (T) size;
    }

    @Override
    public <T> T doPost(HttpConfig config, Setting action, Params urlParams, Params bodyParams, Object requestObj, Class<T> responseCls) throws TaskException {
        return null;
    }

    @Override
    public <T> T doPostFiles(HttpConfig config, Setting action, Params urlParams, Params bodyParams, MultipartFile[] files, Class<T> responseCls) throws TaskException {
        return null;
    }

}
