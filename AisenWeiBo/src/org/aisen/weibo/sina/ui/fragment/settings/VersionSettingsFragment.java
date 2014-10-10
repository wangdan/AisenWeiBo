package org.aisen.weibo.sina.ui.fragment.settings;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.bean.ApkInfo;
import org.aisen.weibo.sina.support.biz.BizLogic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.m.common.context.GlobalContext;
import com.m.common.utils.ActivityHelper;
import com.m.common.utils.SystemUtility;
import com.m.common.utils.Utils;
import com.m.support.task.TaskException;
import com.m.support.task.WorkTask;
import com.m.ui.activity.BaseActivity;
import com.m.ui.utils.ViewUtils;

public class VersionSettingsFragment extends PreferenceFragment
										implements OnPreferenceClickListener{

	private Preference pVersion;// 检查版本
	private Preference pGrade;// 给我评分
	
	private ApkInfo mApkInfo;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String apkInfoJson = ActivityHelper.getInstance().getShareData("apkInfo", null);
		if (!TextUtils.isEmpty(apkInfoJson)) 
			mApkInfo = JSON.parseObject(apkInfoJson, ApkInfo.class);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		refreshVersion();
	}
	
	private void refreshVersion() {
		if (pVersion == null) {
			pVersion = (Preference) (Preference) getPreferenceManager().findPreference("pVersion");
			pVersion.setOnPreferenceClickListener(this);
			if (ActivityHelper.getInstance().getBooleanShareData("newVersion", false))
				pVersion.setIcon(R.drawable.skin_icon_new);
		}
		
		if (pGrade == null) {
			pGrade = (Preference) (Preference) getPreferenceManager().findPreference("pGrade");
			pGrade.setOnPreferenceClickListener(this);
		}
		
		pVersion.setTitle(R.string.settings_version_p);
		pVersion.setSummary(String.format(getString(R.string.settings_current_version), SystemUtility.getVersionName(getActivity())));
		
		if (mApkInfo != null && mApkInfo.getVersionCode() > SystemUtility.getVersionCode(getActivity())) {
			pVersion.setTitle(R.string.settings_find_version);		
			pVersion.setSummary(String.format(getString(R.string.settings_version_summary), 
					SystemUtility.getVersionName(getActivity()), mApkInfo.getVersionName()));
		}
	}
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		if ("pVersion".equals(preference.getKey())) {
			if (mApkInfo != null && mApkInfo.getVersionCode() > SystemUtility.getVersionCode(getActivity())) {
				showNewVersionDialog(getActivity(), mApkInfo, false);
			}
			else {
				showVersionDialog(getActivity());
			}
		}
		else if ("pGrade".equals(preference.getKey())) {
			startMarket();
		}
		return true;
	}
	
	public static void showVersionDialog(final Activity context) {
		VersionDialogFragment.launch(context);
	}
	
	public static void showNewVersionDialog(Activity context, final ApkInfo apkInfo, boolean ignoreAble) {
		final Resources res = GlobalContext.getInstance().getResources();
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context)
				.setTitle(String.format(res.getString(R.string.settings_new_version_remind), apkInfo.getVersionName()))
				.setMessage(apkInfo.getDes().replace("\\", "").replace("n", "\n"))
				.setNeutralButton(R.string.next_time_remind, null);
		if (ignoreAble) {
			dialogBuilder.setNegativeButton(R.string.donnot_remind, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ActivityHelper.getInstance().putBooleanShareData("IgnoreNewVersion-" + apkInfo.getVersionCode(), true);
				}
				
			});
		}
		dialogBuilder.setPositiveButton(R.string.settings_update_now, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				startMarket();
			}
			
		})
		.show();
	}
	
	public static void startMarket() {
		Uri uri = Uri.parse(String.format("market://details?id=%s", SystemUtility.getPackage(GlobalContext.getInstance())));  
		if (Utils.isIntentSafe(BaseActivity.getRunningActivity(), uri)) {
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);  
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
			GlobalContext.getInstance().startActivity(intent); 
		}
		// 没有安装市场
		else {
			ViewUtils.showMessage(R.string.settings_error_market);
		}
	}
	
	public static void checkVersion() {
		new WorkTask<Void, Void, ApkInfo>() {

			@Override
			public ApkInfo workInBackground(Void... params) throws TaskException {
				ApkInfo apkInfo = BizLogic.newInstance().getApkInfo();
				ActivityHelper.getInstance().putShareData("apkInfo", JSON.toJSONString(apkInfo));
				return apkInfo;
			}
			
			protected void onSuccess(ApkInfo result) {
				super.onSuccess(result);
				
				if (result.getVersionCode() > SystemUtility.getVersionCode(GlobalContext.getInstance())) {
					ActivityHelper.getInstance().putBooleanShareData("newVersion", true);
					
					if (!ActivityHelper.getInstance().getBooleanShareData("IgnoreNewVersion-" + result.getVersionCode(), false))
						ActivityHelper.getInstance().putBooleanShareData("IgnoreNewVersion-" + result.getVersionCode(), false);
				}
				else {
					ActivityHelper.getInstance().putBooleanShareData("newVersion", false);
				}
			};
			
		}.execute();
	}
	
}
