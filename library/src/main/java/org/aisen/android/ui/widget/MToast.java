package org.aisen.android.ui.widget;

import android.content.Context;
import android.widget.Toast;

import org.aisen.android.common.context.GlobalContext;

/**
 * Created by wangdan on 15/4/15.
 */
public class MToast {

    public static void showMessage(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

}
