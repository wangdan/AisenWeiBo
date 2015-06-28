package org.aisen.android.ui.widget;

import android.widget.Toast;

import org.aisen.android.common.context.GlobalContext;

/**
 * Created by wangdan on 15/4/15.
 */
public class MToast {

    public static void showMessage(String text) {
        Toast.makeText(GlobalContext.getInstance(), text, Toast.LENGTH_SHORT).show();
    }

}
