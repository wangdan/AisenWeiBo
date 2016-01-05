package org.aisen.android.ui.fragment.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import org.aisen.android.ui.fragment.APagingFragment;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by wangdan on 16/1/5.
 */
public class BaiscRecycleViewAdapter<T extends Serializable> extends RecyclerView.Adapter implements IPagingAdapter {

    private APagingFragment holderFragment;
    private ArrayList<T> datas;

    public BaiscRecycleViewAdapter(APagingFragment holderFragment, ArrayList<T> datas) {
        if (datas == null)
            datas = new ArrayList<T>();
        this.holderFragment = holderFragment;
        this.datas = datas;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = View.inflate(holderFragment.getActivity(), holderFragment.configItemViewRes(), null);
        IITemView<T> itemView = holderFragment.newItemView(convertView);
        itemView.bindingView(convertView);

        if (!(itemView instanceof ARecycleViewItemView)) {
            throw new RuntimeException("RecycleView只支持ARecycleViewItemView，请重新配置");
        }

        return (ARecycleViewItemView) itemView;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ARecycleViewItemView itemView = (ARecycleViewItemView) holder;

        itemView.reset(datas.size(), position);
        itemView.onBindData(itemView.getConvertView(), datas.get(position), position);
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    @Override
    public ArrayList getDatas() {
        return datas;
    }

    @Override
    public int getCount() {
        return getItemCount();
    }

}
