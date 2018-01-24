package org.aisen.android.ui.fragment;

import android.os.Bundle;
import android.view.View;

import org.aisen.android.R;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.adapter.BasicListAdapter;
import org.aisen.android.ui.fragment.adapter.IPagingAdapter;
import org.aisen.android.ui.fragment.itemview.AFooterItemView;
import org.aisen.android.ui.fragment.itemview.AHeaderItemViewCreator;
import org.aisen.android.ui.widget.pla.PLAAbsListView;
import org.aisen.android.ui.widget.pla.PLAAdapterView;
import org.aisen.android.ui.widget.pla.PLAMultiColumnListView;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 维护一个瀑布流
 *
 */
public abstract class AWaterfallFragment<T extends Serializable, Ts extends Serializable, Header extends Serializable>
                                extends APagingFragment<T, Ts, Header, PLAMultiColumnListView>
                                implements PLAAbsListView.OnScrollListener, PLAMultiColumnListView.OnItemClickListener {

    @ViewInject(idStr = "plaMultiColumnList")
    PLAMultiColumnListView mPlaMultiColumnList;

    @Override
    public int inflateContentView() {
        return R.layout.comm_ui_waterfall;
    }

    @Override
    public PLAMultiColumnListView getRefreshView() {
        return mPlaMultiColumnList;
    }

    @Override
    protected void setupRefreshView(Bundle savedInstanceSate) {
        super.setupRefreshView(savedInstanceSate);

        mPlaMultiColumnList.setOnScrollListener(this);
        mPlaMultiColumnList.setOnItemClickListener(this);
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
    public void onItemClick(PLAAdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    protected void addFooterViewToRefreshView(AFooterItemView<?> footerItemView) {
        getRefreshView().addFooterView(footerItemView.getConvertView());
    }

    @Override
    protected void addHeaderViewToRefreshView(AHeaderItemViewCreator<?> headerItemViewCreator) {
        // TODO
    }

    @Override
    protected IPagingAdapter<T> newAdapter(ArrayList<T> datas) {
        return new BasicListAdapter<>(this, datas);
    }

    @Override
    protected void bindAdapter(IPagingAdapter adapter) {
        if (getRefreshView().getAdapter() == null)
            getRefreshView().setAdapter((BasicListAdapter) adapter);
    }

    @Override
    protected int getFirstVisiblePosition() {
        return mPlaMultiColumnList.getFirstVisiblePosition();
    }

    @Override
    public void requestDataOutofdate() {
        getRefreshView().setSelectionFromTop(0, 0);

        super.requestDataOutofdate();
    }

    @Override
    public void onScrollStateChanged(PLAAbsListView view, int scrollState) {
        onScrollStateChanged(scrollState);
    }

    @Override
    public void onScroll(PLAAbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        onScroll(firstVisibleItem, visibleItemCount, totalItemCount);
    }

}
