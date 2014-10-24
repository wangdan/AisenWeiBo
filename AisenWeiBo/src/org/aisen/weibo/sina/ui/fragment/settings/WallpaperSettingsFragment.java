package org.aisen.weibo.sina.ui.fragment.settings;

import java.util.ArrayList;
import java.util.List;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.bean.WallpaperBean;
import org.aisen.weibo.sina.support.db.SinaDB;
import org.aisen.weibo.sina.support.utils.AisenUtil;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.BaiduAnalyzeUtils;
import org.aisen.weibo.sina.ui.activity.common.FragmentContainerActivity;
import org.aisen.weibo.sina.ui.widget.WallpaperSettingsImageView;
import org.aisen.weibo.sina.ui.widget.WallpaperViewGroup;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.m.support.Inject.ViewInject;
import com.m.support.adapter.ABaseAdapter.AbstractItemView;
import com.m.support.task.TaskException;
import com.m.ui.activity.BaseActivity;
import com.m.ui.fragment.AGridFragment;

/**
 * 壁纸设置
 * 
 * @author Jeff.Wang
 *
 * @date 2014年10月19日
 */
public class WallpaperSettingsFragment extends AGridFragment<WallpaperBean, ArrayList<WallpaperBean>>
												implements OnItemClickListener {

	public static void launch(Activity from) {
		FragmentContainerActivity.launch(from, WallpaperSettingsFragment.class, null);
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
		getActivity().getActionBar().setTitle(R.string.title_wallpaper);
		
		getRefreshView().setOnItemClickListener(this);
		
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		AppContext.setWallpaper(getAdapter().getDatas().get(position));
		
		BaseActivity activity = (BaseActivity) getActivity();
		WallpaperViewGroup wallpaper = (WallpaperViewGroup) activity.getRootView();
		wallpaper.setWallpaper();
		
		getActivity().invalidateOptionsMenu();
	}

	@Override
	protected AbstractItemView<WallpaperBean> newItemView() {
		return new WallpaperItemView();
	}

	@Override
	protected void requestData(RefreshMode mode) {
		new WallpaperTask(RefreshMode.reset).execute();
	}
	
	class WallpaperTask extends PagingTask<Void, Void, ArrayList<WallpaperBean>> {

		public WallpaperTask(RefreshMode mode) {
			super("WallpaperTask", mode);
		}

		@Override
		protected List<WallpaperBean> parseResult(ArrayList<WallpaperBean> result) {
			return result;
		}

		@Override
		protected ArrayList<WallpaperBean> workInBackground(RefreshMode mode, String previousPage,
																String nextPage, Void... params) throws TaskException {
			ArrayList<WallpaperBean> wallpaperList = new ArrayList<WallpaperBean>();
			
			wallpaperList.add(AisenUtil.generaterDefaultWallpaper());
			
			List<WallpaperBean> dbWallpaperList = SinaDB.getSqlite().selectAll(WallpaperBean.class);
			wallpaperList.addAll(dbWallpaperList);
			
			return wallpaperList;
		}
		
	}
	
	class WallpaperItemView extends AbstractItemView<WallpaperBean> {

		@ViewInject(id = R.id.imgWallpaper)
		WallpaperSettingsImageView img;
		
		@Override
		public int inflateViewId() {
			return R.layout.item_wallpaper;
		}

		@Override
		public void bindingData(View convertView, WallpaperBean data) {
			img.setWallpaper(data);
		}
		
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		
		inflater.inflate(R.menu.wallpaper, menu);
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		menu.findItem(R.id.disable).setVisible(AppContext.getWallpaper() != null);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.disable) {
			AppContext.setWallpaper(null);
			
			BaseActivity activity = (BaseActivity) getActivity();
			WallpaperViewGroup wallpaper = (WallpaperViewGroup) activity.getRootView();
			wallpaper.setWallpaper();
			
			getActivity().invalidateOptionsMenu();
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		BaiduAnalyzeUtils.onPageStart("自定义壁纸设置");
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		BaiduAnalyzeUtils.onPageEnd("自定义壁纸设置");
	}
	
}
