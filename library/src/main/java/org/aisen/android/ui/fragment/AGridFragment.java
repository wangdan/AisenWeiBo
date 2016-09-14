package org.aisen.android.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import org.aisen.android.R;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.adapter.BasicListAdapter;
import org.aisen.android.ui.fragment.adapter.IPagingAdapter;
import org.aisen.android.ui.fragment.itemview.AFooterItemView;
import org.aisen.android.ui.fragment.itemview.AHeaderItemViewCreator;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 维护GridView
 *
 * Created by wangdan on 16/1/21.
 */
public abstract class AGridFragment<T extends Serializable, Ts extends Serializable, Header extends Serializable>
                        extends APagingFragment<T, Ts, Header, GridView>
                        implements AdapterView.OnItemClickListener {

    @ViewInject(idStr = "gridview")
    private GridView gridView;

    @Override
    public int inflateContentView() {
        return R.layout.comm_ui_gridview;
    }

    @Override
    public GridView getRefreshView() {
        return gridView;
    }

    @Override
    protected void setupRefreshView(Bundle savedInstanceSate) {
        super.setupRefreshView(savedInstanceSate);

        // 设置事件
        getRefreshView().setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

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
    protected void addFooterViewToRefreshView(AFooterItemView<?> footerItemView) {

    }

    @Override
    protected void addHeaderViewToRefreshView(AHeaderItemViewCreator<?> headerItemViewCreator) {

    }

    /**
     * 初始化ListView
     *
     * @param items
     */
    public void setItems(ArrayList<T> items) {
        if (items == null)
            return;

        setViewVisiable(loadingLayout, View.GONE);
        setViewVisiable(loadFailureLayout, View.GONE);
        if (items.size() == 0 && emptyLayout != null) {
            setViewVisiable(emptyLayout, View.VISIBLE);
            setViewVisiable(contentLayout, View.GONE);
        }
        else {
            setViewVisiable(emptyLayout, View.GONE);
            setViewVisiable(contentLayout, View.VISIBLE);
        }
        setAdapterItems(items);
        if (getRefreshView().getAdapter() == null) {
            bindAdapter(getAdapter());
        }
        else {
            getRefreshView().smoothScrollToPosition(0);
            getAdapter().notifyDataSetChanged();
        }
    }

}
