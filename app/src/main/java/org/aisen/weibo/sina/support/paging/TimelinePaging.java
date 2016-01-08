package org.aisen.weibo.sina.support.paging;

import android.text.TextUtils;

import org.aisen.android.support.paging.IPaging;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;
import org.aisen.weibo.sina.support.utils.AisenUtils;

/**
 * Created by wangdan on 16/1/7.
 */
public class TimelinePaging implements IPaging<StatusContent, StatusContents> {

    private static final long serialVersionUID = -1563104012290641720L;

    private String firstId;

    private String lastId;

    @Override
    public void processData(StatusContents newDatas, StatusContent firstData, StatusContent lastData) {
        if (firstData != null)
            firstId = AisenUtils.getId(firstData);
        if (lastData != null)
            lastId = AisenUtils.getId(lastData);
    }

    @Override
    public String getPreviousPage() {
        return firstId;
    }

    @Override
    public String getNextPage() {
        if (TextUtils.isEmpty(lastId))
            return null;

        return (Long.parseLong(lastId) - 1) + "";
    }

}
