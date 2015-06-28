package org.aisen.android.support.adapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.aisen.android.support.inject.InjectUtility;

public abstract class ABaseAdapter<T extends Serializable> extends BaseAdapter {

    private Context context;
    private ArrayList<T> datas;

    private int selectedPosition = -1;

    private BaseAdapterHelper<T> adapterHelper;

    public ABaseAdapter(ArrayList<T> datas, Activity context) {
        if (datas == null)
            datas = new ArrayList<T>();
        this.datas = datas;
        this.context = context;
    }

    abstract protected AbstractItemView<T> newItemView();

    public void setAdapterHelper(BaseAdapterHelper<T> adapterHepler) {
        this.adapterHelper = adapterHepler;
    }

    public BaseAdapterHelper<T> getAdapterHepler() {
        return adapterHelper;
    }

    /**
     * 设置position项ItemView为selected状态
     *
     * @param position
     */
    public void setSelected(int position) {
        selectedPosition = position;
        notifyDataSetChanged();
    }

    public int getSelected() {
        return selectedPosition;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AbstractItemView<T> itemViewProcessor;

        boolean isReusing = true;
        if (convertView != null && adapterHelper != null)
            isReusing = adapterHelper.isReusing(convertView);

        if (convertView == null || !isReusing) {
            itemViewProcessor = newItemView();

            convertView = View.inflate(context, itemViewProcessor.inflateViewId(), null);
            convertView.setTag(itemViewProcessor);

            itemViewProcessor.bindingView(convertView);
        } else {
            itemViewProcessor = (AbstractItemView<T>) convertView.getTag();
        }

        itemViewProcessor.position = position;
        itemViewProcessor.size = datas.size();
        if (adapterHelper != null)
            itemViewProcessor.bindingData(convertView, adapterHelper.getItem(position, datas));
        else
            itemViewProcessor.bindingData(convertView, datas.get(position));

        if (adapterHelper != null)
            itemViewProcessor.updateConvertView(adapterHelper.getItem(position, datas), convertView, selectedPosition);
        else
            itemViewProcessor.updateConvertView(datas.get(position), convertView, selectedPosition);

        convertView.setSelected(selectedPosition == position);

        return convertView;
    }

    public ArrayList<T> getDatas() {
        return datas;
    }

    @Override
    public int getCount() {
        if (adapterHelper != null)
            return adapterHelper.getCount(datas);

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

    private abstract static class BaseAdapterHelper<T> implements Serializable {

        private static final long serialVersionUID = 8411760659150853673L;

        abstract public int getCount(List<T> datas);

        abstract public T getItem(int position, List<T> datas);

        private boolean isReusing(View convertView) {
            return true;
        }

    }

    abstract public static class AbstractItemView<T extends Serializable> {

        private int position;
        private int size;

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
        abstract public void bindingData(View convertView, T data);

        /**
         * 刷新当前ItemView视图
         *
         * @param data
         * @param convertView
         * @param selectedPosition 参照{@link org.aisen.android.component.adapter.ABaseAdapter#setSelected(int)}
         */
        public void updateConvertView(T data, View convertView, int selectedPosition) {

        }

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

    }

}
