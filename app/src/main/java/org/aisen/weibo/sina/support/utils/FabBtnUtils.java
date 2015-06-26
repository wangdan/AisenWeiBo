package org.aisen.weibo.sina.support.utils;

import android.app.Activity;
import android.view.Gravity;
import android.widget.AbsListView;
import android.widget.FrameLayout;

import com.melnykov.fab.FloatingActionButton;

import org.aisen.weibo.sina.base.AppSettings;

/**
 * Created by wangdan on 15/5/2.
 */
public class FabBtnUtils {

    public static void setFabBtn(Activity activity, FloatingActionButton fab, int resId, AbsListView listView) {
        // 更新FAB的颜色
        fab.setColorNormal(AisenUtils.getThemeColor(activity));
        fab.setColorPressed(AisenUtils.getThemeColor(activity));
        fab.setColorRipple(AisenUtils.getThemeColor(activity));
        fab.setImageResource(resId);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) fab.getLayoutParams();
        params.gravity = Gravity.BOTTOM;
        if (AppSettings.getFabBtnPosition() == 0)
            params.gravity |= Gravity.LEFT;
        else
            params.gravity |= Gravity.RIGHT;

        if (listView != null)
            fab.attachToListView(listView);
    }

}
