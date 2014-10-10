package org.aisen.weibo.sina.ui.fragment.mention;

import java.util.ArrayList;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.bean.TimelineGroupBean;
import org.aisen.weibo.sina.support.utils.AisenUtil;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.ui.fragment.profile.UserProfileFragment;
import org.sina.android.bean.WeiBoUser;

import android.os.Bundle;
import android.view.LayoutInflater;

import com.m.common.utils.ActivityHelper;
import com.m.ui.fragment.ABaseFragment;
import com.m.ui.fragment.ATabTitlePagerFragment;

/**
 * 提及的微博、评论Pager
 * 
 * @author wangdan
 *
 */
public class MentionPagerFragment extends ATabTitlePagerFragment<TimelineGroupBean> {

	public static ABaseFragment newInstance(int type) {
		MentionPagerFragment fragment = new MentionPagerFragment();
		
		Bundle args = new Bundle();
		args.putInt("type", type);
		fragment.setArguments(args);
		
		return fragment;
	}
	
	private int type;
	private WeiBoUser loggedIn;
	
	@Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		super.layoutInit(inflater, savedInstanceSate);
		
		loggedIn = AppContext.getUser();
		type = savedInstanceSate == null ? getArguments().getInt("type")
										 : savedInstanceSate.getInt("type");
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putInt("type", type);
	}
	
	@Override
	protected ArrayList<TimelineGroupBean> getPageTitleBeans() {
		ArrayList<TimelineGroupBean> beans = new ArrayList<TimelineGroupBean>();
		
		// 提及的微博
		if (type == 0) {
			beans.add(new TimelineGroupBean("100", getString(R.string.mention_all)));
			beans.add(new TimelineGroupBean("101", getString(R.string.mention_friends)));
			beans.add(new TimelineGroupBean("102", getString(R.string.mention_self_status)));
		}
		// 提及的评论
		else if (type == 1) {
			beans.add(new TimelineGroupBean("103", getString(R.string.mention_all)));
			beans.add(new TimelineGroupBean("104", getString(R.string.mention_friends)));
		}
		
		return beans;
	}
	
	@Override
	protected String setFragmentTitle() {
		return AisenUtil.getUserKey("提及" + type, loggedIn);
	}

	@Override
	protected ABaseFragment newFragment(TimelineGroupBean bean) {
		int type = Integer.parseInt(bean.getType());
		
		switch (type) {
		// 微博
		case 100:
		case 101:
		case 102:
			return MentionTimelineFragment.newInstance(bean);
		// 评论
		case 103:
		case 104:
			return MentionCommentsFragment.newInstance(bean);
		default:
			break;
		}
		
		return UserProfileFragment.newInstance();
	}

	@Override
	protected void replaceSelfInActivity() {
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		// 提及的微博
		if (type == 0) {
			ActivityHelper.getInstance().putShareData("PagerLastPosition" + setFragmentTitle(), "100");
		}
		// 提及的评论
		else if (type == 1) {
			ActivityHelper.getInstance().putShareData("PagerLastPosition" + setFragmentTitle(), "103");
		}
	}
	
}
