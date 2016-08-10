package org.aisen.weibo.sina.support.paging;

import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;

/**
 * Created by wangdan on 16/8/10.
 */
public class HotPaging extends TimelinePaging {

    private static final long serialVersionUID = -7430672310949192135L;

    private long since_id = 0;

    @Override
    public void processData(StatusContents newDatas, StatusContent firstData, StatusContent lastData) {
        if (newDatas != null)
            since_id = newDatas.getSince_id();
    }

    @Override
    public String getNextPage() {
        return since_id + "";
    }

}
