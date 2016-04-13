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

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.support.utils.UMengUtil;
import org.aisen.weibo.sina.ui.activity.base.SinaCommonActivity;

/**
 * 流量控制
 * 
 * @author wangdan
 *
 */
public class FlowSettingsFragment extends BasePreferenceFragment
									implements OnPreferenceClickListener , OnPreferenceChangeListener {

	public static void launch(Activity from) {
		SinaCommonActivity.launch(from, FlowSettingsFragment.class, null);
	}
	
	private ListPreference pUploadSetting;// 图片上传设置
	private CheckBoxPreference pNonePic;// 无图模式
	private ListPreference pPicMode;// 图片加载模式
	private ListPreference pTimelineCount;// 微博加载数量
	private ListPreference pCommentCount;// 评论加载数量
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		addPreferencesFromResource(R.xml.ui_flow_settings);

        BaseActivity activity = (BaseActivity) getActivity();
		activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(false);
        activity.getSupportActionBar().setTitle(R.string.title_flowsetting);
		
		// 图片上传设置
		pUploadSetting = (ListPreference) findPreference("pUploadSetting");
		pUploadSetting.setOnPreferenceChangeListener(this);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
		int value = Integer.parseInt(prefs.getString("pUploadSetting", "0"));
		setListSetting(value, R.array.txtUpload, pUploadSetting);
		if (AppSettings.getUploadSetting() == 0) {
			String summary = pUploadSetting.getSummary().toString();
			
			pUploadSetting.setSummary(String.format("%s (%s)", summary, getString(R.string.settings_pic_upload_auto)));
		}
		else if (AppSettings.getUploadSetting() != 1) {
			String summary = pUploadSetting.getSummary().toString();
			summary = summary.substring(0, summary.length() - 1);
			summary = summary + ", " + getString(R.string.settings_pic_upload_gif) + ")";
			pUploadSetting.setSummary(summary);
		}

		pNonePic = (CheckBoxPreference) findPreference("pNonePic");
		pNonePic.setOnPreferenceChangeListener(this);
		
		// 图片加载模式
		pPicMode = (ListPreference) findPreference("pPicMode");
		pPicMode.setOnPreferenceChangeListener(this);
		value = AppSettings.getPictureMode();
		setListSetting(value, R.array.picMode, pPicMode);
		pPicMode.setEnabled(!pNonePic.isChecked());
		
		// 微博加载数量
		pTimelineCount = (ListPreference) findPreference("pTimelineCount");
		pTimelineCount.setOnPreferenceChangeListener(this);
		value = Integer.parseInt(prefs.getString("pTimelineCount", "3"));
		setListSetting(value, R.array.timelineCount, pTimelineCount);
		
		// 评论加载数量
		pCommentCount = (ListPreference) findPreference("pCommentCount");
		pCommentCount.setOnPreferenceChangeListener(this);
		value = Integer.parseInt(prefs.getString("pCommentCount", "3"));
		setListSetting(value, R.array.commentCount, pCommentCount);
	}
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		return true;
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if ("pUploadSetting".equals(preference.getKey())) {
			setListSetting(Integer.parseInt(newValue.toString()), R.array.txtUpload, pUploadSetting);
			
			if (Integer.parseInt(newValue.toString()) == 0) {
				String summary = pUploadSetting.getSummary().toString();
				
				pUploadSetting.setSummary(String.format("%s (%s)", summary, getString(R.string.settings_pic_upload_auto)));
			}
			else if (Integer.parseInt(newValue.toString()) != 1) {
				String summary = pUploadSetting.getSummary().toString();
				summary = summary.substring(0, summary.length() - 1);
				summary = summary + ", " + getString(R.string.settings_pic_upload_gif) + ")";
				pUploadSetting.setSummary(summary);
			}
		}
		else if ("pPicMode".equals(preference.getKey())) {
			setListSetting(Integer.parseInt(newValue.toString()), R.array.picMode, pPicMode);
		}
		else if ("pTimelineCount".equals(preference.getKey())) {
			setListSetting(Integer.parseInt(newValue.toString()), R.array.timelineCount, pTimelineCount);
		}
		else if ("pCommentCount".equals(preference.getKey())) {
			setListSetting(Integer.parseInt(newValue.toString()), R.array.commentCount, pCommentCount);
		}
		else if ("pNonePic".equals(preference.getKey())) {
			pPicMode.setEnabled(!Boolean.parseBoolean(newValue.toString()));
		}
		return true;
	}

	@Override
	public void onResume() {
		super.onResume();

		UMengUtil.onPageStart(getActivity(), "流量设置页");
	}

	@Override
	public void onPause() {
		super.onPause();

		UMengUtil.onPageEnd(getActivity(), "流量设置页");
	}

}
