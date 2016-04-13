package org.aisen.weibo.sina.ui.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.util.AttributeSet;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by wangdan on 15/4/27.
 */
public class AisenListPreference extends ListPreference {

    public AisenListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private AlertDialog.Builder mBuilder;

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        mBuilder = builder;
    }

    private String getFieldValue(String filed) throws Exception {
        Field titleField = AisenListPreference.class.getSuperclass().getSuperclass().getDeclaredField(filed);
        titleField.setAccessible(true);
        String dialogTitle = titleField.get(this).toString();
        return dialogTitle;
    }

    @Override
    protected void showDialog(Bundle state) {
        try {
            if (Build.VERSION.SDK_INT < 100) {
                CharSequence[] mEntries = getEntries();
                String[] mEntriesStrArr = new String[mEntries.length];
                for (int i = 0; i < mEntries.length; i++)
                    mEntriesStrArr[i] = mEntries[i].toString();

                Method method = AisenListPreference.class.getSuperclass().getDeclaredMethod("getValueIndex");
                method.setAccessible(true);
                int mClickedDialogEntryIndex = Integer.parseInt(method.invoke(this).toString());


                String dialogTitle = getFieldValue("mDialogTitle");
                String mNegativeButtonText = getFieldValue("mNegativeButtonText");


                new AlertDialogWrapper.Builder(getContext())
                        .setTitle(dialogTitle)
                        .setSingleChoiceItems(mEntriesStrArr, mClickedDialogEntryIndex, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    Field titleField = AisenListPreference.class.getSuperclass().getDeclaredField("mClickedDialogEntryIndex");
                                    titleField.setAccessible(true);
                                    titleField.set(AisenListPreference.this, which);
                                } catch (Exception e) {
                                }

                                AisenListPreference.this.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                                dialog.dismiss();
                            }

                        })
                        .setOnDismissListener(AisenListPreference.this)
                        .setNegativeButton(mNegativeButtonText, AisenListPreference.this)
                        .show();
            }
            else {
                super.showDialog(state);
            }
        } catch (Exception e) {
            super.showDialog(state);
        }
    }
}
