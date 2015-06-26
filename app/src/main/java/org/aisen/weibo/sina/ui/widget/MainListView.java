package org.aisen.weibo.sina.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import org.aisen.android.common.utils.Utils;
import org.aisen.android.ui.fragment.ASwipeRefreshListFragment;

import org.aisen.weibo.sina.ui.activity.basic.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 首页的ListView
 *
 */
public class MainListView extends ListView implements OnScrollListener {

	public static final String TAG = "MainListView";

    private MainActivity mMainActivity;

    private ASwipeRefreshListFragment mFragment;
	
	private List<OnScrollListener> onScrollListeners = new ArrayList<OnScrollListener>();

	public MainListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public MainListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MainListView(Context context) {
		super(context);
		init();
	}

    public void setFragment(ASwipeRefreshListFragment fragment) {
        this.mFragment = fragment;
    }

	private void init() {
        if (getContext() instanceof MainActivity) {
            mMainActivity = (MainActivity) getContext();
        }
	}

	@Override
	public void setOnScrollListener(OnScrollListener l) {
		super.setOnScrollListener(this);

		if (l != this && !onScrollListeners.contains(l))
			onScrollListeners.add(l);
	}

    private void toggleToolbarShown(boolean shown) {
        lastTop = 0;

        if (mMainActivity != null && mFragment != null && !mFragment.isRefreshing())
            mMainActivity.toggleToolbarShown(shown);
    }

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		for (OnScrollListener listener : onScrollListeners)
			listener.onScrollStateChanged(view, scrollState);

        this.scrollState = scrollState;

        if (scrollState == SCROLL_STATE_IDLE) {
            isMoving = false;
            lastTop = 0;
        }
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		for (OnScrollListener listener : onScrollListeners)
			listener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);

        if (getChildCount() == 0)
            return;

        if (isMoving) {
            View firstChild = view.getChildAt(0);

            if (firstVisibleItem == 0) {
                toggleToolbarShown(true);
            }
            else if (firstVisibleItem > lastFirstVisibleItem) {
                toggleToolbarShown(false);
            }
            else if (firstVisibleItem < lastFirstVisibleItem) {
                toggleToolbarShown(true);
            }
            else {
                int height = firstChild.getHeight();
                if (height > Utils.dip2px(200)) {
                    if (lastTop == 0) {
                        lastTop = firstChild.getTop();
                    }
                    else {
                        int diffTop = firstChild.getTop() - lastTop;
                        if (Math.abs(diffTop) >= Utils.dip2px(150)) {
                           toggleToolbarShown(diffTop > 0);
                        }
                    }
                }
            }
        }

        lastFirstVisibleItem = firstVisibleItem;
	}

    int lastFirstVisibleItem = 0;
    int lastTop = 0;
    boolean isMoving = false;
    int scrollState = SCROLL_STATE_IDLE;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
        }
        else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            isMoving = true;
        }
        else if (ev.getAction() == MotionEvent.ACTION_UP) {
        }

        return super.onTouchEvent(ev);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        mMainActivity = null;
        mFragment = null;
        onScrollListeners.clear();
    }
}
