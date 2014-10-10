package org.aisen.weibo.sina.support.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.ui.activity.comment.TimelineCommentsActivity;
import org.aisen.weibo.sina.ui.activity.publish.PublishActivity;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment.OnCommentDestoryCallback;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment.OnStatusDestoryCallback;
import org.aisen.weibo.sina.ui.fragment.comment.TimelineCommentsFragment;
import org.aisen.weibo.sina.ui.fragment.menu.MenuFragment;
import org.aisen.weibo.sina.ui.fragment.publish.APublishFragment;
import org.aisen.weibo.sina.ui.fragment.timeline.ATimelineFragment;
import org.android.loader.core.BitmapDecoder;
import org.sina.android.SinaSDK;
import org.sina.android.bean.GroupSortResult;
import org.sina.android.bean.StatusComment;
import org.sina.android.bean.StatusContent;
import org.sina.android.bean.WeiBoUser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.m.common.context.GlobalContext;
import com.m.common.settings.SettingUtility;
import com.m.common.utils.DateUtils;
import com.m.common.utils.FileUtility;
import com.m.common.utils.Logger;
import com.m.common.utils.SystemUtility;
import com.m.common.utils.SystemUtility.NetWorkType;
import com.m.support.task.TaskException;
import com.m.support.task.WorkTask;
import com.m.ui.activity.BaseActivity;
import com.m.ui.fragment.ABaseFragment;
import com.m.ui.fragment.ARefreshFragment;
import com.m.ui.utils.ViewUtils;
import com.spreada.utils.chinese.ZHConverter;

public class AisenUtil {

//	public static String getUserKey(String key) {
//		return key + "-" + AppContext.getUser().getIdstr();
//	}
	
	public static String getUserKey(String key, WeiBoUser user) {
		return key + "-" + user.getIdstr();
	}
	
	public static String getUserScreenName(WeiBoUser user) {
		if (AppSettings.isShowRemark() && !TextUtils.isEmpty(user.getRemark()))
			return user.getRemark();

		return user.getScreen_name();
	}

	@SuppressWarnings("deprecation")
	public static String convDate(String time) {
		Context context = GlobalContext.getInstance();
		Resources res = context.getResources();
		
		StringBuffer buffer = new StringBuffer();

		Calendar createCal = Calendar.getInstance();
		createCal.setTimeInMillis(Date.parse(time));
		Calendar currentcal = Calendar.getInstance();
		currentcal.setTimeInMillis(System.currentTimeMillis());

		long diffTime = (currentcal.getTimeInMillis() - createCal.getTimeInMillis()) / 1000;

		// 同一月
		if (currentcal.get(Calendar.MONTH) == createCal.get(Calendar.MONTH)) {
			// 同一天
			if (currentcal.get(Calendar.DAY_OF_MONTH) == createCal.get(Calendar.DAY_OF_MONTH)) {
				if (diffTime < 3600 && diffTime >= 60) {
					buffer.append((diffTime / 60) + res.getString(R.string.msg_few_minutes_ago));
				} else if (diffTime < 60) {
					buffer.append(res.getString(R.string.msg_now));
				} else {
					buffer.append(res.getString(R.string.msg_today)).append(" ").append(DateUtils.formatDate(createCal.getTimeInMillis(), "HH:mm"));
				}
			}
			// 前一天
			else if (currentcal.get(Calendar.DAY_OF_MONTH) - createCal.get(Calendar.DAY_OF_MONTH) == 1) {
				buffer.append(res.getString(R.string.msg_yesterday)).append(" ").append(DateUtils.formatDate(createCal.getTimeInMillis(), "HH:mm"));
			}
		}

		if (buffer.length() == 0) {
			buffer.append(DateUtils.formatDate(createCal.getTimeInMillis(), "MM-dd HH:mm"));
		}

		String timeStr = buffer.toString();
		if (currentcal.get(Calendar.YEAR) != createCal.get(Calendar.YEAR)) {
			timeStr = createCal.get(Calendar.YEAR) + " " + timeStr;
		}
		return timeStr;
	}


