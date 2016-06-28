package org.aisen.weibo.sina.ui.fragment.settings;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.util.Log;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.setting.SettingUtility;
import org.aisen.android.common.utils.ActivityHelper;
import org.aisen.android.support.action.IAction;
import org.aisen.android.support.permissions.APermissionsAction;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.base.MyApplication;
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
	private CheckBoxPreference pCrashLog;// Crash日志上报
	private CheckBoxPreference pNetworkDelay;// 网络请求延迟
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

		pCrashLog = (CheckBoxPreference) findPreference("pCrashLog");
		pCrashLog.setOnPreferenceChangeListener(this);

		pNetworkDelay = (CheckBoxPreference) findPreference("pNetworkDelay");
		pNetworkDelay.setOnPreferenceChangeListener(this);
		
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
		else if ("pNetworkDelay".equals(preference.getKey())) {
			SettingUtility.setPermanentSetting("http_delay", Boolean.parseBoolean(newValue.toString()) ? 10 * 1000 : 0);
		}
		else if ("pCrashLog".equals(preference.getKey())) {
			if (Boolean.parseBoolean(newValue.toString())) {
				checkPhotoPermission(((BaseActivity) getActivity()), false);
			}
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

	public static void checkPhotoPermission(final BaseActivity activity, final boolean shownever) {
		APermissionsAction permissionsAction = new APermissionsAction(activity, null, activity.getActivityHelper(), Manifest.permission.CALL_PHONE) {

			@Override
			protected void onPermissionDenied(boolean alwaysDenied) {
				if (alwaysDenied) {
					if (shownever && ActivityHelper.getBooleanShareData(GlobalContext.getInstance(), "donot_crash_remind", false)) {
						return;
					}

					MaterialDialog.Builder builder = new MaterialDialog.Builder(activity)
														.forceStacking(true)
														.content(R.string.crash_hint)
														.negativeText(R.string.crash_settings)
														.onPositive(new MaterialDialog.SingleButtonCallback() {

															@Override
															public void onClick(MaterialDialog dialog, DialogAction which) {
																ActivityHelper.putBooleanShareData(GlobalContext.getInstance(), "donot_crash_remind", true);
															}

														})
														.onNegative(new MaterialDialog.SingleButtonCallback() {

															@Override
															public void onClick(MaterialDialog dialog, DialogAction which) {
																AisenUtils.gotoSettings(activity);
															}

														});
					if (shownever)
						builder.positiveText(R.string.donnot_remind);
					builder.show();
				}
			}

		};
		// 开启日志上报
		new IAction(activity, permissionsAction) {

			@Override
			public void doAction() {
				Log.d("Main", "setupCrash");

				((MyApplication) GlobalContext.getInstance()).setupCrash();
			}

		}.run();
	}

}
