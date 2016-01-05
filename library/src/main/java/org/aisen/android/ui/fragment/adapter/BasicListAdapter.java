package org.aisen.android.ui.fragment.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.aisen.android.ui.fragment.APagingFragment;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 1、支持能够使用BaseAdapter的控件
 * 2、支持ViewType，默认是Normal Type
 *
 * @param <T>
 */
public class BasicListAdapter<T extends Serializable> extends BaseAdapter implements IPagingAdapter {

    private APagingFragment holderFragment;
    private ArrayList<T> datas;

    public BasicListAdapter(APagingFragment holderFragment, ArrayList<T> datas) {
        if (datas == null)
            datas = new ArrayList<T>();
        this.holderFragment = holderFragment;
        this.datas = datas;
    }

    @Override
    public int getItemViewType(int position) {
        return IPagingAdapter.TYPE_NORMAL;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        IITemView<T> itemView;

        if (convertView == null) {
            int[][] itemResAndTypeArr = holderFragment.configItemViewAndType();

            int itemRes = -1;
            int itemType = getItemViewType(position);

            for (int[] itemResAndType : itemResAndTypeArr) {
                if (itemType == itemResAndType[1]) {
                    itemRes = itemResAndType[0];

                    break;
                }
            }

            if (itemRes == -1) {
                throw new RuntimeException("没有找到ViewRes，ViewType = " + itemType);
            }

            convertView = View.inflate(holderFragment.getActivity(), itemRes, null);

            itemView = holderFragment.newItemView(convertView, itemType);
            itemView.bindingView(convertView);

            convertView.setTag(itemView);
        } else {
            itemView = (IITemView<T>) convertView.getTag();
        }

        itemView.reset(datas.size(), position);
        itemView.onBindData(convertView, datas.get(position), position);

        return convertView;
    }

    @Override
    public ArrayList<T> getDatas() {
        return datas;
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
