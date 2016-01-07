package org.aisen.android.ui.fragment;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import org.aisen.android.R;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.adapter.ARecycleViewItemView;
import org.aisen.android.ui.fragment.adapter.BasicRecycleViewAdapter;
import org.aisen.android.ui.fragment.adapter.IPagingAdapter;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 维护RecycleView
 *
 * Created by wangdan on 16/1/4.
 */
public abstract class ARecycleViewFragment<T extends Serializable, Ts extends Serializable>
                            extends APagingFragment<T, Ts, RecyclerView>
                            implements AdapterView.OnItemClickListener {

    @ViewInject(idStr = "recycleview")
    RecyclerView mRecycleView;

    @Override
    protected int inflateContentView() {
        return R.layout.comm_ui_recycleview;
    }

    @Override
    public RecyclerView getRefreshView() {
        return mRecycleView;
    }

    @Override
    protected void setupRefreshConfig(RefreshConfig config) {
        super.setupRefreshConfig(config);

        mRecycleView.addOnScrollListener(new RecyclerView.OnScrollListener() {

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
    }

    @Override
    IPagingAdapter<T> newAdapter(ArrayList<T> datas) {
        return new BasicRecycleViewAdapter<>(this, datas);
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
        if (mRecycleView.getAdapter() == null) {
            mRecycleView.setLayoutManager(configLayoutManager());
            mRecycleView.setAdapter((BasicRecycleViewAdapter) adapter);
        }

        if (((BasicRecycleViewAdapter) getAdapter()).getOnItemClickListener() != this) {
            ((BasicRecycleViewAdapter) getAdapter()).setOnItemClickListener(this);
        }
    }

    @Override
    protected void addFooterViewToRefreshView(View footerView) {
        ARecycleViewItemView footerItemView = new ARecycleViewItemView<T>(footerView) {

            @Override
            public void onBindData(View convertView, Serializable data, int position) {

            }

        };

        ((BasicRecycleViewAdapter) getAdapter()).addFooterView(footerItemView);
    }

    @Override
    protected void setupRefreshView(Bundle savedInstanceSate) {
        super.setupRefreshView(savedInstanceSate);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

}
