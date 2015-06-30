package org.aisen.weibo.sina.support.utils;

import java.util.ArrayList;
import java.util.List;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.support.db.SinaDB;
import org.aisen.weibo.sina.sys.service.OfflineService;
import org.aisen.weibo.sina.sinasdk.bean.Group;

import android.app.Activity;
import android.content.DialogInterface;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.ActivityHelper;
import org.aisen.android.common.utils.Logger;
import org.aisen.orm.extra.Extra;

/**
 * Created by wangdan on 15/5/3.
 */
public class OfflineUtils {

    public static final String TAG = "Offline-Utils";

    /**
     * 触发一次离线，如果没有设置过离线分组，优先设置后再离线
     *
     * @param context
     */
    public static void toggleOffline(final Activity context) {
        if (!AppContext.isLogedin())
            return;

        List<Group> groups = SinaDB.getOfflineSqlite().select(new Extra(AppContext.getUser().getIdstr(), null), Group.class);
        if (groups.size() == 0) {
            Logger.d(TAG, "离线分组未设置过");

            new AlertDialogWrapper.Builder(context)
                    .setMessage(R.string.offline_none_groups_remind)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.yes,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    showOfflineGroupsModifyDialog(context, new ArrayList<Group>(),
                                            new OnOfflineGroupSetCallback() {

                                                @Override
                                                public void onChanged(List<Group> newGroups) {
                                                    // 设置离线分组
                                                    Logger.d(TAG, "设置离线分组%d个", newGroups.size());

                                                    if (newGroups.size() > 0) {
                                                        SinaDB.getOfflineSqlite().insert(getLoggedExtra(null), newGroups);

                                                        toggleOffline(context);
                                                    }
                                                }

                                            },
                                            R.string.offline_groups_dialog);
                                }

                            })
                    .show();
        }
        else {
            OfflineService.startOffline((ArrayList) groups);
        }
    }

    /**
     * 显示一个Dialog，用来设置分组
     *
     * @param activity
     * @param selectedGroups
     * @param callback
     * @param titleId
     */
    public static void showOfflineGroupsModifyDialog(Activity activity, List<Group> selectedGroups,
                                                     final OnOfflineGroupSetCallback callback, int titleId) {
        String[] items = new String[AppContext.getGroups().getLists().size()];
        final boolean[] editCheckedItems = new boolean[AppContext.getGroups().getLists().size()];

        for (int i = 0; i < AppContext.getGroups().getLists().size(); i++) {
            Group group = AppContext.getGroups().getLists().get(i);

            items[i] = group.getName();
            editCheckedItems[i] = false;
            for (Group groupSelectd : selectedGroups) {
                if (groupSelectd.getId().equals(group.getIdstr())) {
                    editCheckedItems[i] = true;
                    break;
                }
            }
        }
        AlertDialogWrapper.Builder dialogBuilder = new AlertDialogWrapper.Builder(activity)
                .setTitle(titleId)
                .setMultiChoiceItems(items, editCheckedItems, new DialogInterface.OnMultiChoiceClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        editCheckedItems[which] = isChecked;
                    }

                });
        dialogBuilder.setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 艹，这个控件这里有bug
                        GlobalContext.getInstance().getHandler()
                                .postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        List<Group> groups = new ArrayList<Group>();

                                        for (int i = 0; i < editCheckedItems.length; i++) {
                                            if (editCheckedItems[i]) {
                                                groups.add(AppContext.getGroups().getLists().get(i));
                                            }
                                        }

                                        callback.onChanged(groups);
                                    }
                                }, 300);
                    }
                })
                .show();
    }

    public static Extra getLoggedExtra(String key) {
        return new Extra(AppContext.getUser().getIdstr(), key);
    }

    public interface OnOfflineGroupSetCallback {

        public void onChanged(List<Group> newGroups);

    }

}
