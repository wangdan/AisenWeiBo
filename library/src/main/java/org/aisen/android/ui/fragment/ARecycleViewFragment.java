package org.aisen.android.ui.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;

import org.aisen.android.R;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.adapter.BasicRecycleViewAdapter;
import org.aisen.android.ui.fragment.adapter.IPagingAdapter;
import org.aisen.android.ui.fragment.itemview.AFooterItemView;
import org.aisen.android.ui.fragment.itemview.AHeaderItemViewCreator;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 维护RecycleView
 *
 * Created by wangdan on 16/1/4.
 */
public abstract class ARecycleViewFragment<T extends Serializable, Ts extends Serializable, Header extends Serializable>
                            extends APagingFragment<T, Ts, Header, RecyclerView>
                            implements AdapterView.OnItemClickListener {

    @ViewInject(idStr = "recycleview")
    RecyclerView mRecycleView;

    @Override
    public int inflateContentView() {
        return R.layout.comm_ui_recycleview;
    }

    @Override
    public RecyclerView getRefreshView() {
        return mRecycleView;
    }

    @Override
    protected void setupRefreshConfig(RefreshConfig config) {
        super.setupRefreshConfig(config);

        getRefreshView().addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                ARecycleViewFragment.this.onScrollStateChanged(newState);
            }


            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }

        });
        getRefreshView().setLayoutManager(configLayoutManager());
    }

    @Override
    protected IPagingAdapter<T> newAdapter(ArrayList<T> datas) {
        return new BasicRecycleViewAdapter<>(getActivity(), this, configItemViewCreator(), datas);
    }

    /**
     * 默认是LinearLayoutManager
     *
     * @return
     */
    protected RecyclerView.LayoutManager configLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    @Override
    protected void bindAdapter(IPagingAdapter adapter) {
        if (getRefreshView().getAdapter() == null) {
            getRefreshView().setAdapter((BasicRecycleViewAdapter) adapter);
        }

        if (((BasicRecycleViewAdapter) getAdapter()).getOnItemClickListener() != this) {
            ((BasicRecycleViewAdapter) getAdapter()).setOnItemClickListener(this);
        }
    }

    @Override
    protected void addFooterViewToRefreshView(AFooterItemView<?> footerItemView) {
        ((BasicRecycleViewAdapter) getAdapter()).addFooterView(footerItemView);
    }

    @Override
    protected void addHeaderViewToRefreshView(AHeaderItemViewCreator<?> headerItemViewCreator) {
        ((BasicRecycleViewAdapter) getAdapter()).setHeaderItemViewCreator(headerItemViewCreator);
    }

    @Override
    protected void setupRefreshView(Bundle savedInstanceSate) {
        super.setupRefreshView(savedInstanceSate);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    protected void toLastReadPosition() {
        super.toLastReadPosition();
        if (getRefreshView() == null || TextUtils.isEmpty(refreshConfig.positionKey) ||
                getLastReadPosition() < 0)
            return;

        if (getRefreshView().getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager manager = (LinearLayoutManager) getRefreshView().getLayoutManager();

            if (getAdapterItems().size() > getLastReadPosition()) {
                manager.scrollToPositionWithOffset(getLastReadPosition(), getLastReadTop() + getRefreshView().getPaddingTop());
            }
        }
    }

    @Override
    protected int getFirstVisiblePosition() {
        if (getRefreshView().getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager manager = (LinearLayoutManager) getRefreshView().getLayoutManager();

            return manager.findFirstVisibleItemPosition();
        }

        return 0;
    }

}
