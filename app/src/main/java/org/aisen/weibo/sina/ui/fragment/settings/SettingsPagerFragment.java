package org.aisen.weibo.sina.ui.fragment.settings;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.umeng.analytics.MobclickAgent;

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
	protected void setupTabLayout(Bundle savedInstanceSate, TabLayout tabLayout) {
		super.setupTabLayout(savedInstanceSate, tabLayout);

		tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
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

			MobclickAgent.onEvent(getActivity(), "donate");
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.menu_settings, menu);
	}

	private void showDonateDialog() {
		new AlertDialogWrapper.Builder(getActivity())
				.setTitle(R.string.settings_donate_dialog_title)
				.setMessage(R.string.settings_donate_dialog_message)
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						MobclickAgent.onEvent(getActivity(), "donate_cancel");
					}

				})
				.setPositiveButton(R.string.settings_donate_yes, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						MobclickAgent.onEvent(getActivity(), "donate_yes");

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
