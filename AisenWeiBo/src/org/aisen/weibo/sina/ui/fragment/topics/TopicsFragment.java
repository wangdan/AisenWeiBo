package org.aisen.weibo.sina.ui.fragment.topics;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.ui.fragment.timeline.ATimelineFragment;
import org.sina.android.SinaSDK;
import org.sina.android.bean.StatusContent;
import org.sina.android.bean.StatusContents;

import android.os.Bundle;
import android.view.LayoutInflater;

import com.m.support.paging.IPaging;
import com.m.support.paging.PageIndexPaging;
import com.m.support.task.TaskException;
import com.m.ui.fragment.ABaseFragment;

/**
 * 搜索某一话题下的微博
 * 
 * @author wangdan
 *
 */
public class TopicsFragment extends ATimelineFragment {

	public static ABaseFragment newInstance(String q) {
		ABaseFragment fragment = new TopicsFragment();
		
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
		
		getActivity().getActionBar().setTitle(R.string.title_topics);
		getActivity().getActionBar().setSubtitle(query);
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
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
			
			return SinaSDK.getInstance(AppContext.getToken()).searchTopics(nextPage, query, "30");
	
		}
		
	}

}
