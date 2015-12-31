package org.aisen.android.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.BaseAdapter;

import org.aisen.android.R;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.widget.pla.PLAAbsListView;
import org.aisen.android.ui.widget.pla.PLAMultiColumnListView;

import java.io.Serializable;

/**
 * 维护一个瀑布流
 *
 */
public abstract class AWaterfallFragment<T extends Serializable, Ts extends Serializable>
                                extends APagingFragment<T, Ts, PLAMultiColumnListView>
                                implements PLAAbsListView.OnScrollListener {

    @ViewInject(idStr = "plaMultiColumnList")
    PLAMultiColumnListView mPlaMultiColumnList;

    @Override
    protected int inflateContentView() {
        return R.layout.comm_ui_waterfall;
    }

    @Override
    public PLAMultiColumnListView getRefreshView() {
        return mPlaMultiColumnList;
    }

    @Override
    public boolean setRefreshing() {
        return false;
    }

    @Override
    protected void setInitRefreshView(PLAMultiColumnListView refreshView, Bundle savedInstanceSate) {
        super.setInitRefreshView(refreshView, savedInstanceSate);

        mPlaMultiColumnList.setOnScrollListener(this);
    }

    @Override
    protected void toLastReadPosition() {
        super.toLastReadPosition();

        runUIRunnable(new Runnable() {

            @Override
            public void run() {
                mPlaMultiColumnList.setSelectionFromTop(getLastReadPosition(), getLastReadTop() + mPlaMultiColumnList.getPaddingTop());
            }

        });
    }

    @Override
    protected void onChangedByConfig(RefreshConfig config) {

    }

    @Override
    public void onRefreshViewComplete(RefreshMode mode) {

    }

    @Override
    protected void bindFooterView(PLAMultiColumnListView refreshView, View footerView) {
        if (footerView != null)
            refreshView.addFooterView(footerView);
    }

    @Override
    protected void bindAdapter(PLAMultiColumnListView refreshView, BaseAdapter adapter) {
        if (refreshView.getAdapter() == null)
            refreshView.setAdapter(adapter);
    }

    @Override
    protected int getFirstVisiblePosition() {
        return mPlaMultiColumnList.getFirstVisiblePosition();
    }

    @Override
    public void onScrollStateChanged(PLAAbsListView view, int scrollState) {
        handleScrollStateChanged(mPlaMultiColumnList, scrollState);
    }

    @Override
    public void onScroll(PLAAbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        handleScroll(mPlaMultiColumnList, firstVisibleItem, visibleItemCount, totalItemCount);
    }

}
