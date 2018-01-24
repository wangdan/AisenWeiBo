package org.aisen.weibo.sina.support.utils;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.component.orm.extra.Extra;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.service.OfflineService;
import org.aisen.weibo.sina.sinasdk.bean.Group;
import org.aisen.weibo.sina.support.sqlit.SinaDB;

import java.util.ArrayList;
import java.util.List;

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
        if (!AppContext.isLoggedIn())
            return;

        List<Group> groups = SinaDB.getOfflineSqlite().select(new Extra(AppContext.getAccount().getUser().getIdstr(), null), Group.class);
        if (groups.size() == 0) {
            Logger.d(TAG, "离线分组未设置过");

            new MaterialDialog.Builder(context)
                    .content(R.string.offline_none_groups_remind)
                    .negativeText(R.string.cancel)
                    .positiveText(R.string.yes)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {

                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
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
    public static void showOfflineGroupsModifyDialog(Activity activity, final List<Group> selectedGroups,
                                                     final OnOfflineGroupSetCallback callback, int titleId) {
        String[] items = new String[AppContext.getAccount().getGroups().getLists().size()];
        Integer[] selectedIndices = new Integer[selectedGroups.size()];

        int index = 0;
        for (int i = 0; i < AppContext.getAccount().getGroups().getLists().size(); i++) {
            Group group = AppContext.getAccount().getGroups().getLists().get(i);

            items[i] = group.getName();
            for (Group groupSelectd : selectedGroups) {
                if (groupSelectd.getId().equals(group.getIdstr())) {
                    selectedIndices[index++] = i;
                    break;
                }
            }
        }

        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(activity)
                .title(titleId)
                .items(items)
                .itemsCallbackMultiChoice(selectedIndices, new MaterialDialog.ListCallbackMultiChoice() {

                    @Override
                    public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                        selectedGroups.clear();

                        List<Group> groups = AppContext.getAccount().getGroups().getLists();
                        for (int i = 0; i < groups.size(); i++) {
                            for (int i1 = 0; i1 < which.length; i1++) {
                                if (i == which[i1]) {
                                    selectedGroups.add(groups.get(i));
                                }
                            }
                        }

                        return true;
                    }

                })
                .alwaysCallMultiChoiceCallback();
        dialogBuilder.negativeText(R.string.cancel)
                .positiveText(R.string.confirm)
                .onPositive(new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // 艹，这个控件这里有bug
                        GlobalContext.getInstance().getHandler()
                                .postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        callback.onChanged(selectedGroups);
                                    }
                                }, 300);
                    }

                })
                .show();
    }

    public static Extra getLoggedExtra(String key) {
        return new Extra(AppContext.getAccount().getUser().getIdstr(), key);
    }

    public interface OnOfflineGroupSetCallback {

        public void onChanged(List<Group> newGroups);

    }

}
