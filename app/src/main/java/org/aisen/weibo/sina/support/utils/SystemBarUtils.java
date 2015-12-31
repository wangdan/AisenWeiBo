package org.aisen.weibo.sina.support.utils;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by wangdan on 15/8/19.
 */
public class SystemBarUtils {

    public static void setStatusBar(Activity activity) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );//| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Utils.resolveColor(activity, R.attr.theme_statusbar_color, Color.BLUE));
            window.setStatusBarColor(Color.parseColor("#00000000"));
//            window.setNavigationBarColor(activity.getResources().getColor(ThemeUtils.themeColorArr[AppSettings.getThemeColor()][1]));
        }
    }

}
