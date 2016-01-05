package org.aisen.android.ui.fragment.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.aisen.android.ui.fragment.APagingFragment;

import java.io.Serializable;
import java.util.ArrayList;

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
    public View getView(int position, View convertView, ViewGroup parent) {
        IITemView<T> itemView;

        if (convertView == null) {
            convertView = View.inflate(holderFragment.getActivity(), holderFragment.configItemViewRes(), null);

            itemView = holderFragment.newItemView(convertView);
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
