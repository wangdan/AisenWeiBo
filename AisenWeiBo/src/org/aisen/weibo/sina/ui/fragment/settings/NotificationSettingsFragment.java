package org.aisen.weibo.sina.ui.fragment.settings;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.utils.AppSettings;
import org.aisen.weibo.sina.support.utils.BaiduAnalyzeUtils;
import org.aisen.weibo.sina.sys.service.UnreadService;
import org.aisen.weibo.sina.ui.activity.common.FragmentContainerActivity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

import com.m.common.context.GlobalContext;
import com.m.common.utils.ActivityHelper;

/**
 * 通知设置
 * 
 * @author wangdan
 *
 */
public class NotificationSettingsFragment extends PreferenceFragment implements OnPreferenceClickListener, OnPreferenceChangeListener,
																			OnCheckedChangeListener {

	public static void launch(Activity from) {
		FragmentContainerActivity.launch(from, NotificationSettingsFragment.class, null);
	}

	private ListPreference pInterval;// 消息间隔
	private CheckBoxPreference pNightClose;// 夜间勿扰
	private CheckBoxPreference pStatusMention;// 提及微博
	private CheckBoxPreference pCommentMention;// 提及评论
	private CheckBoxPreference pFollower;// 粉丝
	private CheckBoxPreference pComment;// 评论
	private CheckBoxPreference pNotifySound;// 声音
	private CheckBoxPreference pNotifyVibrate;// 振动
	private CheckBoxPreference pNotifyLED;// LED

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		addPreferencesFromResource(R.xml.ui_notification_settings);

		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		getActivity().getActionBar().setTitle(R.string.title_notification);
		
		setHasOptionsMenu(true);

		pInterval = (ListPreference) findPreference("pInterval");
		pInterval.setOnPreferenceChangeListener(this);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
		int value = Integer.parseInt(prefs.getString("pInterval", "0"));
		setUploadSetting(value);
		
		pNightClose = (CheckBoxPreference) findPreference("pNightClose");
		pNightClose.setOnPreferenceChangeListener(this);
		
		pStatusMention = (CheckBoxPreference) findPreference("pStatusMention");
		pStatusMention.setOnPreferenceChangeListener(this);
		
		pCommentMention = (CheckBoxPreference) findPreference("pCommentMention");
		pCommentMention.setOnPreferenceChangeListener(this);
		
		pFollower = (CheckBoxPreference) findPreference("pFollower");
		pFollower.setOnPreferenceChangeListener(this);
		
		pComment = (CheckBoxPreference) findPreference("pComment");
		pComment.setOnPreferenceChangeListener(this);
		
		pNotifySound = (CheckBoxPreference) findPreference("pNotifySound");
		pNotifySound.setOnPreferenceChangeListener(this);
		
		pNotifyVibrate = (CheckBoxPreference) findPreference("pNotifyVibrate");
		pNotifyVibrate.setOnPreferenceChangeListener(this);
		
		pNotifyLED = (CheckBoxPreference) findPreference("pNotifyLED");
		pNotifyLED.setOnPreferenceChangeListener(this);
		
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
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		
		inflater.inflate(R.menu.notify, menu);
		
		Switch switchView = new Switch(getActivity());
		switchView.setPadding(0, 0, GlobalContext.getInstance().getResources().getDimensionPixelSize(R.dimen.horizontal_margin), 0);
		switchView.setTextOff(getString(R.string.settings_close));
		switchView.setTextOn(getString(R.string.settings_open));
		switchView.setChecked(AppSettings.isNotifyEnable());
		switchView.setOnCheckedChangeListener(this);
		menu.findItem(R.id.notify).setActionView(switchView);
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		ActivityHelper.getInstance().putBooleanShareData("org.aisen.weibo.sina.NOTIFICATION", isChecked);
		
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
		return true;
	}

	private void setUploadSetting(int value) {
		String[] valueTitleArr = getResources().getStringArray(R.array.txtUnread);

		pInterval.setSummary(valueTitleArr[value]);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		BaiduAnalyzeUtils.onPageStart("通知设置");
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		BaiduAnalyzeUtils.onPageEnd("通知设置");
	}

}
