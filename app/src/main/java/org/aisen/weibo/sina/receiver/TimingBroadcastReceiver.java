package org.aisen.weibo.sina.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.service.PublishService;
import org.aisen.weibo.sina.support.bean.PublishBean;
import org.aisen.weibo.sina.support.bean.PublishBean.PublishStatus;
import org.aisen.weibo.sina.support.sqlit.PublishDB;

import java.util.List;

public class TimingBroadcastReceiver extends BroadcastReceiver {

	public static final String TAG = "TimingReceiver";
	
	public static final String ACTION_TIMING_PUBLISH = "org.aisen.weibo.sina.ACTION_TIMING_PUBLISH";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Logger.d(TAG, "触发一个定时发布任务，开始检查发布任务");
		if (intent != null && ACTION_TIMING_PUBLISH.equals(intent.getAction())) {
			final long timing = intent.getLongExtra("timing", 0);
			
			if (timing > 0) {
				new WorkTask<Void, Void, Void>() {

					@Override
					public Void workInBackground(Void... params) throws TaskException {
						List<PublishBean> beans = PublishDB.getPublishList(AppContext.getAccount().getUser());

						Logger.w(TAG, String.format("任务总数%d个", beans.size()));
						
						for (PublishBean bean : beans) {
							Logger.d(TAG, String.format("bean's timing = %s, timing = %s", String.valueOf(bean.getTiming()), String.valueOf(timing)));
							if (bean.getTiming() == timing) {
								Logger.d(TAG, bean.getText() + "-检测到一个同一时间发布的任务，开始发布");
								
								bean.setTiming(0);
								
								bean.setStatus(PublishStatus.waiting);
								PublishDB.updatePublish(bean, AppContext.getAccount().getUser());
								
								PublishService.publish(GlobalContext.getInstance(), bean);
							}
						}
						
						return null;
					}
					
				}.executeOnSerialExecutor();
			}
		}
	}

}
