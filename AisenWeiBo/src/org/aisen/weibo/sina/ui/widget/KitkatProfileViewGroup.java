package org.aisen.weibo.sina.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.m.common.utils.Logger;
import com.m.common.utils.SystemBarUtils;
import com.m.common.utils.Utils;

import org.aisen.weibo.sina.R;

/**
 * Created by wangdan on 15/4/26.
 */
public class KitkatProfileViewGroup extends FrameLayout {

    public KitkatProfileViewGroup(Context context) {
        super(context);

        setInit();
    }

    public KitkatProfileViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);

        setInit();
    }

    public KitkatProfileViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setInit();
    }

    private void setInit() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            Logger.w("setPadding-KitkatViewGroup");
            setPadding(getPaddingLeft(),
                        getPaddingTop(),
                        getPaddingRight(),
                        getPaddingBottom() + SystemBarUtils.getNavigationBarHeight(getContext()));

//            setBackgroundColor(Utils.resolveColor(getContext(), R.attr.colorPrimary, Color.BLACK));
        }
    }

}
