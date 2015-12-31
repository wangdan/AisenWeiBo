package org.aisen.android.support.bean;

import org.aisen.android.network.biz.IResult;

/**
 *
 * Created by wangdan on 15/12/20.
 */
public class ResultBean implements IResult {

    private boolean outofdate;

    private boolean fromCache;

    private boolean endPaging;

    private String[] pagingIndex;

    @Override
    public boolean outofdate() {
        return isOutofdate();
    }

    @Override
    public boolean fromCache() {
        return isFromCache();
    }

    @Override
    public boolean endPaging() {
        return isEndPaging();
    }

    @Override
    public String[] pagingIndex() {
        return getPagingIndex();
    }

    public boolean isOutofdate() {
        return outofdate;
    }

    public void setOutofdate(boolean outofdate) {
        this.outofdate = outofdate;
    }

    public boolean isFromCache() {
        return fromCache;
    }

    public void setFromCache(boolean fromCache) {
        this.fromCache = fromCache;
    }

    public boolean isEndPaging() {
        return endPaging;
    }

    public void setEndPaging(boolean endPaging) {
        this.endPaging = endPaging;
    }

    public String[] getPagingIndex() {
        return pagingIndex;
    }

    public void setPagingIndex(String[] pagingIndex) {
        this.pagingIndex = pagingIndex;
    }

}
