package org.aisen.weibo.sina.sinasdk.http;

import org.aisen.android.network.task.TaskException;
import org.aisen.weibo.sina.service.VideoService;
import org.aisen.weibo.sina.sinasdk.bean.Favorities;
import org.aisen.weibo.sina.sinasdk.bean.Favority;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangdan on 16/7/21.
 */
public class TimelineHttpUtility extends HttpsUtility {

    @Override
    protected <T> T parseResponse(String resultStr, Class<T> responseCls) throws TaskException {
        T result = super.parseResponse(resultStr, responseCls);

        if (result != null) {
            if (result instanceof StatusContents) {
                StatusContents statusContents = (StatusContents) result;

                // 解析普通网络链接、视频链接
                VideoService.parseStatusURL(statusContents.getStatuses());
            }
            else if (result instanceof Favorities) {
                Favorities favorities = (Favorities) result;
                List<StatusContent> statusContents = new ArrayList<>();

                for (Favority favority : favorities.getFavorites()) {
                    statusContents.add(favority.getStatus());
                }

                // 解析普通网络链接、视频链接
                VideoService.parseStatusURL(statusContents);
            }
        }

        return result;
    }
}
