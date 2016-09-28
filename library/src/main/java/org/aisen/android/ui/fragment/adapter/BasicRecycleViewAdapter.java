package org.aisen.android.ui.fragment.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
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

    private IItemViewCreator<T> itemViewCreator;
    private ArrayList<T> datas;
    private IITemView<T> footerItemView;

    private AHeaderItemViewCreator<T> headerItemViewCreator;
    private int[][] headerItemTypes;

    private AdapterView.OnItemClickListener onItemClickListener;
    private AdapterView.OnItemLongClickListener onItemLongClickListener;

    private final Activity activity;
    private final APagingFragment ownerFragment;

    public BasicRecycleViewAdapter(Activity activity, APagingFragment ownerFragment, IItemViewCreator<T> itemViewCreator, ArrayList<T> datas) {
        this.activity = activity;
        if (datas == null)
            datas = new ArrayList<T>();
        this.itemViewCreator = itemViewCreator;
        this.ownerFragment = ownerFragment;
        this.datas = datas;
    }

    public void addFooterView(IITemView<T> footerItemView) {
        this.footerItemView = footerItemView;
        if (footerItemView.getConvertView().getLayoutParams() == null) {
            footerItemView.getConvertView().setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        }
    }

    public void setHeaderItemViewCreator(AHeaderItemViewCreator<T> headerItemViewCreator) {
        this.headerItemViewCreator = headerItemViewCreator;
        headerItemTypes = headerItemViewCreator.setHeaders();
    }

    @Override
    public int getItemViewType(int position) {
        if (footerItemView != null && position == getItemCount() - 1) {
            return IPagingAdapter.TYPE_FOOTER;
        }
        else if (headerItemViewCreator != null && position < headerItemTypes.length) {
            return headerItemTypes[position][1];
        }

        int headerCount = headerItemTypes != null ? headerItemTypes.length : 0;
        if (position >= headerCount) {
            int realPosition = position - headerCount;

            T t = getDatas().get(realPosition);
            if (t instanceof ItemTypeData) {
                return ((ItemTypeData) t).itemType();
            }
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
        View convertView;
        IITemView<T> itemView;

        if (isFooterType(viewType)) {
            itemView = footerItemView;

            convertView = itemView.getConvertView();

            if (ownerFragment.getRefreshView() != null && ownerFragment.getRefreshView() instanceof RecyclerView) {
                RecyclerView recyclerView = (RecyclerView) ownerFragment.getRefreshView();
                if (recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
                    StaggeredGridLayoutManager.LayoutParams layoutParams;
                    if (convertView.getLayoutParams() == null || !(convertView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams)) {
                        layoutParams = new StaggeredGridLayoutManager.LayoutParams(StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT, StaggeredGridLayoutManager.LayoutParams.WRAP_CONTENT);
                        convertView.setLayoutParams(layoutParams);
                    }
                    else {
                        layoutParams = (StaggeredGridLayoutManager.LayoutParams) convertView.getLayoutParams();
                    }
                    if (!layoutParams.isFullSpan()) {
                        layoutParams.setFullSpan(true);
                    }
                }
            }
        }
        else if (isHeaderType(viewType)) {
            convertView = headerItemViewCreator.newContentView(activity.getLayoutInflater(), parent, viewType);

            itemView = headerItemViewCreator.newItemView(convertView, viewType);
            convertView.setTag(R.id.itemview, itemView);
        }
        else {
            convertView = itemViewCreator.newContentView(activity.getLayoutInflater(), parent, viewType);

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

    public T getData(int position) {
        return datas.get(position);
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
