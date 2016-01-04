package org.aisen.android.support.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.aisen.android.ui.fragment.APagingFragment;

import java.io.Serializable;
import java.util.ArrayList;

public class BasicListAdapter<T extends Serializable> extends BaseAdapter implements IPagingAdapter {

    private APagingFragment mPagingFragment;
    private ArrayList<T> datas;

    public BasicListAdapter(APagingFragment pagingFragment, ArrayList<T> datas) {
        if (datas == null)
            datas = new ArrayList<T>();
        this.mPagingFragment = pagingFragment;
        this.datas = datas;
    }

    private AItemView<T> newItemView() {
        return mPagingFragment.newItemView();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AItemView<T> itemView;

        if (convertView == null) {
            itemView = newItemView();

            convertView = View.inflate(mPagingFragment.getActivity(), itemView.inflateViewId(), null);
            convertView.setTag(itemView);

            itemView.convertView = convertView;
            itemView.bindingView(convertView);
        } else {
            itemView = (AItemView<T>) convertView.getTag();
        }

        itemView.position = position;
        itemView.size = datas.size();
        itemView.bindingData(convertView, datas.get(position), position);

        return convertView;
    }

    @Override
    public ArrayList<T> getDatas() {
        return datas;
    }

    @Override
    public void setDatas(ArrayList datas) {
        this.datas = datas;
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
