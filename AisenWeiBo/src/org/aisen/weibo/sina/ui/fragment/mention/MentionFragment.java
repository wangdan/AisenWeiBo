package org.aisen.weibo.sina.ui.fragment.mention;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.iclass.IAcNavigation;
import org.aisen.weibo.sina.support.utils.AisenUtil;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.sina.android.bean.WeiBoUser;

import android.os.Bundle;
import android.view.LayoutInflater;

import com.m.common.utils.ActivityHelper;
import com.m.common.utils.Logger;
import com.m.ui.fragment.ABaseFragment;

/**
 * ActionBar的ListNavigation导航<br/>
 * 切换提及的微博、评论<br/>
 * 
 * @author wangdan
 *
 */
public class MentionFragment extends ABaseFragment implements IAcNavigation {

	public static ABaseFragment newInstance() {
		return new MentionFragment();
	}
	
	static final String TAG = MentionFragment.class.getSimpleName();
	
	private WeiBoUser loggedIn;
	
	public MentionFragment() {
		loggedIn = AppContext.getUser();
	}
	
	@Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		super.layoutInit(inflater, savedInstanceSate);
		
		getActivity().setTitle(R.string.mention_title);
	}
	
	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		ActivityHelper.getInstance().putIntShareData(AisenUtil.getUserKey("MentionFragment_history", loggedIn), 
											itemPosition);
		
		Logger.v(TAG, "切换Navigation, position = " + itemPosition);
		
		desotry();
		getFragmentManager().beginTransaction()
			.add(R.id.content_frame, MentionPagerFragment.newInstance(itemPosition), "MentionPagerFragment").commit();
		
		return true;
	}

	@Override
	public int adapterResource() {
		return R.array.mention;
	}

	@Override
	public int current() {
		int current = ActivityHelper.getInstance().getIntShareData(AisenUtil.getUserKey("MentionFragment_history", loggedIn), 0);
		
		if ("showMentionStatus".equals(ActivityHelper.getInstance().getShareData("showMensitonType")))
			current = 0;
		else if ("showMentionCmt".equals(ActivityHelper.getInstance().getShareData("showMensitonType")))
			current = 1;
		else {
			if (AppContext.getUnreadCount() != null && AppContext.getUnreadCount().getMention_cmt() != 0)
				current = 1;
			else if (AppContext.getUnreadCount() != null && AppContext.getUnreadCount().getMention_status() != 0)
				current = 0;
		}
		
		ActivityHelper.getInstance().putShareData("showMensitonType", "");
		
		return current;
	}

	@Override
	protected int inflateContentView() {
		return 0;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		desotry();
	}
	
	private void desotry() {
		try {
			ABaseFragment fragment = (ABaseFragment) getActivity().getFragmentManager().findFragmentByTag("MentionPagerFragment");
			if (fragment != null)
				getFragmentManager().beginTransaction().remove(fragment).commit();
		} catch (Exception e) {
		}
	}

}
