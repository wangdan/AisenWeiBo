package org.aisen.android.ui.fragment.itemview;

import android.view.View;
import android.widget.TextView;

import org.aisen.android.R;
import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.inject.InjectUtility;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.android.ui.fragment.APagingFragment;

import java.io.Serializable;

/**
 * Created by wangdan on 16/1/9.
 */
public class BasicFooterView<T extends Serializable> extends AFooterItemView<T> {

    public static final int LAYOUT_RES = R.layout.comm_lay_footerview;

    private View footerView;

    @ViewInject(idStr = "btnMore")
    View btnMore;
    @ViewInject(idStr = "layLoading")
    View layLoading;

    public BasicFooterView(View itemView, OnFooterViewCallback callback) {
        super(itemView, callback);

        this.footerView = itemView;
    }

    @Override
    public void onBindView(View convertView) {
        InjectUtility.initInjectedView(this, getConvertView());

        btnMore.setVisibility(View.VISIBLE);
        layLoading.setVisibility(View.GONE);

        btnMore.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (getCallback() != null) {
                    getCallback().onLoadMore();
                }
            }

        });
    }

    @Override
    public void onBindData(View convertView, T data, int position) {
    }

    @Override
    public View getConvertView() {
        return footerView;
    }

    @Override
    public void onTaskStateChanged(AFooterItemView<?> footerItemView, ABaseFragment.ABaseTaskState state, TaskException exception, APagingFragment.RefreshMode mode) {
        if (state == ABaseFragment.ABaseTaskState.finished) {
            View layLoading = footerView.findViewById(R.id.layLoading);
            TextView btnLoadMore = (TextView) footerView.findViewById(R.id.btnMore);
            layLoading.setVisibility(View.GONE);
            btnLoadMore.setVisibility(View.VISIBLE);
        }
        else if (state == ABaseFragment.ABaseTaskState.prepare) {
            View layLoading = footerView.findViewById(R.id.layLoading);
            TextView txtLoadingHint = (TextView) footerView.findViewById(R.id.txtLoading);
            TextView btnLoadMore = (TextView) footerView.findViewById(R.id.btnMore);

            txtLoadingHint.setText(loadingText());
            layLoading.setVisibility(View.VISIBLE);
            btnLoadMore.setVisibility(View.GONE);
            btnLoadMore.setText(moreText());
        }
        else if (state == ABaseFragment.ABaseTaskState.success) {
            final TextView btnLoadMore = (TextView) footerView.findViewById(R.id.btnMore);
            if (!getCallback().canLoadMore()) {
                btnLoadMore.setText(endpagingText());
            } else {
                btnLoadMore.setText(moreText());
            }
        }
        else if (state == ABaseFragment.ABaseTaskState.falid) {
            // 正在加载
            View layLoading = footerView.findViewById(R.id.layLoading);
            if (layLoading.getVisibility() == View.VISIBLE) {
                TextView btnLoadMore = (TextView) footerView.findViewById(R.id.btnMore);
                btnLoadMore.setText(faildText());
            }
        }
    }

    @Override
    public void setFooterViewToRefreshing() {
        if (layLoading.getVisibility() != View.VISIBLE) {
            layLoading.setVisibility(View.VISIBLE);

            getCallback().onLoadMore();
        }
    }

    protected String moreText() {
        return GlobalContext.getInstance().getString(R.string.comm_footer_more);
    }

    protected String loadingText() {
        return GlobalContext.getInstance().getString(R.string.comm_footer_loading);
    }

    protected String endpagingText() {
        return GlobalContext.getInstance().getString(R.string.comm_footer_pagingend);
    }

    protected String faildText() {
        return GlobalContext.getInstance().getString(R.string.comm_footer_faild);
    }

}
