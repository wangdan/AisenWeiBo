package org.aisen.android.ui.fragment.itemview;

import android.app.Activity;
import android.content.Context;
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

    public AFooterItemView(Activity context, View itemView, OnFooterViewCallback callback) {
        super(context, itemView);

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
