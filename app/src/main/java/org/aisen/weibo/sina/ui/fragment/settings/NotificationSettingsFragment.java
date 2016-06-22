package org.aisen.weibo.sina.ui.fragment.settings;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.ActivityHelper;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.service.UnreadService;
import org.aisen.weibo.sina.support.utils.UMengUtil;
import org.aisen.weibo.sina.ui.activity.base.SinaCommonActivity;

/**
 * 通知设置
 * 
 * @author wangdan
 *
 */
public class NotificationSettingsFragment extends BasePreferenceFragment implements OnPreferenceClickListener, OnPreferenceChangeListener,
																			OnCheckedChangeListener {

	public static void launch(Activity from) {
		SinaCommonActivity.launch(from, NotificationSettingsFragment.class, null);
	}

    private CheckBoxPreference pNotificationEnable;// 开启通知提醒
	private ListPreference pInterval;// 消息间隔
	private CheckBoxPreference pNightClose;// 夜间勿扰
	private CheckBoxPreference pStatusMention;// 提及微博
	private CheckBoxPreference pCommentMention;// 提及评论
	private CheckBoxPreference pFollower;// 粉丝
	private CheckBoxPreference pComment;// 评论
	private CheckBoxPreference pNotifySound;// 声音
	private CheckBoxPreference pNotifyVibrate;// 振动
	private CheckBoxPreference pNotifyLED;// LED
    private CheckBoxPreference pDm;// 私信

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		addPreferencesFromResource(R.xml.ui_notification_settings);

        BaseActivity activity = (BaseActivity) getActivity();
		activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(false);
        activity.getSupportActionBar().setTitle(R.string.title_notification);
		
		setHasOptionsMenu(false);

        pNotificationEnable = (CheckBoxPreference) findPreference("pNotificationEnable");
        pNotificationEnable.setOnPreferenceChangeListener(this);

		pInterval = (ListPreference) findPreference("pInterval");
		pInterval.setOnPreferenceChangeListener(this);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
		int value = Integer.parseInt(prefs.getString("pInterval", "0"));
		setUploadSetting(value);
		
		pNightClose = (CheckBoxPreference) findPreference("pNightClose");
//		pNightClose.setOnPreferenceChangeListener(this);
		
		pStatusMention = (CheckBoxPreference) findPreference("pStatusMention");
//		pStatusMention.setOnPreferenceChangeListener(this);
		
		pCommentMention = (CheckBoxPreference) findPreference("pCommentMention");
//		pCommentMention.setOnPreferenceChangeListener(this);
		
		pFollower = (CheckBoxPreference) findPreference("pFollower");
//		pFollower.setOnPreferenceChangeListener(this);
		
		pComment = (CheckBoxPreference) findPreference("pComment");
//		pComment.setOnPreferenceChangeListener(this);
		
		pNotifySound = (CheckBoxPreference) findPreference("pNotifySound");
//		pNotifySound.setOnPreferenceChangeListener(this);
		
		pNotifyVibrate = (CheckBoxPreference) findPreference("pNotifyVibrate");
//		pNotifyVibrate.setOnPreferenceChangeListener(this);
		
		pNotifyLED = (CheckBoxPreference) findPreference("pNotifyLED");
//		pNotifyLED.setOnPreferenceChangeListener(this);

        pDm = (CheckBoxPreference) findPreference("pDm");
//        pDm.setOnPreferenceChangeListener(this);
		
		refreshSettings();
	}
	
	private void refreshSettings() {
		pInterval.setEnabled(AppSettings.isNotifyEnable());
		pNightClose.setEnabled(AppSettings.isNotifyEnable());
		pStatusMention.setEnabled(AppSettings.isNotifyEnable());
		pCommentMention.setEnabled(AppSettings.isNotifyEnable());
		pFollower.setEnabled(AppSettings.isNotifyEnable());
		pComment.setEnabled(AppSettings.isNotifyEnable());
		pNotifySound.setEnabled(AppSettings.isNotifyEnable());
		pNotifyVibrate.setEnabled(AppSettings.isNotifyEnable());
		pNotifyLED.setEnabled(AppSettings.isNotifyEnable());
        pDm.setEnabled(AppSettings.isNotifyEnable());
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		ActivityHelper.putBooleanShareData(GlobalContext.getInstance(), "org.aisen.weibo.sina.NOTIFICATION", isChecked);

		refreshSettings();
		
		if (isChecked)
			UnreadService.startService();
		else
			UnreadService.stopService();
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		return true;
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if ("pInterval".equals(preference.getKey())) {
			setUploadSetting(Integer.parseInt(newValue.toString()));
			
			UnreadService.updateAlarm();
		}
        else if ("pNotificationEnable".equalsIgnoreCase(preference.getKey())) {
            onCheckedChanged(null, Boolean.parseBoolean(newValue.toString()));
        }
		return true;
	}

	private void setUploadSetting(int value) {
		String[] valueTitleArr = getResources().getStringArray(R.array.txtUnread);

		pInterval.setSummary(valueTitleArr[value]);
	}

	@Override
	public void onResume() {
		super.onResume();

		UMengUtil.onPageStart(getActivity(), "通知设置页");
	}

	@Override
	public void onPause() {
		super.onPause();

		UMengUtil.onPageEnd(getActivity(), "通知设置页");
	}
	
}
