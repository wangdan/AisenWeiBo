package org.aisen.weibo.sina.support.paging;

import org.aisen.android.support.paging.IPaging;
import org.aisen.weibo.sina.support.bean.JokeBean;
import org.aisen.weibo.sina.support.bean.JokeBeans;

/**
 * Created by wangdan on 16/3/22.
 */
public class JokePaging implements IPaging<JokeBean, JokeBeans> {

    private static final long serialVersionUID = -4218422318963552361L;

    private long firstId;

    private long lastId;

    @Override
    public void processData(JokeBeans newDatas, JokeBean firstData, JokeBean lastData) {
        if (firstData != null)
            firstId = firstData.getId();
        if (lastData != null)
            lastId = lastData.getId();
    }

    @Override
    public String getPreviousPage() {
        return String.valueOf(firstId);
    }

    @Override
    public String getNextPage() {
        return String.valueOf(lastId);
    }

    public void clear() {
        firstId = 0;
        lastId = 0;
    }

}
