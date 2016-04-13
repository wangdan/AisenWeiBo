package org.aisen.weibo.sina.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.service.publisher.InterfacePublisher;
import org.aisen.weibo.sina.service.publisher.PublishManager;
import org.aisen.weibo.sina.support.bean.PublishBean;
import org.aisen.weibo.sina.support.bean.PublishBean.PublishStatus;
import org.aisen.weibo.sina.support.sqlit.PublishDB;

/**
 * 任务发布服务，将所有发布任务保存在一个队列依次发布
 * 
 * @author wangdan
 * 
 */
public class PublishService extends Service implements InterfacePublisher {

	public static void publish(Context context, PublishBean bean) {
		Intent intent = new Intent(context, PublishService.class);
		intent.setAction("org.aisen.weibo.sina.PUBLISH");
		intent.putExtra("data", bean);
		context.startService(intent);
	}
	
	public static void stopPublish() {
		Intent intent = new Intent(GlobalContext.getInstance(), PublishService.class);
		GlobalContext.getInstance().stopService(intent);
	}
	
	private PublishManager publishManager;
	private PublishService publisher;
	private PublishBinder binder;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (publisher == null)
			publisher = this;
		if (binder == null)
			binder = new PublishBinder();
		if (publishManager == null)
			publishManager = new PublishManager(this, AppContext.getAccount());

		if (intent != null) {
			if ("org.aisen.weibo.sina.PUBLISH".equals(intent.getAction())) {
				PublishBean data = (PublishBean) intent.getSerializableExtra("data");
				
				if (data != null)
					publish(data);
			}
			else if ("org.aisen.weibo.sina.Cancel".equals(intent.getAction())) {
				publishManager.cancelPublish();
			}
		}
		
		
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void publish(PublishBean data) {
		if (data.getStatus() == PublishStatus.create) {
			publishManager.onPublish(data);
		} else {
			// 如果不是新建的，都当做草稿重新发布
			data.setStatus(PublishStatus.create);
			PublishDB.addPublish(data, AppContext.getAccount().getUser());
			
			publishManager.onPublish(data);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		publishManager.stop();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	public class PublishBinder extends Binder {

		public InterfacePublisher getPublisher() {
			return publisher;
		}

	}
	
}
