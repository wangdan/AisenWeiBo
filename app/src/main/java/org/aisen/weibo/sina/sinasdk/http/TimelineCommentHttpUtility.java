package org.aisen.weibo.sina.sinasdk.http;

import org.aisen.android.network.task.TaskException;
import org.aisen.weibo.sina.service.VideoService;
import org.aisen.weibo.sina.sinasdk.bean.StatusComments;

/**
 * Created by wangdan on 16/7/21.
 */
public class TimelineCommentHttpUtility extends HttpsUtility {

    @Override
    protected <T> T parseResponse(String resultStr, Class<T> responseCls) throws TaskException {
        T result = super.parseResponse(resultStr, responseCls);

        if (result != null) {
            if (result instanceof StatusComments) {
                StatusComments comments = (StatusComments) result;

                // 解析普通网络链接、视频链接
                VideoService.parseCommentURL(comments.getComments());
            }
        }

        return result;
    }
}
