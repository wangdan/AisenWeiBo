package org.aisen.android.support.adapter;

import android.view.View;

import org.aisen.android.support.inject.InjectUtility;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * APagingFragment的Adapter
 *
 * Created by wangdan on 16/1/4.
 */
public interface IPagingAdapter<T extends Serializable> {

    ArrayList<T> getDatas();

    void setDatas(ArrayList<T> datas);
    
    void notifyDataSetChanged();

    int getCount();

    abstract class AItemView<T extends Serializable> {

        protected int position;
        protected int size;
        protected View convertView;

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

        public View getConvertView() {
            return convertView;
        }

    }
    
    class Utils {

        public static <T extends Serializable> void setDatasAndRefresh(IPagingAdapter<T> adapter, ArrayList<T> datas) {
            adapter.setDatas(datas);
            
            adapter.notifyDataSetChanged();
        }

        public static <T extends Serializable> void addItem(IPagingAdapter<T> adapter, T entry) {
            adapter.getDatas().add(entry);
        }

        public static <T extends Serializable> void addItemAndRefresh(IPagingAdapter<T> adapter, T entry) {
            addItem(adapter, entry);
            
            adapter.notifyDataSetChanged();
        }

        public static <T extends Serializable> void addItems(IPagingAdapter<T> adapter, List<T> entries) {
            adapter.getDatas().addAll(entries);
        }

        public static <T extends Serializable> void addItemsAndRefresh(IPagingAdapter<T> adapter, List<T> entries) {
            addItems(adapter, entries);
            adapter.notifyDataSetChanged();
        }

        public static <T extends Serializable> void addItem(IPagingAdapter<T> adapter, T entry, int to) {
            adapter.getDatas().add(to, entry);
        }

        public static <T extends Serializable> void addItemsAndRefresh(IPagingAdapter<T> adapter, T entry, int to) {
            addItem(adapter, entry, to);
            
            adapter.notifyDataSetChanged();
        }

        public static <T extends Serializable> void addItemAtFront(IPagingAdapter<T> adapter, T entry) {
            adapter.getDatas().add(0, entry);
        }

        public static <T extends Serializable> void addItemAtFrontAndRefresh(IPagingAdapter<T> adapter, T entry) {
            addItemAtFront(adapter, entry);
            
            adapter.notifyDataSetChanged();
        }

        public static <T extends Serializable> void addItemsAtFront(IPagingAdapter<T> adapter, List<T> entries) {
            for (int i = entries.size() - 1; i >= 0; i--) {
                adapter.getDatas().add(0, entries.get(i));
            }
        }

        public static <T extends Serializable> void addItemsAtFrontAndRefresh(IPagingAdapter<T> adapter, List<T> entries) {
            addItemsAtFront(adapter, entries);
            
            adapter.notifyDataSetChanged();
        }
        
    }

}
