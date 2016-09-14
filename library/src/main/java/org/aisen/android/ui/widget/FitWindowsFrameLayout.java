package org.aisen.android.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import org.aisen.android.R;
import org.aisen.android.common.utils.Logger;

/**
 * Created by wangdan on 15/8/18.
 */
public class FitWindowsFrameLayout extends FrameLayout {

    static final String TAG = "FitWindowsFrameLayout";

    public static final int INSERT_TOP = 0;
    public static final int INSERT_BOTTOM = 1;
    public static final int INSERT_BOTH = 2;

    private Rect mInserts;

    private int mInsertType;

    private boolean drawStatusBar = true;
    private boolean drawNavigationBar = true;

    private Drawable mStatusBarBackground;
    private Drawable mNavigationBarBackground;

    private OnFitSystemWindowsListener onFitSystemWindowsListener;

    public FitWindowsFrameLayout(Context context) {
        super(context);

        readAttributeSet(null);
    }

    public FitWindowsFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        readAttributeSet(attrs);
    }

    public FitWindowsFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        readAttributeSet(attrs);
    }

    private void readAttributeSet(AttributeSet attrs) {
        // ViewGroup默认不会调用onDraw()方法，清除一下这个flag
        setWillNotDraw(false);

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.FitWindowsFrameLayout);

            // 默认全部填充
            mInsertType = a.getInt(R.styleable.FitWindowsFrameLayout_fitType, INSERT_BOTH);

            Logger.d(TAG, "mInsertType = " + mInsertType);

            int statusBarColor = a.getColor(R.styleable.FitWindowsFrameLayout_statusBarColor, 0);
            if (statusBarColor > 0) {
                mStatusBarBackground = new ColorDrawable(statusBarColor);
            }
            else {
                mStatusBarBackground = readDefStatusBarDrawable();
            }

            int navigationBarColor = a.getColor(R.styleable.FitWindowsFrameLayout_navigationBarColor, 0);
            if (navigationBarColor > 0) {
                mNavigationBarBackground = new ColorDrawable(statusBarColor);
            }
            else {
                mNavigationBarBackground = readDefNavigationBarDrawable();
            }

            drawStatusBar = a.getBoolean(R.styleable.FitWindowsFrameLayout_drawStatusBar, true);
            drawNavigationBar = a.getBoolean(R.styleable.FitWindowsFrameLayout_drawNavigationBar, true);
        }
        else {
            mInsertType = INSERT_BOTH;
            mStatusBarBackground = readDefStatusBarDrawable();
            mNavigationBarBackground = readDefNavigationBarDrawable();
        }
    }

    private Drawable readDefStatusBarDrawable() {
        final int version = Build.VERSION.SDK_INT;

        TypedArray a = null;
        if (version >= 21) {
            a = getContext().obtainStyledAttributes(new int[]{ android.R.attr.colorPrimaryDark });;
        }
        else {
            a = getContext().obtainStyledAttributes(new int[]{ R.attr.colorPrimaryDark });;
        }
        if (a != null) {
            try {
                return a.getDrawable(0);
            } finally {
                a.recycle();
            }
        }

        return null;
    }

    private Drawable readDefNavigationBarDrawable() {
        final int version = Build.VERSION.SDK_INT;

        TypedArray a = null;
        if (version >= 21) {
            a = getContext().obtainStyledAttributes(new int[]{ android.R.attr.colorPrimary });;
        }
        else {
            a = getContext().obtainStyledAttributes(new int[]{ R.attr.colorPrimary });;
        }
        if (a != null) {
            try {
                return a.getDrawable(0);
            } finally {
                a.recycle();
            }
        }

        return null;
    }

    /**
     * 在一个布局中，同时存在多个View的fitsSystemWindows设置为true，只有一个有效
     *
     * @param insets
     */
    public void setFitWindowns(Rect insets) {
        Logger.e(TAG, "setFitWindowns");
        Logger.e(TAG, insets);

        if (insets != null)
            fitSystemWindows(insets);
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        Logger.e(TAG, "fitSystemWindows");
        Logger.e(TAG, insets);

        if (onFitSystemWindowsListener != null)
            onFitSystemWindowsListener.onFitSystemWindows(insets);

        mInserts = new Rect(insets.left, insets.top, insets.right, insets.bottom);

        switch (mInsertType) {
        case INSERT_BOTH:
            break;
        case INSERT_TOP:
            setPadding(getPaddingLeft(), insets.top, getPaddingRight(), getPaddingBottom());

            invalidate();
            return false;
        case INSERT_BOTTOM:
            setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), insets.bottom);

            invalidate();
            return false;
        }

        invalidate();

        return super.fitSystemWindows(insets);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Logger.d(TAG, "onDraw()");

        if (mInserts != null) {
            if (mInsertType == INSERT_BOTTOM) {
                drawNavigationBarBackground(canvas);
            }
            else if (mInsertType == INSERT_TOP) {
                drawStatusBarBackground(canvas);
            }
            else if (mInsertType == INSERT_BOTH) {
                drawStatusBarBackground(canvas);
                drawNavigationBarBackground(canvas);
            }
        }
    }

    private void drawStatusBarBackground(Canvas canvas) {
        if (!drawStatusBar)
            return;

        if (mInserts != null && mStatusBarBackground != null) {
            if (mInserts.top > 0) {
//                Logger.d(TAG, "drawStatusBarBackground=" + mStatusBarBackground);

                mStatusBarBackground.setBounds(0, 0, getWidth(), mInserts.top);
                mStatusBarBackground.draw(canvas);
            }
        }
    }

    private void drawNavigationBarBackground(Canvas canvas) {
        if (!drawNavigationBar)
            return;

        if (mInserts != null && mNavigationBarBackground != null) {
            if (mInserts.bottom > 0) {
//                Logger.d(TAG, "drawNavigationBarBackground=" + mNavigationBarBackground);

                mNavigationBarBackground.setBounds(0, getHeight() - mInserts.bottom, getWidth(), getHeight());
                mNavigationBarBackground.draw(canvas);
            }
        }
    }

    public Rect getInserts() {
        return mInserts;
    }

    public void setOnFitSystemWindowsListener(OnFitSystemWindowsListener onFitSystemWindowsListener) {
        this.onFitSystemWindowsListener = onFitSystemWindowsListener;
    }

    public interface OnFitSystemWindowsListener {
        void onFitSystemWindows(Rect insets);
    }

}
