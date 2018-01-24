package org.aisen.android.ui.fragment.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import org.aisen.android.support.inject.InjectUtility;
import org.aisen.android.ui.fragment.itemview.IITemView;

import java.io.Serializable;

/**
 * Created by wangdan on 16/1/5.
 */
public abstract class ABasicItemView<T extends Serializable> implements IITemView<T> {

    private final Activity context;

    private int size;

    private int position;

    private final View convertView;

    public ABasicItemView(Activity context, View convertView) {
        this.context = context;
        this.convertView = convertView;
    }

    @Override
    public void onBindView(View convertView) {
        InjectUtility.initInjectedView(context, this, convertView);
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

    public Context getContext() {
        return context;
    }

}
