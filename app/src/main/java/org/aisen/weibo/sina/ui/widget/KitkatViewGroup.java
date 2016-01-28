package org.aisen.weibo.sina.ui.widget;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.SystemUtils;

/**
 * Created by wangdan on 15/4/26.
 */
public class KitkatViewGroup extends LinearLayout {

    public KitkatViewGroup(Context context) {
        super(context);

        setInit();
    }

    public KitkatViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);

        setInit();
    }

    public KitkatViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setInit();
    }

    private void setInit() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            Logger.w("setPadding-KitkatViewGroup");
            setPadding(getPaddingLeft(),
                            getPaddingTop() + SystemUtils.getStatusBarHeight(getContext()),
                            getPaddingRight(),
                            getPaddingBottom() + SystemUtils.getNavigationBarHeight(getContext()));

//            setBackgroundColor(Utils.resolveColor(getContext(), R.attr.colorPrimary, Color.BLACK));
        }
    }

}
