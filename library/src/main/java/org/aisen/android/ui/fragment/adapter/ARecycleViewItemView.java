package org.aisen.android.ui.fragment.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.aisen.android.support.inject.InjectUtility;

import java.io.Serializable;

/**
 * Created by wangdan on 16/1/5.
 */
public abstract class ARecycleViewItemView<T extends Serializable> extends RecyclerView.ViewHolder implements IITemView<T> {

    private int size;

    private int position;

    private View convertView;

    public ARecycleViewItemView(View itemView) {
        super(itemView);

        this.convertView = itemView;
    }

    @Override
    public void onBindView(View convertView) {
        InjectUtility.initInjectedView(this, convertView);
    }

    @Override
    public void reset(int size, int position) {
        this.size = size;
        this.position = position;
    }

    @Override
    public int itemSize() {
        return size;
    }

    @Override
    public View getConvertView() {
        return convertView;
    }

}
