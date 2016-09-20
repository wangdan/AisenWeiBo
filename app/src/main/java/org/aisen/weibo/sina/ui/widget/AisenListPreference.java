package org.aisen.weibo.sina.ui.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

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


                new MaterialDialog.Builder(getContext())
                        .title(dialogTitle)
                        .items(mEntriesStrArr)
                        .itemsCallbackSingleChoice(mClickedDialogEntryIndex, new MaterialDialog.ListCallbackSingleChoice() {

                            @Override
                            public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {

                                try {
                                    Field titleField = AisenListPreference.class.getSuperclass().getDeclaredField("mClickedDialogEntryIndex");
                                    titleField.setAccessible(true);
                                    titleField.set(AisenListPreference.this, which);
                                } catch (Exception e) {
                                }

                                AisenListPreference.this.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                                dialog.dismiss();

                                return true;
                            }

                        })
                        .dismissListener(AisenListPreference.this)
                        .negativeText(mNegativeButtonText)
                        .onNegative(new MaterialDialog.SingleButtonCallback() {

                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                AisenListPreference.this.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);

                                dialog.dismiss();
                            }

                        })
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
