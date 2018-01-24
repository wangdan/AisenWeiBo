package org.aisen.android.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

import org.aisen.android.R;
import org.aisen.android.common.md.MDHelper;

/**
 * Created by wangdan on 16/1/11.
 */
public class MDButton extends TextView {

    public MDButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public MDButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MDButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final Drawable d = MDHelper.resolveDrawable(context, R.attr.MDbuttonSelector);
        setDefaultSelector(d);
        int themeColor = MDHelper.resolveColor(context, R.attr.colorPrimary);
        final ColorStateList colorStateList = getMDTextStateList(context, themeColor);
        setTextColor(colorStateList);
    }

    public void setDefaultSelector(Drawable d) {
        MDHelper.setBackgroundCompat(this, d);
    }

    public static ColorStateList getMDTextStateList(Context context, int newPrimaryColor) {
        final int fallBackButtonColor = MDHelper.resolveColor(context, android.R.attr.textColorPrimary);
        if (newPrimaryColor == 0) newPrimaryColor = fallBackButtonColor;
        int[][] states = new int[][]{
                new int[]{-android.R.attr.state_enabled}, // disabled
                new int[]{} // enabled
        };
        int[] colors = new int[]{
                MDHelper.adjustAlpha(newPrimaryColor, 0.4f),
                newPrimaryColor
        };
        return new ColorStateList(states, colors);
    }

}
