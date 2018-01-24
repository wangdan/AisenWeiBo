package org.aisen.android.ui.fragment.itemview;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import org.aisen.android.R;
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
    TextView btnMore;
    @ViewInject(idStr = "layLoading")
    View layLoading;
    @ViewInject(idStr = "txtLoading")
    TextView txtLoading;

    public BasicFooterView(Activity context, View itemView, OnFooterViewCallback callback) {
        super(context, itemView, callback);

        this.footerView = itemView;

        InjectUtility.initInjectedView(getContext(), this, getConvertView());

        btnMore.setVisibility(View.VISIBLE);
        layLoading.setVisibility(View.GONE);

        btnMore.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (getCallback() != null && getCallback().canLoadMore()) {
                    getCallback().onLoadMore();
                }
            }

        });
    }

    @Override
    public void onBindView(View convertView) {

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
            if (mode == APagingFragment.RefreshMode.update) {
                if (layLoading.getVisibility() == View.VISIBLE) {
                    layLoading.setVisibility(View.GONE);
                    btnMore.setVisibility(View.VISIBLE);
                }
            }
        }
        else if (state == ABaseFragment.ABaseTaskState.prepare) {
            if (mode == APagingFragment.RefreshMode.update) {
                txtLoading.setText(loadingText());
                layLoading.setVisibility(View.VISIBLE);
                btnMore.setVisibility(View.GONE);
                btnMore.setText(moreText());
            }
        }
        else if (state == ABaseFragment.ABaseTaskState.success) {
            if ((mode == APagingFragment.RefreshMode.update || mode == APagingFragment.RefreshMode.reset)) {
                if (!getCallback().canLoadMore()) {
                    btnMore.setText(endpagingText());
                } else {
                    btnMore.setText(moreText());
                }
            }
        }
        else if (state == ABaseFragment.ABaseTaskState.falid) {
            if (mode == APagingFragment.RefreshMode.update) {
                if (layLoading.getVisibility() == View.VISIBLE) {
                    btnMore.setText(faildText());
                }
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
        return getContext().getString(R.string.comm_footer_more);
    }

    protected String loadingText() {
        return getContext().getString(R.string.comm_footer_loading);
    }

    protected String endpagingText() {
        return getContext().getString(R.string.comm_footer_pagingend);
    }

    protected String faildText() {
        return getContext().getString(R.string.comm_footer_faild);
    }

}
