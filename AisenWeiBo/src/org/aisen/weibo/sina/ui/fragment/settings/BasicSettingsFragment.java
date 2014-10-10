package org.aisen.weibo.sina.ui.fragment.settings;

import java.io.File;
import java.util.Locale;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.db.FriendMentionDB;
import org.aisen.weibo.sina.support.utils.AppSettings;
import org.aisen.weibo.sina.support.utils.BaiduAnalyzeUtils;
import org.aisen.weibo.sina.ui.activity.common.FragmentContainerActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.m.common.context.GlobalContext;
import com.m.common.settings.SettingUtility;
import com.m.common.utils.ActivityHelper;
import com.m.common.utils.SystemUtility;
import com.m.support.task.TaskException;
import com.m.support.task.WorkTask;
import com.m.ui.activity.BaseActivity;
import com.m.ui.utils.ViewUtils;

/**
 * 基本设置
 * 
 * @author wangdan
 *
 */
@SuppressLint("SdCardPath") public class BasicSettingsFragment extends PreferenceFragment 
										implements OnPreferenceClickListener , OnPreferenceChangeListener {

	public static void launch(Activity from) {
		FragmentContainerActivity.launch(from, BasicSettingsFragment.class, null);
	}
	
	private ListPreference pTextSize;// 字体大小
	private ListPreference pLanguage;// 语言设置
	private CheckBoxPreference pShowRemark;// 显示备注
	private CheckBoxPreference pFastScrollBar;// 显示快速滚动条
	private CheckBoxPreference pShowDefGroup;// 显示默认分组微博
	private Preference pPicSavePath;// 图片保存路径
	private Preference pClearRecentMention;// 清理@历史记录
	private CheckBoxPreference pDoubleClickToRefresh;// 置顶并刷新
	private ListPreference pPicLargeMode;// 高清图片排版模式
	private ListPreference pListViewAnim;// 列表动画
	private ListPreference pRefreshViewType;// 列表刷新控件
	private CheckBoxPreference pAutoRefresh;// 列表自动刷新
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		addPreferencesFromResource(R.xml.ui_basic_settings);
		
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		getActivity().getActionBar().setTitle(R.string.title_basic_settings);
		
		pTextSize = (ListPreference) findPreference("pTextSize");
		pTextSize.setOnPreferenceChangeListener(this);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
		int value = Integer.parseInt(prefs.getString("pTextSize", "0"));
		setTextSize(value);
		
		pLanguage = (ListPreference) findPreference("pLanguage");
		pLanguage.setOnPreferenceChangeListener(this);
		value = Integer.parseInt(prefs.getString("pLanguage", "0"));
		setLanguage(value);
		
		pShowRemark = (CheckBoxPreference) findPreference("pShowRemark");
		pShowRemark.setOnPreferenceChangeListener(this);
		
		pDoubleClickToRefresh = (CheckBoxPreference) findPreference("pDoubleClickToRefresh");
		pDoubleClickToRefresh.setOnPreferenceChangeListener(this);
		
		pShowDefGroup = (CheckBoxPreference) findPreference("pShowDefGroup");
		pShowDefGroup.setOnPreferenceChangeListener(this);
		
		pFastScrollBar = (CheckBoxPreference) findPreference("pFastScrollBar");
		if (pFastScrollBar != null)
			pFastScrollBar.setOnPreferenceChangeListener(this);
		
		pPicSavePath = (Preference) findPreference("pPicSavePath");
		pPicSavePath.setOnPreferenceClickListener(this);
//		pPicSavePath.setSummary(SystemUtility.getSdcardPath() + File.separator + AppSettings.getImageSavePath() + File.separator);
		pPicSavePath.setSummary("/sdcard" + File.separator + AppSettings.getImageSavePath() + File.separator);
		
		pClearRecentMention = (Preference) findPreference("pClearRecentMention");
		pClearRecentMention.setOnPreferenceClickListener(this);
		setMentionHint();
		
		// 图片上传设置
		pPicLargeMode = (ListPreference) findPreference("pPicLargeMode");
		pPicLargeMode.setOnPreferenceChangeListener(this);
		prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
		value = Integer.parseInt(prefs.getString("pPicLargeMode", "0"));
		setListSetting(value, R.array.picLargeMode, pPicLargeMode);
		
		pListViewAnim = (ListPreference) findPreference("pListViewAnim");
		if (pListViewAnim != null) {
			pListViewAnim.setOnPreferenceChangeListener(this);
			prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
			value = Integer.parseInt(prefs.getString("pListViewAnim", "0"));
			setListSetting(value, R.array.pListViewAnim, pListViewAnim);
		}
		
		pRefreshViewType = (ListPreference) findPreference("pRefreshViewType");
		pRefreshViewType.setOnPreferenceChangeListener(this);
		prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
		value = Integer.parseInt(prefs.getString("pRefreshViewType", "0"));
		setListSetting(value, R.array.pRefreshViewType, pRefreshViewType);
		
		pAutoRefresh = (CheckBoxPreference) findPreference("pAutoRefresh");
		pAutoRefresh.setOnPreferenceChangeListener(this);
		pAutoRefresh.setChecked(SettingUtility.getPermanentSettingAsBool("pAutoRefresh", true));
	}
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		// 设置图片保存路径
		if ("pPicSavePath".equals(preference.getKey())) {
			modifyImageSavePath();
		}
		// 清理好友提及历史记录
		else if ("pClearRecentMention".equals(preference.getKey())) {
			clearMentionHistory();
		}
		return true;
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// 列表字体
		if ("pTextSize".equals(preference.getKey())) {
			setTextSize(Integer.parseInt(newValue.toString()));
		}
		// 语言设置
		else if ("pLanguage".equals(preference.getKey())) {
			int language = Integer.parseInt(newValue.toString());
			switch (language) {
			// 跟随系统语言
			case 0:
				SettingUtility.setPermanentSetting("language", null);
				SettingUtility.setPermanentSetting("language-country", null);
				break;
			// 中文简体
			case 1:
				SettingUtility.setPermanentSetting("language", Locale.SIMPLIFIED_CHINESE.getLanguage());
				SettingUtility.setPermanentSetting("language-country", Locale.SIMPLIFIED_CHINESE.getCountry());
				break;
			// 中文繁体
			case 2:
				SettingUtility.setPermanentSetting("language", Locale.TAIWAN.getLanguage());
				SettingUtility.setPermanentSetting("language-country", Locale.TAIWAN.getCountry());
				break;
			}
			
			setLanguage(language);
			
			((BaseActivity) getActivity()).reload();
		}
		// 是否显示默认分组
		else if ("pShowDefGroup".equals(preference.getKey())) {
			ActivityHelper.getInstance().putBooleanShareData("ChanneSortHasChanged", true);
		}
		// 双击ActionBar同时刷新
		else if ("pDoubleClickToRefresh".equals(preference.getKey())) {
			SettingUtility.setPermanentSetting("com.m.ON_DOUBLE_CLICK_AC_TO_REFRESH", Boolean.parseBoolean(newValue.toString()));
		}
		// 高清图片排版设置
		else if ("pPicLargeMode".equals(preference.getKey())) {
			setListSetting(Integer.parseInt(newValue.toString()), R.array.picLargeMode, pPicLargeMode);
		}
		// 列表动画
		else if ("pListViewAnim".equals(preference.getKey())) {
			setListSetting(Integer.parseInt(newValue.toString()), R.array.pListViewAnim, pListViewAnim);
		}
		// 设置刷新控件
		else if ("pRefreshViewType".equals(preference.getKey())) {
			setListSetting(Integer.parseInt(newValue.toString()), R.array.pRefreshViewType, pRefreshViewType);
		}
		// 列表控件是否自动刷新
		else if ("pAutoRefresh".equals(preference.getKey())) {
			SettingUtility.setPermanentSetting("pAutoRefresh", Boolean.parseBoolean(newValue.toString()));
		}
		return true;
	}
	
	private void setMentionHint() {
		new WorkTask<Void, Void, Integer>() {

			@Override
			public Integer workInBackground(Void... params) throws TaskException {
				return FriendMentionDB.query().size();
			}
			
			@Override
			protected void onSuccess(Integer result) {
				if (result == 0)
					pClearRecentMention.setSummary(null);
				else 
					pClearRecentMention.setSummary(String.format(getString(R.string.settings_basic_history_remind), result));
			};
			
		}.execute();
	}
	
	private void setTextSize(int value) {
		String[] valueTitleArr = getResources().getStringArray(R.array.txtSize);
		
		pTextSize.setSummary(valueTitleArr[value]);
	}
	
	private void setLanguage(int value) {
		String[] valueTitleArr = getResources().getStringArray(R.array.pLanguage);
		
		pLanguage.setSummary(valueTitleArr[value]);
	}
	
	// 修改图片保存路径
	private void modifyImageSavePath() {
		View entryView = View.inflate(getActivity(), R.layout.lay_dialog_remark_entry, null);
		final EditText editRemark = (EditText) entryView.findViewById(R.id.editRemark);
		editRemark.setHint(R.string.settings_dir_hint);
		editRemark.setText(AppSettings.getImageSavePath());
		editRemark.setSelection(editRemark.getText().toString().length());
		new AlertDialog.Builder(getActivity()).setTitle(R.string.settings_modify_picpath_title)
							.setView(entryView)
							.setNegativeButton(R.string.cancel, null)
							.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									if (TextUtils.isEmpty(editRemark.getText().toString())) {
										ViewUtils.showMessage(R.string.update_faild);
										return;
									}
									
									String path = SystemUtility.getSdcardPath() + File.separator + editRemark.getText().toString() + File.separator;
									File file = new File(path);
									if (file.exists() || file.mkdirs()) {
										AppSettings.setImageSavePath(editRemark.getText().toString());
										
//										pPicSavePath.setSummary(path);
										pPicSavePath.setSummary("/sdcard" + File.separator + editRemark.getText().toString() + File.separator);
										
										ViewUtils.showMessage(R.string.update_success);
									}
									else {
										ViewUtils.showMessage(R.string.update_faild);
									}
								}
										
							})
							.show();
	}
	
	private void clearMentionHistory() {
		new AlertDialog.Builder(getActivity()).setTitle(R.string.remind)
							.setMessage(R.string.settings_clear_history_remind)
							.setNegativeButton(R.string.cancel, null)
							.setPositiveButton(R.string.settings_clear, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									FriendMentionDB.clear();
									
									setMentionHint();
								}
							})
							.show();
	}
	
	private void setListSetting(int value, int hintId, ListPreference listPreference) {
		String[] valueTitleArr = getResources().getStringArray(hintId);
		
		listPreference.setSummary(valueTitleArr[value]);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		BaiduAnalyzeUtils.onPageStart("基础设置");
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		BaiduAnalyzeUtils.onPageEnd("基础设置");
	}
	
}
