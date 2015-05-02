package org.aisen.weibo.sina.ui.fragment.publish;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.m.common.context.GlobalContext;
import com.m.common.utils.ActivityHelper;
import com.m.common.utils.BitmapUtil;
import com.m.common.utils.FileUtils;
import com.m.common.utils.Logger;
import com.m.common.utils.SystemBarUtils;
import com.m.common.utils.SystemUtils;
import com.m.common.utils.Utils;
import com.m.component.bitmaploader.BitmapLoader;
import com.m.component.bitmaploader.core.BitmapDecoder;
import com.m.component.bitmaploader.core.ImageConfig;
import com.m.component.bitmaploader.download.ContentProviderDownloader;
import com.m.component.bitmaploader.download.DownloadProcess;
import com.m.component.bitmaploader.download.SdcardDownloader;
import com.m.network.http.Params;
import com.m.network.task.TaskException;
import com.m.network.task.WorkTask;
import com.m.support.inject.ViewInject;
import com.m.support.utils.PhotoChoice;
import com.m.ui.activity.basic.BaseActivity;
import com.m.ui.fragment.ABaseFragment;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.support.bean.Emotion;
import org.aisen.weibo.sina.support.bean.PublishBean;
import org.aisen.weibo.sina.support.bean.PublishBean.PublishStatus;
import org.aisen.weibo.sina.support.compress.TimelineBitmapCompress;
import org.aisen.weibo.sina.support.db.EmotionsDB;
import org.aisen.weibo.sina.support.db.PublishDB;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.sys.service.PublishService;
import org.aisen.weibo.sina.ui.fragment.pics.PicturePickFragment;
import org.aisen.weibo.sina.ui.fragment.publish.EmotionFragment.OnEmotionSelectedListener;
import org.sina.android.bean.WeiBoUser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class APublishFragment extends ABaseFragment
						implements OnEmotionSelectedListener, PhotoChoice.PhotoChoiceListener {

	public static final String TAG = "Publish";
	
	public static final int MAX_STATUS_LENGTH = 140;
	
	@ViewInject(id = R.id.layBtns)
	View layBtns;
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
    @ViewInject(id = R.id.btnSend, click = "sendContent")
    View btnSend;
	
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
	
	@ViewInject(id = R.id.checkbox)
	CheckBox checkBox;
	@ViewInject(id = R.id.txtContent)
	TextView txtContent;
	
	private final LayoutTransition transitioner = new LayoutTransition();
	
	private EmotionFragment emotionFragment;
	private PhotoChoice photoChoice;
	
	private PublishBean mBean;
	
	private int emotionHeight;
	
	@Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceState) {
		super.layoutInit(inflater, savedInstanceState);
		
//		getActivity().getActionBar().setSubtitle(AppContext.getUser().getScreen_name());
	
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
                SystemUtils.getScreenHeight(getActivity()), emotionHeight).
                setDuration(transitioner.getDuration(LayoutTransition.APPEARING));
		transitioner.setAnimator(LayoutTransition.APPEARING, animIn);

        ObjectAnimator animOut = ObjectAnimator.ofFloat(null, "translationY", emotionHeight,
                SystemUtils.getScreenHeight(getActivity())).
                setDuration(transitioner.getDuration(LayoutTransition.DISAPPEARING));
        transitioner.setAnimator(LayoutTransition.DISAPPEARING, animOut);
        layRoot.setLayoutTransition(transitioner);
		
		refreshUI();
	}
	
	// 如果有照片了，也显示黑色文字
	protected boolean configWhite() {
		return getPublishBean().getExtras() != null && getPublishBean().getPics() != null && getPublishBean().getPics().length > 0;
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
		
		
		// 显示图片
		if (bean.getExtras() != null && (bean.getPics() != null || bean.getParams().containsKey("url"))) {
			String[] images = bean.getPics();
			if (images == null) {
                images = new String[]{ bean.getParams().getParameter("url") };
            }

			if (TextUtils.isEmpty(editContent.getText().toString().trim()) && 
					TextUtils.isEmpty(getPublishBean().getText())) {
				getPublishBean().setText(getString(R.string.publish_share_pic) + " ");
			}
			
			ImageConfig config = new ImageConfig();
			config.setLoadfaildRes(R.drawable.bg_timeline_loading);
			config.setLoadingRes(R.drawable.bg_timeline_loading);
			config.setMaxWidth(SystemUtils.getScreenWidth());
			config.setMaxHeight(SystemUtils.getScreenHeight() / 2);
			config.setBitmapCompress(TimelineBitmapCompress.class);
			config.setProgress(new PublishDownloadProcess());
			
			String path = images[0];
			if (path.toString().startsWith("content://")) {
				Logger.v(TAG, "相册图片地址, path = " + path);
				
				config.setDownloaderClass(ContentProviderDownloader.class);
			}
			else if (path.toString().startsWith("http://") || path.toString().startsWith("https://")) {
				Logger.v(TAG, "网络图片地址, path = " + path);
			}
			else {
				path = path.toString().replace("file://", "");
				Logger.v(TAG, "拍照图片地址, path = " + path);
				
				// 扫描文件
				SystemUtils.scanPhoto(new File(path));
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
		
		// 文字内容
		if (!TextUtils.isEmpty(bean.getText())) {
			editContent.setText(bean.getText());
			// 如果是意见反馈，不默认将光标移动到最后
			if (bean.getExtras() != null && bean.getExtras().containsKey("feedback"))
				;
			else 
				editContent.setSelection(editContent.getText().toString().length());
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
            String[] pics = getPublishBean().getPics();
            List<String> picList = new ArrayList<String>();
            for (String pic : pics)
                picList.add(pic);
            picList.remove(0);
            pics = picList.toArray(new String[0]);
            if (pics.length > 0)
                getPublishBean().setPics(pics);
            else
                getPublishBean().setPics(null);
			
			showMessage(R.string.publish_pic_none);
		}
		
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
		if ("[最右]".equals(emotion.getKey()))
			editAble.insert(start, "→_→");
		else
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
			txtContentSurplus.setText((MAX_STATUS_LENGTH - AisenUtils.getStrLength(content)) + "");
			
			if (AisenUtils.getStrLength(content) > MAX_STATUS_LENGTH) {
				txtErrorHint.setVisibility(View.VISIBLE);
				txtErrorHint.setText(String.format(getString(R.string.error_length_too_long), AisenUtils.getStrLength(content) - MAX_STATUS_LENGTH));
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
				int size = BaseActivity.getRunningActivity().getResources().getDimensionPixelSize(R.dimen.publish_emotion_size);
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
		if (getPublishBean().getExtras() != null && 
				(getPublishBean().getPics() != null && getPublishBean().getPics().length > 0) || getPublishBean().getParams().containsKey("url")) {
			new AlertDialogWrapper.Builder(getActivity())
							.setItems(R.array.publish_pic_edit, new OnClickListener() {
					
								@Override
								public void onClick(DialogInterface dialog, int which) {
									if (which == 0) {
										showGetPictureDialog();
									}
									else {
                                        getPublishBean().setPics(null);
//										getPublishBean().getExtras().remove("images");
										getPublishBean().getParams().remove("url");

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
		new AlertDialogWrapper.Builder(getActivity())
							.setItems(R.array.publish_pic, new OnClickListener() {
					
								@Override
								public void onClick(DialogInterface dialog, int which) {
									if (photoChoice == null) {
										String albumPath = SystemUtils.getSdcardPath() + File.separator + "/DCIM/Camera/";
										File albumFile = new File(albumPath);
										if (!albumFile.exists())
											albumFile.mkdirs();
										photoChoice = new PhotoChoice(getActivity(), APublishFragment.this, albumPath);
										photoChoice.setFileName(String.format("%s.jpg", String.valueOf(System.currentTimeMillis() / 1000)));
									}

									photoChoice.setMode(PhotoChoice.PhotoChoiceMode.uriType);
									switch (which) {
									// 相册
									case 0:
//										photoChoice.start(APublishFragment.this, 0);
                                        String[] images = getPublishBean().getPics();
                                        images = null;// 这里只选中一个图片，不设置默认图片
                                        PicturePickFragment.launch(APublishFragment.this, 1, images, 3333);
										break;
									// 拍照
									case 1:
										photoChoice.start(APublishFragment.this, 1);
										break;
									// 最后一次拍照
									case 2:
										getLastPhoto();
										break;
									// 来自剪切板
									case 3:
										getPictureFromClipbroad();
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
        	showEmotionView(SystemUtils.isKeyBoardShow(getActivity()));
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

	            SystemUtils.showKeyBoard(editContent);
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

        int statusBarHeight = SystemBarUtils.getStatusBarHeight(getActivity());
		emotionHeight = SystemUtils.getKeyboardHeight(getActivity());
        if (Build.VERSION.SDK_INT >= 19) {
            emotionHeight += statusBarHeight;
        }

        SystemUtils.hideSoftInput(editContent);
        layEmotion.getLayoutParams().height = emotionHeight;
        layEmotion.setVisibility(View.VISIBLE);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        lockContainerHeight(SystemUtils.getAppContentHeight(getActivity()) - statusBarHeight);
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
		if (!TextUtils.isEmpty(getPublishBean().getText()) || getPublishBean().getPics() != null) {
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
		else if (!TextUtils.isEmpty(getPublishBean().getText()) || getPublishBean().getPics() != null) {
			askSaveToDraft();

			return true;
		}

		return super.onBackClick();
	}

	private void askSaveToDraft() {
		new AlertDialogWrapper.Builder(getActivity()).setMessage(R.string.publish_draft_title)
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
		if (!AppSettings.isRotatePic() && !ActivityHelper.getBooleanShareData("RotatePicNoRemind", false)) {
			new AlertDialogWrapper.Builder(getActivity()).setTitle(R.string.remind)
									.setMessage(R.string.publish_rotate_remind)
									.setNegativeButton(R.string.donnot_remind, new OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int which) {
											ActivityHelper.putBooleanShareData("RotatePicNoRemind", true);
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
						Bitmap bitmap = BitmapDecoder.decodeSampledBitmapFromFile(path, SystemUtils.getScreenHeight(), SystemUtils.getScreenHeight());
						bitmap = BitmapUtil.rotateBitmap(bitmap, 90);
						
						ByteArrayOutputStream outArray = new ByteArrayOutputStream();
						bitmap.compress(CompressFormat.JPEG, 100, outArray);
						
						FileUtils.writeFile(new File(path), outArray.toByteArray());
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
	
	/**
	 * 从剪切板获取链接
	 * 
	 */
	private void getPictureFromClipbroad() {
		ClipboardManager cmb = (ClipboardManager) GlobalContext.getInstance().getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData data = cmb.getPrimaryClip();
		if (data != null && data.getItemCount() > 0) {
			Item item = data.getItemAt(0);
			CharSequence image = item.getText();
			if (!TextUtils.isEmpty(image)) {
				if (image.toString().toLowerCase().endsWith(".gif") ||
						image.toString().toLowerCase().endsWith(".jpg") ||
						image.toString().toLowerCase().endsWith(".jpeg") ||
						image.toString().toLowerCase().endsWith(".bmp") ||
						image.toString().toLowerCase().endsWith(".png")) {
					
					getPublishBean().getParams().addParameter("url", image.toString());
					
					refreshUI();
				}
				else {
					showMessage(R.string.publish_clip_faild);
				}
			}
			else {
				showMessage(R.string.publish_clip_empty);
			}
		}
		else {
			showMessage(R.string.publish_clip_empty);
		}
	}
	
	private void setPicUri(String image) {
		Params extraParams = getPublishBean().getExtras();
		if (extraParams == null) {
			extraParams = new Params();
			getPublishBean().setExtras(extraParams);
		}
        String[] pics = getPublishBean().getPics();
        // TODO 现在只支持一个图
        pics = null;
        if (pics != null) {
            List<String> list = new ArrayList<String>();
            for (String pic : pics) {
                list.add(pic);
            }
            list.add(image);
            pics = list.toArray(new String[0]);
        }
        else {
            pics = new String[]{ image };
        }
        getPublishBean().setPics(pics);

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
        else if (requestCode == 3333 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String[] pics = data.getStringArrayExtra("pics");
                if (pics != null) {
                    getPublishBean().setPics(pics);

                    refreshUI();
                }
            }
        }
		else {
			if (photoChoice != null)
				photoChoice.onActivityResult(requestCode, resultCode, data);
		}
		
	}
	
	public PublishBean getPublishBean() {
		return mBean;
	}
	
    void sendContent(View v) {
        if (txtErrorHint.getVisibility() == View.VISIBLE) {
            showMessage(txtErrorHint.getText().toString());
            return;
        }

        if (checkValid(getPublishBean()))
            send();
    }
	
	/**
	 * 空创建时，初始化Bean
	 * 
	 * @return
	 */
	abstract PublishBean newPublishBean();
	
//	abstract void popOverflowMenu(View v);

	abstract boolean checkValid(PublishBean bean);

}
