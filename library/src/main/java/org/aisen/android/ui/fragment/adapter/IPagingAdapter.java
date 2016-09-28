package org.aisen.android.ui.fragment.adapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * APagingFragment的Adapter
 *
 * Created by wangdan on 16/1/4.
 */
public interface IPagingAdapter<T extends Serializable> {

    int TYPE_NORMAL = 1000;

    int TYPE_FOOTER = 2000;

    ArrayList<T> getDatas();

    void notifyDataSetChanged();

    class Utils {

        public static <T extends Serializable> void setDatasAndRefresh(IPagingAdapter<T> adapter, ArrayList<T> datas) {
            adapter.getDatas().clear();
            adapter.getDatas().addAll(datas);
            
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

    interface ItemTypeData {

        int itemType();

    }

}