	public static String getGender(WeiBoUser user) {
		Resources res = GlobalContext.getInstance().getResources();
		if (user != null) {
			if ("m".equals(user.getGender())) {
				return res.getString(R.string.msg_male);
			} else if ("f".equals(user.getGender())) {
				return res.getString(R.string.msg_female);
			} else if ("n".equals(user.getGender())) {
				return res.getString(R.string.msg_gender_unknow);
			}
		}
		return "";
	}

	public static String convCount(int count) {
		if (count < 10000) {
			return count + "";
		} else {
			Resources res = GlobalContext.getInstance().getResources();
			String result = new DecimalFormat("#.0").format(count * 1.0f / 10000) + res.getString(R.string.msg_ten_thousand);
			return result;
		}
	}

	public static String getFirstId(@SuppressWarnings("rawtypes") List datas) {
		int size = datas.size();
		if (size > 0)
			return getId(datas.get(0));
		return null;
	}

	public static String getLastId(@SuppressWarnings("rawtypes") List datas) {
		int size = datas.size();
		if (size > 0)
			return getId(datas.get(size - 1));
		return null;
	}

	public static String getId(Object t) {
		try {
			Field idField = t.getClass().getDeclaredField("id");
			idField.setAccessible(true);
			return idField.get(t).toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getCommentText(String text) {
		if (TextUtils.isEmpty(text))
			return "";
		
		try {
			if (text.startsWith("回覆") || text.startsWith("回复")) {
				if (text.indexOf(":") != -1) {
					text = text.substring(text.indexOf(":") + 1, text.length());
				}
				else if (text.indexOf("：") != -1) {
					text = text.substring(text.indexOf("：") + 1, text.length());
				}
			}
		} catch (Exception e) {
		}
		
		return text.trim();
	}
	
	/**
	 * 显示高清头像
	 * 
	 * @param user
	 * @return
	 */
	public static String getUserPhoto(WeiBoUser user) {
		if (user == null)
			return "";
		
		if (AppSettings.isLargePhoto()) {
			return user.getAvatar_large();
		}
		
		return user.getProfile_image_url();
//		switch (AppSettings.getPhotoMode()) {
//		// MODE_AUTO
//		case 0:
//			if (SystemUtility.getNetworkType() == NetWorkType.wifi)
//				return user.getAvatar_large();
//
//			return user.getProfile_image_url();
//		// MODE_ALWAYS_ORIG
//		case 1:
//			return user.getAvatar_large();
//		// MODE_ALWAYS_THUMB
//		case 2:
//			return user.getProfile_image_url();
//		default:
//			return user.getProfile_image_url();
//		}
	}
	
	public static String getStatusMulImage(String thumbImage) {
		switch (AppSettings.getPictureMode()) {
		// MODE_AUTO
		case 0:
			if (SystemUtility.getNetworkType() == NetWorkType.wifi)
				return thumbImage.replace("thumbnail", "large");

			return thumbImage;
		// MODE_ALWAYS_ORIG
		case 1:
			return thumbImage.replace("thumbnail", "large");
		case 2:
			return thumbImage.replace("thumbnail", "bmiddle");
		// MODE_ALWAYS_THUMB
		case 3:
			return thumbImage;
		default:
			return thumbImage;
		}
	}
	
	public static String getCounter(int count) {
		Resources res = GlobalContext.getInstance().getResources();
		
		if (count < 10000)
			return String.valueOf(count);
		else if (count < 100 * 10000)
			return new DecimalFormat("#.0" + res.getString(R.string.msg_ten_thousand)).format(count * 1.0f / 10000);
		else 
			return new DecimalFormat("#" + res.getString(R.string.msg_ten_thousand)).format(count * 1.0f / 10000);
	}
	
	public static void showMenuDialog(ABaseFragment fragment, View targetView, 
			String[] menuArr, DialogInterface.OnClickListener onItemClickListener) {
		final AlertDialog dialog = new AlertDialog.Builder(fragment.getActivity(), R.style.ChanneMenuDialog)
												.setItems(menuArr, onItemClickListener).create();
		// 4.0没有这个方法
		if (android.os.Build.VERSION.SDK_INT >= 16 )
			dialog.getListView().setScrollBarDefaultDelayBeforeFade(5 * 1000);
		dialog.show();
		
		Rect rect = new Rect();
		targetView.getGlobalVisibleRect(rect);
		int width = fragment.getResources().getDimensionPixelSize(R.dimen.channelist_menu_width);
		int size = menuArr.length > 8 ? 8 : menuArr.length;
		int height = Math.round(fragment.getResources().getDimensionPixelSize(R.dimen.channelist_menu_item_height) * (size + 0.5f));
		WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
		params.width = width;
		params.x = rect.right - Math.round(params.width * 1.2f);
		Rect actionBarRect = new Rect();
		fragment.getRootView().getGlobalVisibleRect(actionBarRect);
		// 下面
		if (rect.bottom + height < SystemUtility.getScreenHeight()) {
			int off = fragment.getResources().getDimensionPixelSize(R.dimen.vertical_margin);
			if (fragment instanceof MenuFragment || fragment instanceof APublishFragment)
				off = 0;
			params.y = rect.bottom - off; 
		}
		// 上面
		else {
			params.y = rect.top - height;

			if (BaseActivity.getRunningActivity() != null) {
				Activity activity = BaseActivity.getRunningActivity();
				
//				Rect frame = new Rect();  
//				activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);  
//				int statusBarHeight = frame.top;
				
				int contentTop = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
				if (params.y < contentTop)
					params.y = contentTop;
				//statusBarHeight是上面所求的状态栏的高度  
//				int titleBarHeight = contentTop - statusBarHeight ; 
//				if (height > rect.top - contentTop) {
//					height = rect.top - contentTop;
//				}
			}

		}
		
		dialog.setCanceledOnTouchOutside(true);
		dialog.getWindow().setGravity(Gravity.LEFT | Gravity.TOP);
		dialog.getWindow().setLayout(Math.round(params.width * 1.2f), height);
		dialog.getWindow().setAttributes(params);
	}
	
	@SuppressWarnings("deprecation")
	public static void copyToClipboard(String text) {
		// 得到剪贴板管理器 
		ClipboardManager cmb = (ClipboardManager) GlobalContext.getInstance().getSystemService(Context.CLIPBOARD_SERVICE); 
		cmb.setText(text.trim()); 
	}
	
	public static void commentMenuSelected(final ABaseFragment fragment, String selectedItem, final StatusComment comment) {
		final String[] commentMenuArr = GlobalContext.getInstance().getResources().getStringArray(R.array.cmt_menus);
		
		try {
			int position = 0;
			for (int i = 0; i < commentMenuArr.length; i++) {
				if (commentMenuArr[i].equals(selectedItem)) {
					position = i;
					break;
				}
			}
			
			switch (position) {
			// 复制
			case 0:
				AisenUtil.copyToClipboard(comment.getText());
				
				ViewUtils.showMessage(R.string.msg_text_copyed);
				break;
			// 转发
			case 1:
				BizFragment.getBizFragment(fragment).commentRepost(comment);
				break;
			// 删除
			case 2:
				new AlertDialog.Builder(fragment.getActivity()).setMessage(R.string.msg_del_cmt_remind)
											.setNegativeButton(R.string.cancel, null)
											.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
												
												@Override
												public void onClick(DialogInterface dialog, int which) {
													BizFragment.getBizFragment(fragment).commentDestory(comment, new OnCommentDestoryCallback() {
														
														@SuppressWarnings("unchecked")
														@Override
														public void onCommentDestory(StatusComment commnet) {
															if (fragment instanceof ARefreshFragment) {
																@SuppressWarnings("rawtypes")
																ARefreshFragment aRefreshFragment = ((ARefreshFragment) fragment);
																for (Object so : aRefreshFragment.getAdapter().getDatas()) {
																	StatusComment s = (StatusComment) so;
																	if (s.getId().equals(commnet.getId())) {
																		aRefreshFragment.getAdapter().removeItemAndRefresh(s);
																		break;
																	}
																}
															}
														}
													});
												}
											})
											.show();
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void deleteStatus(final ABaseFragment fragment, final StatusContent status) {
		new AlertDialog.Builder(fragment.getActivity())
								.setMessage(R.string.msg_del_status_remind)
								.setNegativeButton(R.string.cancel, null)
								.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										BizFragment.getBizFragment(fragment).statusDestory(status.getId(), new OnStatusDestoryCallback() {
											
											@SuppressWarnings({ "rawtypes", "unchecked" })
											@Override
											public void onStatusDestory(StatusContent status) {
												if (fragment instanceof ATimelineFragment) {
													ARefreshFragment aRefreshFragment = ((ARefreshFragment) fragment);
													for (Object so : aRefreshFragment.getAdapter().getDatas()) {
														StatusContent s = (StatusContent) so;
														if (s.getId().equals(status.getId())) {
															aRefreshFragment.getAdapter().removeItemAndRefresh(s);
															break;
														}
													}
												}
												else {
													if (fragment.getActivity() != null && fragment instanceof TimelineCommentsFragment) {
														Intent data = new Intent();
														data.putExtra("status", status.getId());
														
														fragment.getActivity().setResult(Activity.RESULT_OK, data);
													}
													ViewUtils.showMessage(R.string.delete_success);
												}
											}
											
											@Override
											public boolean onFaild(TaskException e) {
												ViewUtils.showMessage(R.string.delete_faild);
												
												return true;
											}
										});
									}
								})
								.show();
	}
	
