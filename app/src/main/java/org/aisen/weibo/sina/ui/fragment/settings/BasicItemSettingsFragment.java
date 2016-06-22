package org.aisen.weibo.sina.ui.fragment.settings;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.umeng.analytics.MobclickAgent;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.setting.SettingUtility;
import org.aisen.android.common.utils.ActivityHelper;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.common.utils.ViewUtils;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.base.MyApplication;
import org.aisen.weibo.sina.support.sqlit.FriendMentionDB;
import org.aisen.weibo.sina.ui.activity.base.AisenActivityHelper;

import java.io.File;

@SuppressLint("SdCardPath") public class BasicItemSettingsFragment extends BasePreferenceFragment
					implements OnPreferenceClickListener , OnPreferenceChangeListener {

	public static BasePreferenceFragment newInstance() {
		return new BasicItemSettingsFragment();
	}

    private Preference pTheme;// 主题设置

	private ListPreference pTextSize;// 字体大小
	private CheckBoxPreference pShowRemark;// 显示备注
	private CheckBoxPreference pShowDefGroup;// 显示默认分组微博
	
	private CheckBoxPreference pFastScrollBar;// 显示快速滚动条
	private Preference pPicSavePath;// 图片保存路径
	private Preference pClearRecentMention;// 清理@历史记录

	private CheckBoxPreference pAutoRefresh;// 列表自动刷新
	private ListPreference pSwipebackEdgeMode;// 手势返回方向
    private ListPreference pFabType;// 首页fab按钮功能
    private ListPreference pFabPosition;// 首页fab按钮位置

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		addPreferencesFromResource(R.xml.ui_basic_settings_item);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
		int value = 0;

        // 主题
        pTheme = (Preference) findPreference("pTheme");
        pTheme.setOnPreferenceClickListener(this);
        pTheme.setSummary(getResources().getStringArray(R.array.mdColorNames)[AppSettings.getThemeColor()]);
//        findPreference("pThemeCustom").setOnPreferenceClickListener(this);

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
		
		pTextSize = (ListPreference) findPreference("pTextSize");
		pTextSize.setOnPreferenceChangeListener(this);
		value = Integer.parseInt(prefs.getString("pTextSize", "4"));
		setTextSize(value);
		
		pShowRemark = (CheckBoxPreference) findPreference("pShowRemark");
		pShowRemark.setOnPreferenceChangeListener(this);
		
		pShowDefGroup = (CheckBoxPreference) findPreference("pShowDefGroup");
		if (pShowDefGroup != null)
			pShowDefGroup.setOnPreferenceChangeListener(this);
		
//        CheckBoxPreference pDoubleClickToRefresh = (CheckBoxPreference) findPreference("pDoubleClickToRefresh");
//		pDoubleClickToRefresh.setOnPreferenceChangeListener(this);
		
		pAutoRefresh = (CheckBoxPreference) findPreference("pAutoRefresh");
		pAutoRefresh.setOnPreferenceChangeListener(this);
		pAutoRefresh.setChecked(SettingUtility.getPermanentSettingAsBool("pAutoRefresh", true));
		
		pSwipebackEdgeMode = (ListPreference) findPreference("pSwipebackEdgeMode");
		pSwipebackEdgeMode.setOnPreferenceChangeListener(this);
		value = Integer.parseInt(prefs.getString("pSwipebackEdgeMode", "0"));
		setListSetting(value, R.array.swipebackEdgeMode, pSwipebackEdgeMode);

//        pFabType = (ListPreference) findPreference("pFabType");
//        pFabType.setOnPreferenceChangeListener(this);
//        value = Integer.parseInt(prefs.getString("pFabType", "0"));
//        setListSetting(value, R.array.fabTypes, pFabType);

        pFabPosition = (ListPreference) findPreference("pFabPosition");
		if (pFabPosition != null) {
			pFabPosition.setOnPreferenceChangeListener(this);
			value = Integer.parseInt(prefs.getString("pFabPosition", "1"));
			setListSetting(value, R.array.fabPosition, pFabPosition);
		}

        // 缓存清理
        Preference pClearCache = (Preference) findPreference("pClearCache");
        CacheClearFragment clearFragment = (CacheClearFragment) getActivity().getFragmentManager().findFragmentByTag("CacheClearFragment");
        if (clearFragment == null) {
            clearFragment = new CacheClearFragment();
            getActivity().getFragmentManager().beginTransaction().add(clearFragment, "CacheClearFragment").commit();
        }
        clearFragment.setPreference(pClearCache, MyApplication.getImagePath());
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
        // MD Colors
        else if ("pTheme".equals(preference.getKey())) {
//			ThemeStyleSettingsFragment.launch(getActivity());
            MDColorsDialogFragment.launch(getActivity());

			MobclickAgent.onEvent(getActivity(), "theme_setting");
        }
        // 自定义颜色
        else if ("pThemeCustom".equals(preference.getKey())) {
            CustomThemeColorFragment.launch(getActivity());
        }
		return true;
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// 是否显示默认分组
		if ("pShowDefGroup".equals(preference.getKey())) {
			ActivityHelper.putBooleanShareData(GlobalContext.getInstance(), "ChanneSortHasChanged", true);
		}
		// 列表字体
		else if ("pTextSize".equals(preference.getKey())) {
			setTextSize(Integer.parseInt(newValue.toString()));
		}
		// 列表控件是否自动刷新
		else if ("pAutoRefresh".equals(preference.getKey())) {
			SettingUtility.setPermanentSetting("pAutoRefresh", Boolean.parseBoolean(newValue.toString()));
		}
		else if ("pSwipebackEdgeMode".equals(preference.getKey())) {
			setListSetting(Integer.parseInt(newValue.toString()), R.array.swipebackEdgeMode, pSwipebackEdgeMode);
			
			mHandler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					AisenActivityHelper activityHelper = (AisenActivityHelper) ((BaseActivity) getActivity()).getActivityHelper();
					activityHelper.setSwipebackEdgeMode();
				}
				
			}, 500);
		}
        else if ("pFabType".equals(preference.getKey())) {
            setListSetting(Integer.parseInt(newValue.toString()), R.array.fabTypes, pFabType);
        }
        else if ("pFabPosition".equals(preference.getKey())) {
            setListSetting(Integer.parseInt(newValue.toString()), R.array.fabPosition, pFabPosition);
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
				else  {
                    Resources res = GlobalContext.getInstance().getResources();
                    pClearRecentMention.setSummary(String.format(res.getString(R.string.settings_basic_history_remind), result));
                }
			}
			
		}.execute();
	}
	
	// 修改图片保存路径
	private void modifyImageSavePath() {
		View entryView = View.inflate(getActivity(), R.layout.lay_dialog_remark_entry, null);
		final EditText editRemark = (EditText) entryView.findViewById(R.id.editRemark);
		editRemark.setHint(R.string.settings_dir_hint);
		editRemark.setText(AppSettings.getImageSavePath());
		editRemark.setSelection(editRemark.getText().toString().length());
		new AlertDialogWrapper.Builder(getActivity()).setTitle(R.string.settings_modify_picpath_title)
							.setView(entryView)
							.setNegativeButton(R.string.cancel, null)
							.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									if (TextUtils.isEmpty(editRemark.getText().toString())) {
										ViewUtils.showMessage(getActivity(), R.string.update_faild);
										return;
									}
									
									String path = SystemUtils.getSdcardPath() + File.separator + editRemark.getText().toString() + File.separator;
									File file = new File(path);
									if (file.exists() || file.mkdirs()) {
										AppSettings.setImageSavePath(editRemark.getText().toString());
										
//										pPicSavePath.setSummary(path);
										pPicSavePath.setSummary("/sdcard" + File.separator + editRemark.getText().toString() + File.separator);
										
										ViewUtils.showMessage(getActivity(), R.string.update_success);
									}
									else {
										ViewUtils.showMessage(getActivity(), R.string.update_faild);
									}
								}
										
							})
							.show();
	}
	
	private void clearMentionHistory() {
		new AlertDialogWrapper.Builder(getActivity()).setTitle(R.string.remind)
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
	
	private void setTextSize(int value) {
		String[] valueTitleArr = getResources().getStringArray(R.array.txtSizeNum);
		
		pTextSize.setSummary(valueTitleArr[value]);
	}
	
	Handler mHandler = new Handler();

}
