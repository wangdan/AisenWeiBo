package org.aisen.weibo.sina.ui.fragment.search;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import com.lapism.searchview.adapter.SearchAdapter;
import com.lapism.searchview.adapter.SearchItem;
import com.lapism.searchview.view.SearchCodes;

import org.aisen.android.common.utils.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangdan on 16/2/25.
 */
public class SearchsSuggestAdapter extends SearchAdapter {

    private Context context;
    private int theme;
    private List<SearchItem> searchList = new ArrayList<>();
    private List<Integer> startList = new ArrayList<>();
    private int keyLength = 0;

    public SearchsSuggestAdapter(Context context, List<SearchItem> searchList, List<SearchItem> dataList, int theme) {
        super(context, searchList, dataList, theme);
        
        this.context = context;
        this.theme = theme;
        this.searchList = searchList;
    }

    @Override
    public void onBindViewHolder(ResultViewHolder viewHolder, int position) {
        super.onBindViewHolder(viewHolder, position);
//        SearchItem item = searchList.get(position);
//
//        if (startList == null) {
//            try {
//                Field startListField = SearchsSuggestAdapter.class.getSuperclass().getDeclaredField("mStartList");
//                startListField.setAccessible(true);
//                startList = (List<Integer>) startListField.get(this);
//
//                Field keyLengthField = SearchsSuggestAdapter.class.getSuperclass().getDeclaredField("mKeyLength");
//                keyLengthField.setAccessible(true);
//                keyLength = Integer.parseInt(keyLengthField.get(this).toString());
//            } catch (Exception e) {
//                Logger.printExc(SearchsSuggestAdapter.class, e);
//            }
//        }
//
//        int start = 0;
//        int end = 0;
//        if (startList.size() > 0) {
//            start = startList.get(position);
//            end = start + keyLength;
//        }
//
//        viewHolder.icon_left.setImageResource(item.get_icon());
//
//        if (theme == SearchCodes.THEME_LIGHT) {
//            viewHolder.icon_left.setColorFilter(ContextCompat.getColor(context, com.lapism.searchview.R.color.search_light_icon));
//            viewHolder.text.setTextColor(ContextCompat.getColor(context, com.lapism.searchview.R.color.search_light_text));
//
//            viewHolder.text.setText(item.get_text(), TextView.BufferType.SPANNABLE);
//            Spannable s = (Spannable) viewHolder.text.getText();
//            s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, com.lapism.searchview.R.color.search_light_text_highlight)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        }
//        if (theme == SearchCodes.THEME_DARK) {
//            viewHolder.icon_left.setColorFilter(ContextCompat.getColor(context, com.lapism.searchview.R.color.search_dark_icon));
//            viewHolder.text.setTextColor(ContextCompat.getColor(context, com.lapism.searchview.R.color.search_dark_text));
//
//            viewHolder.text.setText(item.get_text(), TextView.BufferType.SPANNABLE);
//            Spannable s = (Spannable) viewHolder.text.getText();
//            s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, com.lapism.searchview.R.color.search_dark_text_highlight)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        }
    }

}
