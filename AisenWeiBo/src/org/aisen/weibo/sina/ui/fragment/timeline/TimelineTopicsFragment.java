package org.aisen.weibo.sina.ui.fragment.timeline;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;

import com.m.network.task.TaskException;
import com.m.support.paging.IPaging;
import com.m.support.paging.PageIndexPaging;
import com.m.ui.activity.basic.BaseActivity;
import com.m.ui.fragment.ABaseFragment;

import org.aisen.weibo.sina.base.AppContext;
import org.sina.android.SinaSDK;
import org.sina.android.bean.PicUrls;
import org.sina.android.bean.StatusContent;
import org.sina.android.bean.StatusContents;

/**
 * 搜索某一话题下的微博
 * 
 * @author wangdan
 *
 */
public class TimelineTopicsFragment extends ATimelineFragment {

	public static ABaseFragment newInstance(String q) {
		ABaseFragment fragment = new TimelineTopicsFragment();
		
		Bundle args = new Bundle();
		args.putString("q", q);
		fragment.setArguments(args);
		
		return fragment;
	}
	
	private String query;
	
	@Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		super.layoutInit(inflater, savedInstanceSate);
		
		query = savedInstanceSate != null ? savedInstanceSate.getString("q") : getArguments().getString("q");
		
		BaseActivity activity = (BaseActivity) getActivity();
		activity.getSupportActionBar().setTitle(query);
		activity.getSupportActionBar().setDisplayShowHomeEnabled(false);
		activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putString("q", query);
	}

	@Override
	protected void requestData(RefreshMode mode) {
		new TopicsTimelineTask(mode == RefreshMode.refresh ? RefreshMode.reset : mode).execute();
	}
	
	@Override
	protected IPaging<StatusContent, StatusContents> configPaging() {
		return new PageIndexPaging<StatusContent, StatusContents>("total_number");
	}
	
	class TopicsTimelineTask extends TimelineTask {

		public TopicsTimelineTask(RefreshMode mode) {
			super(mode);
		}

		@Override
		protected StatusContents workInBackground(RefreshMode mode, String previousPage, String nextPage,
				Void... params) throws TaskException {
			
			// 搜索话题没有多图
			StatusContents statuses = SinaSDK.getInstance(AppContext.getToken()).searchTopics(nextPage, query, "30");
			if (statuses != null && statuses.getStatuses().size() > 0) {
				for (StatusContent status : statuses.getStatuses()) {
                    if (status.getRetweeted_status() != null)
                        status = status.getRetweeted_status();

					if (!TextUtils.isEmpty(status.getThumbnail_pic())) {
						status.setPic_urls(new PicUrls[1]);
						PicUrls picUrls = new PicUrls();
						picUrls.setThumbnail_pic(status.getThumbnail_pic());
						status.getPic_urls()[0] = picUrls;
					}
				}
			}
			
			return statuses;
	
		}
		
	}

}
