package org.aisen.weibo.sina.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.widget.TextView;

import org.aisen.weibo.sina.R;

/**
 * Created by wangdan on 16/1/11.
 */
public class ActionIconThemedTextView extends AppCompatTextView {

    private final int mIconWidth, mIconHeight;
    private int mColor, mDisabledColor, mActivatedColor;

    public ActionIconThemedTextView(Context context) {
        this(context, null);
    }

    public ActionIconThemedTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActionIconThemedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ActionIconThemedTextView);
        mColor = a.getColor(R.styleable.ActionIconThemedTextView_iabColor, 0);
        mDisabledColor = a.getColor(R.styleable.ActionIconThemedTextView_iabDisabledColor, 0);
        mActivatedColor = a.getColor(R.styleable.ActionIconThemedTextView_iabActivatedColor, 0);
        mIconWidth = a.getDimensionPixelSize(R.styleable.ActionIconThemedTextView_iabIconWidth, 0);
        mIconHeight = a.getDimensionPixelSize(R.styleable.ActionIconThemedTextView_iabIconHeight, 0);
        a.recycle();
        updateCompoundDrawables();
    }

    public int getActivatedColor() {
        if (mActivatedColor != 0) return mActivatedColor;
        final ColorStateList colors = getLinkTextColors();
        if (colors != null) return colors.getDefaultColor();
        return getCurrentTextColor();
    }

    @Override
    public void setCompoundDrawablesWithIntrinsicBounds(Drawable left, Drawable top, Drawable right, Drawable bottom) {
        super.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
        updateCompoundDrawables();
    }

    @Override
    public void setCompoundDrawablesWithIntrinsicBounds(int left, int top, int right, int bottom) {
        super.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
        updateCompoundDrawables();
    }

    @Override
    public void setCompoundDrawablesRelativeWithIntrinsicBounds(Drawable start, Drawable top, Drawable end, Drawable bottom) {
        super.setCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom);
        updateCompoundDrawables();
    }

    @Override
    public void setCompoundDrawablesRelativeWithIntrinsicBounds(int start, int top, int end, int bottom) {
        super.setCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom);
        updateCompoundDrawables();
    }

    public int getColor() {
        if (mColor != 0) return mColor;
        final ColorStateList colors = getTextColors();
        if (colors != null) return colors.getDefaultColor();
        return getCurrentTextColor();
    }

    public int getDisabledColor() {
        if (mDisabledColor != 0) return mDisabledColor;
        final ColorStateList colors = getTextColors();
        if (colors != null) return colors.getColorForState(new int[0], colors.getDefaultColor());
        return getCurrentTextColor();
    }

    @Override
    public void setActivated(boolean activated) {
        super.setActivated(activated);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        updateCompoundDrawables();
    }

    private void updateCompoundDrawables() {
        updateCompoundDrawables(getCompoundDrawables());
        updateCompoundDrawables(getCompoundDrawablesRelative(this));
    }

    private void updateCompoundDrawables(Drawable[] drawables) {
        if (drawables == null) return;
        for (Drawable d : drawables) {
            if (d == null) continue;
            d.mutate();
            final int color;
            if (isActivated()) {
                color = getActivatedColor();
            } else if (isEnabled()) {
                color = getColor();
            } else {
                color = getDisabledColor();
            }
            if (mIconWidth > 0 && mIconHeight > 0) {
                d.setBounds(0, 0, mIconWidth, mIconHeight);
            }
            d.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
    }

    public static Drawable[] getCompoundDrawablesRelative(TextView view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) return null;
        return TextViewSupportJBMR1.getCompoundDrawablesRelative(view);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private static class TextViewSupportJBMR1 {
        public static Drawable[] getCompoundDrawablesRelative(TextView view) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) return null;
            return view.getCompoundDrawablesRelative();
        }
    }

}
