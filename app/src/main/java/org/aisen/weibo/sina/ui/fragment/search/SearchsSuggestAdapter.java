package org.aisen.weibo.sina.ui.fragment.search;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;

import com.lapism.searchview.adapter.SearchAdapter;
import com.lapism.searchview.adapter.SearchItem;
import com.lapism.searchview.view.SearchCodes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangdan on 16/2/25.
 */
public class SearchsSuggestAdapter extends SearchAdapter {

    private final List<Integer> mStartList = new ArrayList<>();
    private final Context mContext;
    private final int mTheme;
    private List<SearchItem> mSearchList = new ArrayList<>();
    private List<SearchItem> mDataList = new ArrayList<>();

    public SearchsSuggestAdapter(Context context, List<SearchItem> searchList, List<SearchItem> dataList, int theme) {
        super(context, searchList, dataList, theme);
        this.mContext = context;
        this.mSearchList = searchList;
        this.mDataList = dataList;
        this.mTheme = theme;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
            }
        };
    }

    @Override
    public ResultViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        final LayoutInflater mInflater = LayoutInflater.from(parent.getContext());
        final View sView = mInflater.inflate(com.lapism.searchview.R.layout.search_item, parent, false);
        return new ResultViewHolder(sView);
    }

    @Override
    public void onBindViewHolder(ResultViewHolder viewHolder, int position) {
        SearchItem item = mSearchList.get(position);

        int start = 0;
        int end = 0;

        viewHolder.icon_left.setImageResource(item.get_icon());

        if (mTheme == SearchCodes.THEME_LIGHT) {
            viewHolder.icon_left.setColorFilter(ContextCompat.getColor(mContext, com.lapism.searchview.R.color.search_light_icon));
            viewHolder.text.setTextColor(ContextCompat.getColor(mContext, com.lapism.searchview.R.color.search_light_text));

            viewHolder.text.setText(item.get_text(), TextView.BufferType.SPANNABLE);
            Spannable s = (Spannable) viewHolder.text.getText();
//            s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, com.lapism.searchview.R.color.search_light_text_highlight)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (mTheme == SearchCodes.THEME_DARK) {
            viewHolder.icon_left.setColorFilter(ContextCompat.getColor(mContext, com.lapism.searchview.R.color.search_dark_icon));
            viewHolder.text.setTextColor(ContextCompat.getColor(mContext, com.lapism.searchview.R.color.search_dark_text));

            viewHolder.text.setText(item.get_text(), TextView.BufferType.SPANNABLE);
            Spannable s = (Spannable) viewHolder.text.getText();
            s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, com.lapism.searchview.R.color.search_dark_text_highlight)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    @Override
    public int getItemCount() {
        return mSearchList.size();
    }

    public void setSearchList(List<SearchItem> searchList) {
        this.mSearchList = searchList;
        notifyDataSetChanged();
    }

}
