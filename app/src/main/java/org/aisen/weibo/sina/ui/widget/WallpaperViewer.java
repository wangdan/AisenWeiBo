package org.aisen.weibo.sina.ui.widget;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.RelativeLayout;

import org.aisen.weibo.sina.R;

public class WallpaperViewer extends RelativeLayout {
    public static final float INN_WIDTH_PERCENTS = 0.5f;

    private boolean isDispatch;
    private float mDownX, mMovePercents;
    private int mInnViewPositionX, mInnCenterPositionX;
    private View mInnerView;
    private WallpaperViewerLisenter mWallpaperViewerLisenter;
    private ObjectAnimator mAnimator;

    public static interface WallpaperViewerLisenter {
        /**
         * 当滑动缩略图的时候出发
         *
         * @param percent 滑动的百分比，负的时候为向左，正的时候为向右
         */
        public void onWallpaperViewerScroll(float percent);
    }

    public WallpaperViewer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public WallpaperViewer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WallpaperViewer(Context context) {
        super(context);
    }

    public void setWallpaperViewerLisenter(WallpaperViewerLisenter wallpaperViewerLisenter) {
        this.mWallpaperViewerLisenter = wallpaperViewerLisenter;
    }


    public void init() {
        setBackgroundResource(R.drawable.wallpaper_viewer_bg);

        getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);
                // 初始化 innerView
                mInnerView = new View(getContext());
                mInnerView.setLayoutParams(new LayoutParams((int) (getWidth() * INN_WIDTH_PERCENTS),
                        LayoutParams.MATCH_PARENT));
                mInnerView.setBackgroundResource(R.drawable.wallpaper_viewer_inn_bg);
                mInnCenterPositionX = (int) (getWidth() * (1 - INN_WIDTH_PERCENTS) / 2);
                setInnViewPositionX(mInnCenterPositionX);
                addView(mInnerView);

                return true;
            }
        });
    }

    public void setInnViewPositionX(int x) {
        if (mInnViewPositionX == x) {
            return;
        }
        mInnerView.setX(x);
        mInnViewPositionX = (int) x;

        if (mWallpaperViewerLisenter != null) {
            float percent = ((float) x - mInnCenterPositionX) / ((float) mInnCenterPositionX);
            // 粗力度化防止抖动
            percent = ((float) ((int) (percent * 50)) / 50);
            if (mMovePercents != percent) {
                mMovePercents = percent;
                mWallpaperViewerLisenter.onWallpaperViewerScroll(percent);
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getVisibility() != View.VISIBLE) {
            if (isDispatch) {
                return true;
            }
            return super.dispatchTouchEvent(ev);
        }
        float downX = ev.getX();

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = downX;

                if (mAnimator != null && mAnimator.isRunning()) {
                    mAnimator.end();
                }

                isDispatch = true;
                break;
            case MotionEvent.ACTION_MOVE:

                int distance = (int) (downX - mDownX);
                if (distance < -mInnCenterPositionX) {
                    distance = -mInnCenterPositionX;
                } else if (distance > mInnCenterPositionX) {
                    distance = mInnCenterPositionX;
                }

                setInnViewPositionX(distance + mInnCenterPositionX);

                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:

                if (mInnViewPositionX != mInnCenterPositionX) {
                    // 滑动结束动画
                    doEndAnim();
                }
                isDispatch = false;
                break;
            default:
                break;
        }


        if (isDispatch) {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    public void doEndAnim() {
        mAnimator = ObjectAnimator.ofInt(this, "InnViewPositionX", mInnViewPositionX, mInnCenterPositionX);
        mAnimator.setDuration(250);
        mAnimator.start();
    }
}
