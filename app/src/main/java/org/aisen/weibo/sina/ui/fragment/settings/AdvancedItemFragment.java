package org.aisen.weibo.sina.ui.fragment.settings;

import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;

import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.support.bean.AccountBean;
import org.aisen.weibo.sina.support.utils.AccountUtils;
import org.aisen.weibo.sina.ui.fragment.account.AccountFragment;
import org.aisen.weibo.sina.ui.widget.AisenTextView;

import java.util.List;

/**
 * 更多高级设置
 * 
 * @author Jeff.Wang
 *
 * @date 2014年10月21日
 */
public class AdvancedItemFragment extends BasePreferenceFragment
									implements OnPreferenceChangeListener, OnPreferenceClickListener {

	public static BasePreferenceFragment newInstance() {
		return new AdvancedItemFragment();
	}

	private CheckBoxPreference pInnerBrowser;// 设置默认浏览器
	private Preference pAccount;// 账号管理
	private Preference pNotification;// 通知中心
	private Preference pFlow;// 流量控制
//    private Preference pGroupsOffline;// 离线设置
	private CheckBoxPreference pRotatePic;// 设置旋转照片
	private CheckBoxPreference pSendDelay;// 内容发布延迟

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		addPreferencesFromResource(R.xml.ui_advanced_item);

		pRotatePic = (CheckBoxPreference) findPreference("pRotatePic");
		pRotatePic.setOnPreferenceChangeListener(this);
		pSendDelay = (CheckBoxPreference) findPreference("pSendDelay");
		pSendDelay.setOnPreferenceChangeListener(this);
//        pGroupsOffline = (Preference) findPreference("pGroupsOffline");
//        pGroupsOffline.setOnPreferenceClickListener(this);
		
		pNotification = (Preference) findPreference("pNotification");
		pNotification.setOnPreferenceClickListener(this);
		pFlow = (Preference) findPreference("pFlow");
        findPreference("pOffline").setOnPreferenceClickListener(this);
		pFlow.setOnPreferenceClickListener(this);
		pAccount = (Preference) findPreference("pAccount");
		pAccount.setOnPreferenceClickListener(this);
		
		pInnerBrowser = (CheckBoxPreference) findPreference("pInnerBrowser");
		pInnerBrowser.setOnPreferenceChangeListener(this);
	}
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		if ("pNotification".equals(preference.getKey())) {
			NotificationSettingsFragment.launch(getActivity());
		}
		else if ("pFlow".equals(preference.getKey())) {
			FlowSettingsFragment.launch(getActivity());
		}
		else if ("pAccount".equals(preference.getKey())) {
			AccountFragment.launch(getActivity());
		}
        else if ("pOffline".equals(preference.getKey())) {
            OfflineSettingsFragment.launch(getActivity());
        }
		return true;
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if ("pRotatePic".equals(preference.getKey())) {
		}
		else if ("pSendDelay".equals(preference.getKey())) {
		}
		else if ("pInnerBrowser".equals(preference.getKey())) {
			try {
				AisenTextView.stringMemoryCache.evictAll();
			} catch (Exception e) {
			}
		}

		return true;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (AppContext.isLoggedIn())
			new AccountTask().execute();
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	class AccountTask extends WorkTask<Void, Void, List<AccountBean>> {

		@Override
		public List<AccountBean> workInBackground(Void... params) throws TaskException {
			return AccountUtils.queryAccount();
		}
		
		@Override
		protected void onSuccess(List<AccountBean> result) {
			super.onSuccess(result);
			
			if (getActivity() != null)
				pAccount.setSummary(String.format(getString(R.string.settings_account_sumary), result.size()));
		}
		
	}
	
	Handler mHandler = new Handler();
	
}