	private static void shieldStatus(final ABaseFragment fragment, final StatusContent status) {
		new AlertDialog.Builder(fragment.getActivity())
							.setMessage(R.string.msg_shield_remind)
							.setNegativeButton(R.string.cancel, null)
							.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									new WorkTask<Void, Void, GroupSortResult>() {
					
										@Override
										protected void onPrepare() {
											super.onPrepare();
											
											Resources res = GlobalContext.getInstance().getResources();
											ViewUtils.createNormalProgressDialog(fragment.getActivity(), res.getString(R.string.processing)).show();
										};
										
										@Override
										protected void onFinished() {
											super.onFinished();
											
											ViewUtils.dismissNormalProgressDialog();
										};
										
										@Override
										protected void onFailure(TaskException exception) {
											super.onFailure(exception);
											
											ViewUtils.showMessage(exception.getMessage());
										};
										
										@Override
										protected void onSuccess(GroupSortResult result) {
											super.onSuccess(result);
											
											if ("true".equals(result.getResult()))
												ViewUtils.showMessage(R.string.msg_shield_success);
											else
												ViewUtils.showMessage(R.string.msg_shield_faild);
										};
										
										@Override
										public GroupSortResult workInBackground(Void... params) throws TaskException {
											return SinaSDK.getInstance(AppContext.getToken()).statusMentionsShield(status.getId());
										}
										
									}.execute();
								}
							})
							.show();
	}
	
