package org.aisen.android.common.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.opengl.GLES10;
import android.provider.MediaStore;
import android.support.annotation.AttrRes;
import android.text.TextUtils;
import android.util.TypedValue;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.component.bitmaploader.BitmapLoader;

import java.io.File;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

public class Utils {

	public static int getBitmapMaxWidthAndMaxHeight() {
		int[] maxSizeArray = new int[1];
		GLES10.glGetIntegerv(GL10.GL_MAX_TEXTURE_SIZE, maxSizeArray, 0);

		if (maxSizeArray[0] == 0) {
			GLES10.glGetIntegerv(GL11.GL_MAX_TEXTURE_SIZE, maxSizeArray, 0);
		}
		return 2048;
	}

	public static int getAppHeight(Activity paramActivity) {
		Rect localRect = new Rect();
		paramActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
		return localRect.height();
	}

	public static Intent getShareIntent(String title, String content, String url) {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra("imageURL", url);

		if (!TextUtils.isEmpty(url)) {
			File file = BitmapLoader.getInstance().getCacheFile(url);
			if (file.exists()) {
				shareIntent.setType("image/*");

				Uri uri = Uri.fromFile(file);
				shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
			}
		}

		if (TextUtils.isEmpty(content)) {
			content = title;
		} else {
			if (!TextUtils.isEmpty(title))
				shareIntent.putExtra(Intent.EXTRA_TITLE, title);
		}

		shareIntent.putExtra(Intent.EXTRA_TEXT, content);

		return shareIntent;
	}

	public static int dip2px(Context context, float dipValue) {
		float reSize = context.getResources().getDisplayMetrics().density;
		return (int) ((dipValue * reSize) + 0.5);
	}

	public static int px2dip(Context context, int pxValue) {
		float reSize = context.getResources().getDisplayMetrics().density;
		return (int) ((pxValue / reSize) + 0.5);
	}

	public static float sp2px(Context context, int spValue) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, context.getResources().getDisplayMetrics());
	}

	public static int length(String paramString) {
		int i = 0;
		for (int j = 0; j < paramString.length(); j++) {
			if (paramString.substring(j, j + 1).matches("[Α-￥]")) {
				i += 2;
			} else {
				i++;
			}
		}

		if (i % 2 > 0) {
			i = 1 + i / 2;
		} else {
			i = i / 2;
		}

		return i;
	}

	public static boolean isIntentSafe(Activity activity, Uri uri) {
		Intent mapCall = new Intent(Intent.ACTION_VIEW, uri);
		PackageManager packageManager = activity.getPackageManager();
		List<ResolveInfo> activities = packageManager.queryIntentActivities(mapCall, 0);
		return activities.size() > 0;
	}

	public static boolean isIntentSafe(Activity activity, Intent intent) {
		PackageManager packageManager = activity.getPackageManager();
		List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
		return activities.size() > 0;
	}

	public static boolean isGooglePlaySafe(Activity activity) {
		Uri uri = Uri.parse("http://play.google.com/store/apps/details?id=com.google.android.gms");
		Intent mapCall = new Intent(Intent.ACTION_VIEW, uri);
		mapCall.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		mapCall.setPackage("com.android.vending");
		PackageManager packageManager = activity.getPackageManager();
		List<ResolveInfo> activities = packageManager.queryIntentActivities(mapCall, 0);
		return activities.size() > 0;
	}

	public static String getLatestCameraPicture(Activity activity) {
		String[] projection = new String[] { MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA,
				MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, MediaStore.Images.ImageColumns.DATE_TAKEN,
				MediaStore.Images.ImageColumns.MIME_TYPE };
		final Cursor cursor = activity.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null,
				MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
		if (cursor.moveToFirst()) {
			String path = cursor.getString(1);
			return path;
		}
		return null;
	}

    public static Drawable resolveDrawable(Context context, @AttrRes int attr) {
        return resolveDrawable(context, attr, null);
    }

    public static Drawable resolveDrawable(Context context, @AttrRes int attr, @SuppressWarnings("SameParameterValue") Drawable fallback) {
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{attr});
        try {
            Drawable d = a.getDrawable(0);
            if (d == null && fallback != null)
                d = fallback;
            return d;
        } finally {
            a.recycle();
        }
    }

}
