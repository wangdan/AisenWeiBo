package org.aisen.weibo.sina.ui.fragment.timeline;

import android.os.Bundle;
import android.text.TextUtils;

import com.m.network.http.Params;
import com.m.network.task.TaskException;
import com.m.ui.fragment.ABaseFragment;
import com.m.ui.fragment.ARefreshFragment;
import com.m.ui.fragment.AStripTabsFragment;

import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.sina.android.SinaSDK;
import org.sina.android.bean.StatusContents;

import java.lang.reflect.Method;

/**
 * 默认分组的微博<br/>
 * 使用反射调用接口方法
 * 
 * @author wangdan
 *
 */
public class TimelineDefaultFragment extends ATimelineFragment implements AStripTabsFragment.IStripTabInitData {

	public static ABaseFragment newInstance(AStripTabsFragment.StripTabItem groupBean) {
		TimelineDefaultFragment fragment = new TimelineDefaultFragment();
		
		Bundle args = new Bundle();
		args.putSerializable("bean", groupBean);
		fragment.setArguments(args);
		
		return fragment;
	}

    @Override
    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStripTabRequestData() {
        // 如果还没有加载过数据，就开始加载
        if (getTaskCount("TimelineTask") == 0) {
            requestData(ARefreshFragment.RefreshMode.reset);
        }
    }

    @Override
	protected void requestData(ARefreshFragment.RefreshMode mode) {
        // 如果还没有加载过数据，切且显示的是当前的页面
        if (getTaskCount("TimelineTask") == 0) {
            if (getPagerCurrentFragment() == this)
                new DefGroupTimelineTask(mode).execute();
        }
        else {
            new DefGroupTimelineTask(mode).execute();
        }
	}
	
	class DefGroupTimelineTask extends TimelineTask {

		public DefGroupTimelineTask(ARefreshFragment.RefreshMode mode) {
			super(mode);
		}

		@Override
		protected StatusContents workInBackground(ARefreshFragment.RefreshMode mode, String previousPage, String nextPage, Void... p) throws TaskException {
			try {
				String uid = AppContext.getUser().getIdstr();
				
				Params params = new Params();

				if (mode == ARefreshFragment.RefreshMode.refresh && !TextUtils.isEmpty(previousPage))
					params.addParameter("since_id", previousPage);

				if (mode == ARefreshFragment.RefreshMode.update && !TextUtils.isEmpty(nextPage))
					params.addParameter("max_id", nextPage);
				
				params.addParameter("count", String.valueOf(AppSettings.getTimelineCount()));

				Method method = SinaSDK.class.getMethod(getGroup().getType(), new Class[] { Params.class });
				StatusContents beans = (StatusContents) method.invoke(SinaSDK.getInstance(AppContext.getToken(), getTaskCacheMode(this)), params);

				return beans;
			} catch (Exception e) {
				e.printStackTrace();

				if (e.getCause() instanceof TaskException)
					throw ((TaskException) e.getCause());
				if (e instanceof TaskException)
					throw (TaskException) e;
				
				throw new TaskException(TextUtils.isEmpty(e.getMessage()) ? "服务器错误" : e.getMessage());
			}
		}
		
	}
	
}
