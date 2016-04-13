package org.aisen.weibo.sina.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangdan on 16/2/2.
 */
public class MultiScrollListView extends ListView implements AbsListView.OnScrollListener {

    private List<OnScrollListener> onScrollListeners;

    public MultiScrollListView(Context context) {
        super(context);
    }

    public MultiScrollListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MultiScrollListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        super.setOnScrollListener(this);

        if (onScrollListeners == null) {
            onScrollListeners = new ArrayList<>();
        }

        if (!onScrollListeners.contains(l)) {
            onScrollListeners.add(l);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        for (OnScrollListener onScrollListener : onScrollListeners) {
            onScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        for (OnScrollListener onScrollListener : onScrollListeners) {
            onScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

}
