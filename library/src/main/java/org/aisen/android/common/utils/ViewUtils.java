package org.aisen.android.common.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.aisen.android.common.context.GlobalContext;

public class ViewUtils {

	public static int getResId(String resName, String defType) {
		try {
			String packageName = GlobalContext.getInstance().getPackageName();
			Resources resources = GlobalContext.getInstance().getPackageManager().getResourcesForApplication(packageName);

			int resId = resources.getIdentifier(resName, defType, packageName);

			return resId;
		} catch (Exception e) {
		}
		return 0;
	}

	public static int getStringResId(String resName) {
		return getResId(resName, "string");
	}

	public static int getDrawableResId(String resName) {
		return getResId(resName, "drawable");
	}

	public static void setTextViewValue(Activity context, int txtId, String content) {
		if (context != null)
			((TextView) context.findViewById(txtId)).setText(content);
	}

	public static void setTextViewValue(View container, int txtId, String content) {
		((TextView) container.findViewById(txtId)).setText(content);
	}

	public static void setTextViewValue(Activity context, View container, int txtId, int contentId) {
		if (context != null)
			((TextView) container.findViewById(txtId)).setText(context.getString(contentId));
	}

	public static void setImgResource(Activity context, int imgId, int sourceId) {
		if (context != null)
			((ImageView) context.findViewById(imgId)).setImageResource(sourceId);
	}

	public static void setImgResource(Activity context, int imgId, Bitmap source) {
		if (context != null)
			((ImageView) context.findViewById(imgId)).setImageBitmap(source);
	}

	public static void setImgResource(View container, int imgId, int sourceId) {
		((ImageView) container.findViewById(imgId)).setImageResource(sourceId);
	}

	public static void setImgResource(View container, int imgId, Bitmap source) {
		((ImageView) container.findViewById(imgId)).setImageBitmap(source);
	}

	public static void showMessage(String message) {
//		MToast.showMessage(message);
		Toast.makeText(GlobalContext.getInstance(), message, Toast.LENGTH_SHORT).show();
	}

	public static void showMessage(int messageId) {
//		MToast.showMessage(messageId);
		Toast.makeText(GlobalContext.getInstance(), messageId, Toast.LENGTH_SHORT).show();
	}

	public static ProgressDialog progressDialog2;

	public static ProgressDialog createProgressDialog(Activity context, String message, Drawable indeterminateDrawable) {
		dismissProgressDialog();
		//Theme.Material.Dialog.Alert
        progressDialog2 = new ProgressDialog(context);
        if (indeterminateDrawable != null)
            progressDialog2.setIndeterminateDrawable(indeterminateDrawable);
		progressDialog2.setMessage(message);
		progressDialog2.setIndeterminate(true);
		progressDialog2.setCancelable(false);
		
		return progressDialog2;
	}
	
	public static void updateProgressDialog(String message) {
		if (progressDialog2 != null && progressDialog2.isShowing()) {
			progressDialog2.setMessage(message);
		}
	}
	
	public static void dismissProgressDialog() {
		if (progressDialog2 != null && progressDialog2.isShowing()) {
			try {
				progressDialog2.dismiss();
			} catch (IllegalArgumentException e) {
			}
			progressDialog2 = null;
		}
	}

}
