package org.aisen.weibo.sina.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.m.common.utils.Logger;
import com.m.common.utils.SystemBarUtils;

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
        Logger.w("setPadding-KitkatViewGroup");
        setPadding(getPaddingLeft(),
                        getPaddingTop() + SystemBarUtils.getStatusBarHeight(getContext()),
                        getPaddingRight(),
                        getPaddingBottom() + SystemBarUtils.getNavigationBarHeight(getContext()));
    }

}
