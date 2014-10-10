package org.aisen.weibo.sina.ui.fragment.mention;

import java.util.ArrayList;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.bean.TimelineGroupBean;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.BaiduAnalyzeUtils;

import android.os.Bundle;

import com.m.ui.fragment.ABaseFragment;

/**
 * 2014-08-28<br/>
 * 修改为只有提及的微博、评论两页的Pager
 * 
 * @author wangdan
 *
 */
public class Mention_v2Fragment extends MentionPagerFragment {

	public static ABaseFragment newInstance() {
		Mention_v2Fragment fragment = new Mention_v2Fragment();
		
		Bundle args = new Bundle();
		args.putInt("type", 2);
		if (AppContext.getUnreadCount() != null && AppContext.getUnreadCount().getMention_cmt() > 0)
			args.putInt("index", 1);
		fragment.setArguments(args);
		
		return fragment;
	}
	
	@Override
	protected ArrayList<TimelineGroupBean> getPageTitleBeans() {
		ArrayList<TimelineGroupBean> beans = new ArrayList<TimelineGroupBean>();
		
		// 提及的微博
		beans.add(new TimelineGroupBean("100", getString(R.string.mention_status)));
		// 提及的评论
		beans.add(new TimelineGroupBean("103", getString(R.string.mention_cmt)));
		
		return beans;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		BaiduAnalyzeUtils.onPageStart("@我的");
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		BaiduAnalyzeUtils.onPageEnd("@我的");
	}

}
