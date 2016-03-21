package org.aisen.android.ui.widget;

import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MotionEvent;

import org.aisen.android.ui.activity.basic.BaseActivity;

/**
 * Created by wangdan on 15-3-28.
 */
public class AsToolbar extends Toolbar {

    static final String TAG = "AsToolbar";

    public AsToolbar(Context context) {
        super(context);
    }

    public AsToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AsToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private long lastClickTime = 0;
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean handler = super.onTouchEvent(ev);

        if (ev.getAction() == MotionEvent.ACTION_UP) {
            if (lastClickTime != 0) {
                if (System.currentTimeMillis() - lastClickTime <= 500) {
                    BaseActivity activity = BaseActivity.getRunningActivity();
                    if (activity != null && activity instanceof OnToolbarDoubleClick)
                        ((OnToolbarDoubleClick) activity).onToolbarDoubleClick();
                }
            }

            lastClickTime = System.currentTimeMillis();
        }

        return handler;
    }

    public void performDoublcClick() {
        BaseActivity activity = BaseActivity.getRunningActivity();
        if (activity != null && activity instanceof OnToolbarDoubleClick)
            ((OnToolbarDoubleClick) activity).onToolbarDoubleClick();
    }
    
    public interface OnToolbarDoubleClick {
    	
    	boolean onToolbarDoubleClick();
    	
    }

}
