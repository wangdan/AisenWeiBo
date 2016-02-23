package org.aisen.weibo.sina.ui.fragment.timeline;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;

import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.paging.IPaging;
import org.aisen.android.support.paging.PageIndexPaging;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.PicUrls;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;

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
	public void requestData(RefreshMode mode) {
		new TopicsTimelineTask(mode == RefreshMode.refresh ? RefreshMode.reset : mode).execute();
	}

	@Override
	protected IPaging<StatusContent, StatusContents> newPaging() {
		return new PageIndexPaging<StatusContent, StatusContents>("total_number");
	}

	class TopicsTimelineTask extends ATimelineTask {

		public TopicsTimelineTask(RefreshMode mode) {
			super(mode);
		}

		@Override
		public StatusContents getStatusContents(Params params) throws TaskException {
			// 搜索话题没有多图
			String nextPage = "1";
			if (!TextUtils.isEmpty(params.getParameter("max_id"))) {
				nextPage = params.getParameter("max_id");
				params.remove("max_id");
			}

			StatusContents statuses = SinaSDK.getInstance(AppContext.getAccount().getAdvancedToken()).searchTopics(nextPage, query, "30");
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
