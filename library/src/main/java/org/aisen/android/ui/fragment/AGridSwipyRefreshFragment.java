package org.aisen.android.ui.fragment;

import android.os.Bundle;
import android.view.View;

import org.aisen.android.R;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.widget.swipyrefresh.SwipyRefreshLayout;
import org.aisen.android.ui.widget.swipyrefresh.SwipyRefreshLayoutDirection;

import java.io.Serializable;

/**
 * 维护SwipyRefresh刷新的GridView
 *
 * Created by wangdan on 16/1/21.
 */
public abstract class AGridSwipyRefreshFragment<T extends Serializable, Ts extends Serializable, Header extends Serializable>
                                            extends AGridFragment<T, Ts, Header>
                                            implements SwipyRefreshLayout.OnRefreshListener  {

    @ViewInject(idStr = "swipyRefreshLayout")
    protected SwipyRefreshLayout swipyRefreshLayout;

    @Override
    public int inflateContentView() {
        return R.layout.comm_ui_swipy_grid;
    }

    @Override
    final protected void setupRefreshView(Bundle savedInstanceSate) {
        super.setupRefreshView(savedInstanceSate);

        setupSwipyRefreshLayout(swipyRefreshLayout);
    }

    protected void setupSwipyRefreshLayout(SwipyRefreshLayout swipyRefreshLayout) {
        swipyRefreshLayout.setOnRefreshListener(this);
        swipyRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        swipyRefreshLayout.setVisibility(View.VISIBLE);
        setSwipyDirection(SwipyRefreshLayoutDirection.BOTH);
    }

    @Override
    protected void setupRefreshViewWithConfig(RefreshConfig config) {
        if (!config.pagingEnd)
            setSwipyDirection(SwipyRefreshLayoutDirection.BOTH);
        else
            setSwipyDirection(SwipyRefreshLayoutDirection.TOP);
    }

    protected void setSwipyDirection(SwipyRefreshLayoutDirection direction) {
        swipyRefreshLayout.setDirection(direction);
    }

    @Override
    public void onRefresh(SwipyRefreshLayoutDirection direction) {
        if (direction == SwipyRefreshLayoutDirection.TOP)
            onPullDownToRefresh();
        else if (direction == SwipyRefreshLayoutDirection.BOTTOM)
            onPullUpToRefresh();
    }

    @Override
    public boolean setRefreshViewToLoading() {
        swipyRefreshLayout.setRefreshing(true);

        return false;
    }

    @Override
    public void onRefreshViewFinished(RefreshMode mode) {
        swipyRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onTaskStateChanged(ABaseTaskState state, TaskException exception, RefreshMode mode) {
        super.onTaskStateChanged(state, exception, mode);

        if (state == ABaseTaskState.finished) {
            if (swipyRefreshLayout != null && swipyRefreshLayout.isRefreshing())
                onRefreshViewFinished(mode);

        } else if (state == ABaseTaskState.prepare) {
            if (loadingLayout != null && loadingLayout.getVisibility() != View.VISIBLE && swipyRefreshLayout != null) {
                setRefreshViewToLoading();
            }
        }
    }

}
