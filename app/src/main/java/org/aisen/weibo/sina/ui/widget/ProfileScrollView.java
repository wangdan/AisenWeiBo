package org.aisen.weibo.sina.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.SystemBarUtils;
import org.aisen.android.common.utils.Utils;
import org.aisen.android.ui.activity.basic.BaseActivity;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;

/**
 * Created by wangdan on 15-2-11.
 */
public class ProfileScrollView extends ScrollView {

    public static final String TAG = "ProfileScrollView";

    public ProfileScrollView(Context context) {
        super(context);
    }

    public ProfileScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProfileScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private static final int INVALID_POINTER = -1;

    private LinearLayout child;
    private View layTop;
    private View layTabStrip;
    private ViewPager viewPager;
    private View viewToolbar;
    private View imgCover;

    private View refreshView;
    private int mActivePointerId = INVALID_POINTER;
    private float mInitialMotionY;
    private int action_size;

    private BaseActivity activity;
    private WeiBoUser mUser;

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (getChildCount() > 0) {
            if (getChildAt(0).getHeight() > 0 && child == null) {
                child = (LinearLayout) getChildAt(0);
            }
        }

        int themeColor = Utils.resolveColor(getContext(), R.attr.colorPrimary, Color.BLUE);

        if (child != null && child.getHeight() > 0 && layTabStrip == null) {
            layTabStrip = child.getChildAt(1);
            layTabStrip.setBackgroundColor(themeColor);
            viewPager = (ViewPager) child.getChildAt(2);

            activity = (BaseActivity) getContext();
            layTop = activity.findViewById(R.id.layTop);
            layTop.setBackgroundColor(themeColor);
            viewToolbar = activity.findViewById(R.id.viewToolbar);
            if (viewToolbar != null)
                viewToolbar.setBackgroundColor(themeColor);
            imgCover = activity.findViewById(R.id.imgCover);
            activity.findViewById(R.id.viewBgDes).setBackgroundColor(themeColor);

            action_size = activity.getResources().getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material);
            int statusBar = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                statusBar = SystemBarUtils.getStatusBarHeight(activity);
            }

            Logger.d(TAG, String.format("topview height = %d, stripView height = %d, toolbar height = %d",
                    child.getChildAt(0).getHeight(), layTabStrip.getHeight(), action_size));

            viewPager.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    getHeight() - layTabStrip.getHeight() - action_size - statusBar));
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (viewToolbar != null)
            viewToolbar.setAlpha(Math.abs(t * 1.0f / (imgCover.getHeight() - action_size)));

        // 设置显示Actionbar的title
        if (activity != null) {
            if (viewToolbar != null && viewToolbar.getAlpha() >= 0.75f) {
                activity.getSupportActionBar().setTitle(mUser.getScreen_name());
            }
            else {
                activity.getSupportActionBar().setTitle("");
            }
        }
    }

    public void setAbsListView(View refreshView) {
        this.refreshView = refreshView;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);

        if (refreshView != null) {
            boolean canChildScrollUp = ViewCompat.canScrollVertically(refreshView, -1);
            Logger.d(TAG, String.format("canChildScrollUp = %s", String.valueOf(canChildScrollUp)));
            if (canChildScrollUp) {
                return false;
            }
        }

        switch (action) {
        case MotionEvent.ACTION_DOWN:
            mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
            final float initialMotionY = getMotionEventY(ev, mActivePointerId);
            if (initialMotionY == -1) {
                return false;
            }
            mInitialMotionY = initialMotionY;
            break;
        case MotionEvent.ACTION_MOVE:
            if (mActivePointerId == INVALID_POINTER) {
                Logger.e(TAG, "Got ACTION_MOVE event but don't have an active pointer id.");
                return false;
            }

            final float y = getMotionEventY(ev, mActivePointerId);
            if (y == -1) {
                return false;
            }
            final float yDiff = y - mInitialMotionY;
            if (yDiff < 0) {
                if (getChildAt(0).getMeasuredHeight() <= getHeight() + getScrollY()) {
                    return false;
                }
            }
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            mActivePointerId = INVALID_POINTER;
            break;
        }

        return super.onInterceptTouchEvent(ev);
    }

    private float getMotionEventY(MotionEvent ev, int activePointerId) {
        final int index = MotionEventCompat.findPointerIndex(ev, activePointerId);
        if (index < 0) {
            return -1;
        }
        return MotionEventCompat.getY(ev, index);
    }

    public void setUser(WeiBoUser user) {
        this.mUser = user;
    }

}
