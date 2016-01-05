package org.aisen.android.ui.fragment.adapter;

import android.view.View;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wangdan on 16/1/5.
 */
public interface IITemView<T extends Serializable> {

    /**
     * 将View绑定到属性
     *
     * @param convertView
     */
    void bindingView(View convertView);

    /**
     * 将Data绑定到View
     *
     * @param convertView
     * @param data
     * @param position
     */
    void onBindData(View convertView, T data, int position);

    /**
     * Item的Position
     *
     * @return
     */
    int getPosition();

    /**
     * 重置数据
     *
     * @return
     */
    void reset(int size, int position);

    /**
     * Item的数据size
     *
     * @return
     */
    int itemSize();

    /**
     * Item的ConvertView
     *
     * @return
     */
    View getConvertView();

}
