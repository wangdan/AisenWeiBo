package org.aisen.weibo.sina.service.publisher;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.FileUtils;
import org.aisen.android.common.utils.KeyGenerator;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.component.orm.extra.Extra;
import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.MyApplication;
import org.aisen.weibo.sina.receiver.TimingBroadcastReceiver;
import org.aisen.weibo.sina.service.PublishService;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.UploadPictureBean;
import org.aisen.weibo.sina.sinasdk.bean.UploadPictureResultBean;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;
import org.aisen.weibo.sina.sinasdk.core.SinaErrorMsgUtil;
import org.aisen.weibo.sina.support.bean.AccountBean;
import org.aisen.weibo.sina.support.bean.PublishBean;
import org.aisen.weibo.sina.support.bean.PublishBean.PublishStatus;
import org.aisen.weibo.sina.support.bean.PublishType;
import org.aisen.weibo.sina.support.sqlit.PublishDB;
import org.aisen.weibo.sina.support.sqlit.SinaDB;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.ui.fragment.account.AccountFragment;
import org.aisen.weibo.sina.ui.fragment.comment.TimelineDetailPagerFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PublishManager extends Handler implements PublishQueue.PublishQueueCallback {

	public static final int publishDelay = 1;

	public static final String ACTION_PUBLISH_CHANNGED = "org.aisen.weibo.ACTION_PUBLISH_SUCCESSED";

	private Context context;

	private PublishQueue publishQueue;
	private PublishNotifier publishNotifier;

	private PublishTask publishTask;

	private AccountBean mAccount;
	private WeiBoUser loggedIn;

	public PublishManager(Context context, AccountBean accountBean) {
		this.context = context.getApplicationContext();
		this.mAccount = accountBean;
		this.loggedIn = accountBean.getUser();
		publishQueue = new PublishQueue(this);
		publishNotifier = new PublishNotifier(context);

		publishInit();
	}

	public void stop() {
		if (publishTask != null && publishTask.getStatus() != WorkTask.Status.FINISHED)
			publishTask.cancel(true);

		removeMessages(publishDelay);
		if (!AppContext.isLoggedIn() || !AppContext.getAccount().getUser().getIdstr().equals(loggedIn.getIdstr()))
			PublishNotifier.cancelAll();
		
		if (publishQueue.size() > 0) {
			new WorkTask<WeiBoUser, Void, Void>() {
				
				@Override
				public Void workInBackground(WeiBoUser... params) throws TaskException {
					PublishBean bean = null;
					Logger.d(AccountFragment.TAG, String.format("共有%d个发布任务未完成", publishQueue.size()));
					while((bean = publishQueue.poll()) != null) {
						Logger.d(AccountFragment.TAG, "停止发布一个任务，添加到草稿");
						
						bean.setStatus(PublishStatus.draft);
						
						PublishDB.addPublish(bean, params[0]);
					}
					return null;
				}
				
			}.execute(loggedIn);
		}
	}
	
	public void cancelPublish() {
		removeMessages(publishDelay);
		
		PublishBean bean = publishQueue.poll();
		if (bean != null) {
			bean.setStatus(PublishStatus.draft);
			PublishDB.addPublish(bean, loggedIn);
			
			publishNotifier.notifyPublishCancelled(bean);
			
			refreshDraftbox();
			
			onPublish(publishQueue.peek());
		}
	}

	public void onPublish(PublishBean bean) {
		if (bean == null)
			return;

		// 如果队列为空，放入首位等待发送，否则，添加到队列等待发布
		if (publishQueue.isEmpty()) {
			publishQueue.add(bean);
		}

		PublishBean firstBean = publishQueue.peek();
		if (firstBean != null && firstBean.getId().equals(bean.getId())) {
			// 定时发布
			if (bean.getTiming() > 0) {
				Logger.d(TimingBroadcastReceiver.TAG, bean.getText() + "-定时发布");
				
				// 从队列移除
				publishQueue.poll();
				// 更改状态为草稿
				bean.setStatus(PublishStatus.draft);
				// 添加到DB
				PublishDB.addPublish(bean, loggedIn);
				// 刷新草稿
				refreshDraftbox();
				// 发送广播
				publishNotifier.notifyTimingPublish(bean);
				// 发布下一个
				onPublish(publishQueue.peek());
				// 刷新定时
				MyApplication.refreshPublishAlarm();
			}
			// 立即发布
			else if (bean.getDelay() <= 0) {
				Logger.d(TimingBroadcastReceiver.TAG, bean.getText() + "-立即发布");
				
				publishTask = new PublishTask(bean);
				publishTask.executeOnSerialExecutor();
			}
			// 延迟发布
			else {
				Logger.d(TimingBroadcastReceiver.TAG, bean.getText() + "-延迟发布");
				
				publishNotifier.notifyPrePublish(bean);
				Message msg = obtainMessage(publishDelay);
				msg.obj = bean;
				bean.setDelay(bean.getDelay() - 1000);
				sendMessageDelayed(msg, 1000);
				
				if (bean.getStatus() != PublishStatus.waiting) {
					bean.setStatus(PublishStatus.waiting);
					PublishDB.updatePublish(bean, loggedIn);
				}
			}
		} else {
			Logger.d(TimingBroadcastReceiver.TAG, bean.getText() + "-添加到队列等等发布");
			
			publishQueue.add(bean);
			if (firstBean == null)
				onPublish(bean);

			if (bean.getStatus() != PublishStatus.waiting) {
				bean.setStatus(PublishStatus.waiting);
				PublishDB.updatePublish(bean, loggedIn);
			}
		}
		
		refreshDraftbox();
	}

	/**
	 * 将添加状态的消息都加入到队列当中
	 */
	public void publishInit() {
		List<PublishBean> beans = PublishDB.getPublishOfAddStatus(loggedIn);
		for (PublishBean bean : beans)
			publishQueue.add(bean);

		onPublish(publishQueue.peek());
	}
	
	public void delete(PublishBean bean) {
		Iterator<PublishBean> iterator = publishQueue.iterator();
		while (iterator.hasNext()) {
			if (bean == iterator.next()) {
				iterator.remove();
				// TODO 删除一个发布任务，存入草稿
				PublishDB.deletePublish(bean, loggedIn);
				break;
			}
		}
	}

	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		switch (msg.what) {
		case publishDelay:
			PublishBean bean = (PublishBean) msg.obj;
			onPublish(bean);
			break;
		}
	}

	@Override
	public void onPublishPoll(PublishBean bean) {
		// 任务失败不从DB删除
		if (bean.getStatus() != PublishStatus.faild)
			PublishDB.deletePublish(bean, loggedIn);

		refreshDraftbox();
	}

	@Override
	public void onPublishAdd(PublishBean bean) {
		if (bean.getStatus() != PublishStatus.create)
			bean.setStatus(PublishStatus.draft);
		bean.setErrorMsg("");
		PublishDB.addPublish(bean, loggedIn);
	}

	@Override
	public void onPublishPeek(PublishBean bean) {
		bean.setStatus(PublishStatus.sending);
		PublishDB.updatePublish(bean, loggedIn);
		
		//更新界面
		refreshDraftbox();
	}

	private synchronized void publishFinished(PublishBean bean) {
		publishQueue.poll();

		Logger.w("publishFinished" + publishQueue.size());

		// 队列发送完毕了，且当前运行的页面不是发布页面，就停止服务
		if (publishQueue.size() == 0)
			context.stopService(new Intent(context, PublishService.class));
		else {
			postDelayed(new Runnable() {
				
				@Override
				public void run() {
					onPublish(publishQueue.peek());
				}
				
			}, 2 * 1000);
		}
	}
	
	private void refreshDraftbox() {
		Intent intent = new Intent();
		intent.setAction(ACTION_PUBLISH_CHANNGED);
		context.sendBroadcast(intent);
	}

	class PublishTask extends WorkTask<Void, Void, Object> {

		PublishBean bean;

		public PublishTask(PublishBean bean) {
			this.bean = bean;
		}
		
		@Override
		protected void onPrepare() {
			super.onPrepare();
			if (bean != null)
				publishNotifier.notifyPublishing(bean);
		}
		
		@Override
		protected void onFailure(TaskException exception) {
			super.onFailure(exception);
			if (bean != null) {
				// 响应成功，就让他发成功
				if (TaskException.TaskError.socketTimeout.toString().equalsIgnoreCase(exception.getCode())) {
					onSuccess(null);
				}
				else {
					publishNotifier.notifyPublishFaild(bean, exception.getMessage());
					
					bean.setStatus(PublishStatus.faild);
					SinaErrorMsgUtil util = new SinaErrorMsgUtil();
					if (util.checkCode(exception.getCode()) != null)
						bean.setErrorMsg(util.checkCode(exception.getCode()));
					else
						bean.setErrorMsg(exception.getMessage());
					PublishDB.updatePublish(bean, loggedIn);
					
					refreshDraftbox();
				}
			}
		}
		
		@Override
		protected void onFinished() {
			super.onFinished();
			if (bean != null){
				publishFinished(bean);

				refreshDraftbox();
			}
		}

		@Override
		protected void onSuccess(Object result) {
			super.onSuccess(result);

			if (bean != null) {
				// 回复微博、评论和转发微博时，通知刷新界面
				if (bean.getType() == PublishType.commentCreate) {
					GlobalContext.getInstance().sendBroadcast(new Intent(TimelineDetailPagerFragment.ACTION_REFRESH_CMT_CREATE));
				}
				else if (bean.getType() == PublishType.commentReply) {
					GlobalContext.getInstance().sendBroadcast(new Intent(TimelineDetailPagerFragment.ACTION_REFRESH_CMT_REPLY));
				}
				else if (bean.getType() == PublishType.statusRepost) {
					GlobalContext.getInstance().sendBroadcast(new Intent(TimelineDetailPagerFragment.ACTION_REFRESH_REPOST));
				}

				publishNotifier.notifyPublishSuccess(bean);

				PublishDB.deletePublish(bean, loggedIn);
			}
		}

		@Override
		public Object workInBackground(Void... params) throws TaskException {
			if (bean == null) {
				publishInit();
			} else {
				// 发布微博(带图片)
				if (bean.getType() == PublishType.status) {
					if (!bean.getParams().containsKey("status"))
						bean.getParams().addParameter("status", bean.getText());
					
					// 意见反馈或者分享图片
					if (bean.getParams().containsKey("url") && 
							// sina有分享GIF图片后不再是GIF图片的BUG
							!bean.getParams().getParameter("url").toLowerCase().endsWith(".gif")) {
						String url = bean.getParams().getParameter("url");
						if (url.indexOf("/bmiddle/") != -1 && url.indexOf("sina") != -1) {
							url = url.replace("/bmiddle/", "/large/");
							bean.getParams().addParameter("url", url);
						}
						
						return SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).statusesUploadUrlText(bean.getParams());
					}
					// 带图片
					else if (bean.getPics() != null && bean.getPics().length > 0) {
						String[] images = bean.getPics();

						Object result = null;

						// 2015-06-29 在这里切入多图上传支持，暂时支持挨个挨个上传图片不并发

						// 如果只有一个图片，就用Aisen，
						if (images.length == 1) {
							String path = images[0];
							if (path.toString().startsWith("content://")) {
								Uri uri = Uri.parse(path);
								path = FileUtils.getPath(GlobalContext.getInstance(), uri);
							}
							else {
								path = path.toString().replace("file://", "");
							}

							Logger.w("上传文件路径 = " + path);

							File file = new File(path);

							if (!file.exists())
								throw new TaskException("图片不存在或已删除");

							// 压缩文件
							file = AisenUtils.getUploadFile(context, file);
							Logger.w("上传图片大小" + (file.length() / 1024) + "KB");

							result = SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).statusesUpload(bean.getParams(), file);
						}
						// 如果存在多个图片，就用Weico高级权限
						else {
							List<String> picIdList = new ArrayList<>();

							for (int i = 0; i < images.length; i++) {
								String path = images[i];

								// 检查这个图片是否已经上传过了
								String pathKey = KeyGenerator.generateMD5(path);
								UploadPictureBean uploadPictureBean = SinaDB.getDB().selectById(new Extra(loggedIn.getIdstr(), null), UploadPictureBean.class, pathKey);
								if (uploadPictureBean != null && !TextUtils.isEmpty(uploadPictureBean.getPic_id())) {
									Logger.w("这个图片已经上传过了, resultId = " + uploadPictureBean.getPic_id() + ", path = " + path);

									picIdList.add(uploadPictureBean.getPic_id());
								}
								else {
									uploadPictureBean = new UploadPictureBean();
									uploadPictureBean.setKey(pathKey);
									uploadPictureBean.setPath(path);

									if (path.toString().startsWith("content://")) {
										Uri uri = Uri.parse(path);
										path = FileUtils.getPath(GlobalContext.getInstance(), uri);
									}
									else {
										path = path.toString().replace("file://", "");
									}

									Logger.w(String.format("上传第%d个文件， 路径 = %s", i + 1, path));

									File file = new File(path);

									if (!file.exists())
										throw new TaskException("图片不存在或已删除");

									// 压缩文件
									file = AisenUtils.getUploadFile(context, file);
									Logger.w("上传图片大小" + (file.length() / 1024) + "KB");

									// 开始上传
									UploadPictureResultBean resultBean = SinaSDK.getInstance(mAccount.getAdvancedToken()).uploadPicture(file);
									Logger.w("成功上传一张图片，pic_id = " + resultBean.getPic_id());
									uploadPictureBean.setPic_id(resultBean.getPic_id());
									picIdList.add(resultBean.getPic_id());

									// 更新到数据库
									SinaDB.getDB().insertOrReplace(new Extra(loggedIn.getIdstr(), null), uploadPictureBean);
								}
							}

							// 图片都上传完了，开始发布微博
							String picIdStr = "";
							for (String picId : picIdList) {
								picIdStr = picIdStr + picId + ",";
							}
							picIdStr = picIdStr.substring(0, picIdStr.length() - 1);
							bean.getParams().addParameter("pic_id", picIdStr);
							return SinaSDK.getInstance(mAccount.getAccessToken()).statusesUploadUrlText(bean.getParams());
						}

//						file.delete();
						return result;
					}
					// 纯文字
					else {
						return SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).statusesUpdate(bean.getParams());
					}
				}
				// 回复微博(同时转发微博)
				else if (bean.getType() == PublishType.commentCreate) {
					if (!bean.getParams().containsKey("comment"))
						bean.getParams().addParameter("comment", bean.getText());
					
					// 评论微博同时转发
					if (bean.getExtras().containsKey("forward") && Boolean.parseBoolean(bean.getExtras().getParameter("forward"))) {
						Params repostParams = new Params();
						repostParams.addParameter("id", bean.getParams().getParameter("id"));
						repostParams.addParameter("status", bean.getParams().getParameter("comment"));
						SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).statusesReport(repostParams);
					}
					return SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).commentCreate(bean.getParams());
				}
				// 回复评论
				else if (bean.getType() == PublishType.commentReply) {
					if (!bean.getParams().containsKey("comment"))
						bean.getParams().addParameter("comment", bean.getText());
					
					// 评论微博同时转发
					if (bean.getExtras().containsKey("forward") && Boolean.parseBoolean(bean.getExtras().getParameter("forward"))) {
						Params repostParams = new Params();
						repostParams.addParameter("id", bean.getParams().getParameter("id"));
						repostParams.addParameter("status", bean.getParams().getParameter("comment"));
						SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).statusesReport(repostParams);
					}

					return SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).commentsReply(bean.getParams());
				}
				// 转发微博
				else if (bean.getType() == PublishType.statusRepost) {
					if (!bean.getParams().containsKey("status"))
						bean.getParams().addParameter("status", bean.getText());
					
					return SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).statusesReport(bean.getParams());
				}
			}
			
			throw new TaskException("发送失败");
		}
	}

}
