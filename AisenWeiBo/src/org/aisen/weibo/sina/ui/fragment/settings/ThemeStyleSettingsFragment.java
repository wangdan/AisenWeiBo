package org.aisen.weibo.sina.ui.fragment.settings;

import java.util.ArrayList;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.utils.AppSettings;
import org.aisen.weibo.sina.support.utils.BaiduAnalyzeUtils;
import org.aisen.weibo.sina.ui.activity.common.FragmentContainerActivity;
import org.aisen.weibo.sina.ui.widget.WallpaperViewGroup;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.m.support.Inject.ViewInject;
import com.m.support.adapter.ABaseAdapter.AbstractItemView;
import com.m.ui.activity.BaseActivity;
import com.m.ui.fragment.AGridFragment;

/**
 * 主题设置
 * 
 * @author Jeff.Wang
 *
 * @date 2014年10月15日
 */
public class ThemeStyleSettingsFragment extends AGridFragment<String, String[]> implements OnItemClickListener {

	public static void launch(Activity from) {
		FragmentContainerActivity.launch(from, ThemeStyleSettingsFragment.class, null);
	}
	
	@Override
	protected int inflateContentView() {
		return R.layout.ui_wallpaper;
	}
	
	@Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		super.layoutInit(inflater, savedInstanceSate);
		
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		getActivity().getActionBar().setDisplayShowHomeEnabled(false);
		getActivity().getActionBar().setTitle(R.string.title_actionbar);
		
		getRefreshView().setOnItemClickListener(this);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		AppSettings.setThemeColor(getAdapterItems().get(position));

		BaseActivity activity = (BaseActivity) getActivity();
		WallpaperViewGroup wallpaper = (WallpaperViewGroup) activity.getRootView();
		wallpaper.setWallpaper();
	}
	
	@Override
	protected AbstractItemView<String> newItemView() {
		return new ActionbarSettingsItemView();
	}

	@Override
	protected void requestData(RefreshMode mode) {
		setItems(generateStyles());
	}
	
	private ArrayList<String> generateStyles() {
		ArrayList<String> list = new ArrayList<String>();

		String[] colors = getResources().getStringArray(R.array.actionbarBgColors);
		for (String color : colors)
			list.add(color);
		
		return list;
	}
	
	class ActionbarSettingsItemView extends AbstractItemView<String> {

		@ViewInject(id = R.id.viewColor)
		View viewColor;
		
		@Override
		public int inflateViewId() {
			return R.layout.item_theme;
		}

		@Override
		public void bindingData(View convertView, String data) {
			viewColor.setBackgroundColor(Color.parseColor(data));
		}
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		BaiduAnalyzeUtils.onPageStart("主题设置");
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		BaiduAnalyzeUtils.onPageEnd("主题设置");
	}

}
