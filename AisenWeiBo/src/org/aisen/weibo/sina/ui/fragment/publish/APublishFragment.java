package org.aisen.weibo.sina.ui.fragment.publish;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.bean.Emotion;
import org.aisen.weibo.sina.support.bean.PublishBean;
import org.aisen.weibo.sina.support.bean.PublishBean.PublishStatus;
import org.aisen.weibo.sina.support.compress.TimelineBitmapCompress;
import org.aisen.weibo.sina.support.db.EmotionsDB;
import org.aisen.weibo.sina.support.publish.PublishDB;
import org.aisen.weibo.sina.support.utils.AisenUtil;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.AppSettings;
import org.aisen.weibo.sina.sys.service.PublishService;
import org.aisen.weibo.sina.ui.fragment.publish.EmotionFragment.OnEmotionSelectedListener;
import org.android.loader.BitmapLoader;
import org.android.loader.core.BitmapDecoder;
import org.android.loader.core.ImageConfig;
import org.android.loader.download.ContentProviderDownloader;
import org.android.loader.download.DownloadProcess;
import org.android.loader.download.SdcardDownloader;
import org.sina.android.bean.WeiBoUser;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.m.common.params.Params;
import com.m.common.utils.ActivityHelper;
import com.m.common.utils.BitmapUtil;
import com.m.common.utils.FileUtility;
import com.m.common.utils.Logger;
import com.m.common.utils.SystemUtility;
import com.m.common.utils.Utils;
import com.m.support.Inject.ViewInject;
import com.m.support.task.TaskException;
import com.m.support.task.WorkTask;
import com.m.ui.activity.BaseActivity;
import com.m.ui.fragment.ABaseFragment;
import com.m.ui.utils.PhotoChoice;
import com.m.ui.utils.PhotoChoice.PhotoChoiceListener;
import com.m.ui.utils.PhotoChoice.PhotoChoiceMode;

