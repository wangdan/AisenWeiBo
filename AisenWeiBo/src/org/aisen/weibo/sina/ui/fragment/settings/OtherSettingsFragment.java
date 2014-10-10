package org.aisen.weibo.sina.ui.fragment.settings;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.utils.AisenUtil;
import org.aisen.weibo.sina.support.utils.AppSettings;
import org.aisen.weibo.sina.support.utils.BaiduAnalyzeUtils;
import org.aisen.weibo.sina.ui.activity.common.FragmentContainerActivity;
import org.aisen.weibo.sina.ui.activity.publish.PublishActivity;

import com.m.common.context.GlobalContext;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;

/**
 * 其他
 * 
 * @author wangdan
 *
 */
public class OtherSettingsFragment extends PreferenceFragment 
										implements OnPreferenceClickListener , OnPreferenceChangeListener {

	public static void launch(Activity form) {
		FragmentContainerActivity.launch(form, OtherSettingsFragment.class, null);
	}
	
	private Preference pAbout;
	private Preference pHelp;
	private Preference pFeedback;
	private Preference pOpensource;// 开源协议
	private Preference pGithub;// Github
	private CheckBoxPreference pScreenRotate;// 屏幕旋转
	private CheckBoxPreference pDisableCache;// 禁用缓存
	private ListPreference pCacheValidity;// 业务数据有效期
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		addPreferencesFromResource(R.xml.ui_about);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
		
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		getActivity().getActionBar().setTitle(R.string.title_about);
		
		pFeedback = (Preference) findPreference("pFeedback");
		pFeedback.setOnPreferenceClickListener(this);
		
		pAbout = (Preference) findPreference("pAbout");
		pAbout.setOnPreferenceClickListener(this);
		
		pHelp = (Preference) findPreference("pHelp");
		pHelp.setOnPreferenceClickListener(this);
		
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
			AisenUtil.launchBrowser(getActivity(), "https://github.com/wangdan/AisenWeiBo");
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
		
		BaiduAnalyzeUtils.onPageStart("关于设置");
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		BaiduAnalyzeUtils.onPageEnd("关于设置");
	}

}
