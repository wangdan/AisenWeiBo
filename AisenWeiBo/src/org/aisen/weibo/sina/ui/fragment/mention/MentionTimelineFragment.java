package org.aisen.weibo.sina.ui.fragment.mention;

import org.aisen.weibo.sina.support.bean.TimelineGroupBean;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.AppSettings;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment.RemindType;
import org.aisen.weibo.sina.ui.fragment.timeline.ATimelineFragment;
import org.sina.android.SinaSDK;
import org.sina.android.bean.StatusContents;

import android.os.Bundle;
import android.text.TextUtils;

import com.m.common.params.Params;
import com.m.support.task.TaskException;
import com.m.ui.fragment.ABaseFragment;

/**
 * 提及的微博
 * 100:全部, 101:关注人的 102:原创
 * 
 * @author wangdan
 *
 */
public class MentionTimelineFragment extends ATimelineFragment {

	public static ABaseFragment newInstance(TimelineGroupBean bean) {
		ABaseFragment fragment = new MentionTimelineFragment();
		
		Bundle args = new Bundle();
		args.putSerializable("bean", bean);
		fragment.setArguments(args);
		
		return fragment;
	}
	
	@Override
	protected void requestData(RefreshMode mode) {
		new MentionTimelineTask(mode).execute();
	}
	
	class MentionTimelineTask extends TimelineTask {

		public MentionTimelineTask(RefreshMode mode) {
			super(mode);
		}

		@Override
		protected StatusContents workInBackground(RefreshMode mode, String previousPage, String nextPage,
				Void... p) throws TaskException {
			Params params = new Params();
			if (mode == RefreshMode.refresh && !TextUtils.isEmpty(previousPage))
				params.addParameter("since_id", previousPage);
			if (mode == RefreshMode.update && !TextUtils.isEmpty(nextPage))
				params.addParameter("max_id", nextPage);
			switch (Integer.parseInt(getGroup().getGroup())) {
			case 100:
				params.addParameter("filter_by_author", "0");
				break;
			case 101:
				params.addParameter("filter_by_author", "1");
				break;
			case 102:
				params.addParameter("filter_by_type", "0");
				break;
			}
			
			params.addParameter("count", String.valueOf(AppSettings.getTimelineCount()));
			
			return SinaSDK.getInstance(AppContext.getToken(), getTaskCacheMode(this)).statusesMentions(params);
		}
		
		@Override
		protected void onSuccess(StatusContents result) {
			super.onSuccess(result);
			
			try {
				if (AppContext.getUnreadCount() != null && AppContext.getUnreadCount().getMention_status() > 0) {
					requestDataDelay(1000);

					// fuck sina
					AppContext.getUnreadCount().setMention_status(0);
					
					BizFragment.getBizFragment(MentionTimelineFragment.this).remindSetCount(RemindType.mention_status);
				}
					
			} catch (Exception e) {
			}
		}
		
	}
	
	@Override
	public String getLastReadKey() {
		if (AppContext.isLogedin())
			return String.format("MentionTimeline-%s", AppContext.getUser().getIdstr());
		
		return super.getLastReadKey();
	}
	
}
