package org.aisen.weibo.sina.sinasdk.http;

import org.aisen.android.network.task.TaskException;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;
import org.aisen.weibo.sina.sinasdk.bean.StatusHots;

import java.util.ArrayList;

/**
 * Created by wangdan on 16/7/21.
 */
public class TimelineHotHttpUtility extends HttpsUtility {

    @Override
    protected <T> T parseResponse(String resultStr, Class<T> responseCls) throws TaskException {
        try {
            StatusHots statusHots = super.parseResponse(resultStr, StatusHots.class);

            if (statusHots.getCards().size() == 0) {
                StatusContents result = new StatusContents();
                result.setStatuses(new ArrayList<StatusContent>());
                result.setEndPaging(true);

                return (T) result;
            }

            String[] ids = new String[statusHots.getCards().size()];
            for (int i = 0; i < statusHots.getCards().size(); i++) {
                ids[i] = statusHots.getCards().get(i).getMblog().getId() + "";
            }

            return (T) SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).statusShowBatch(ids);
        } catch (Exception e) {
            throw  new TaskException(TaskException.TaskError.resultIllegal.toString());
        }
    }

}
