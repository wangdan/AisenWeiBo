package org.aisen.weibo.sina.ui.fragment.settings;

import java.io.File;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.utils.AppSettings;
import org.aisen.weibo.sina.support.utils.BaiduAnalyzeUtils;
import org.aisen.weibo.sina.ui.activity.common.FragmentContainerActivity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.m.common.context.GlobalContext;
import com.m.common.settings.SettingUtility;
import com.m.common.utils.SystemUtility;
import com.m.ui.fragment.CacheClearFragment;

/**
 * 流量控制
 * 
 * @author wangdan
 *
 */
public class FlowSettingsFragment extends PreferenceFragment 
									implements OnPreferenceClickListener , OnPreferenceChangeListener {

	public static void launch(Activity from) {
		FragmentContainerActivity.launch(from, FlowSettingsFragment.class, null);
	}
	
	private ListPreference pUploadSetting;// 图片上传设置
	private ListPreference pPicMode;// 图片加载模式
	private ListPreference pTimelineCount;// 微博加载数量
	private ListPreference pCommentCount;// 评论加载数量
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		addPreferencesFromResource(R.xml.ui_flow_settings);
		
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		getActivity().getActionBar().setTitle(R.string.title_flowsetting);
		
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

		// 缓存清理
		Preference pClearCache = (Preference) findPreference("pClearCache");
		CacheClearFragment clearFragment = (CacheClearFragment) getActivity().getFragmentManager().findFragmentByTag("CacheClearFragment");
		if (clearFragment == null) {
			clearFragment = new CacheClearFragment();
			getActivity().getFragmentManager().beginTransaction().add(clearFragment, "CacheClearFragment").commit();
		}
		String cachePath = SystemUtility.getSdcardPath() + File.separator + SettingUtility.getStringSetting("root_path") +
								File.separator + SettingUtility.getStringSetting("com_m_common_image");
		clearFragment.setPreference(pClearCache, cachePath);
		
		// 图片加载模式
		pPicMode = (ListPreference) findPreference("pPicMode");
		pPicMode.setOnPreferenceChangeListener(this);
		value = Integer.parseInt(prefs.getString("pPicMode", "2"));
		setListSetting(value, R.array.picMode, pPicMode);
		
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
		return true;
	}
	
	private void setListSetting(int value, int hintId, ListPreference listPreference) {
		String[] valueTitleArr = getResources().getStringArray(hintId);
		
		listPreference.setSummary(valueTitleArr[value]);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		BaiduAnalyzeUtils.onPageStart("流量控制设置");
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		BaiduAnalyzeUtils.onPageEnd("流量控制设置");
	}

}
