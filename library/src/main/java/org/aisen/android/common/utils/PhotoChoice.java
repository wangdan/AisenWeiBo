package org.aisen.android.common.utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.provider.MediaStore;

import org.aisen.android.component.bitmaploader.core.BitmapDecoder;
import org.aisen.android.ui.fragment.ABaseFragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class PhotoChoice {

	public static final int PHONE_IMAGE_REQUEST_CODE = 8888;
	public static final int CAMERA_IMAGE_REQUEST_CODE = 9999;

	public int picMaxDecodeWidth;
	public int picMaxDecodeHeight;

	private Activity mContext;
	private static final Object[][] options = { { null, "本地相册" }, { null, "相机拍摄" } };
	/**
	 * 临时文件目录
	 */
	private String tempFilePath = "/sdcard/photoChoice/";
	/**
	 * 临时文件的URI
	 */
	private Uri tempFileUri;
	/**
	 * 临时文件名
	 */
	private String tempFileName = "photodata.o";

	private PhotoChoiceMode mode;

	private PhotoChoiceListener choiceListener;

	/**
	 * 图片选择模式<br/>
	 * 如果是bitmapType，则图片解析最大尺寸为(screenWidth * 2) * ( screenHeight * 2)
	 * 
	 * @author wangdan
	 * 
	 */
	public enum PhotoChoiceMode {
		bitmapType, byteType, uriType
	}

	private PhotoChoice(Activity context) {
		this.mContext = context;
		picMaxDecodeWidth = SystemUtils.getScreenWidth(mContext) * 5;
		picMaxDecodeHeight = SystemUtils.getScreenHeight(mContext) * 3;
	}

	public PhotoChoice(Activity context, PhotoChoiceListener choiceListener) {
		this(context);
		this.choiceListener = choiceListener;
		setPhotoChoice();
	}

	public PhotoChoice(Activity context, PhotoChoiceListener choiceListener, String tempFilePath) {
		this(context);
		this.mContext = context;
		this.tempFilePath = tempFilePath;
		this.choiceListener = choiceListener;
		setPhotoChoice();
	}

	private void setPhotoChoice() {
		// 默认是URI模式
		setMode(PhotoChoiceMode.uriType);
		File file = new File(tempFilePath);
		if (!file.exists()) {
			file.mkdirs();
		}
		tempFileUri = Uri.fromFile(new File(tempFilePath + tempFileName));
	}

	public void showChoice(final ABaseFragment fragment) {
//		List<Menu> menus = new ArrayList<Menu>();
//		menus.add(new Menu("0", "本地相册", null));
//		menus.add(new Menu("1", "启动相机", null));
//		new CustomDialogBuilder(mContext).setTitle("请选择").setMenus(menus).setMenuCallback(new MenuCallback() {
//
//			@Override
//			public void onMenuSelected(int position, Menu menu) {
//				start(fragment, position);
//			}
//		}).setPositiveButton("取消", null).show();
	}

	public void start(ABaseFragment fragment, int position) {
		Intent intent = null;
		switch (position) {
		case 0:
			intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
			if (fragment == null)
				mContext.startActivityForResult(Intent.createChooser(intent, "请选择文件..."), PHONE_IMAGE_REQUEST_CODE);
			else
				fragment.startActivityForResult(Intent.createChooser(intent, "请选择文件..."), PHONE_IMAGE_REQUEST_CODE);
			break;
		case 1:
			// 准备启动相机
			intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			// 设置照片缓存路径
			intent.putExtra(MediaStore.EXTRA_OUTPUT, tempFileUri);
			// 等待结果返回
			if (fragment == null)
				mContext.startActivityForResult(intent, CAMERA_IMAGE_REQUEST_CODE);
			else
				fragment.startActivityForResult(intent, CAMERA_IMAGE_REQUEST_CODE);
			break;
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			// 本地图片
			if (requestCode == PHONE_IMAGE_REQUEST_CODE) {
				switch (mode) {
				case bitmapType:
					Bitmap bitmap = null;
					try {
						InputStream is = mContext.getContentResolver().openInputStream(data.getData());
						byte[] datas = FileUtils.readStreamToBytes(is);
						bitmap = BitmapDecoder.decodeSampledBitmapFromByte(mContext, datas);
					} catch (Exception e) {
					}
					choiceListener.choiceBitmap(bitmap);
					break;
				case uriType:
					choiceListener.choieUri(data.getData(), requestCode);
					break;
				case byteType:
					try {
						InputStream is = mContext.getContentResolver().openInputStream(data.getData());
						choiceListener.choiceByte(FileUtils.readStreamToBytes(is));
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				}
			}
			// 相机拍摄
			else if (requestCode == CAMERA_IMAGE_REQUEST_CODE) {
				switch (mode) {
				case bitmapType:
					Bitmap bitmap = null;
					try {
						byte[] datas = FileUtils.readStreamToBytes(new FileInputStream(tempFilePath + tempFileName));
						bitmap = BitmapDecoder.decodeSampledBitmapFromByte(mContext, datas);
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						// 删除原文件
						deleteTempFile();
					}
					choiceListener.choiceBitmap(bitmap);
					break;
				case uriType:
					choiceListener.choieUri(tempFileUri, requestCode);
					break;
				case byteType:
					try {
						choiceListener.choiceByte(FileUtils.readFileToBytes(new File(tempFilePath + tempFileName)));
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						// 删除原文件
						deleteTempFile();
					}
					break;
				}
			}
		} else {
			choiceListener.unChoice();
		}
	};

	public void setFileName(String fileName) {
		this.tempFileName = fileName;
		tempFileUri = Uri.fromFile(new File(tempFilePath + tempFileName));
	}
	
	public void deleteTempFile() {
		File file = new File(tempFilePath + tempFileName);
		if (file.exists()) {
			file.delete();
		}
	}

	public byte[] parseBitmap(Bitmap bitmap) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.JPEG, 100, out);
		return out.toByteArray();
	}

	public interface PhotoChoiceListener {

		public void choiceByte(byte[] datas);

		public void choiceBitmap(Bitmap bitmap);

		public void choieUri(Uri uri, int request);

		public void unChoice();

	}

	public PhotoChoiceMode getMode() {
		return mode;
	}

	public PhotoChoice setMode(PhotoChoiceMode mode) {
		this.mode = mode;
		return this;
	}

	// private void deleteOrigFile() {
	//
	// try {
	// File root = getFilesDir();
	// File file = new File(root, "photodata");
	// String[] projection = { MediaStore.Images.Media._ID,
	// MediaStore.Images.Media.DATA };
	// Cursor cursor = MediaStore.Images.Media.query(getContentResolver(),
	// MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
	// null, MediaStore.Images.Media.DATE_ADDED);
	// if (cursor.moveToLast()) {
	// int columnIndex = cursor
	// .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	//
	// String imagePath = cursor.getString(columnIndex);
	// int idColumnIndex = cursor
	// .getColumnIndexOrThrow(MediaStore.Images.Media._ID);
	// int id = cursor.getInt(idColumnIndex);
	// Uri origFileUri = ContentUris.withAppendedId(
	// MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
	// cursor.close();
	//
	// if (imagePath != null && !"".equals(imagePath)) {
	// try {
	// if (MD5Compare.getHash(file, "MD5").equals(
	// MD5Compare.getHash(imagePath, "MD5"))) {
	// Log.d("imagePath", imagePath);
	// Log.d("origFileUri", origFileUri.toString());
	// getContentResolver()
	// .delete(origFileUri, null, null);
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// } finally {
	// deleteFile(fileName);
	// }
	//
	// }

}
