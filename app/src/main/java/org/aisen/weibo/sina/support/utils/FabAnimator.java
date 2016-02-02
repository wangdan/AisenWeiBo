package org.aisen.weibo.sina.support.utils;

import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AbsListView;

import com.melnykov.fab.ScrollDirectionListener;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

/**
 * Created by wangdan on 16/2/2.
 */
public class FabAnimator {

    private static final int TRANSLATE_DURATION_MILLIS = 300;

    private int duration = TRANSLATE_DURATION_MILLIS;

    private int scrollThreshold;

    private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

    private FabAnimator(View fabBtn, int scrollThreshold) {
        this.fabBtn = fabBtn;
        this.scrollThreshold = scrollThreshold;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public static FabAnimator create(View fabBtn, int scrollThreshold) {
        return new FabAnimator(fabBtn, scrollThreshold);
    }

    private View fabBtn;
    private boolean mVisible = true;

    public void attachToListView(AbsListView listView,ScrollDirectionListener scrollDirectionListener,
                          AbsListView.OnScrollListener onScrollListener) {
        AbsListViewScrollDetectorImpl scrollDetector = new AbsListViewScrollDetectorImpl();
        scrollDetector.setScrollDirectionListener(scrollDirectionListener);
        scrollDetector.setOnScrollListener(onScrollListener);
        scrollDetector.setListView(listView);
        scrollDetector.setScrollThreshold(scrollThreshold);
        listView.setOnScrollListener(scrollDetector);
    }

    public void attachToRecyclerView(RecyclerView recyclerView,
                                     ScrollDirectionListener scrollDirectionlistener,
                                     RecyclerView.OnScrollListener onScrollListener) {
        RecyclerViewScrollDetectorImpl scrollDetector = new RecyclerViewScrollDetectorImpl();
        scrollDetector.setScrollDirectionListener(scrollDirectionlistener);
        if (onScrollListener != null) {
            scrollDetector.setOnScrollListener(onScrollListener);
        }
        scrollDetector.setScrollThreshold(scrollThreshold);
        recyclerView.addOnScrollListener(scrollDetector);
    }

    private abstract class AbsListViewScrollDetector implements AbsListView.OnScrollListener {
        private int mLastScrollY;
        private int mPreviousFirstVisibleItem;
        private AbsListView mListView;
        private int mScrollThreshold;

        abstract void onScrollUp();

        abstract void onScrollDown();

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (totalItemCount == 0) return;
            if (isSameRow(firstVisibleItem)) {
                int newScrollY = getTopItemScrollY();
                boolean isSignificantDelta = Math.abs(mLastScrollY - newScrollY) > mScrollThreshold;
                if (isSignificantDelta) {
                    if (mLastScrollY > newScrollY) {
                        onScrollUp();
                    } else {
                        onScrollDown();
                    }
                }
                mLastScrollY = newScrollY;
            } else {
                if (firstVisibleItem > mPreviousFirstVisibleItem) {
                    onScrollUp();
                } else {
                    onScrollDown();
                }

                mLastScrollY = getTopItemScrollY();
                mPreviousFirstVisibleItem = firstVisibleItem;
            }
        }

        public void setScrollThreshold(int scrollThreshold) {
            mScrollThreshold = scrollThreshold;
        }

        public void setListView(AbsListView listView) {
            mListView = listView;
        }

        private boolean isSameRow(int firstVisibleItem) {
            return firstVisibleItem == mPreviousFirstVisibleItem;
        }

        private int getTopItemScrollY() {
            if (mListView == null || mListView.getChildAt(0) == null) return 0;
            View topChild = mListView.getChildAt(0);
            return topChild.getTop();
        }
    }

    private class AbsListViewScrollDetectorImpl extends AbsListViewScrollDetector {
        private ScrollDirectionListener mScrollDirectionListener;
        private AbsListView.OnScrollListener mOnScrollListener;

        private void setScrollDirectionListener(ScrollDirectionListener scrollDirectionListener) {
            mScrollDirectionListener = scrollDirectionListener;
        }

