package org.aisen.weibo.sina.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import org.aisen.android.common.utils.SystemUtils;
import org.aisen.weibo.sina.R;

/**
 * Created by wangdan on 15/4/26.
 */
public class KitkatViewGroup extends LinearLayout {

    private Drawable mInsetForeground;
    private Drawable mInsetDrawable;
    private Rect mInsets;
    private Rect mTempRect = new Rect();

    public KitkatViewGroup(Context context) {
        super(context);

        setInit(null, 0);
    }

    public KitkatViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);

        setInit(attrs, 0);
    }

    public KitkatViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setInit(attrs, defStyleAttr);
    }

    private void setInit(AttributeSet attrs, int defStyle) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            if (attrs != null) {
                final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.KitkatViewGroup, defStyle, 0);
                if (a == null) {
                    return;
                }
                mInsetForeground = a.getDrawable(R.styleable.KitkatViewGroup_insetStatusForeground);
                mInsetDrawable = a.getDrawable(R.styleable.KitkatViewGroup_insetStatus);
                a.recycle();

                setWillNotDraw(false);

                mInsets = new Rect(0, SystemUtils.getStatusBarHeight(getContext()), 0, 0);

                ViewCompat.postInvalidateOnAnimation(this);
            }

            setPadding(getPaddingLeft(),
                        getPaddingTop() + SystemUtils.getStatusBarHeight(getContext()),
                        getPaddingRight(),
                        0);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        int width = getWidth();
        if (mInsets != null && mInsetForeground != null && mInsetDrawable != null) {
            int sc = canvas.save();
            canvas.translate(getScrollX(), getScrollY());

            // Top
            mTempRect.set(0, 0, width, mInsets.top);
            mInsetDrawable.setBounds(mTempRect);
            mInsetDrawable.draw(canvas);
            mInsetForeground.setBounds(mTempRect);
            mInsetForeground.draw(canvas);

            canvas.restoreToCount(sc);
        }
    }

}
