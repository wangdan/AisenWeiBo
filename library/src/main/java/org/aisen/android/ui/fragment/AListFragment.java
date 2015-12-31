package org.aisen.android.ui.fragment;

import android.view.View;
import android.widget.ListView;

import org.aisen.android.R;
import org.aisen.android.support.inject.ViewInject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 维护ListView
 *
 */
public abstract class AListFragment<T extends Serializable, Ts extends Serializable>
                                extends APagingFragment<T, Ts, ListView> {

    @ViewInject(idStr = "listView")
    ListView mListView;

    @Override
    protected int inflateContentView() {
        return R.layout.comm_ui_list;
    }

    @Override
    public ListView getRefreshView() {
        return mListView;
    }

    @Override
    public boolean setRefreshing() {
        return false;
    }

    @Override
    protected void onChangedByConfig(RefreshConfig config) {

    }

    @Override
    public void onRefreshViewComplete(RefreshMode mode) {

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
        getAdapter().notifyDataSetChanged();
        if (mListView.getAdapter() == null) {
            mListView.setAdapter(getAdapter());
        }
        else {
            mListView.setSelectionFromTop(0, 0);
        }
    }

}
