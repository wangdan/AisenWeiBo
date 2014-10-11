package org.aisen.weibo.sina.ui.fragment.friendship;

import java.util.ArrayList;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.bean.TimelineGroupBean;
import org.aisen.weibo.sina.support.utils.AisenUtil;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.BaiduAnalyzeUtils;
import org.aisen.weibo.sina.ui.activity.common.FragmentContainerActivity;
import org.sina.android.bean.WeiBoUser;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;

import com.m.ui.fragment.ABaseFragment;
import com.m.ui.fragment.ATabTitlePagerFragment;
import com.m.ui.utils.FragmentArgs;

/**
 * 首页朋友关系Pager
 * 
 * @author wangdan
 *
 */
public class FriendshipPagerFragment extends ATabTitlePagerFragment<TimelineGroupBean> {

	/**
	 * 用户关系 
	 * 
	 * @param from
	 * @param user
	 * @param type 1:粉丝 0:关注 2:互粉
	 */
	public static void launch(Activity from, WeiBoUser user, int type) {
		FragmentArgs args = new FragmentArgs();
		args.add("user", user);
		args.add("index", type);
		
		FragmentContainerActivity.launch(from, FriendshipPagerFragment.class, args);
	}
	
	public static ABaseFragment newInstance() {
		FriendshipPagerFragment fragment = new FriendshipPagerFragment();
		
		Bundle args = new Bundle();
		args.putSerializable("user", AppContext.getUser());
		// 如果有新粉丝，默认选择粉丝页面
		args.putInt("index", AppContext.getUnreadCount().getFollower() > 0 ? 1 : 0);;
		fragment.setArguments(args);
		
		return fragment;
	}
	
	private WeiBoUser mUser;
	private int type;
	
	@Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		super.layoutInit(inflater, savedInstanceSate);
		
		mUser = savedInstanceSate == null ? (WeiBoUser) getArguments().getSerializable("user")
										  : (WeiBoUser) savedInstanceSate.getSerializable("user");
		type = savedInstanceSate == null ? getArguments().getInt("index", type)
										 : savedInstanceSate.getInt("index");
		
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		getActivity().getActionBar().setTitle(AisenUtil.getUserScreenName(mUser));
		
		setSubtitle(0);
		
		getViewPager().setCurrentItem(type);
	}
	
	@Override
	public void onPageSelected(int position) {
		super.onPageSelected(position);
		
		setSubtitle(position);
	}
	
	public void setSubtitle() {
		setSubtitle(getViewPager().getCurrentItem());
	}
	
	public void setSubtitle(int position) {
		String[] titleArr = new String[]{ getString(R.string.friends), getString(R.string.followers) };
		if (mUser.getIdstr().equals(AppContext.getUser().getIdstr()))
			titleArr = new String[]{ getString(R.string.friends), getString(R.string.followers), getString(R.string.bilateral) };
		if (getFragment(titleArr[position] + setFragmentTitle()) != null) {
			int loaded = ((AFriendshipFragment) getFragment(titleArr[position] + setFragmentTitle())).getSize();
			switch (position) {
			case 0:
				getActivity().getActionBar().setSubtitle(String.format("%d/%d", loaded, mUser.getFriends_count()));
				break;
			case 1:
				getActivity().getActionBar().setSubtitle(String.format("%d/%d", loaded, mUser.getFollowers_count()));
				break;
			case 2:
				getActivity().getActionBar().setSubtitle(String.format("%d/%d", loaded, mUser.getBi_followers_count()));
				break;
			default:
				break;
			}
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putSerializable("user", mUser);
		outState.putInt("index", type);
	}
	
	@Override
	protected ArrayList<TimelineGroupBean> getPageTitleBeans() {
		ArrayList<TimelineGroupBean> beans = new ArrayList<TimelineGroupBean>();
		
		beans.add(new TimelineGroupBean("300", getString(R.string.friends)));
		beans.add(new TimelineGroupBean("301", getString(R.string.followers)));
		// 是当前授权用户时，显示互粉
		if (mUser.getIdstr().equals(AppContext.getUser().getIdstr()))
			beans.add(new TimelineGroupBean("302", getString(R.string.bilateral)));
		
		return beans;
	}

	@Override
	protected String setFragmentTitle() {
		return "FriendshipPagerFragment";
	}

	@Override
	protected ABaseFragment newFragment(TimelineGroupBean bean) {
		switch (Integer.parseInt(bean.getType())) {
		case 300:
			return FriendsFragment.newInstance(mUser);
		case 301:
			return FollowersFragment.newInstance(mUser);
		case 302:
			return BilateralFragment.newInstance(mUser);
		}
		
		return null;
	}

	@Override
	protected void replaceSelfInActivity() {
		getActivity().getFragmentManager().beginTransaction()
							.replace(R.id.content_frame, newInstance(), "MainFragment")
							.commit();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		BaiduAnalyzeUtils.onPageStart("朋友圈");
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		BaiduAnalyzeUtils.onPageEnd("朋友圈");
	}

}
