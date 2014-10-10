package org.aisen.weibo.sina.ui.activity.search;

import java.util.List;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.utils.AppSettings;
import org.aisen.weibo.sina.ui.fragment.search.SearchTopicsFragment;
import org.aisen.weibo.sina.ui.fragment.search.SearchUsers_v2Fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.m.ui.activity.AViewpagerActivity;
import com.m.ui.fragment.ABaseFragment;

/**
 * 搜索话题下的微博或者用户
 * 
 * @author wangdan
 *
 */
public class SearchActivity extends AViewpagerActivity implements OnClickListener {

	public static void launch(Activity from) {
		Intent intent = new Intent(from, SearchActivity.class);
		from.startActivity(intent);
	}
	
	private View menuActionView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setTitle(R.string.title_search);
	}
	
	@Override
	protected boolean showIndicator() {
		return false;
	}

	@Override
	protected int setViewPagerTitles() {
		return R.array.search_title;
	}

	@Override
	protected void setViewPagerFragments(List<ABaseFragment> fragmentList) {
		fragmentList.add(SearchUsers_v2Fragment.newInstance());
		fragmentList.add(SearchTopicsFragment.newInstance());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		getMenuInflater().inflate(R.menu.timeline_comments, menu);
		
		MenuItem switchItem = menu.findItem(R.id.menuSwitch);
		View viewTitle = switchItem.getActionView();
		viewTitle.setOnClickListener(this);
		menuActionView = viewTitle;
		
		TextView txtComment = (TextView) viewTitle.findViewById(R.id.txtComment);
		txtComment.setText(R.string.search_search_user);
		txtComment.setSelected(getViewPager().getCurrentItem() == 0);
		TextView txtRepost = (TextView) viewTitle.findViewById(R.id.txtRepost);
		txtRepost.setText(R.string.search_search_status);
		txtRepost.setSelected(getViewPager().getCurrentItem() == 1);
		
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public void onPageSelected(int position) {
		super.onPageSelected(position);
		
		if (menuActionView != null) {
			View txtComment = menuActionView.findViewById(R.id.txtComment);
			txtComment.setSelected(getViewPager().getCurrentItem() == 0);
			View txtRepost = menuActionView.findViewById(R.id.txtRepost);
			txtRepost.setSelected(getViewPager().getCurrentItem() == 1);
		}
	}

	@Override
	public void onClick(View v) {
		getViewPager().setCurrentItem(getViewPager().getCurrentItem() == 0 ? 1 : 0);
		
		View txtComment = v.findViewById(R.id.txtComment);
		txtComment.setSelected(getViewPager().getCurrentItem() == 0);
		View txtRepost = v.findViewById(R.id.txtRepost);
		txtRepost.setSelected(getViewPager().getCurrentItem() == 1);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// 开启屏幕旋转
		if (AppSettings.isScreenRotate()) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		}
		else {
			if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_SENSOR)
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
	}

}
