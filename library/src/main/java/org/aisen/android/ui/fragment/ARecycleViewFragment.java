package org.aisen.android.ui.fragment;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import org.aisen.android.R;
import org.aisen.android.support.adapter.IPagingAdapter;
import org.aisen.android.support.inject.ViewInject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by wangdan on 16/1/4.
 */
public abstract class ARecycleViewFragment<T extends Serializable, Ts extends Serializable> extends APagingFragment<T, Ts, RecyclerView> {

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
    IPagingAdapter<T> newAdapter(ArrayList<T> datas) {
        return null;
    }

    @Override
    protected void setupRefreshView(Bundle savedInstanceSate) {
        super.setupRefreshView(savedInstanceSate);
    }

}
