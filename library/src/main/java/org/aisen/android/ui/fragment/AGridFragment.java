package org.aisen.android.ui.fragment;

import android.view.View;
import android.widget.AbsListView;
import android.widget.GridView;

import org.aisen.android.R;
import org.aisen.android.support.inject.ViewInject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by wangdan on 15/4/25.
 */
public abstract class AGridFragment<T extends Serializable, Ts extends Serializable> extends ARefreshFragment<T, Ts, GridView> {

    @ViewInject(idStr = "gridview")
    private GridView gridView;

    @Override
    public AbsListView getRefreshView() {
        return gridView;
    }

    @Override
    protected int inflateContentView() {
        return R.layout.comm_lay_gridview;
    }

    protected GridView getGridView() {
        return gridView;
    }

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
        notifyDataSetChanged();
        if (gridView.getAdapter() == null) {
            gridView.setAdapter(getAdapter());
        }
    }

    @Override
    public boolean setRefreshing() {
        return false;
    }

    @Override
    public void onRefreshViewComplete() {
    }

    @Override
    public void onChangedByConfig(RefreshConfig config) {
    }

}
