package org.aisen.weibo.sina.ui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import org.aisen.weibo.sina.support.utils.ThemeUtils;

/**
 * Created by wangdan on 15/7/8.
 */
public class ThemeProgressBar extends ProgressBar {

    public ThemeProgressBar(Context context) {
        super(context);
    }

    public ThemeProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ThemeProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        int color = ThemeUtils.getThemeColor();
        try {
            ProgressBar progressBar = this;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ColorStateList stateList = ColorStateList.valueOf(color);
                progressBar.setProgressTintList(stateList);
                progressBar.setSecondaryProgressTintList(stateList);
                progressBar.setIndeterminateTintList(stateList);
            } else {
                PorterDuff.Mode mode = PorterDuff.Mode.SRC_IN;
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
                    mode = PorterDuff.Mode.MULTIPLY;
                }
                if (progressBar.getIndeterminateDrawable() != null)
                    progressBar.getIndeterminateDrawable().setColorFilter(color, mode);
                if (progressBar.getProgressDrawable() != null)
                    progressBar.getProgressDrawable().setColorFilter(color, mode);
            }
        } catch (Throwable throwable) {
        }
    }

}
