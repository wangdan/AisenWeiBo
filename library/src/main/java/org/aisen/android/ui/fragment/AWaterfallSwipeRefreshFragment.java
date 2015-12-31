package org.aisen.android.ui.fragment;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;

import org.aisen.android.R;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.widget.pla.PLAMultiColumnListView;

import java.io.Serializable;

/**
 * 维护瀑布流的SwipeRefreshLayout控件
 *
 */
public abstract class AWaterfallSwipeRefreshFragment<T extends Serializable, Ts extends Serializable>
                                    extends AWaterfallFragment<T, Ts>
                                    implements SwipeRefreshLayout.OnRefreshListener {

    @ViewInject(idStr = "swipeRefreshLayout")
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected int inflateContentView() {
        return R.layout.comm_ui_waterfall_swiperefresh;
    }

    @Override
    protected void setInitRefreshView(PLAMultiColumnListView refreshView, Bundle savedInstanceSate) {
        super.setInitRefreshView(refreshView, savedInstanceSate);

        setInitSwipeRefreshLayout();
    }

    protected void setInitSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                                                    android.R.color.holo_green_light,
                                                    android.R.color.holo_orange_light,
                                                    android.R.color.holo_red_light);
    }

    @Override
    public void onRefresh() {
        onPullDownToRefresh();
    }

    @Override
    public boolean setRefreshing() {
        swipeRefreshLayout.setRefreshing(true);

        return false;
    }

    @Override
    public void onRefreshViewComplete(RefreshMode mode) {
        if (mode != RefreshMode.update && swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

}
