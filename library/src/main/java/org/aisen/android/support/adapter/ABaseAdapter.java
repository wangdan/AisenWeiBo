package org.aisen.android.support.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.aisen.android.support.inject.InjectUtility;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class ABaseAdapter<T extends Serializable> extends BaseAdapter {

    private Context context;
    private ArrayList<T> datas;

    public ABaseAdapter(ArrayList<T> datas, Activity context) {
        if (datas == null)
            datas = new ArrayList<T>();
        this.datas = datas;
        this.context = context;
    }

    abstract protected AItemView<T> newItemView();

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AItemView<T> itemView;

        if (convertView == null) {
            itemView = newItemView();

            convertView = View.inflate(context, itemView.inflateViewId(), null);
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

    public void setDatas(ArrayList<T> datas) {
        this.datas = datas;
    }

    public void setDatasAndRefresh(ArrayList<T> datas) {
        setDatas(datas);
        notifyDataSetChanged();
    }

    public void addItem(T entry) {
        datas.add(entry);
    }

    public void addItemAndRefresh(T entry) {
        addItem(entry);
        notifyDataSetChanged();
    }

    public void addItems(List<T> entries) {
        for (T entry : entries)
            datas.add(entry);
    }

    public void addItemsAndRefresh(List<T> entries) {
        addItems(entries);
        notifyDataSetChanged();
    }

    public void addItem(T entry, int to) {
        datas.add(to, entry);
    }

    public void addItemsAndRefresh(T entry, int to) {
        datas.add(to, entry);
        notifyDataSetChanged();
    }

    public void addItemAtFront(T entry) {
        datas.add(0, entry);
    }

    public void addItemAtFrontAndRefresh(T entry) {
        addItemAtFront(entry);
        notifyDataSetChanged();
    }

    public void addItemsAtFront(List<T> entries) {
        for (int i = entries.size() - 1; i >= 0; i--) {
            datas.add(0, entries.get(i));
        }
    }

    public void addItemsAtFrontAndRefresh(List<T> entries) {
        addItemsAtFront(entries);
        notifyDataSetChanged();
    }

    public void removeItem(int index) {
        datas.remove(index);
    }

    public void removeItemAndRefresh(int index) {
        removeItem(index);
        notifyDataSetChanged();
    }

    public void removeItemAndRefresh(T entry) {
        removeItem(entry);
        notifyDataSetChanged();
    }

    public void removeItem(T entry) {
        datas.remove(entry);
    }

    protected void itemIsEmpty() {
    }

    abstract public static class AItemView<T extends Serializable> {

        private int position;
        private int size;
        private View convertView;

        /**
         * ItemView的layoutId
         *
         * @return
         */
        abstract public int inflateViewId();

        /**
         * 绑定ViewHolder视图
         *
         * @param convertView
         */
        public void bindingView(View convertView) {
            InjectUtility.initInjectedView(this, convertView);
        }

        /**
         * 绑定ViewHolder数据
         *
         * @param data
         */
        abstract public void bindingData(View convertView, T data, int position);

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public int getSize() {
            return size;
        }

        public void recycleView(View view) {

        }

        public View getConvertView() {
            return convertView;
        }

    }

}
