package org.aisen.weibo.sina.ui.fragment.timeline;

import org.aisen.weibo.sina.support.bean.TimelineGroupBean;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.AppSettings;
import org.sina.android.SinaSDK;
import org.sina.android.bean.StatusContents;

import android.os.Bundle;
import android.text.TextUtils;

import com.m.common.params.Params;
import com.m.support.task.TaskException;
import com.m.ui.fragment.ABaseFragment;

/**
 * 好友分组微博
 * 
 * @author wangdan
 *
 */
public class FriendGroupTimelineFragment extends ATimelineFragment {

	public static ABaseFragment newInstance(TimelineGroupBean groupBean) {
		FriendGroupTimelineFragment fragment = new FriendGroupTimelineFragment();
		
		Bundle args = new Bundle();
		args.putSerializable("bean", groupBean);
		fragment.setArguments(args);
		
		return fragment;
	}
	
	// 2014-8-30 解决因为状态保存而导致的耗时阻塞
	@Override
	protected void saveRefreshListState(Bundle outState) {
//		super.saveRefreshListState(outState);
		cleatTaskCount("TimelineTask");
	}
	
	@Override
	protected void requestData(RefreshMode mode) {
		new FriendsGroupTimelineTask(mode).execute();
	}
	
	// 加载分组好友的task
	class FriendsGroupTimelineTask extends TimelineTask {

		public FriendsGroupTimelineTask(RefreshMode mode) {
			super(mode);
		}

		@Override
		protected StatusContents workInBackground(RefreshMode mode, String previousPage, String nextPage, Void... p) throws TaskException {
			Params params = new Params();

			if (mode == RefreshMode.refresh && !TextUtils.isEmpty(previousPage))
				params.addParameter("since_id", previousPage);

			if (mode == RefreshMode.update && !TextUtils.isEmpty(nextPage))
				params.addParameter("max_id", nextPage);

			params.addParameter("list_id", getGroup().getType());
			
			params.addParameter("count", String.valueOf(AppSettings.getTimelineCount()));

			return SinaSDK.getInstance(AppContext.getToken(), getTaskCacheMode(this)).friendshipGroupsTimeline(params);
		}

	}
	
}
