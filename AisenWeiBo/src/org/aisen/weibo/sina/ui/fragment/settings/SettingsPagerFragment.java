package org.aisen.weibo.sina.ui.fragment.settings;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;

import com.m.common.utils.ActivityHelper;
import com.m.component.container.FragmentArgs;
import com.m.component.container.FragmentContainerActivity;
import com.m.ui.activity.basic.BaseActivity;
import com.m.ui.fragment.AStripTabsFragment;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.support.utils.ThemeUtils;

import java.util.ArrayList;

/**
 * 程序设置
 * 
 * @author wangdan
 *
 */
public class SettingsPagerFragment extends AStripTabsFragment<AStripTabsFragment.StripTabItem> {

	public static void launch(Activity from) {
        FragmentArgs args = new FragmentArgs();
        args.add(SET_INDEX, ActivityHelper.getIntShareData("org.aisen.weibo.sina.Setting_pager", 0));

		FragmentContainerActivity.launch(from, SettingsPagerFragment.class, args);
	}

    private int theme = AppSettings.getThemeColor();

    @Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		super.layoutInit(inflater, savedInstanceSate);

        BaseActivity activity = (BaseActivity) getActivity();
		activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(false);
        activity.getSupportActionBar().setTitle(R.string.title_settings);
	}

    @Override
	protected ArrayList<StripTabItem> generateTabs() {
		ArrayList<StripTabItem> items = new ArrayList<StripTabItem>();
		
		String[] itemArr = getResources().getStringArray(R.array.settingsPager);
		int index = 1;
		for (String item : itemArr) {
            StripTabItem bean = new StripTabItem();
			bean.setTitle(item);
			bean.setType(String.valueOf(index++));
			items.add(bean);
		}
		
		return items;
	}

	@Override
	protected Fragment newFragment(StripTabItem bean) {
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
    public void onPageSelected(int position) {
        super.onPageSelected(position);

        ActivityHelper.putIntShareData("org.aisen.weibo.sina.Setting_pager", position);
    }

}
