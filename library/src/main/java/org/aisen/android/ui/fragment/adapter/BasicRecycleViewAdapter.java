package org.aisen.android.ui.fragment.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import org.aisen.android.R;
import org.aisen.android.ui.fragment.APagingFragment;
import org.aisen.android.ui.fragment.itemview.AHeaderItemViewCreator;
import org.aisen.android.ui.fragment.itemview.IITemView;
import org.aisen.android.ui.fragment.itemview.IItemViewCreator;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 1、支持RecycleView
 * 2、支持ViewType，默认是Normal Type
 *
 * Created by wangdan on 16/1/5.
 */
public class BasicRecycleViewAdapter<T extends Serializable> extends RecyclerView.Adapter implements IPagingAdapter {

    private APagingFragment holderFragment;
    private IItemViewCreator<T> itemViewCreator;
    private ArrayList<T> datas;
    private IITemView<T> footerItemView;

    private AHeaderItemViewCreator<T> headerItemViewCreator;
    private int[][] headerItemTypes;

    private AdapterView.OnItemClickListener onItemClickListener;
    private AdapterView.OnItemLongClickListener onItemLongClickListener;

    public BasicRecycleViewAdapter(APagingFragment holderFragment, IItemViewCreator<T> itemViewCreator, ArrayList<T> datas) {
        if (datas == null)
            datas = new ArrayList<T>();
        this.holderFragment = holderFragment;
        this.itemViewCreator = itemViewCreator;
        this.datas = datas;
        notifyDataSetChanged();
    }

    public void addFooterView(IITemView<T> footerItemView) {
        this.footerItemView = footerItemView;
        if (footerItemView.getConvertView().getLayoutParams() == null) {
            footerItemView.getConvertView().setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        }
    }

    public void setHeaderItemViewCreator(AHeaderItemViewCreator<T> headerItemViewCreator) {
        this.headerItemViewCreator = headerItemViewCreator;
        headerItemTypes = headerItemViewCreator.setHeaderLayoutRes();
    }

    @Override
    public int getItemViewType(int position) {
        if (footerItemView != null && position == getItemCount() - 1) {
            return IPagingAdapter.TYPE_FOOTER;
        }
        else if (headerItemViewCreator != null && position < headerItemTypes.length) {
            return headerItemTypes[position][1];
        }

        return IPagingAdapter.TYPE_NORMAL;
    }

    private boolean isHeaderType(int viewType) {
        if (headerItemTypes != null) {
            for (int[] itemResAndType : headerItemTypes) {
                if (viewType == itemResAndType[1]) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isFooterType(int viewType) {
        return viewType == IPagingAdapter.TYPE_FOOTER;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int[][] itemResAndTypeArr = itemViewCreator.setLayoutRes();

        int itemRes = -1;
        int itemType = viewType;

        if (!isHeaderType(viewType) && !isFooterType(viewType)) {
            for (int[] itemResAndType : itemResAndTypeArr) {
                if (itemType == itemResAndType[1]) {
                    itemRes = itemResAndType[0];

                    break;
                }
            }

            if (itemRes == -1) {
                throw new RuntimeException("没有找到ViewRes，ViewType = " + itemType);
            }
        }

        View convertView;
        IITemView<T> itemView;
        if (viewType == IPagingAdapter.TYPE_FOOTER) {
            itemView = footerItemView;

            convertView = itemView.getConvertView();
        }
        else if (isHeaderType(viewType)) {
            for (int[] itemResAndType : headerItemTypes) {
                if (itemType == itemResAndType[1]) {
                    itemRes = itemResAndType[0];

                    break;
                }
            }

            convertView = LayoutInflater.from(holderFragment.getActivity()).inflate(itemRes, parent, false);

            itemView = headerItemViewCreator.newItemView(convertView, viewType);
            convertView.setTag(R.id.itemview, itemView);
        }
        else {
            convertView = LayoutInflater.from(holderFragment.getActivity()).inflate(itemRes, parent, false);

            itemView = itemViewCreator.newItemView(convertView, viewType);
            convertView.setTag(R.id.itemview, itemView);
        }
        itemView.onBindView(convertView);

        if (!(itemView instanceof ARecycleViewItemView)) {
            throw new RuntimeException("RecycleView只支持ARecycleViewItemView，请重新配置");
        }

        return (ARecycleViewItemView) itemView;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ARecycleViewItemView itemView = (ARecycleViewItemView) holder;

        int headerCount = headerItemTypes != null ? headerItemTypes.length : 0;

        if (position >= headerCount) {
            int realPosition = position - headerCount;

            itemView.reset(datas.size(), realPosition);
            if (realPosition < datas.size()) {
                itemView.onBindData(itemView.getConvertView(), datas.get(realPosition), realPosition);
            }

            if (onItemClickListener != null) {
                itemView.getConvertView().setOnClickListener(innerOnClickListener);
            }
            else {
                itemView.getConvertView().setOnClickListener(null);
            }
            if (onItemLongClickListener != null) {
                itemView.getConvertView().setOnLongClickListener(innerOnLongClickListener);
            }
            else {
                itemView.getConvertView().setOnLongClickListener(null);
            }
        }
    }

    View.OnClickListener innerOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            IITemView<T> itemView = (IITemView<T>) v.getTag(R.id.itemview);

            if (onItemClickListener != null && itemView != null) {
                onItemClickListener.onItemClick(null, itemView.getConvertView(),
                                                    itemView.itemPosition(), getItemId(itemView.itemPosition()));
            }
        }

    };

    View.OnLongClickListener innerOnLongClickListener = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            IITemView<T> itemView = (IITemView<T>) v.getTag(R.id.itemview);

            if (onItemLongClickListener != null) {
                return onItemLongClickListener.onItemLongClick(null, itemView.getConvertView(),
                        itemView.itemPosition(), getItemId(itemView.itemPosition()));
            }

            return false;
        }

    };

    @Override
    public int getItemCount() {
        int footerCount = footerItemView == null ? 0 : 1;
        int headerCount = headerItemTypes != null ? headerItemTypes.length : 0;

        return datas.size() + footerCount + headerCount;
    }

    @Override
    public ArrayList<T> getDatas() {
        return datas;
    }

    public AdapterView.OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public AdapterView.OnItemLongClickListener getOnItemLongClickListener() {
        return onItemLongClickListener;
    }

    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

}
