package org.aisen.weibo.sina.ui.fragment.comment;

import java.util.ArrayList;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.bean.TimelineGroupBean;
import org.aisen.weibo.sina.support.utils.AisenUtil;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.BaiduAnalyzeUtils;
import org.sina.android.bean.WeiBoUser;

import android.os.Bundle;

import com.m.common.utils.ActivityHelper;
import com.m.ui.fragment.ABaseFragment;
import com.m.ui.fragment.ATabTitlePagerFragment;

/**
 * 我收到、发出的评论Pager
 * 
 * @author wangdan
 *
 */
public class CommentPagerFragment extends ATabTitlePagerFragment<TimelineGroupBean> {

	public static ABaseFragment newInstance() {
		return new CommentPagerFragment();
	}
	
	private WeiBoUser loggedIn;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		loggedIn = AppContext.getUser();
	}
	
	@Override
	protected ArrayList<TimelineGroupBean> getPageTitleBeans() {
		ArrayList<TimelineGroupBean> beans = new ArrayList<TimelineGroupBean>();
		
		beans.add(new TimelineGroupBean("200", getString(R.string.title_to_me)));
		beans.add(new TimelineGroupBean("201", getString(R.string.title_by_me)));
		
		return beans;
	}

	@Override
	protected String setFragmentTitle() {
		return AisenUtil.getUserKey("CommentFragment", loggedIn);
	}

	@Override
	protected ABaseFragment newFragment(TimelineGroupBean bean) {
		return CommentsFragment.newInstance(bean);
	}

	@Override
	protected void replaceSelfInActivity() {
		getActivity().getFragmentManager().beginTransaction()
							.replace(R.id.content_frame, newInstance(), "MainFragment")
							.commit();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		ActivityHelper.getInstance().putShareData("PagerLastPosition" + setFragmentTitle(), "200");
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		BaiduAnalyzeUtils.onPageStart("评论");
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		BaiduAnalyzeUtils.onPageEnd("评论");
	}

}
