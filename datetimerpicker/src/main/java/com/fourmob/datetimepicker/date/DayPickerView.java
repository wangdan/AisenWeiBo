package com.fourmob.datetimepicker.date;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.ListView;

public class DayPickerView extends ListView implements AbsListView.OnScrollListener, DatePickerDialog.OnDateChangedListener {

    protected static final int GOTO_SCROLL_DURATION = 250;
    protected static final int SCROLL_CHANGE_DELAY = 40;

    public static int LIST_TOP_OFFSET = -1;

    protected Context mContext;
    protected Handler mHandler = new Handler();

	protected SimpleMonthAdapter mAdapter;
	private final DatePickerController mController;

    protected int mCurrentMonthDisplayed;
    protected int mCurrentScrollState = 0;
	private boolean mPerformingScroll;
	protected long mPreviousScrollPosition;
	protected int mPreviousScrollState = 0;

	protected ScrollStateRunnable mScrollStateChangedRunnable = new ScrollStateRunnable();
	protected SimpleMonthAdapter.CalendarDay mSelectedDay = new SimpleMonthAdapter.CalendarDay();
	protected SimpleMonthAdapter.CalendarDay mTempDay = new SimpleMonthAdapter.CalendarDay();

    protected int mNumWeeks = 6;
    protected boolean mShowWeekNumber = false;
    protected int mDaysPerWeek = 7;

    protected float mFriction = 1.0F;

	public DayPickerView(Context context, DatePickerController datePickerController) {
		super(context);
		mController = datePickerController;
		mController.registerOnDateChangedListener(this);
		setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		setDrawSelectorOnTop(false);
		init(context);
		onDateChanged();
	}

	public int getMostVisiblePosition() {
        final int firstPosition = getFirstVisiblePosition();
        final int height = getHeight();

        int maxDisplayedHeight = 0;
        int mostVisibleIndex = 0;
        int i=0;
        int bottom = 0;
        while (bottom < height) {
            View child = getChildAt(i);
            if (child == null) {
                break;
            }
            bottom = child.getBottom();
            int displayedHeight = Math.min(bottom, height) - Math.max(0, child.getTop());
            if (displayedHeight > maxDisplayedHeight) {
                mostVisibleIndex = i;
                maxDisplayedHeight = displayedHeight;
            }
            i++;
        }
        return firstPosition + mostVisibleIndex;
	}

	public boolean goTo(SimpleMonthAdapter.CalendarDay day, boolean animate, boolean setSelected, boolean forceScroll) {
        // Set the selected day
        if (setSelected) {
            mSelectedDay.set(day);
        }

        mTempDay.set(day);
        final int position = (day.year - mController.getMinYear())
                * SimpleMonthAdapter.MONTHS_IN_YEAR + day.month;

        View child;
        int i = 0;
        int top = 0;
        // Find a child that's completely in the view
        do {
            child = getChildAt(i++);
            if (child == null) {
                break;
            }
            top = child.getTop();
        } while (top < 0);

        // Compute the first and last position visible
        int selectedPosition;
        if (child != null) {
            selectedPosition = getPositionForView(child);
        } else {
            selectedPosition = 0;
        }

        if (setSelected) {
            mAdapter.setSelectedDay(mSelectedDay);
        }

        // Check if the selected day is now outside of our visible range
        // and if so scroll to the month that contains it
        if (position != selectedPosition || forceScroll) {
            setMonthDisplayed(mTempDay);
            mPreviousScrollState = OnScrollListener.SCROLL_STATE_FLING;
            if (animate && Build.VERSION.SDK_INT >= 11) {
                smoothScrollToPositionFromTop(position, LIST_TOP_OFFSET, GOTO_SCROLL_DURATION);
                return true;
            } else {
                postSetSelection(position);
            }
        } else if (setSelected) {
            setMonthDisplayed(mSelectedDay);
        }
        return false;
	}

