package org.aisen.android.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ListView;

import org.aisen.android.R;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.widget.swipyrefresh.SwipyRefreshLayout;
import org.aisen.android.ui.widget.swipyrefresh.SwipyRefreshLayoutDirection;

import java.io.Serializable;

/**
 * Created by wangdan on 15-3-9.
 */
public abstract class ASwipyRefreshGridLayout<T extends Serializable, Ts extends Serializable>
                                    extends ARefreshFragment<T, Ts, ListView>
                                    implements SwipyRefreshLayout.OnRefreshListener {
    static String TAG = "ASwipeRefreshGridFragment";

    @ViewInject(idStr = "swipeRefreshLayout")
    protected SwipyRefreshLayout swipeRefreshLayout;
    @ViewInject(idStr = "gridview")
    GridView mGridView;

    @Override
    final protected void setInitRefreshView(AbsListView refreshView, Bundle savedInstanceSate) {
        super.setInitRefreshView(refreshView, savedInstanceSate);

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        swipeRefreshLayout.setVisibility(View.VISIBLE);
        configSwipeDirection(SwipyRefreshLayoutDirection.BOTH);

        setInitRefreshGridView(swipeRefreshLayout, refreshView, savedInstanceSate);
    }

    protected void setInitRefreshGridView(SwipyRefreshLayout swipeRefreshLayout, AbsListView refreshView, Bundle savedInstanceSate) {

    }

    @Override
    protected void onChangedByConfig(RefreshConfig config) {
        if (config.canLoadMore)
            configSwipeDirection(SwipyRefreshLayoutDirection.BOTH);
        else
            configSwipeDirection(SwipyRefreshLayoutDirection.TOP);
    }

    protected void configSwipeDirection(SwipyRefreshLayoutDirection direction) {
        swipeRefreshLayout.setDirection(direction);
    }

    @Override
    public AbsListView getRefreshView() {
        return mGridView;
    }

    @Override
    public void onRefresh(SwipyRefreshLayoutDirection direction) {
        if (direction == SwipyRefreshLayoutDirection.TOP)
            onPullDownToRefresh();
        else if (direction == SwipyRefreshLayoutDirection.BOTTOM)
            onPullUpToRefresh();
    }

    @Override
    protected int inflateContentView() {
        return R.layout.comm_lay_swipy_grid;
    }

    public GridView getGridView() {
        return (GridView) getRefreshView();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        super.onScrollStateChanged(view, scrollState);

    }

    @Override
    public boolean setRefreshing() {
        swipeRefreshLayout.setRefreshing(true);

        return false;
    }

    @Override
    public boolean isRefreshing() {
//        if (swipeRefreshLayout.isr)
//            return true;

        return super.isRefreshing();
    }

    @Override
    public void onRefreshViewComplete() {
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    protected void taskStateChanged(ABaseTaskState state, Serializable extra) {
        super.taskStateChanged(state, extra);

        if (state == ABaseTaskState.finished) {
            if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing())
                onRefreshViewComplete();

        } else if (state == ABaseTaskState.prepare) {
            if (loadingLayout != null && loadingLayout.getVisibility() != View.VISIBLE && swipeRefreshLayout != null) {
                setRefreshing();
            }
        }
    }

}
