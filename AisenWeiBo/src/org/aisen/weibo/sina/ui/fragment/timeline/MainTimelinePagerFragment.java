package org.aisen.weibo.sina.ui.fragment.timeline;

import java.util.ArrayList;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.bean.TimelineGroupBean;
import org.aisen.weibo.sina.support.utils.AisenUtil;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.AppSettings;
import org.aisen.weibo.sina.support.utils.BaiduAnalyzeUtils;
import org.aisen.weibo.sina.ui.activity.main.MainActivity;
import org.aisen.weibo.sina.ui.fragment.group.GroupSortFragment;
import org.sina.android.bean.Group;
import org.sina.android.bean.Groups;
import org.sina.android.bean.WeiBoUser;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.m.common.context.GlobalContext;
import com.m.ui.fragment.ABaseFragment;
import com.m.ui.fragment.ATabTitlePagerFragment;

/**
 * 首页微博界面
 * 
 * @author wangdan
 *
 */
public class MainTimelinePagerFragment extends ATabTitlePagerFragment<TimelineGroupBean> {

	public static ABaseFragment newInstance() {
		return new MainTimelinePagerFragment();
	}
	
	private WeiBoUser loggedIn;
	
	@Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		loggedIn = AppContext.getUser();

		// 2014-8-30 解决因为状态保存而导致的耗时阻塞
		if (savedInstanceSate != null) {
			ArrayList<TimelineGroupBean> mChanneList = getPageTitleBeans();
			for (int i = 0; i < mChanneList.size(); i++) {
				ABaseFragment fragment = (ABaseFragment) getActivity().getFragmentManager()
											.findFragmentByTag(mChanneList.get(i).getTitle() + setFragmentTitle());
				if (fragment != null)
					getActivity().getFragmentManager().beginTransaction().remove(fragment).commit();
			}
		}
		
		super.layoutInit(inflater, null);
		
		
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
//		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected ArrayList<TimelineGroupBean> getPageTitleBeans() {
		ArrayList<TimelineGroupBean> groupList = new ArrayList<TimelineGroupBean>();
		
		// 是否显示默认分组(好友分组为空时，忽略设置)
		if (AppSettings.isShowDefGroup() || AppContext.getGroups() == null ||
				AppContext.getGroups().getLists().size() == 0) {
			// 全部好友
			groupList.add(newGroup("0", getString(R.string.timeline_all), "statusesFriendsTimeLine"));
			// 相互关注
			groupList.add(newGroup("0", getString(R.string.timeline_bilateral), "statusesBilateralTimeLine"));
			// 发给我的
			groupList.add(newGroup("0", getString(R.string.timeline_tome), "statusesToMe"));
		}
		// 好友分组
		Groups groups = AppContext.getGroups();
		if (groups != null) {
			for (Group group : groups.getLists()) {
				groupList.add(newGroup("1", group.getName(), group.getId()));
			}
		}
		
		return groupList;
	}
	
	private TimelineGroupBean newGroup(String group, String title, String type) {
		TimelineGroupBean bean = new TimelineGroupBean();
		
		bean.setGroup(group);
		bean.setTitle(title);
		bean.setType(type);
		
		return bean;
	}

	@Override
	protected String setFragmentTitle() {
		return AisenUtil.getUserKey(GlobalContext.getInstance().getResources().getString(R.string.draw_timeline), loggedIn);
	}

	@Override
	protected ABaseFragment newFragment(TimelineGroupBean bean) {
		// 默认分组
		if ("0".equals(bean.getGroup()))
			return DefGroupTimelineFragment.newInstance(bean);
		// 好友分组
		return FriendGroupTimelineFragment.newInstance(bean);
	}

	@Override
	protected void replaceSelfInActivity() {
		getActivity().getFragmentManager().beginTransaction()
							.replace(R.id.content_frame, newInstance(), "MainFragment")
							.commit();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		
		if (AppContext.getGroups() != null && AppContext.getGroups().getLists().size() > 0)
			inflater.inflate(R.menu.timeline, menu);
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		if (getActivity() != null && AppContext.getGroups() != null && AppContext.getGroups().getLists().size() > 0) {
			menu.setGroupVisible(R.id.timelineMenus, !((MainActivity) getActivity()).isDrawerOpened());
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.friendGroups)
			GroupSortFragment.lanuch(getActivity());

		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		BaiduAnalyzeUtils.onPageStart("微博首页");
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		BaiduAnalyzeUtils.onPageEnd("微博首页");
	}
	
}
