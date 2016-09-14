package org.aisen.android.ui.fragment.itemview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.Serializable;

/**
 * Created by wangdan on 16/1/9.
 */
public interface IItemViewCreator<T extends Serializable> {

    /**
     * 遇到一个先有鸡还是先有蛋的问题，操蛋的RecycleView.Adapter，新增这个方法来处理，返回ItemView的LayoutRes
     *
     * @param inflater
     * @param viewType
     * @return
     */
    View newContentView(LayoutInflater inflater, ViewGroup parent, int viewType);

    IITemView<T> newItemView(View convertView, int viewType);

}
