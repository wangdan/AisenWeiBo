package org.aisen.weibo.sina.ui.fragment.timeline;

import java.lang.reflect.Method;

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
 * 默认分组的微博<br/>
 * 使用反射调用接口方法
 * 
 * @author wangdan
 *
 */
public class DefGroupTimelineFragment extends ATimelineFragment {

	public static ABaseFragment newInstance(TimelineGroupBean groupBean) {
		DefGroupTimelineFragment fragment = new DefGroupTimelineFragment();
		
		Bundle args = new Bundle();
		args.putSerializable("bean", groupBean);
		fragment.setArguments(args);
		
		return fragment;
	}
	
	// 2014-8-30 解决因为状态保存而导致的耗时阻塞
	@Override
	public void saveRefreshListState(Bundle outState) {
//		super.saveRefreshListState(outState);
		cleatTaskCount("TimelineTask");
	}
	
	@Override
	protected void requestData(RefreshMode mode) {
		new DefGroupTimelineTask(mode).execute();
	}
	
	class DefGroupTimelineTask extends TimelineTask {

		public DefGroupTimelineTask(RefreshMode mode) {
			super(mode);
		}

		@Override
		protected StatusContents workInBackground(RefreshMode mode, String previousPage, String nextPage, Void... p) throws TaskException {
			try {
				Params params = new Params();

				if (mode == RefreshMode.refresh && !TextUtils.isEmpty(previousPage))
					params.addParameter("since_id", previousPage);

				if (mode == RefreshMode.update && !TextUtils.isEmpty(nextPage))
					params.addParameter("max_id", nextPage);
				
				params.addParameter("count", String.valueOf(AppSettings.getTimelineCount()));

				Method method = SinaSDK.class.getMethod(getGroup().getType(), new Class[] { Params.class });
				return (StatusContents) method.invoke(SinaSDK.getInstance(AppContext.getToken(), getTaskCacheMode(this)), params);
			} catch (Exception e) {
				e.printStackTrace();

				if (e.getCause() instanceof TaskException)
					throw new TaskException(e.getCause().getMessage());
				if (e instanceof TaskException)
					throw (TaskException) e;
				
				throw new TaskException(TextUtils.isEmpty(e.getMessage()) ? "服务器错误" : e.getMessage());
			}
		}
		
	}
	
}
