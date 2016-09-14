package org.aisen.android.common.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;

import java.lang.reflect.Field;

/**
 * A helper class to manage the system navigation bar and status bar. This will add various
 * systemUiVisibility flags to the given Window or View to make them follow the Setup Wizard style.
 *
 * When the useImmersiveMode intent extra is true, a screen in Setup Wizard should hide the system
 * bars using methods from this class. For Lollipop, {@link #hideSystemBars(Window)} will completely
 * hide the system navigation bar and change the status bar to transparent, and layout the screen
 * contents (usually the illustration) behind it.
 */
public class SystemBarUtils {

    @SuppressLint("InlinedApi")
    private static final int DEFAULT_IMMERSIVE_FLAGS = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

    @SuppressLint("InlinedApi")
    private static final int DIALOG_IMMERSIVE_FLAGS = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

    /**
     * Needs to be equal to View.STATUS_BAR_DISABLE_BACK
     */
    private static final int STATUS_BAR_DISABLE_BACK = 0x00400000;

    /**
     * Hide the navigation bar for a dialog.
     *
     * This will only take effect in versions Lollipop or above. Otherwise this is a no-op.
     */
    public static void hideSystemBars(final Dialog dialog) {
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            final Window window = dialog.getWindow();
            temporarilyDisableDialogFocus(window);
            addImmersiveFlagsToWindow(window, DIALOG_IMMERSIVE_FLAGS);
            addImmersiveFlagsToDecorView(window, new Handler(), DIALOG_IMMERSIVE_FLAGS);
        }
    }

    /**
     * Hide the navigation bar, and make the color of the status and navigation bars transparent,
     * and specify the LAYOUT_FULLSCREEN flag so that the content is laid-out behind the transparent
     * status bar. This is commonly used with Activity.getWindow() to make the navigation and status
     * bars follow the Setup Wizard style.
     *
     * This will only take effect in versions Lollipop or above. Otherwise this is a no-op.
     */
    public static void hideSystemBars(final Window window) {
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            addImmersiveFlagsToWindow(window, DEFAULT_IMMERSIVE_FLAGS);
            addImmersiveFlagsToDecorView(window, new Handler(), DEFAULT_IMMERSIVE_FLAGS);
        }
    }

    /**
     * Convenience method to add a visibility flag in addition to the existing ones.
     */
    public static void addVisibilityFlag(final View view, final int flag) {
        if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
            final int vis = view.getSystemUiVisibility();
            view.setSystemUiVisibility(vis | flag);
        }
    }

    /**
     * Convenience method to add a visibility flag in addition to the existing ones.
     */
    public static void addVisibilityFlag(final Window window, final int flag) {
        if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
            WindowManager.LayoutParams attrs = window.getAttributes();
            attrs.systemUiVisibility |= flag;
            window.setAttributes(attrs);
        }
    }

    /**
     * Convenience method to remove a visibility flag from the view, leaving other flags that are
     * not specified intact.
     */
    public static void removeVisibilityFlag(final View view, final int flag) {
        if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
            final int vis = view.getSystemUiVisibility();
            view.setSystemUiVisibility(vis & ~flag);
        }
    }

    /**
     * Convenience method to remove a visibility flag from the window, leaving other flags that are
     * not specified intact.
     */
    public static void removeVisibilityFlag(final Window window, final int flag) {
        if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
            WindowManager.LayoutParams attrs = window.getAttributes();
            attrs.systemUiVisibility &= ~flag;
            window.setAttributes(attrs);
        }
    }

    public static void setBackButtonVisible(final Window window, final boolean visible) {
        if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
            if (visible) {
                removeVisibilityFlag(window, STATUS_BAR_DISABLE_BACK);
            } else {
                addVisibilityFlag(window, STATUS_BAR_DISABLE_BACK);
            }
        }
    }

    /**
     * Set a view to be resized when the keyboard is shown. This will set the bottom margin of the
     * view to be immediately above the keyboard, and assumes that the view sits immediately above
     * the navigation bar.
     *
     * Note that you must set windowSoftInputMode to adjustResize for this class to work. Otherwise
     * window insets are not dispatched and this method will have no effect.
     *
     * This will only take effect in versions Lollipop or above. Otherwise this is a no-op.
     *
     * @param view The view to be resized when the keyboard is shown.
     */
    public static void setImeInsetView(final View view) {
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            view.setOnApplyWindowInsetsListener(new WindowInsetsListener(view.getContext()));
        }
    }

    /**
     * View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN only takes effect when it is added a view instead of
     * the window.
     */
    @TargetApi(VERSION_CODES.LOLLIPOP)
    private static void addImmersiveFlagsToDecorView(final Window window, final Handler handler, final int vis) {
        // Use peekDecorView instead of getDecorView so that clients can still set window features
        // after calling this method.
        final View decorView = window.peekDecorView();
        if (decorView != null) {
            addVisibilityFlag(decorView, vis);
        } else {
            // If the decor view is not installed yet, try again in the next loop.
            handler.post(new Runnable() {
                @Override
                public void run() {
                    addImmersiveFlagsToDecorView(window, handler, vis);
                }
            });
        }
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    private static void addImmersiveFlagsToWindow(final Window window, final int vis) {
        WindowManager.LayoutParams attrs = window.getAttributes();
        attrs.systemUiVisibility |= vis;
        window.setAttributes(attrs);

        // Also set the navigation bar and status bar to transparent color. Note that this doesn't
        // work on some devices.
        window.setNavigationBarColor(0);
        window.setStatusBarColor(0);
    }

    /**
     * Apply a hack to temporarily set the window to not focusable, so that the navigation bar will
     * not show up during the transition.
     */
    private static void temporarilyDisableDialogFocus(final Window window) {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        // Add the SOFT_INPUT_IS_FORWARD_NAVIGATION_FLAG. This is normally done by the system when
        // FLAG_NOT_FOCUSABLE is not set. Setting this flag allows IME to be shown automatically
        // if the dialog has editable text fields.
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_IS_FORWARD_NAVIGATION);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            }
        });
    }

    public static int getNavigationBarHeight(Context activity) {
        Resources resources = activity.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height","dimen", "android");
        //获取NavigationBar的高度
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }

    public static int getStatusBarHeight(Context context){
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    private static class WindowInsetsListener implements View.OnApplyWindowInsetsListener {

        private int mNavigationBarHeight;

        public WindowInsetsListener(Context context) {
            mNavigationBarHeight = getNavigationBarHeight(context);
        }

        @Override
        public WindowInsets onApplyWindowInsets(View view, WindowInsets insets) {
            int bottomInset = insets.getSystemWindowInsetBottom();

            final int bottomMargin = Math.max(insets.getSystemWindowInsetBottom() - mNavigationBarHeight, 0);

            final ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            // Check that we have enough space to apply the bottom margins before applying it.
            // Otherwise the framework may think that the view is empty and exclude it from layout.
            if (bottomMargin < lp.bottomMargin + view.getHeight()) {
                lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, bottomMargin);
                view.setLayoutParams(lp);
                bottomInset = 0;
            }


            return insets.replaceSystemWindowInsets(insets.getSystemWindowInsetLeft(),
                    insets.getSystemWindowInsetTop(), insets.getSystemWindowInsetRight(), bottomInset);
        }
    }

}
