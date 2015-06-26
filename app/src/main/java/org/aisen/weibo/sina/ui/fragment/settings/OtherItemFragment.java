package org.aisen.weibo.sina.ui.fragment.settings;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;

import org.aisen.android.common.context.GlobalContext;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.ui.activity.publish.PublishActivity;

/**
 * 其他
 * 
 * @author wangdan
 *
 */
public class OtherItemFragment extends VersionSettingsFragment 
										implements OnPreferenceClickListener , OnPreferenceChangeListener {

	public static BasePreferenceFragment newInstance() {
		return new OtherItemFragment();
	}
	
	private Preference pAppFeedback;// 用户反馈
	private Preference pAbout;
	private Preference pFeedback;
	private Preference pOpensource;// 开源协议
	private Preference pGithub;// Github
	private CheckBoxPreference pScreenRotate;// 屏幕旋转
	private CheckBoxPreference pDisableCache;// 禁用缓存
	private ListPreference pCacheValidity;// 业务数据有效期

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		addPreferencesFromResource(R.xml.ui_about_item);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
		
		pFeedback = (Preference) findPreference("pFeedback");
		if (pFeedback != null)
			pFeedback.setOnPreferenceClickListener(this);
		
		pAbout = (Preference) findPreference("pAbout");
		pAbout.setOnPreferenceClickListener(this);

//        Preference pHelp = (Preference) findPreference("pHelp");
//		pHelp.setOnPreferenceClickListener(this);
		
		pScreenRotate = (CheckBoxPreference) findPreference("pScreenRotate");
		pScreenRotate.setOnPreferenceChangeListener(this);
		
		pDisableCache = (CheckBoxPreference) findPreference("pDisableCache");
		pDisableCache.setOnPreferenceChangeListener(this);
		
		pOpensource = (Preference) findPreference("pOpensource");
		pOpensource.setOnPreferenceClickListener(this);
		
		pGithub = (Preference) findPreference("pGithub");
		pGithub.setOnPreferenceClickListener(this);
		
		// 缓存有效期
		pCacheValidity = (ListPreference) findPreference("pCacheValidity");
		pCacheValidity.setOnPreferenceChangeListener(this);
		pCacheValidity.setEnabled(!AppSettings.isDisableCache());
		int value = Integer.parseInt(prefs.getString("pCacheValidity", "1"));
		setListSetting(value, R.array.pCacheValidity, pCacheValidity);
		
		pAppFeedback = (Preference) findPreference("pAppFeedback");
		pAppFeedback.setOnPreferenceClickListener(this);
		
	}
	
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if ("pScreenRotate".equals(preference.getKey())) {
			if (Boolean.parseBoolean(newValue.toString())) 
				getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			else 
				getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		else if ("pDisableCache".equals(preference.getKey())) {
			pCacheValidity.setEnabled(!Boolean.parseBoolean(newValue.toString()));
		}
		else if ("pCacheValidity".equals(preference.getKey())) {
			setListSetting(Integer.parseInt(newValue.toString()), R.array.pCacheValidity, pCacheValidity);
		}
		return true;
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if ("pFeedback".equals(preference.getKey())) {
			PublishActivity.publishFeedback(getActivity());
		}
		else if ("pAbout".equals(preference.getKey())) {
			AboutWebFragment.launchAbout(getActivity());
		}
		else if ("pHelp".equals(preference.getKey())) {
			AboutWebFragment.launchHelp(getActivity());
		}
		else if ("pOpensource".equals(preference.getKey())) {
			AboutWebFragment.launchOpensource(getActivity());
		}
		else if ("pGithub".equals(preference.getKey())) {
			AisenUtils.launchBrowser(getActivity(), "https://github.com/wangdan/AisenWeiBo");
		}
		else if ("pAppFeedback".equals(preference.getKey())) {
			PublishActivity.publishFeedback(getActivity());
		}
		return super.onPreferenceClick(preference);
	}
	
//	private void setLanguage(int value) {
//		String[] valueTitleArr = getResources().getStringArray(R.array.pLanguage);
//
//		pLanguage.setSummary(valueTitleArr[value]);
//	}

}
