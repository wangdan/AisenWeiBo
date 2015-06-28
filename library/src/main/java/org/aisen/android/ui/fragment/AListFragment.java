package org.aisen.android.ui.fragment;

import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import org.aisen.android.R;
import org.aisen.android.support.inject.ViewInject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 普通的ListView
 *
 */
public abstract class AListFragment<T extends Serializable, Ts extends Serializable> extends ARefreshFragment<T, Ts, ListView> {

	@ViewInject(idStr = "listView")
	ListView listView;

	@Override
	public AbsListView getRefreshView() {
		return listView;
	}

	@Override
	protected int inflateContentView() {
		return R.layout.comm_lay_listview;
	}

	protected ListView getListView() {
		return listView;
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
        if (listView.getAdapter() == null) {
            listView.setAdapter(getAdapter());
        }
        else {
            getListView().setSelectionFromTop(0, 0);
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
