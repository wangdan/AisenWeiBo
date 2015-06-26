package org.aisen.weibo.sina.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangdan on 15/5/2.
 */
public class MutScrollListView extends ListView implements AbsListView.OnScrollListener {

    private List<OnScrollListener> onScrollListeners = new ArrayList<OnScrollListener>();

    public MutScrollListView(Context context) {
        super(context);
    }

    public MutScrollListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MutScrollListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        super.setOnScrollListener(this);

        if (l != this && !onScrollListeners.contains(l))
            onScrollListeners.add(l);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        for (OnScrollListener listener : onScrollListeners)
            listener.onScrollStateChanged(view, scrollState);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        for (OnScrollListener listener : onScrollListeners)
            listener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
    }

}
