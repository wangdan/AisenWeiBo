package org.aisen.android.ui.fragment.itemview;

import android.view.View;

import java.io.Serializable;

/**
 * Created by wangdan on 16/1/9.
 */
public interface IItemViewCreator<T extends Serializable> {

    /**
     * 遇到一个先有鸡还是先有蛋的问题，操蛋的RecycleView.Adapter，新增这个方法来处理，返回ItemView的LayoutRes
     * 暂时只支持一个ItemType
     *
     * @return new int[][]{ { ItemLayoutRes, ItemType } }
     */
    int[][] setLayoutRes();

    IITemView<T> newItemView(View convertView, int viewType);

}