	public void init(Context paramContext) {
		mContext = paramContext;
		setUpListView();
		setUpAdapter();
		setAdapter(mAdapter);
	}

	protected void layoutChildren() {
		super.layoutChildren();
		if (mPerformingScroll) {
			mPerformingScroll = false;
		}
	}

	public void onChange() {
		setUpAdapter();
		setAdapter(mAdapter);
	}

	public void onDateChanged() {
		goTo(mController.getSelectedDay(), false, true, true);
	}

	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        SimpleMonthView child = (SimpleMonthView) view.getChildAt(0);
        if (child == null) {
            return;
        }

        // Figure out where we are
        long currScroll = view.getFirstVisiblePosition() * child.getHeight() - child.getBottom();
        mPreviousScrollPosition = currScroll;
        mPreviousScrollState = mCurrentScrollState;
	}

	public void onScrollStateChanged(AbsListView absListView, int scroll) {
		mScrollStateChangedRunnable.doScrollStateChange(absListView, scroll);
	}

	public void postSetSelection(final int position) {
		clearFocus();
		post(new Runnable() {
			public void run() {
				DayPickerView.this.setSelection(position);
			}
		});
		onScrollStateChanged(this, 0);
	}

	protected void setMonthDisplayed(SimpleMonthAdapter.CalendarDay calendarDay) {
		this.mCurrentMonthDisplayed = calendarDay.month;
		invalidateViews();
	}

	protected void setUpAdapter() {
		if (mAdapter == null) {
			mAdapter = new SimpleMonthAdapter(getContext(), mController);
        }
		mAdapter.setSelectedDay(this.mSelectedDay);
		mAdapter.notifyDataSetChanged();
	}

	protected void setUpListView() {
		setCacheColorHint(0);
		setDivider(null);
		setItemsCanFocus(true);
		setFastScrollEnabled(false);
		setVerticalScrollBarEnabled(false);
		setOnScrollListener(this);
		setFadingEdgeLength(0);
		setFrictionIfSupported(ViewConfiguration.getScrollFriction() * mFriction);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	void setFrictionIfSupported(float friction) {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			setFriction(friction);
		}
	}

    protected class ScrollStateRunnable implements Runnable {
        private int mNewState;

        /**
         * Sets up the runnable with a short delay in case the scroll state
         * immediately changes again.
         *
         * @param view The list view that changed state
         * @param scrollState The new state it changed to
         */
        public void doScrollStateChange(AbsListView view, int scrollState) {
            mHandler.removeCallbacks(this);
            mNewState = scrollState;
            mHandler.postDelayed(this, SCROLL_CHANGE_DELAY);
        }

        @Override
        public void run() {
            mCurrentScrollState = mNewState;
            // Fix the position after a scroll or a fling ends
            if (mNewState == OnScrollListener.SCROLL_STATE_IDLE
                    && mPreviousScrollState != OnScrollListener.SCROLL_STATE_IDLE
                    && mPreviousScrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                mPreviousScrollState = mNewState;
                int i = 0;
                View child = getChildAt(i);
                while (child != null && child.getBottom() <= 0) {
                    child = getChildAt(++i);
                }
                if (child == null) {
                    // The view is no longer visible, just return
                    return;
                }
                int firstPosition = getFirstVisiblePosition();
                int lastPosition = getLastVisiblePosition();
                boolean scroll = firstPosition != 0 && lastPosition != getCount() - 1;
                final int top = child.getTop();
                final int bottom = child.getBottom();
                final int midpoint = getHeight() / 2;
                if (scroll && top < LIST_TOP_OFFSET) {
                    if (bottom > midpoint) {
                        smoothScrollBy(top, GOTO_SCROLL_DURATION);
                    } else {
                        smoothScrollBy(bottom, GOTO_SCROLL_DURATION);
                    }
                }
            } else {
                mPreviousScrollState = mNewState;
            }
        }
    }

}