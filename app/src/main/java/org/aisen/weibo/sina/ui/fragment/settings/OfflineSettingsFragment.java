package org.aisen.weibo.sina.ui.fragment.settings;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.component.orm.extra.Extra;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.sinasdk.bean.Group;
import org.aisen.weibo.sina.support.sqlit.SinaDB;
import org.aisen.weibo.sina.support.utils.OfflineUtils;
import org.aisen.weibo.sina.support.utils.UMengUtil;
import org.aisen.weibo.sina.ui.activity.base.SinaCommonActivity;

import java.util.List;

/**
 * 离线设置
 *
 * Created by wangdan on 15/4/3.
 */
public class OfflineSettingsFragment extends BasePreferenceFragment
                                        implements Preference.OnPreferenceChangeListener,
                                                    Preference.OnPreferenceClickListener {

    public static void launch(Activity from) {
        SinaCommonActivity.launch(from, OfflineSettingsFragment.class, null);
    }

    private Preference pOfflineGroups;// 主题设置

    private ListPreference pOfflineStatusSize;// 微博离线数量
    private ListPreference pOfflinePicTaskCount;// 图片线程数量

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        BaseActivity baseActivity = (BaseActivity) getActivity();
        baseActivity.getSupportActionBar().setTitle(R.string.settings_offline);
        baseActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        addPreferencesFromResource(R.xml.ui_offline_settings);

        pOfflineGroups = (Preference) findPreference("pOfflineGroups");
        pOfflineGroups.setOnPreferenceClickListener(this);
        setGroupsSummary();

        pOfflineStatusSize = (ListPreference) findPreference("pOfflineStatusSize");
        pOfflineStatusSize.setOnPreferenceChangeListener(this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        int value = Integer.parseInt(prefs.getString("pOfflineStatusSize", "2"));
        setListSetting(value, R.array.offlineTimelineCount, pOfflineStatusSize);

        pOfflinePicTaskCount = (ListPreference) findPreference("pOfflinePicTaskCount");
        pOfflinePicTaskCount.setOnPreferenceChangeListener(this);
        value = Integer.parseInt(prefs.getString("pOfflinePicTaskCount", "2"));
        setPicTaskSetting(value);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if ("pOfflineStatusSize".equals(preference.getKey())) {
            setListSetting(Integer.parseInt(newValue.toString()), R.array.offlineTimelineCount, pOfflineStatusSize);
        }
        else if ("pOfflinePicTaskCount".equals(preference.getKey())) {
            setPicTaskSetting(Integer.parseInt(newValue.toString()));
        }

        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals("pOfflineGroups")) {
            final List<Group> groups = SinaDB.getOfflineSqlite().select(new Extra(AppContext.getAccount().getUser().getIdstr(), null), Group.class);

            OfflineUtils.showOfflineGroupsModifyDialog(getActivity(), groups,
                    new OfflineUtils.OnOfflineGroupSetCallback() {

                        @Override
                        public void onChanged(List<Group> newGroups) {
                            SinaDB.getOfflineSqlite().deleteAll(OfflineUtils.getLoggedExtra(null), Group.class);

                            if (newGroups == null || newGroups.size() == 0) {

                            } else {
                                SinaDB.getOfflineSqlite().insertOrReplace(OfflineUtils.getLoggedExtra(null), newGroups);
                            }

                            setGroupsSummary();
                        }

                    },
                    R.string.offline_groups_dialog);
        }

        return true;
    }

    private void setGroupsSummary() {
        List<Group> groups = SinaDB.getOfflineSqlite().select(new Extra(AppContext.getAccount().getUser().getIdstr(), null), Group.class);
        String summary = "";
        if (groups.size() == 0) {
            summary = getString(R.string.offline_none_groups) + ",";
        }
        else {
            for (Group group : groups) {
                summary = summary + group.getName() + ",";
            }
        }
        pOfflineGroups.setSummary(summary.substring(0, summary.length() - 1));
    }

    protected void setPicTaskSetting(int value) {
        String[] valueTitleArr = getResources().getStringArray(R.array.picTaskCount);

        pOfflinePicTaskCount.setTitle(String.format(getString(R.string.offline_pic_task_count), valueTitleArr[value]));
        pOfflinePicTaskCount.setSummary(String.format(getString(R.string.offline_pic_task_count_summary), valueTitleArr[value]));
    }

    @Override
    public void onResume() {
        super.onResume();

        UMengUtil.onPageStart(getActivity(), "离线设置页");
    }

    @Override
    public void onPause() {
        super.onPause();

        UMengUtil.onPageEnd(getActivity(), "离线设置页");
    }

}
