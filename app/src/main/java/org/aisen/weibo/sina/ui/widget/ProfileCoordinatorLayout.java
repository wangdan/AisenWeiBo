package org.aisen.weibo.sina.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;

import org.aisen.android.common.utils.Logger;

/**
 * Created by wangdan on 16/2/2.
 */
public class ProfileCoordinatorLayout extends CoordinatorLayout {

    private static final String TAG = ProfileCoordinatorLayout.class.getSimpleName();

    public ProfileCoordinatorLayout(Context context) {
        super(context);
    }

    public ProfileCoordinatorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProfileCoordinatorLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i) instanceof Toolbar) {
                View toolbar = getChildAt(i);

                toolbar.layout(0, 0, toolbar.getWidth(), toolbar.getHeight());

                break;
            }
        }
    }

    @Override
    public void onLayoutChild(View child, int layoutDirection) {
        super.onLayoutChild(child, layoutDirection);
//        if (child instanceof Toolbar) {
            Logger.d(TAG, "child = + " + child.getClass().getSimpleName() + ", layoutDirection = " + layoutDirection);
//        }
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
    }

}
