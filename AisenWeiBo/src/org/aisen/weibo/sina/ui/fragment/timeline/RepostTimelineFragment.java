package org.aisen.weibo.sina.ui.fragment.timeline;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.AppSettings;
import org.aisen.weibo.sina.ui.component.TimelineItemView;
import org.sina.android.SinaSDK;
import org.sina.android.bean.StatusContent;
import org.sina.android.bean.StatusContents;
import org.sina.android.bean.StatusRepost;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.m.common.params.Params;
import com.m.support.adapter.ABaseAdapter.AbstractItemView;
import com.m.support.task.TaskException;
import com.m.ui.fragment.ABaseFragment;

/**
 * 某条原创微博的转发微博
 * 
 * @author wangdan
 *
 */
public class RepostTimelineFragment extends ATimelineFragment {

	public static ABaseFragment newInstance(StatusContent status) {
		RepostTimelineFragment fragment = new RepostTimelineFragment();
		
		Bundle args = new Bundle();
		args.putSerializable("status", status);
		fragment.setArguments(args);
		
		return fragment;
	}
	
	private View headerView;
	
	private StatusContent mStatusContent;

	@Override
	protected int inflateContentView() {
		return R.layout.ui_repost_timeline;
	}
	
	public int refreshLayoutInfo() {
		return R.id.layContent;
	}
	
	@Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		super.layoutInit(inflater, savedInstanceSate);
		
		ListView listView = (ListView) getRefreshView();
		TimelineItemView timelineItem = new TimelineItemView(this, true);
		View view = View.inflate(getActivity(), timelineItem.inflateViewId(), null);
		timelineItem.bindingView(view);
		view.setTag(timelineItem);
		listView.addHeaderView(view);
		
		headerView = view;
		
		mStatusContent = savedInstanceSate == null ? (StatusContent) getArguments().getSerializable("status")
												   : (StatusContent) savedInstanceSate.getSerializable("status");
		
		if (savedInstanceSate == null) {
			listView.setSelectionFromTop(listView.getFooterViewsCount(), 0);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putSerializable("status", mStatusContent);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		TimelineItemView timelineItem = (TimelineItemView) headerView.getTag();
		if (timelineItem != null)
			timelineItem.bindingData(headerView, mStatusContent);
//			timelineItem.bindingData(headerView, mStatusContent.getRetweeted_status() != null ? 
//														mStatusContent.getRetweeted_status() :
//														mStatusContent);
	}
	
	@Override
	protected AbstractItemView<StatusContent> newItemView() {
		return new TimelineItemView(this, mStatusContent, false);
	}

	@Override
	protected void requestData(RefreshMode mode) {
		new RepostTimeTask(mode).execute();
	}
	
	class RepostTimeTask extends TimelineTask {

		public RepostTimeTask(RefreshMode mode) {
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

//			String statusId = mStatusContent.getRetweeted_status() == null ? mStatusContent.getId() : mStatusContent.getRetweeted_status().getId();
			String statusId = mStatusContent.getId();
			params.addParameter("id", statusId);
			
			params.addParameter("count", String.valueOf(AppSettings.getTimelineCount()));
			
			StatusRepost statusRepost = SinaSDK.getInstance(AppContext.getToken()).statusRepostTimeline(params);
			if (statusRepost != null) {
				for (StatusContent status : statusRepost.getReposts()) {
					status.setRetweeted_status(null);
				}
				
				return new StatusContents(statusRepost.getReposts());
			}

			return null;
		}
		
	}
	
}
