package org.aisen.weibo.sina.ui.fragment.timeline;

import org.aisen.android.support.adapter.ABaseAdapter;
import org.aisen.android.ui.fragment.AListSwipeRefreshFragment;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;

/**
 * 微博列表基类
 *
 * Created by wangdan on 16/1/2.
 */
public abstract class ATimelineFragment extends AListSwipeRefreshFragment<StatusContent, StatusContents> {

    @Override
    protected ABaseAdapter.AbstractItemView<StatusContent> newItemView() {
        return null;
    }

    @Override
    protected void requestData(RefreshMode mode) {

    }

}
