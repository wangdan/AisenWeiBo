package org.aisen.android.common.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import org.aisen.android.common.context.GlobalContext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class BitmapUtil {

	private static final String TAG = BitmapUtil.class.getSimpleName();

	public enum BitmapType {
		gif, jpg, png, bmp
	}

	public static BitmapType getType(byte[] imageData) {
		// 是否是jpg
		if (imageData.length >= 2) {
			if ((imageData[0] & 0xFF) == 0xFF && (imageData[1] & 0xD8) == 0xD8) {
				return BitmapType.jpg;
			}
		}

		// 是否是png
		if (imageData.length >= 8) {
			short[] png = new short[] { 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
			for (int i = 0; i < png.length; i++) {
				if ((imageData[i] & png[i]) != png[i]) {
					break;
				}
				if (i == png.length - 1) {
					return BitmapType.png;
				}
			}
		}

		// 是否是gif
		if (imageData.length >= 5) {
			short[] png = new short[] { 0x47, 0x49, 0x46, 0x38, 0x39, 0x61 };
			short fourIndex = 0x37;
			for (int i = 0; i < png.length; i++) {
				if ((imageData[i] & png[i]) != png[i] || (i == 4 && (imageData[i] & fourIndex) == fourIndex)) {
					break;
				}
				if (i == png.length - 1) {
					return BitmapType.gif;
				}
			}
		}

		// 是否是bmp
		if (imageData.length >= 2) {
			if ((imageData[0] & 0x42) == 0x42 && (imageData[1] & 0x4D) == 0x4D) {
				return BitmapType.bmp;
			}
		}

		return BitmapType.jpg;
	}

	/**
	 * 获得图片的后缀
	 * 
	 * @param type
	 * @return
	 */
	public static String getSuffix(BitmapType type) {
		switch (type) {
		case gif:
			return ".gif";
		case jpg:
			return ".jpg";
		case bmp:
			return ".bmp";
		case png:
			return ".png";
		default:
			return ".jpg";
		}
	}

	public static Bitmap setImageCorner(Bitmap source, float roundPx) {

		Bitmap result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(result);

		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, source.getWidth(), source.getHeight());
		final RectF rectF = new RectF(rect);

		paint.setAntiAlias(true);
		paint.setColor(Color.RED);
		paint.setXfermode(null);
		paint.setAlpha(255);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(source, 0, 0, paint);

		source.recycle();
		return result;
	}

	public static Bitmap zoomBitmap(Bitmap source, int width) {
		Matrix matrix = new Matrix();
		float scale = width * 1.0f / source.getWidth();
		matrix.setScale(scale, scale);
		Bitmap result = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
		try {
			Logger.d(
					TAG,
					String.format("zoom bitmap, source(%d,%d) result(%d,%d)", source.getWidth(), source.getHeight(), result.getWidth(),
							result.getHeight()));
		} catch (Exception e) {
		}
//		source.recycle();
		return result;
	}

    public static Bitmap decodeRegion(byte[] bytes, int width, int height) {
        try {
            BitmapRegionDecoder bitmapDecoder = BitmapRegionDecoder.newInstance(bytes, 0, bytes.length, true);
            Rect rect = new Rect(0, 0, width, height);
            return bitmapDecoder.decodeRegion(rect, null).copy(Bitmap.Config.ARGB_8888, true);
        } catch (Exception e) {
        }
        return null;
    }
	
	public static Bitmap rotateBitmap(Bitmap source, int degrees) {
		Matrix matrix = new Matrix();
		matrix.setRotate(degrees, source.getWidth() / 2, source.getHeight() / 2);
		Bitmap result = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
		try {
			Logger.d(
					TAG,
					String.format("rotate bitmap, source(%d,%d) result(%d,%d)", source.getWidth(), source.getHeight(), result.getWidth(),
							result.getHeight()));
		} catch (Exception e) {
		}
		source.recycle();
		return result;
	}

	public static Bitmap getFromDrawableAsBitmap(Context context, String resName) {
		try {
			String packageName = context.getPackageName();
			Resources resources = context.getPackageManager().getResourcesForApplication(packageName);

			int resId = resources.getIdentifier(resName, "drawable", packageName);

			try {
				if (resId != 0)
					return BitmapFactory.decodeResource(context.getResources(), resId);
				else
					Logger.e(String.format("配置的图片ResourceId=%s不存在", resName));
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 目前只支持JPG图片
	 * 
	 * @param resName
	 * @return
	 */
	public static InputStream getFromDrawableAsStream(Context context, String resName) {
		try {
			String packageName = context.getPackageName();
			Resources resources = context.getPackageManager().getResourcesForApplication(packageName);

			int resId = resources.getIdentifier(resName, "drawable", packageName);

			if (resId != 0)
				return Bitmap2InputStream(BitmapFactory.decodeResource(resources, resId));
			else
				Logger.e(String.format("配置的图片ResourceId=%s不存在", resName));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	// 将byte[]转换成InputStream
	public static InputStream Byte2InputStream(byte[] b) {
		ByteArrayInputStream bais = new ByteArrayInputStream(b);
		return bais;
	}

	// 将InputStream转换成byte[]
	public static byte[] InputStream2Bytes(InputStream is) {
		String str = "";
		byte[] readByte = new byte[1024];
		@SuppressWarnings("unused")
		int readCount = -1;
		try {
			while ((readCount = is.read(readByte, 0, 1024)) != -1) {
				str += new String(readByte).trim();
			}
			return str.getBytes();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// 将Bitmap转换成InputStream
	public static InputStream Bitmap2InputStream(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		InputStream is = new ByteArrayInputStream(baos.toByteArray());
		return is;
	}

	// 将Bitmap转换成InputStream
	public static InputStream Bitmap2InputStream(Bitmap bm, int quality) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, quality, baos);
		InputStream is = new ByteArrayInputStream(baos.toByteArray());
		return is;
	}

	// 将InputStream转换成Bitmap
	public static Bitmap InputStream2Bitmap(InputStream is) {
		return BitmapFactory.decodeStream(is);
	}

	// Drawable转换成InputStream
	public static InputStream Drawable2InputStream(Drawable d) {
		Bitmap bitmap = drawable2Bitmap(d);
		return Bitmap2InputStream(bitmap);
	}

	// InputStream转换成Drawable
	public static Drawable InputStream2Drawable(Context context, InputStream is) {
		Bitmap bitmap = InputStream2Bitmap(is);
		return bitmap2Drawable(context, bitmap);
	}

	// Drawable转换成byte[]
	public static byte[] Drawable2Bytes(Drawable d) {
		Bitmap bitmap = drawable2Bitmap(d);
		return Bitmap2Bytes(bitmap);
	}

	// byte[]转换成Drawable
	public static Drawable Bytes2Drawable(Context context, byte[] b) {
		Bitmap bitmap = Bytes2Bitmap(b);
		return bitmap2Drawable(context, bitmap);
	}

	// Bitmap转换成byte[]
	public static byte[] Bitmap2Bytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	// byte[]转换成Bitmap
	public static Bitmap Bytes2Bitmap(byte[] b) {
		if (b.length != 0) {
			return BitmapFactory.decodeByteArray(b, 0, b.length);
		}
		return null;
	}

	// Drawable转换成Bitmap
	public static Bitmap drawable2Bitmap(Drawable drawable) {
		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
				drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		return bitmap;
	}

	// Bitmap转换成Drawable
	public static Drawable bitmap2Drawable(Context context, Bitmap bitmap) {
		BitmapDrawable bd = new BitmapDrawable(context.getResources(), bitmap);
		Drawable d = (Drawable) bd;
		return d;
	}

}
