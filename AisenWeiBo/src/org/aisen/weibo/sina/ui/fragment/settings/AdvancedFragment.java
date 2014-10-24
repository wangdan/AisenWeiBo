package org.aisen.weibo.sina.ui.fragment.settings;

import java.util.List;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.bean.AccountBean;
import org.aisen.weibo.sina.support.db.AccountDB;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.BaiduAnalyzeUtils;
import org.aisen.weibo.sina.ui.activity.common.FragmentContainerActivity;
import org.aisen.weibo.sina.ui.fragment.account.AccountFragment;

import android.app.Activity;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;

import com.m.support.task.TaskException;
import com.m.support.task.WorkTask;

/**
 * 更多高级设置
 * 
 * @author Jeff.Wang
 *
 * @date 2014年10月21日
 */
public class AdvancedFragment extends BasePreferenceFragment 
									implements OnPreferenceChangeListener, OnPreferenceClickListener {

	public static void launch(Activity from) {
		FragmentContainerActivity.launch(from, AdvancedFragment.class, null);
	}
	
	private Preference pAccount;
	private CheckBoxPreference pRotatePic;// 设置旋转照片
	private CheckBoxPreference pSendDelay;// 内容发布延迟
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		addPreferencesFromResource(R.xml.ui_advanced);
		
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		getActivity().getActionBar().setDisplayShowHomeEnabled(false);
		getActivity().getActionBar().setTitle(R.string.title_advanced);
		
		pAccount = (Preference) findPreference("pAccount");
		pAccount.setOnPreferenceClickListener(this);
		
		pRotatePic = (CheckBoxPreference) findPreference("pRotatePic");
		pRotatePic.setOnPreferenceChangeListener(this);
		pSendDelay = (CheckBoxPreference) findPreference("pSendDelay");
		pSendDelay.setOnPreferenceChangeListener(this);
	}
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		if ("pAccount".equals(preference.getKey())) {
			AccountFragment.launch(getActivity());
		}
		return true;
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
		
		BaiduAnalyzeUtils.onPageStart("高级设置");
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		BaiduAnalyzeUtils.onPageEnd("高级设置");
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

}
