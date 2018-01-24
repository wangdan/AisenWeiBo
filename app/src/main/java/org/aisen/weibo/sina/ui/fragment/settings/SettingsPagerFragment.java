package org.aisen.weibo.sina.ui.fragment.settings;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.support.bean.TabItem;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.ATabsTabLayoutFragment;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.support.utils.UMengUtil;
import org.aisen.weibo.sina.ui.activity.base.SinaCommonActivity;

import java.util.ArrayList;

/**
 * 程序设置
 * 
 * @author wangdan
 *
 */
public class SettingsPagerFragment extends ATabsTabLayoutFragment<TabItem> {

	public static void launch(Activity from) {
		SinaCommonActivity.launch(from, SettingsPagerFragment.class, null);
	}

	@Override
	public int inflateContentView() {
		return R.layout.ui_settings_tabs;
	}

	@Override
	protected void setupTabLayout(Bundle savedInstanceSate) {
		super.setupTabLayout(savedInstanceSate);

		getTablayout().setTabMode(TabLayout.MODE_SCROLLABLE);
	}

	@Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		super.layoutInit(inflater, savedInstanceSate);

        setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		BaseActivity activity = (BaseActivity) getActivity();
		activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		activity.getSupportActionBar().setDisplayShowHomeEnabled(false);
		activity.getSupportActionBar().setTitle(R.string.title_settings);
	}

	@Override
	protected ArrayList<TabItem> generateTabs() {
		ArrayList<TabItem> items = new ArrayList<TabItem>();
		
		String[] itemArr = getResources().getStringArray(R.array.settingsPager);
		int index = 1;
		for (String item : itemArr) {
			TabItem bean = new TabItem();
			bean.setTitle(item);
			bean.setType(String.valueOf(index++));
			items.add(bean);
		}
		
		return items;
	}

	@Override
	protected Fragment newFragment(TabItem bean) {
		int index = Integer.parseInt(bean.getType());
		
		switch (index) {
		// 基本
		case 1:
			return BasicItemSettingsFragment.newInstance();
		// 高级
		case 2:
			return AdvancedItemFragment.newInstance();
		// 其他
		case 3:
			return OtherItemFragment.newInstance();
		// 帮助
		case 4:
			return AisenHelpFragment.newInstance();
		}
		
		return BasicItemSettingsFragment.newInstance();
	}

    @Override
    protected String configLastPositionKey() {
        return "Settings";
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.donate) {
			showDonateDialog();

			UMengUtil.onEvent(getActivity(), "donate");
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.menu_settings, menu);

		menu.findItem(R.id.donate).setVisible(false);
	}

	private void showDonateDialog() {
		new MaterialDialog.Builder(getActivity())
				.title(R.string.settings_donate_dialog_title)
				.content(R.string.settings_donate_dialog_message)
				.negativeText(R.string.cancel)
				.onNegative(new MaterialDialog.SingleButtonCallback() {

					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
						UMengUtil.onEvent(getActivity(), "donate_cancel");
					}

				})
				.positiveText(R.string.settings_donate_yes)
				.onPositive(new MaterialDialog.SingleButtonCallback() {

					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
						UMengUtil.onEvent(getActivity(), "donate_yes");

						AisenUtils.copyToClipboard("binglanhappy@163.com");

						SystemUtils.startActivity(getActivity(), "com.eg.android.AlipayGphone");
					}

				})
				.show();
	}

	@Override
	public void onResume() {
		super.onResume();

		UMengUtil.onPageStart(getActivity(), "设置页");
	}

	@Override
	public void onPause() {
		super.onPause();

		UMengUtil.onPageEnd(getActivity(), "设置页");
	}

}
