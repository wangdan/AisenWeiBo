package org.aisen.android.ui.fragment;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import org.aisen.android.R;
import org.aisen.android.support.inject.ViewInject;

import java.io.Serializable;

/**
 * Created by wangdan on 15-1-19.
 */
public abstract class ASwipeRefreshListFragment<T extends Serializable, Ts extends Serializable>
                                            extends ARefreshFragment<T, Ts, ListView>
                                            implements SwipeRefreshLayout.OnRefreshListener {
    static String TAG = "ASwipeRefreshListFragment";

    @ViewInject(idStr = "swipeRefreshLayout")
    SwipeRefreshLayout swipeRefreshLayout;
    @ViewInject(idStr = "listView")
    ListView mListView;

    protected View mFooterView;

    @Override
    void _layoutInit(LayoutInflater inflater, Bundle savedInstanceState) {
        super._layoutInit(inflater, savedInstanceState);
    }

    @Override
    final protected void setInitRefreshView(AbsListView refreshView, Bundle savedInstanceSate) {
        super.setInitRefreshView(refreshView, savedInstanceSate);

        if (canFooterAutoLoadMore()) {
            mFooterView = View.inflate(getActivity(), R.layout.comm_lay_footerview, null);
            getListView().addFooterView(mFooterView);

//            final View layLoading = mFooterView.findViewById(R.id.layLoading);
//            final TextView btnLoadMore = (TextView) mFooterView.findViewById(R.id.btnLoadMore);
//            layLoading.setVisibility(View.VISIBLE);
//            btnLoadMore.setVisibility(View.GONE);
        }

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        swipeRefreshLayout.setVisibility(View.VISIBLE);

        setInitSwipeRefresh(getListView(), swipeRefreshLayout, savedInstanceSate);
    }

    protected void setInitSwipeRefresh(ListView listView, SwipeRefreshLayout swipeRefreshLayout, Bundle savedInstanceState) {

    }

    @Override
    public AbsListView getRefreshView() {
        return mListView;
    }

    @Override
    public void onRefresh() {
        onPullDownToRefresh();
    }

    @Override
    protected int inflateContentView() {
        return R.layout.comm_lay_swiperefreshlist;
    }

    private ListView getListView() {
        return (ListView) getRefreshView();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        super.onScrollStateChanged(view, scrollState);

        if (scrollState == SCROLL_STATE_FLING) {
        } else if (scrollState == SCROLL_STATE_IDLE) {
        	if (canFooterAutoLoadMore() && !isRefreshing()) {
        		for (int i = 0; i < getListView().getFooterViewsCount(); i++) {
                    if (getListView().getChildAt(getListView().getChildCount() - i - 1) == mFooterView) {
                        if (getRefreshConfig().canLoadMore) {
                        	final View layLoading = mFooterView.findViewById(R.id.layLoading);
                        	final TextView btnLoadMore = (TextView) mFooterView.findViewById(R.id.btnLoadMore);
                        	layLoading.setVisibility(View.VISIBLE);
                            btnLoadMore.setVisibility(View.GONE);

                            onPullUpToRefresh();
                        }

                        break;
                    }
                }
        	}
        }
    }

    @Override
    public boolean setRefreshing() {
        swipeRefreshLayout.setRefreshing(true);

        return false;
    }

    @Override
    public boolean isRefreshing() {
        if (swipeRefreshLayout.isRefreshing())
            return true;

        return super.isRefreshing();
    }

    @Override
    public void onRefreshViewComplete() {
        if (swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void taskStateChanged(ABaseTaskState state, Serializable extra) {
        super.taskStateChanged(state, extra);

        if (state == ABaseTaskState.finished) {
            onRefreshViewComplete();

        	if (canFooterAutoLoadMore()) {
        		final View layLoading = mFooterView.findViewById(R.id.layLoading);
                final TextView btnLoadMore = (TextView) mFooterView.findViewById(R.id.btnLoadMore);
                layLoading.setVisibility(View.GONE);
                btnLoadMore.setVisibility(View.VISIBLE);
        	}
        } else if (state == ABaseTaskState.prepare) {
//            if (loadingLayout != null && loadingLayout.getVisibility() != View.VISIBLE
//                    && layLoading.getVisibility() != View.VISIBLE) {
//                setRefreshing();
//            }
        }
    };

    @Override
    public void onChangedByConfig(RefreshConfig config) {
        if (canFooterAutoLoadMore() && mFooterView != null) {
        	final View layLoading = mFooterView.findViewById(R.id.layLoading);
        	TextView txtLoadingHint = (TextView) mFooterView.findViewById(R.id.txtLoadingHint);
        	final TextView btnLoadMore = (TextView) mFooterView.findViewById(R.id.btnLoadMore);
            
            if (config.canLoadMore) {
                layLoading.setVisibility(View.VISIBLE);
                btnLoadMore.setVisibility(View.GONE);
                btnLoadMore.setText(loadMoreBtnLabel());
                if (TextUtils.isEmpty(txtLoadingHint.getText()))
                    txtLoadingHint.setText(loadingLabel());
                btnLoadMore.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        layLoading.setVisibility(View.VISIBLE);
                        btnLoadMore.setVisibility(View.GONE);

                        onPullUpToRefresh();
                    }
                });
            } else {
                layLoading.setVisibility(View.GONE);
                btnLoadMore.setVisibility(View.VISIBLE);
                btnLoadMore.setText(loadDisabledLabel());
                btnLoadMore.setOnClickListener(null);
            }
        }
    }

    protected boolean canFooterAutoLoadMore() {
        return true;
    }

    protected String loadMoreBtnLabel() {
        return getString(R.string.comm_request_more);// 加载更多
    }

    protected String loadingLabel() {
        return getString(R.string.comm_request_loading);// 加载中
    }

    protected String loadDisabledLabel() {
        return getString(R.string.comm_request_disable);// 不能加载更多了
    }

}
