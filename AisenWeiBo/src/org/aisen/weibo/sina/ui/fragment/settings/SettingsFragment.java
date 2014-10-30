package org.aisen.weibo.sina.ui.fragment.settings;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.bean.WallpaperBean;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.AppSettings;
import org.aisen.weibo.sina.support.utils.BaiduAnalyzeUtils;
import org.aisen.weibo.sina.ui.activity.common.FragmentContainerActivity;
import org.aisen.weibo.sina.ui.activity.publish.PublishActivity;
import org.aisen.weibo.sina.ui.widget.WallpaperViewGroup;

import android.app.Activity;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;

import com.m.common.utils.CommSettings;
import com.m.ui.activity.BaseActivity;

/**
 * 程序设置
 * 
 * @author wangdan
 *
 */
public class SettingsFragment extends VersionSettingsFragment implements OnPreferenceChangeListener {

	public static void launch(Activity from) {
		FragmentContainerActivity.launch(from, SettingsFragment.class, null);
	}
	
	private Preference pBasic;
	private Preference pNotification;
	private Preference pFlow;
	private Preference pAbout;
	private Preference pShareAisen;
	
	
	private Preference pTheme;// 主题设置
	private Preference pCustomWallpaper;// 自定义壁纸设置
	private CheckBoxPreference pLaunchWallpaper;// 桌面壁纸
	private CheckBoxPreference pTranslucent;// Translucent Modes
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		addPreferencesFromResource(R.xml.ui_settings);
		
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		getActivity().getActionBar().setDisplayShowHomeEnabled(false);
		getActivity().getActionBar().setTitle(R.string.title_settings);
		
		pBasic = (Preference) findPreference("pBasic");
		pBasic.setOnPreferenceClickListener(this);
		pNotification = (Preference) findPreference("pNotification");
		pNotification.setOnPreferenceClickListener(this);
		pFlow = (Preference) findPreference("pFlow");
		pFlow.setOnPreferenceClickListener(this);
		pAbout = (Preference) findPreference("pAbout");
		pAbout.setOnPreferenceClickListener(this);
		pShareAisen = (Preference) findPreference("pShareAisen");
		pShareAisen.setOnPreferenceClickListener(this);
		
		pTheme = (Preference) findPreference("pTheme");
		pTheme.setOnPreferenceClickListener(this);
		
		Preference pMoreAdvanced = (Preference) findPreference("pMoreAdvanced");
		pMoreAdvanced.setOnPreferenceClickListener(this);
		
		pCustomWallpaper = (Preference) findPreference("pCustomWallpaper");
		pCustomWallpaper.setOnPreferenceClickListener(this);
		
		pLaunchWallpaper = (CheckBoxPreference) findPreference("pLaunchWallpaper");
		pLaunchWallpaper.setOnPreferenceChangeListener(this);
		
		pTranslucent = (CheckBoxPreference) findPreference("pTranslucent");
		pTranslucent.setOnPreferenceChangeListener(this);
		if (!WallpaperViewGroup.isKitKat())
			pTranslucent.setEnabled(false);
		
		wallpaperBean = AppContext.getWallpaper();
	}
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		if ("pBasic".equals(preference.getKey())) {
			BasicSettingsFragment.launch(getActivity());
		}
		else if ("pNotification".equals(preference.getKey())) {
			NotificationSettingsFragment.launch(getActivity());
		}
		else if ("pFlow".equals(preference.getKey())) {
			FlowSettingsFragment.launch(getActivity());
		}
		else if ("pAbout".equals(preference.getKey())) {
			OtherSettingsFragment.launch(getActivity());
		}
		else if ("pShareAisen".equals(preference.getKey())) {
			PublishActivity.publishRecommend(getActivity());
		}
		else if ("pMoreAdvanced".equals(preference.getKey())) {
			AdvancedFragment.launch(getActivity());
		} 
		// 主题设置
		else if ("pTheme".equals(preference.getKey())) {
			ThemeStyleSettingsFragment.launch(getActivity());
		}
		// 自定义壁纸
		if ("pCustomWallpaper".equals(preference.getKey())) {
			WallpaperSettingsFragment.launch(getActivity());
		}
		
		return super.onPreferenceClick(preference);
	}
	
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// Translucent Modes
		if ("pTranslucent".equals(preference.getKey())) {
			if (Boolean.parseBoolean(newValue.toString())) {

				if (AppSettings.isLaunchWallpaper())
					CommSettings.setAppTheme(R.style.BaseTheme_Wallpaper_Translucent);
				else 
					CommSettings.setAppTheme(R.style.BaseTheme_Translucent);
			}
			else {
				if (AppSettings.isLaunchWallpaper()) 
					CommSettings.setAppTheme(R.style.BaseTheme_Wallpaper);
				else
					CommSettings.setAppTheme(R.style.BaseTheme);
			}
			
			((BaseActivity) getActivity()).reload();
		}
		// 桌面壁纸
		else if ("pLaunchWallpaper".equals(preference.getKey())) {
			if (Boolean.parseBoolean(newValue.toString())) {
				if (AppSettings.isTranslucentModes())
					CommSettings.setAppTheme(R.style.BaseTheme_Wallpaper_Translucent);
				else
					CommSettings.setAppTheme(R.style.BaseTheme_Wallpaper);
			}
			else {
				if (AppSettings.isTranslucentModes())
					CommSettings.setAppTheme(R.style.BaseTheme_Translucent);
				else
					CommSettings.setAppTheme(R.style.BaseTheme);
			}
			
			((BaseActivity) getActivity()).reload();
		}
		
		return true;
	}
	
	private WallpaperBean wallpaperBean;
	
	@Override
	public void onResume() {
		super.onResume();
		
		BaiduAnalyzeUtils.onPageStart("设置");

		resetTheme();
		
		if (wallpaperBean != AppContext.getWallpaper())
			((BaseActivity) getActivity()).reload();
	}
	
	private void resetTheme() {
		pCustomWallpaper.setEnabled(true);
		pCustomWallpaper.setSummary("");
		
		pTheme.setEnabled(true);
		pTheme.setSummary("");
		
		// 桌面壁纸
		if (AppSettings.isLaunchWallpaper()) {
			pTheme.setEnabled(false);
			pTheme.setSummary(R.string.settings_ui_bg_summary_02);
			
			pCustomWallpaper.setEnabled(false);
			pCustomWallpaper.setSummary(R.string.settings_ui_bg_summary_02);
		}
		else {
			WallpaperBean wallpaper = AppContext.getWallpaper();
			if (wallpaper != null) {
				pTheme.setEnabled(false);
				pTheme.setSummary(R.string.settings_ui_bg_summary_03);
				
				pCustomWallpaper.setEnabled(true);
				pCustomWallpaper.setSummary(R.string.settings_ui_bg_summary_03);
			}
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		BaiduAnalyzeUtils.onPageEnd("设置");
	}

}
