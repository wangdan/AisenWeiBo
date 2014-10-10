package org.aisen.weibo.sina.ui.fragment.settings;

import java.util.List;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.bean.AccountBean;
import org.aisen.weibo.sina.support.db.AccountDB;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.BaiduAnalyzeUtils;
import org.aisen.weibo.sina.ui.activity.common.FragmentContainerActivity;
import org.aisen.weibo.sina.ui.activity.publish.PublishActivity;
import org.aisen.weibo.sina.ui.fragment.account.AccountFragment;

import android.app.Activity;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;

import com.m.support.task.TaskException;
import com.m.support.task.WorkTask;

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
	private Preference pAccount;
	private Preference pAbout;
	private Preference pShareAisen;
	private CheckBoxPreference pRotatePic;// 设置旋转照片
	private CheckBoxPreference pSendDelay;// 内容发布延迟
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		addPreferencesFromResource(R.xml.ui_settings);
		
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		getActivity().getActionBar().setTitle(R.string.title_settings);
		
		pBasic = (Preference) findPreference("pBasic");
		pBasic.setOnPreferenceClickListener(this);
		pNotification = (Preference) findPreference("pNotification");
		pNotification.setOnPreferenceClickListener(this);
		pFlow = (Preference) findPreference("pFlow");
		pFlow.setOnPreferenceClickListener(this);
		pAccount = (Preference) findPreference("pAccount");
		pAccount.setOnPreferenceClickListener(this);
		pAbout = (Preference) findPreference("pAbout");
		pAbout.setOnPreferenceClickListener(this);
		pShareAisen = (Preference) findPreference("pShareAisen");
		pShareAisen.setOnPreferenceClickListener(this);
		pRotatePic = (CheckBoxPreference) findPreference("pRotatePic");
		pRotatePic.setOnPreferenceChangeListener(this);
		pSendDelay = (CheckBoxPreference) findPreference("pSendDelay");
		pSendDelay.setOnPreferenceChangeListener(this);
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
		else if ("pAccount".equals(preference.getKey())) {
			AccountFragment.launch(getActivity());
		}
		else if ("pAbout".equals(preference.getKey())) {
			OtherSettingsFragment.launch(getActivity());
		}
		else if ("pShareAisen".equals(preference.getKey())) {
			PublishActivity.publishRecommend(getActivity());
		}
		
		return super.onPreferenceClick(preference);
	}
	
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if ("pRotatePic".equals(preference.getKey())) {
			if (Boolean.parseBoolean(newValue.toString()))
				BaiduAnalyzeUtils.onEvent("set_rotate_pic", "设置旋转照片");
		}
		else if ("pSendDelay".equals(preference.getKey())) {
			if (Boolean.parseBoolean(newValue.toString()))
				BaiduAnalyzeUtils.onEvent("set_publish_delay", "设置延迟发布");
		}
		
		return true;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (AppContext.isLogedin())
			new AccountTask().execute();
		else
			getActivity().finish();
		
		BaiduAnalyzeUtils.onPageStart("设置");
	}
	
	class AccountTask extends WorkTask<Void, Void, List<AccountBean>> {

		@Override
		public List<AccountBean> workInBackground(Void... params) throws TaskException {
			return AccountDB.query();
		}
		
		@Override
		protected void onSuccess(List<AccountBean> result) {
			super.onSuccess(result);
			
			if (getActivity() != null)
				pAccount.setSummary(String.format(getString(R.string.settings_account_sumary), result.size()));
		}
		
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		BaiduAnalyzeUtils.onPageEnd("设置");
	}

}
