package org.aisen.weibo.sina.support.paging;

import android.text.TextUtils;

import org.aisen.android.support.paging.IPaging;
import org.aisen.weibo.sina.sinasdk.bean.WebHotTopicsBean;
import org.aisen.weibo.sina.sinasdk.bean.WebHotTopicssBean;

/**
 * Created by wangdan on 16/8/14.
 */
public class TopicsHotPaging implements IPaging<WebHotTopicsBean, WebHotTopicssBean> {

    private static final long serialVersionUID = 797586663582605502L;

    private String nextPage;

    @Override
    public void processData(WebHotTopicssBean newDatas, WebHotTopicsBean firstData, WebHotTopicsBean lastData) {
        if (newDatas != null) {
            if (!TextUtils.isEmpty(newDatas.getSince_id())) {
                nextPage = newDatas.getSince_id();
            }
            else if (newDatas.getPage() != -1) {
                nextPage = "page_" + newDatas.getPage();
            }
        }
    }

    @Override
    public String getPreviousPage() {
        return null;
    }

    @Override
    public String getNextPage() {
        return nextPage;
    }

}
