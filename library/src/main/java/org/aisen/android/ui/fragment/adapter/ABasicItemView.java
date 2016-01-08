package org.aisen.android.ui.fragment.adapter;

import android.view.View;

import org.aisen.android.support.inject.InjectUtility;

import java.io.Serializable;

/**
 * Created by wangdan on 16/1/5.
 */
public abstract class ABasicItemView<T extends Serializable> implements IITemView<T> {

    private int size;

    private int position;

    private View convertView;

    public ABasicItemView(View convertView) {
        this.convertView = convertView;
    }

    @Override
    public void onBindView(View convertView) {
        InjectUtility.initInjectedView(this, convertView);
    }

    @Override
    public int itemPosition() {
        return position;
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
