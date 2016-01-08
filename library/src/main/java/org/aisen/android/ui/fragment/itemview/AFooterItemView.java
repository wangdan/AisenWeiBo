package org.aisen.android.ui.fragment.itemview;

import android.view.View;

import org.aisen.android.ui.fragment.adapter.ARecycleViewItemView;

import java.io.Serializable;

/**
 * FooterView
 *
 * Created by wangdan on 16/1/9.
 */
public abstract class AFooterItemView<T extends Serializable> extends ARecycleViewItemView<T>
                                                                implements OnFooterViewListener {

    private OnFooterViewCallback onFooterViewCallback;

    public AFooterItemView(View itemView, OnFooterViewCallback callback) {
        super(itemView);

        this.onFooterViewCallback = callback;
    }

    protected OnFooterViewCallback getCallback() {
        return onFooterViewCallback;
    }

    public interface OnFooterViewCallback {

        void onLoadMore();

        boolean canLoadMore();

    }

}
