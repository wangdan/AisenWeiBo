package org.aisen.weibo.sina.ui.fragment.settings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;

import com.umeng.analytics.MobclickAgent;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.common.utils.Utils;
import org.aisen.android.common.utils.ViewUtils;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.weibo.sina.R;

public class VersionSettingsFragment extends BasePreferenceFragment
										implements OnPreferenceClickListener{

	private Preference pVersion;// 检查版本
	private Preference pGrade;// 给我评分
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		refreshVersion();
	}
	
	private void refreshVersion() {
		if (pVersion == null) {
			pVersion = getPreferenceManager().findPreference("pVersion");
			pVersion.setOnPreferenceClickListener(this);
		}
		
		if (pGrade == null) {
			pGrade = getPreferenceManager().findPreference("pGrade");
			pGrade.setOnPreferenceClickListener(this);
		}
		
		pVersion.setTitle(R.string.settings_version_p);
		pVersion.setSummary(String.format(getString(R.string.settings_current_version), SystemUtils.getVersionName(getActivity())));
	}
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		if ("pVersion".equals(preference.getKey())) {
            showVersionDialog(getActivity());

			MobclickAgent.onEvent(getActivity(), "show_version");
        }
		else if ("pGrade".equals(preference.getKey())) {
			startMarket();
		}
		return true;
	}
	
	public static void showVersionDialog(final Activity context) {
		VersionDialogFragment.launch(context);
	}
	
	public static void startMarket() {
		Uri uri = Uri.parse(String.format("market://details?id=%s", SystemUtils.getPackage(GlobalContext.getInstance())));
		if (Utils.isIntentSafe(BaseActivity.getRunningActivity(), uri)) {
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);  
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
			GlobalContext.getInstance().startActivity(intent);
		}
		// 没有安装市场
		else {
			ViewUtils.showMessage(GlobalContext.getInstance(), R.string.settings_error_market);
		}
	}
	
}