public abstract class APublishFragment extends ABaseFragment 
						implements OnEmotionSelectedListener, PhotoChoiceListener {

	public static final String TAG = "Publish";
	
	public static final int MAX_STATUS_LENGTH = 140;
	
	@ViewInject(id = R.id.btnLocation, click = "loadGPSLocation")
	View btnLocation;
	@ViewInject(id = R.id.btnCamera, click = "getPicture")
	View btnCamera;
	@ViewInject(id = R.id.btnEmotion, click = "switchEmotionSoftinput")
	View btnEmotion;
	@ViewInject(id = R.id.btnMention, click = "getFriend")
	View btnMention;
	@ViewInject(id = R.id.btnTrends, click = "insertTrends")
	View btnTrends;
	@ViewInject(id = R.id.btnOverflow, click = "popOverflowMenu")
	View btnOverflow;
	
	@ViewInject(id = R.id.layContainer)
	View layContainer;
	@ViewInject(id = R.id.layRoot)
	ViewGroup layRoot;
	@ViewInject(id = R.id.txtErrorHint)
	TextView txtErrorHint;
	@ViewInject(id = R.id.editContent)
	EditText editContent;
	@ViewInject(id = R.id.imgBk)
	ImageView picShow;
	@ViewInject(id = R.id.layImageCover)
	View layImageCover;
	@ViewInject(id = R.id.layEmotion)
	View layEmotion;
	@ViewInject(id = R.id.txtContentSurplus)
	TextView txtContentSurplus;
	
	private final LayoutTransition transitioner = new LayoutTransition();
	
	private EmotionFragment emotionFragment;
	private PhotoChoice photoChoice;
	
	private PublishBean mBean;
	
	private int emotionHeight;
	
	@Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceState) {
		super.layoutInit(inflater, savedInstanceState);
		
		getActivity().getActionBar().setSubtitle(AppContext.getUser().getScreen_name());
	
		btnLocation.setVisibility(View.GONE);
		
		if (savedInstanceState == null) {
			if (getArguments() != null)
				mBean = (PublishBean) getArguments().getSerializable("bean");
		}
		else {
			mBean = (PublishBean) savedInstanceState.getSerializable("bean");
		}
		
		if (mBean == null)
			mBean = newPublishBean();
		
		setHasOptionsMenu(true);

		if (savedInstanceState == null) {
			emotionFragment = EmotionFragment.newInstance();
			getActivity().getFragmentManager().beginTransaction().add(R.id.layEmotion, emotionFragment, "EmotionFragemnt").commit();
		}
		else {
			emotionFragment = (EmotionFragment) getActivity().getFragmentManager().findFragmentByTag("EmotionFragemnt");
		}
		emotionFragment.setOnEmotionListener(this);
		
		// 内容编辑
		editContent.addTextChangedListener(editContentWatcher);
		// 更换表情
		editContent.setFilters(new InputFilter[] { emotionFilter });
		editContent.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				hideEmotionView(true);
			}
		});
		
		ObjectAnimator animIn = ObjectAnimator.ofFloat(null, "translationY",
                SystemUtility.getScreenHeight(getActivity()), emotionHeight).
                setDuration(transitioner.getDuration(LayoutTransition.APPEARING));
		transitioner.setAnimator(LayoutTransition.APPEARING, animIn);

        ObjectAnimator animOut = ObjectAnimator.ofFloat(null, "translationY", emotionHeight,
                SystemUtility.getScreenHeight(getActivity())).
                setDuration(transitioner.getDuration(LayoutTransition.DISAPPEARING));
        transitioner.setAnimator(LayoutTransition.DISAPPEARING, animOut);
        layRoot.setLayoutTransition(transitioner);
		
		refreshUI();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putSerializable("bean", mBean);
	}
	
	/**
	 * 刷新视图
	 */
	void refreshUI() {
		if (getPublishBean() == null)
			return;
		
		PublishBean bean = getPublishBean();
		
		// 文字内容
		if (!TextUtils.isEmpty(bean.getText())) {
			editContent.setText(bean.getText());
			// 如果是意见反馈，不默认将光标移动到最后
			if (bean.getExtras() != null && bean.getExtras().containsKey("feedback"))
				;
			else 
				editContent.setSelection(editContent.getText().toString().length());
		}
		
		// 显示图片
		if (bean.getExtras() != null && bean.getExtras().containsKey("images")) {
			String[] images = bean.getExtras().getParameter("images").split("&");
			
			ImageConfig config = new ImageConfig();
			config.setLoadfaildBitmapRes(R.drawable.bg_timeline_loading);
			config.setLoadingBitmapRes(R.drawable.bg_timeline_loading);
			config.setMaxWidth(SystemUtility.getScreenWidth());
			config.setMaxHeight(SystemUtility.getScreenHeight());
			config.setBitmapCompress(TimelineBitmapCompress.class);
			config.setProgress(new PublishDownloadProcess());
			
			String path = images[0];
			if (path.toString().startsWith("content://")) {
				Logger.v(TAG, "相册图片地址, path = " + path);
				
				config.setDownloaderClass(ContentProviderDownloader.class);
			}
			else {
				path = path.toString().replace("file://", "");
				Logger.v(TAG, "拍照图片地址, path = " + path);
				
				// 扫描文件
				SystemUtility.scanPhoto(new File(path));
				config.setDownloaderClass(SdcardDownloader.class);
			}
			
			BitmapLoader.getInstance().display(this, path, picShow, config);
			
			layImageCover.setVisibility(View.VISIBLE);
			picShow.setVisibility(View.VISIBLE);
		}
		else {
			layImageCover.setVisibility(View.GONE);
			picShow.setVisibility(View.GONE);
		}
	}
	
	class PublishDownloadProcess extends DownloadProcess {

		@Override
		public void receiveLength(long length) {
			
		}

		@Override
		public void receiveProgress(long progressed) {
			
		}

		@Override
		public void prepareDownload(String url) {
			
		}

		@Override
		public void finishedDownload(byte[] bytes) {
			
		}

		@Override
		public void downloadFailed(Exception e) {
			getPublishBean().getExtras().remove("images");
			
			showMessage(R.string.publish_pic_none);
		}
		
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.publish, menu);
		
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.send) {
			if (txtErrorHint.getVisibility() == View.VISIBLE) {
				showMessage(txtErrorHint.getText().toString());
				return true;
			}
			
			if (checkValid(getPublishBean()))
				send();
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private void send() {
		if (AppSettings.isSendDelay()) 
			getPublishBean().setDelay(AppSettings.getPublishDelay());
		else 
			getPublishBean().setDelay(0);
		
		PublishService.publish(getActivity(), getPublishBean());
		
		getActivity().finish();
	}
	
	@Override
	public void onEmotionSelected(Emotion emotion) {
		Editable editAble = editContent.getEditableText();
		int start = editContent.getSelectionStart();
		editAble.insert(start, emotion.getKey());
	};
	
	/**
	 * 微博内容监听，刷新提示信息
	 */
	private TextWatcher editContentWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// 设置长度提示
			String content = editContent.getText().toString() + appendContent();
			txtContentSurplus.setText((MAX_STATUS_LENGTH - AisenUtil.getStrLength(content)) + "");
			
			if (AisenUtil.getStrLength(content) > MAX_STATUS_LENGTH) {
				txtErrorHint.setVisibility(View.VISIBLE);
				txtErrorHint.setText(String.format(getString(R.string.error_length_too_long), AisenUtil.getStrLength(content) - MAX_STATUS_LENGTH));
			}
			else {
				txtErrorHint.setVisibility(View.GONE);
			}
			
			getPublishBean().setText(content);
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void afterTextChanged(Editable s) {

		}
		
	};
	
	/**
	 * 输入文本的过滤，根据输入替换库中的表情
	 */
	private InputFilter emotionFilter = new InputFilter() {

		@Override
		public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
			// 是delete直接返回
			if ("".equals(source)) {
				return null;
			}

			byte[] emotionBytes = EmotionsDB.getEmotion(source.toString());
			// 输入的表情字符存在，则替换成表情图片
			if (emotionBytes != null) {
				byte[] data = emotionBytes;
				Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
				int size = BaseActivity.getRunningActivity().getResources().getDimensionPixelSize(R.dimen.emotion_size);
				bitmap = BitmapUtil.zoomBitmap(bitmap, size);
				SpannableString emotionSpanned = new SpannableString(source.toString());
				ImageSpan span = new ImageSpan(getActivity(), bitmap);
				emotionSpanned.setSpan(span, 0, source.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				return emotionSpanned;
			} else {
				return source;
			}
		}

	};
	
	protected String appendContent() {
		return "";
	}

	/**
	 * 插入图片
	 * 
	 * @param v
	 */
	void getPicture(View v) {
		// 已经有图片了
		if (getPublishBean().getExtras() != null && getPublishBean().getExtras().containsKey("images")) {
			new AlertDialog.Builder(getActivity())
							.setItems(R.array.publish_pic_edit, new OnClickListener() {
					
								@Override
								public void onClick(DialogInterface dialog, int which) {
									if (which == 0) {
										showGetPictureDialog();
									}
									else {
										getPublishBean().getExtras().remove("images");
										
										refreshUI();
									}
								}
							})
							.show();
		}
		else {
			showGetPictureDialog();
		}
	}
	
	private void showGetPictureDialog() {
		new AlertDialog.Builder(getActivity())
							.setItems(R.array.publish_pic, new OnClickListener() {
					
								@Override
								public void onClick(DialogInterface dialog, int which) {
									if (photoChoice == null) {
										String albumPath = SystemUtility.getSdcardPath() + java.io.File.separator + "/DCIM/Camera/";
										File albumFile = new File(albumPath);
										if (!albumFile.exists())
											albumFile.mkdirs();
										photoChoice = new PhotoChoice(getActivity(), APublishFragment.this, albumPath);
										photoChoice.setFileName(String.format("%s.jpg", String.valueOf(System.currentTimeMillis() / 1000)));
									}
									
									photoChoice.setMode(PhotoChoiceMode.uriType);
									switch (which) {
									// 相册
									case 0:
										photoChoice.start(APublishFragment.this, 0);
										break;
									// 拍照
									case 1:
										photoChoice.start(APublishFragment.this, 1);
										break;
									// 最后一次拍照
									case 2:
										getLastPhoto();
										break;
									default:
										break;
									}
								}
							})
							.show();
	}
	
	/**
	 * 插入话题
	 * 
	 * @param v
	 */
	void insertTrends(View v) {
		Editable editAble = editContent.getEditableText();
		int start = editContent.getSelectionStart();
		editAble.insert(start, "##");

		editContent.setSelection(editContent.getSelectionStart() - 1);
	}
	
	/**
	 * 切换表情跟键盘
	 * 
	 * @param v
	 */
	void switchEmotionSoftinput(View v) {
		if (layEmotion.isShown()) {
			hideEmotionView(true);
        } else {
        	showEmotionView(SystemUtility.isKeyBoardShow(getActivity()));
        }
	}
	
	private void hideEmotionView(boolean showKeyBoard) {
		if (layEmotion.isShown()) {
			if (showKeyBoard) {
				LinearLayout.LayoutParams localLayoutParams = (LinearLayout.LayoutParams) layContainer.getLayoutParams();
	            localLayoutParams.height = layEmotion.getTop();
	            localLayoutParams.weight = 0.0F;
	            layEmotion.setVisibility(View.GONE);
	            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

	            SystemUtility.showKeyBoard(editContent);
	            editContent.postDelayed(new Runnable() {
	              
	            	@Override
	                    public void run() {
	                        unlockContainerHeightDelayed();
	                    }
	            	
	            }, 200L);
            } else {
            	layEmotion.setVisibility(View.GONE);
	            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                unlockContainerHeightDelayed();
            }
        }
	}
	
	private void showEmotionView(boolean showAnimation) {
		if (showAnimation) {
            transitioner.setDuration(200);
        } else {
            transitioner.setDuration(0);
        }

		emotionHeight = SystemUtility.getKeyboardHeight(getActivity());
		
        SystemUtility.hideSoftInput(editContent);
        layEmotion.getLayoutParams().height = emotionHeight;
        layEmotion.setVisibility(View.VISIBLE);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        
        lockContainerHeight(SystemUtility.getAppContentHeight(getActivity()));
	}
	
	 private void lockContainerHeight(int paramInt) {
        LinearLayout.LayoutParams localLayoutParams = (LinearLayout.LayoutParams) layContainer.getLayoutParams();
        localLayoutParams.height = paramInt;
        localLayoutParams.weight = 0.0F;
    }

    public void unlockContainerHeightDelayed() {
        ((LinearLayout.LayoutParams) layContainer.getLayoutParams()).weight = 1.0F;
    }
	
	void getFriend(View v) {
		AddFriendMentionFragment.launch(this, 1000);
	}
	
	@Override
	public boolean onHomeClick() {
		if (!TextUtils.isEmpty(getPublishBean().getText()) || getPublishBean().getExtras().containsKey("images")) {
			askSaveToDraft();
			return true;
		}
		
		return super.onBackClick();
	}
	
	@Override
	public boolean onBackClick() {
		if (layEmotion.isShown()) {
			hideEmotionView(false);
			return true;
		}
		else if (!TextUtils.isEmpty(getPublishBean().getText()) || getPublishBean().getExtras().containsKey("images")) {
			askSaveToDraft();
			
			return true;
		}
		
		return super.onBackClick();
	}
	
	private void askSaveToDraft() {
		new AlertDialog.Builder(getActivity()).setMessage(R.string.publish_draft_title)
												.setNegativeButton(R.string.no, new OnClickListener() {
													
													@Override
													public void onClick(DialogInterface dialog, int which) {
														getActivity().finish();
													}
												})
												.setPositiveButton(R.string.yes, new OnClickListener() {
													
													@Override
													public void onClick(DialogInterface dialog, int which) {
														getPublishBean().setStatus(PublishStatus.draft);
														
														PublishDB.addPublish(getPublishBean(), AppContext.getUser());
														
														getActivity().finish();
													}
												})
												.show();
		
	}
	
	@Override
	public void choiceByte(byte[] datas) {
		
	}

	@Override
	public void choiceBitmap(Bitmap bitmap) {
		
	}

	@Override
	public void choieUri(Uri uri, int requestCode) {
		// 当拍摄照片时，提示是否设置旋转90度
		if (!AppSettings.isRotatePic() && !ActivityHelper.getInstance().getBooleanShareData("RotatePicNoRemind", false)) {
			new AlertDialog.Builder(getActivity()).setTitle(R.string.remind)
									.setMessage(R.string.publish_rotate_remind)
									.setNegativeButton(R.string.donnot_remind, new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int which) {
											ActivityHelper.getInstance().putBooleanShareData("RotatePicNoRemind", true);
										}
									})
									.setPositiveButton(R.string.i_know, null)
									.show();
		}
		
		// 拍摄照片时，顺时针旋转90度
		if (requestCode == PhotoChoice.CAMERA_IMAGE_REQUEST_CODE && AppSettings.isRotatePic()) {
			final String path = uri.toString().replace("file://", "");
			
			new WorkTask<Void, Void, String>() {

				@Override
				public String workInBackground(Void... params) throws TaskException {
					try {
						Bitmap bitmap = BitmapDecoder.decodeSampledBitmapFromFile(path, SystemUtility.getScreenHeight(), SystemUtility.getScreenHeight());
						bitmap = BitmapUtil.rotateBitmap(bitmap, 90);
						
						ByteArrayOutputStream outArray = new ByteArrayOutputStream();
						bitmap.compress(CompressFormat.JPEG, 100, outArray);
						
						FileUtility.writeFile(new File(path), outArray.toByteArray());
					} catch (OutOfMemoryError e) {
						e.printStackTrace();
					}
					return path;
				}
				
				protected void onSuccess(String result) {
					setPicUri(result);
				};
				
			}.execute();
		}
		else {
			setPicUri(uri.toString());
		}
	}
	
	private void setPicUri(String image) {
		Params extraParams = getPublishBean().getExtras();
		if (extraParams == null) {
			extraParams = new Params();
			getPublishBean().setExtras(extraParams);
		}
		extraParams.addParameter("images", image);
		if (TextUtils.isEmpty(editContent.getText().toString()))
			getPublishBean().setText(getString(R.string.publish_share_pic));
		
		// 刷新视图
		refreshUI();
	}

	@Override
	public void unChoice() {
		
	}
	
	private void getLastPhoto() {
		new WorkTask<Void, Void, String> () {

			@Override
			public String workInBackground(Void... params) throws TaskException {
				String path = "";
//				ContentResolver mResolver = getActivity().getContentResolver();
//		        String[] projection = new String[]{ MediaStore.Images.Media.DATA,
//		        									MediaStore.Images.Media.DATE_ADDED,
//		        									"MAX(" + MediaStore.Images.Media.DATE_ADDED + ")" };
//		        Cursor cursor = mResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
//		        										projection, null, null, MediaStore.Images.Media.DEFAULT_SORT_ORDER);
//				cursor.moveToFirst();
//				while(!cursor.isAfterLast()){
//					path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
//					cursor.moveToNext();
//				}
//				cursor.close();
				
				if (getActivity() != null)
					path = Utils.getLatestCameraPicture(getActivity());
				
				return path;
			}
			
			@Override
			protected void onSuccess(String result) {
				super.onSuccess(result);
				
				if (!TextUtils.isEmpty(result)) {
					Logger.v(TAG, "最近拍摄照片, path = " + result);
					
					File file = new File(result);
					if (file.exists() && file.length() == 0) {
						showMessage(R.string.publish_get_recent_pic_faild);
					}
					else {
						choieUri(Uri.parse(result), PhotoChoice.PHONE_IMAGE_REQUEST_CODE);
					}
					
				}
				else {
					showMessage(R.string.publish_recent_pic_none);
				}
			};
			
		}.execute();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == 1000 && resultCode == Activity.RESULT_OK) {
			WeiBoUser user = (WeiBoUser) data.getSerializableExtra("bean");
			
			Editable editAble = editContent.getEditableText();
			int start = editContent.getSelectionStart();
			editAble.insert(start, String.format("@%s ", user.getScreen_name()));
		}
		else {
			if (photoChoice != null)
				photoChoice.onActivityResult(requestCode, resultCode, data);
		}
		
	}
	
	public PublishBean getPublishBean() {
		return mBean;
	}
	
	/**
	 * 空创建时，初始化Bean
	 * 
	 * @return
	 */
	abstract PublishBean newPublishBean();
	
	abstract void popOverflowMenu(View v);
	
	abstract boolean checkValid(PublishBean bean);

}