//	public static final String[] timelineMenuArr = new String[]{ "原微博", "复制", "转发", "评论", "收藏", "取消收藏", "删除", "屏蔽", "围观" };
	public static void timelineMenuSelected(final ABaseFragment fragment, String selectedItem, final StatusContent status) {
		final String[] timelineMenuArr = GlobalContext.getInstance().getResources().getStringArray(R.array.timeline_menus);
		
		try {
			int position = 0;
			for (int i = 0; i < timelineMenuArr.length; i++) {
				if (timelineMenuArr[i].equals(selectedItem)) {
					position = i;
					break;
				}
			}
			
			switch (position) {
			// 原微博
			case 0:
				TimelineCommentsActivity.launch(fragment, status.getRetweeted_status());
				break;
			// 复制
			case 1:
				AisenUtil.copyToClipboard(status.getText());
				
				ViewUtils.showMessage(R.string.msg_text_copyed);
				break;
			// 转发
			case 2:
				BizFragment.getBizFragment(fragment).statusRepost(status);
				break;
			// 评论
			case 3:
				BizFragment.getBizFragment(fragment).commentCreate(status);
				break;
			// 收藏
			case 4:
				BizFragment.getBizFragment(fragment).favorityCreate(status.getId(), null);
				break;
			// 取消收藏
			case 5:
				BizFragment.getBizFragment(fragment).favorityDestory(status.getId(), null);
				break;
			// 删除微博
			case 6:
				deleteStatus(fragment, status);
				break;
			// 屏蔽微博
			case 7:
				shieldStatus(fragment, status);
				break;
			// 围观
			case 8:
				PublishActivity.publishStatusRepostAndWeiguan(fragment.getActivity(), null, status);
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void setTextSize(TextView textView) {
		textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, AppSettings.getTextSize());
	}
	
	public static int getStrLength(String content) {
		int length = 0;
		int tempLength = 0;
		for (int i = 0; i < content.length(); i++) {
			String temp = content.charAt(i) + "";
			if (temp.getBytes().length == 3) {
				length++;
			} else {
				tempLength++;
			}
		}
		length += tempLength / 2 + ((tempLength % 2) == 0 ? 0 : 1);
		return length;
	}
	
	public static File getUploadFile(File source) {
		if (source.getName().toLowerCase().endsWith(".gif")) {
			Logger.w("上传图片是GIF图片，上传原图");
			return source;
		}
		
		File file = null;
		
		String imagePath = SystemUtility.getSdcardPath() + File.separator + SettingUtility.getStringSetting("root_path") + 
									File.separator + SettingUtility.getStringSetting("draft") + File.separator;
		
		int sample = 1;
		int maxSize = 0;
		
		int type = AppSettings.getUploadSetting();
		// 自动，WIFI时原图，移动网络时高
		if (type == 0) {
			if (SystemUtility.getNetworkType() == NetWorkType.wifi)
				type = 1;
			else 
				type = 2;
		}
		Options opts = new Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(source.getAbsolutePath(), opts);
		switch (type) {
		// 原图
		case 1:
			Logger.w("原图上传");
			file = source;
			break;
		// 高
		case 2:
			sample = BitmapDecoder.calculateInSampleSize(opts, 1920, 1080);
			Logger.w("高质量上传");
			maxSize = 700 * 1024;
			imagePath = imagePath + "高" + File.separator + source.getName();
			file = new File(imagePath);
			break;
		// 中
		case 3:
			Logger.w("中质量上传");
			sample = BitmapDecoder.calculateInSampleSize(opts, 1280, 720);
			maxSize = 300 * 1024;
			imagePath = imagePath + "中" + File.separator + source.getName();
			file = new File(imagePath);
			break;
		// 低
		case 4:
			Logger.w("低质量上传");
			sample = BitmapDecoder.calculateInSampleSize(opts, 1280, 720);
			maxSize = 100 * 1024;
			imagePath = imagePath + "低" + File.separator + source.getName();
			file = new File(imagePath);
			break;
		default:
			break;
		}
		
		// 压缩图片
		if (type != 1 && !file.exists()) {
			Logger.w(String.format("压缩图片，原图片 path = %s", source.getAbsolutePath()));
			byte[] imageBytes = FileUtility.readFileToBytes(source);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				out.write(imageBytes);
			} catch (Exception e) {
			}
			
			Logger.w(String.format("原图片大小%sK", String.valueOf(imageBytes.length / 1024)));
			if (imageBytes.length > maxSize) {
				// 尺寸做压缩
				Options options = new Options();

				if (sample > 1) {
					options.inSampleSize = sample;
					Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, options);
					Logger.w(String.format("压缩图片至大小：%d*%d", bitmap.getWidth(), bitmap.getHeight()));
					out.reset();
					bitmap.compress(CompressFormat.JPEG, 100, out);
					imageBytes = out.toByteArray();
				}
				
				options.inSampleSize = 1;
				if (imageBytes.length > maxSize) {
					BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, options);
					Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, options);
					
					int quality = 90;
					out.reset();
					Logger.w(String.format("压缩图片至原来的百分之%d大小", quality));
					bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
					while (out.toByteArray().length > maxSize) {
						out.reset();
						quality -= 10;
						Logger.w(String.format("压缩图片至原来的百分之%d大小", quality));
						bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
					}
				}
				
			}
			
			try {
				if (!file.getParentFile().exists())
					file.getParentFile().mkdirs();
				
				Logger.w(String.format("最终图片大小%sK", String.valueOf(out.toByteArray().length / 1024)));
				FileOutputStream fo = new FileOutputStream(file);
				fo.write(out.toByteArray());
				fo.flush();
				fo.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return file;
	}
	
	public static <T> List<T> getHistoryDatas(List<T> datas, int lastPosition) {
		if (datas.size() > 100 && lastPosition < 100) {
			List<T> newList = new ArrayList<T>();
			Logger.i("AisenUtil", "保存数据大于最大100条，且当前读的位置不大于100。取部分数据");
			
			for (int i = 0; i < 100; i++)
				newList.add(datas.get(i));
			
			return newList;
		}
		
		
		return datas;
	}
	
	public static void setImageVerified(ImageView imgVerified, WeiBoUser user) {
		// 2014-08-27 新增判断，VerifiedType存在为null的情况
		if (user == null || user.getVerified_type() == null) {
			imgVerified.setVisibility(View.GONE);
			return;
		}
		
		// 黄V
		if (user.getVerified_type() == 0) {
			imgVerified.setImageResource(R.drawable.avatar_vip);
		}
		// 200:初级达人 220:高级达人
		else if (user.getVerified_type() == 200 || user.getVerified_type() == 220) {
			imgVerified.setImageResource(R.drawable.avatar_grassroot);
		}
		// 蓝V
		else if (user.getVerified_type() > 0) {
			imgVerified.setImageResource(R.drawable.avatar_enterprise_vip);
		}
		if (user.getVerified_type() >= 0) 
			imgVerified.setVisibility(View.VISIBLE);
		else 
			imgVerified.setVisibility(View.GONE);
	}
	
	public static void playSound() {
		if (AppSettings.isRefreshSound() && BaseActivity.getRunningActivity() != null) {
			SoundPool soundPool= new SoundPool(10, AudioManager.STREAM_SYSTEM,5);
			soundPool.load(BaseActivity.getRunningActivity(), R.raw.pull_event, 1);
			
			soundPool.play(1, 1, 1, 0, 0, 1);
		}
	}
	
	public static void launchBrowser(Activity from, String url) {
		Intent intent = new Intent();        
        intent.setAction(Intent.ACTION_VIEW);    
        Uri content_url = Uri.parse(url);   
        intent.setData(content_url);  
        from.startActivity(intent);
	}
	
	static ZHConverter converter = ZHConverter.getInstance(ZHConverter.TRADITIONAL);
	public static String convertToTraditional(String text) {
		long time = System.currentTimeMillis();
		int length = text.length();
		text = converter.convert(text);
		Logger.d(String.format("文字长度%s,简繁体转换耗时%sms", String.valueOf(length), String.valueOf(System.currentTimeMillis() - time)));
		return text;
	}
	
}
