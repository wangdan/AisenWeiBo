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
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.ActivityHelper;
import org.aisen.android.common.utils.BitmapUtil;
import org.aisen.android.common.utils.FileUtils;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.PhotoChoice;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.common.utils.Utils;
import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.component.bitmaploader.core.BitmapDecoder;
import org.aisen.android.component.bitmaploader.core.ImageConfig;
import org.aisen.android.component.bitmaploader.download.ContentProviderDownloader;
import org.aisen.android.component.bitmaploader.download.DownloadProcess;
import org.aisen.android.component.bitmaploader.download.SdcardDownloader;
import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.action.IAction;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.service.PublishService;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;
import org.aisen.weibo.sina.support.bean.Emotion;
import org.aisen.weibo.sina.support.bean.PublishBean;
import org.aisen.weibo.sina.support.bean.PublishBean.PublishStatus;
import org.aisen.weibo.sina.support.compress.TimelineBitmapCompress;
import org.aisen.weibo.sina.support.permissions.SdcardPermissionAction;
import org.aisen.weibo.sina.support.sqlit.EmotionsDB;
import org.aisen.weibo.sina.support.sqlit.PublishDB;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.ui.fragment.picturepick.PicturePickFragment;
import org.aisen.weibo.sina.ui.fragment.publish.EmotionFragment.OnEmotionSelectedListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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

	// 支持多图
	@ViewInject(id = R.id.layPicContainer)
	LinearLayout layPicContainer;
	@ViewInject(id = R.id.scrollPicContainer)
	HorizontalScrollView scrollPicContainer;
	
	private final LayoutTransition transitioner = new LayoutTransition();
	
	private EmotionFragment emotionFragment;
	private PhotoChoice photoChoice;
	
	private PublishBean mBean;
	
	private int emotionHeight;

	private String tempFilePath;
	
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

		String albumPath = SystemUtils.getSdcardPath() + File.separator + "/DCIM/Camera/";
		File albumFile = new File(albumPath);
		if (!albumFile.exists())
			albumFile.mkdirs();
		photoChoice = new PhotoChoice(getActivity(), APublishFragment.this, albumPath);
		if (savedInstanceState != null)
			tempFilePath = savedInstanceState.getString("tempFilePath");
		photoChoice.setFileName(tempFilePath);
		photoChoice.setMode(PhotoChoice.PhotoChoiceMode.uriType);
	}
	
	// 如果有照片了，也显示黑色文字
	protected boolean configWhite() {
		return getPublishBean().getExtras() != null && getPublishBean().getPics() != null && getPublishBean().getPics().length > 0;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putSerializable("bean", mBean);
		if (!TextUtils.isEmpty(tempFilePath))
			outState.putString("tempFilePath", tempFilePath);
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
//				getPublishBean().setText(getString(R.string.publish_share_pic) + " ");
			}

			// 修改为支持多图
			if (true) {
				layImageCover.setVisibility(View.GONE);
				picShow.setVisibility(View.GONE);
			}

			ImageConfig config = new ImageConfig();
			config.setLoadfaildRes(R.drawable.bg_timeline_loading);
			config.setLoadingRes(R.drawable.bg_timeline_loading);
			config.setMaxWidth(SystemUtils.getScreenWidth(getActivity()));
			config.setMaxHeight(SystemUtils.getScreenHeight(getActivity()) / 2);
			config.setBitmapCompress(TimelineBitmapCompress.class);
			config.setProgress(new PublishDownloadProcess());

			if (layPicContainer != null) {
				layPicContainer.removeAllViews();
				scrollPicContainer.setVisibility(View.VISIBLE);
			}
			for (String path : images) {
				View itemView = View.inflate(getActivity(), R.layout.item_publish_pic, null);
				ImageView img = (ImageView) itemView.findViewById(R.id.img);

				itemView.setTag(path);
				itemView.setOnClickListener(onPictureClickListener);

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
					SystemUtils.scanPhoto(getActivity(), new File(path));
					config.setDownloaderClass(SdcardDownloader.class);
				}

				BitmapLoader.getInstance().display(this, path, img, config);

				if (layPicContainer != null) {
					layPicContainer.addView(itemView,
							new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
				}
			}
		}
		else {
			if (scrollPicContainer != null)
				scrollPicContainer.setVisibility(View.GONE);
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

	// 暂时只支持删除，不支持预览
	View.OnClickListener onPictureClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			final String path = v.getTag().toString();

			new AlertDialogWrapper.Builder(getActivity())
					.setMessage(R.string.publish_delete_pic)
					.setNegativeButton(R.string.cancel, null)
					.setPositiveButton(R.string.confirm, new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							String[] pathArr = getPublishBean().getPics();
							List<String> pathList = new ArrayList<String>();
							Collections.addAll(pathList, pathArr);
							pathList.remove(path);
							getPublishBean().setPics(pathList.toArray(new String[0]));

							for (int i = 0; i < layPicContainer.getChildCount(); i++) {
								View view = layPicContainer.getChildAt(i);

								if (view.getTag().toString().equals(path)) {
									layPicContainer.removeView(view);

									break;
								}
							}

							if (getPublishBean().getPics() == null || getPublishBean().getPics().length == 0) {
								getPublishBean().setPics(null);

								scrollPicContainer.setVisibility(View.GONE);
							}
						}

					})
					.show();
		}

	};
	
	protected void send() {
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
	}
	
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
		if (false && getPublishBean().getExtras() != null &&
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
		new IAction(getActivity(), new SdcardPermissionAction(((BaseActivity) getActivity()), null)) {

			@Override
			public void doAction() {
				new AlertDialogWrapper.Builder(getActivity())
						.setItems(R.array.publish_pic, new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								switch (which) {
									// 相册
									case 0:
										String[] images = getPublishBean().getPics();
										PicturePickFragment.launch(APublishFragment.this, 9, images, 3333);
										break;
									// 拍照
									case 1:
										tempFilePath = String.format("%s.jpg", String.valueOf(System.currentTimeMillis() / 1000));
										photoChoice.setFileName(tempFilePath);
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

		}.run();
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

	            SystemUtils.showKeyBoard(getActivity(), editContent);
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

        int statusBarHeight = SystemUtils.getStatusBarHeight(getActivity());
		emotionHeight = SystemUtils.getKeyboardHeight(getActivity());

        SystemUtils.hideSoftInput(getActivity(), editContent);
        layEmotion.getLayoutParams().height = emotionHeight;
        layEmotion.setVisibility(View.VISIBLE);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // 2015 05-09 在5.0有navigationbar的手机，高度高了一个statusBar
        int lockHeight = SystemUtils.getAppContentHeight(getActivity());
//        if (Build.VERSION.SDK_INT < 19)
//            lockHeight = lockHeight - statusBarHeight;
        lockContainerHeight(lockHeight);
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

														PublishDB.addPublish(getPublishBean(), AppContext.getAccount().getUser());

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
		Logger.e(uri.toString());

		// 当拍摄照片时，提示是否设置旋转90度
		if (!AppSettings.isRotatePic() && !ActivityHelper.getBooleanShareData(GlobalContext.getInstance(), "RotatePicNoRemind", false)) {
			new AlertDialogWrapper.Builder(getActivity()).setTitle(R.string.remind)
									.setMessage(R.string.publish_rotate_remind)
									.setNegativeButton(R.string.donnot_remind, new OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int which) {
											ActivityHelper.putBooleanShareData(GlobalContext.getInstance(), "RotatePicNoRemind", true);
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
						Bitmap bitmap = BitmapDecoder.decodeSampledBitmapFromFile(path, SystemUtils.getScreenHeight(getActivity()), SystemUtils.getScreenHeight(getActivity()));
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
				}
				
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
		new WorkTask<Void, Void, String>() {

			@Override
			public String workInBackground(Void... params) throws TaskException {
				String path = "";
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
			}
			
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
				Logger.w(pics);
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