        public void setOnScrollListener(AbsListView.OnScrollListener onScrollListener) {
            mOnScrollListener = onScrollListener;
        }

        @Override
        public void onScrollDown() {
            show();
            if (mScrollDirectionListener != null) {
                mScrollDirectionListener.onScrollDown();
            }
        }

        @Override
        public void onScrollUp() {
            hide();
            if (mScrollDirectionListener != null) {
                mScrollDirectionListener.onScrollUp();
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                             int totalItemCount) {
            if (mOnScrollListener != null) {
                mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }

            super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (mOnScrollListener != null) {
                mOnScrollListener.onScrollStateChanged(view, scrollState);
            }

            super.onScrollStateChanged(view, scrollState);
        }
    }

    public void show() {
        show(true);
    }

    public void hide() {
        hide(true);
    }

    public void show(boolean animate) {
        toggle(true, animate, false);
    }

    public void hide(boolean animate) {
        toggle(false, animate, false);
    }

    private void toggle(final boolean visible, final boolean animate, boolean force) {
        if (mVisible != visible || force) {
            mVisible = visible;
            int height = fabBtn.getHeight();
            if (height == 0 && !force) {
                ViewTreeObserver vto = fabBtn.getViewTreeObserver();
                if (vto.isAlive()) {
                    vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            ViewTreeObserver currentVto = fabBtn.getViewTreeObserver();
                            if (currentVto.isAlive()) {
                                currentVto.removeOnPreDrawListener(this);
                            }
                            toggle(visible, animate, true);
                            return true;
                        }
                    });
                    return;
                }
            }
            int translationY = visible ? 0 : height + getMarginBottom();
            if (animate) {
                ViewPropertyAnimator.animate(fabBtn).setInterpolator(mInterpolator)
                        .setDuration(TRANSLATE_DURATION_MILLIS)
                        .translationY(translationY);
            } else {
                ViewHelper.setTranslationY(fabBtn, translationY);
            }

            // On pre-Honeycomb a translated view is still clickable, so we need to disable clicks manually
            if (!hasHoneycombApi()) {
                fabBtn.setClickable(visible);
            }
        }
    }

    private int getMarginBottom() {
        int marginBottom = 0;
        final ViewGroup.LayoutParams layoutParams = fabBtn.getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            marginBottom = ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin;
        }
        return marginBottom;
    }

    private boolean hasHoneycombApi() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    abstract class RecyclerViewScrollDetector extends RecyclerView.OnScrollListener {
        private int mScrollThreshold;

        abstract void onScrollUp();

        abstract void onScrollDown();

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            boolean isSignificantDelta = Math.abs(dy) > mScrollThreshold;
            if (isSignificantDelta) {
                if (dy > 0) {
                    onScrollUp();
                } else {
                    onScrollDown();
                }
            }
        }

        public void setScrollThreshold(int scrollThreshold) {
            mScrollThreshold = scrollThreshold;
        }
    }

    private class RecyclerViewScrollDetectorImpl extends RecyclerViewScrollDetector {
        private ScrollDirectionListener mScrollDirectionListener;
        private RecyclerView.OnScrollListener mOnScrollListener;

        private void setScrollDirectionListener(ScrollDirectionListener scrollDirectionListener) {
            mScrollDirectionListener = scrollDirectionListener;
        }

        public void setOnScrollListener(RecyclerView.OnScrollListener onScrollListener) {
            mOnScrollListener = onScrollListener;
        }

        @Override
        public void onScrollDown() {
            show();
            if (mScrollDirectionListener != null) {
                mScrollDirectionListener.onScrollDown();
            }
        }

        @Override
        public void onScrollUp() {
            hide();
            if (mScrollDirectionListener != null) {
                mScrollDirectionListener.onScrollUp();
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (mOnScrollListener != null) {
                mOnScrollListener.onScrolled(recyclerView, dx, dy);
            }

            super.onScrolled(recyclerView, dx, dy);
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (mOnScrollListener != null) {
                mOnScrollListener.onScrollStateChanged(recyclerView, newState);
            }

            super.onScrollStateChanged(recyclerView, newState);
        }
    }

}
