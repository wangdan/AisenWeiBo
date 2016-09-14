package org.aisen.android.ui.fragment.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.aisen.android.support.inject.InjectUtility;
import org.aisen.android.ui.fragment.itemview.IITemView;

import java.io.Serializable;

/**
 * Created by wangdan on 16/1/5.
 */
public abstract class ARecycleViewItemView<T extends Serializable> extends RecyclerView.ViewHolder implements IITemView<T> {

    private int size;

    private int position;

    private View convertView;

    private Activity context;

    private T data;

    public ARecycleViewItemView(Activity context, View itemView) {
        super(itemView);

        this.context = context;
        this.convertView = itemView;
    }

    @Override
    public void onBindView(View convertView) {
        InjectUtility.initInjectedView(getContext(), this, convertView);
    }

    @Override
    public void reset(int size, int position, T data) {
        this.size = size;
        this.position = position;
        this.data = data;
    }

    @Override
    public int itemSize() {
        return size;
    }

    @Override
    public int itemPosition() {
        return position;
    }

    @Override
    public View getConvertView() {
        return convertView;
    }

    public Activity getContext() {
        return context;
    }

    public T getData() {
        return data;
    }

}
