package org.aisen.weibo.sina.support.paging;

import org.aisen.android.support.paging.IPaging;

import java.io.Serializable;

/**
 * Created by wangdan on 16/3/22.
 */
public class OffsetPaging<T extends Serializable, Ts extends OffsetResult> implements IPaging<T, Ts> {

    private static final long serialVersionUID = -760856121924132682L;

    private int offset;

    @Override
    public void processData(Ts newDatas, T firstData, T lastData) {
        offset = newDatas.getOffset();
    }

    @Override
    public String getPreviousPage() {
        return null;
    }

    @Override
    public String getNextPage() {
        return String.valueOf(offset);
    }

}
