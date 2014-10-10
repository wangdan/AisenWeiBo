package org.aisen.weibo.sina.support.compress;

import java.io.File;

import org.aisen.weibo.sina.R;
import org.android.loader.core.BitmapDecoder;
import org.android.loader.core.IBitmapCompress;
import org.android.loader.core.ImageConfig;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.m.common.context.GlobalContext;
import com.m.common.utils.BitmapUtil;
import com.m.common.utils.Logger;
import com.m.common.utils.BitmapUtil.BitmapType;
import com.m.common.utils.SystemUtility;

public class TimelineBitmapCompress implements IBitmapCompress {

	@Override
	public Bitmap compress(byte[] bitmapBytes, File file, String url, ImageConfig config, int origW, int origH) throws Exception {
		Logger.v("ATimeline", "压缩小图片");
		
		Bitmap bitmap = null;

		int maxWidth = config.getMaxWidth() == 0 ? SystemUtility.getScreenWidth() : config.getMaxWidth();
		int maxHeight = config.getMaxHeight() == 0 ? SystemUtility.getScreenHeight() : config.getMaxHeight();
		
		// 如果高度比宽度在2倍以上，取高度的一部分
		if (origH * 1.0f / origW > 2) {
			int reqHeight = maxHeight;
			
			// 截取局部图片
			BitmapRegionDecoder bitmapDecoder = BitmapRegionDecoder.newInstance(bitmapBytes, 0, bitmapBytes.length, true);
			Rect rect = new Rect(0, 0, origW, reqHeight);
			bitmap = bitmapDecoder.decodeRegion(rect, null).copy(Config.ARGB_8888, true);
			
		} else {
			bitmap = BitmapDecoder.decodeSampledBitmapFromByte(bitmapBytes, maxWidth, maxHeight);
		}
		
		// 如果是GIF图片
		if (BitmapUtil.getType(bitmapBytes) == BitmapType.gif) {
			// 画GIF图片
			bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
			if (bitmap.getWidth() != maxWidth)
				bitmap = BitmapUtil.zoomBitmap(bitmap, maxWidth);
			Canvas canvas = new Canvas(bitmap);
			Paint paint = new Paint();
			paint.setAlpha(200);
			
			Bitmap gifHint = BitmapFactory.decodeResource(GlobalContext.getInstance().getResources(), R.drawable.ic_play_gif);
			if (bitmap.getWidth() < 500)
				gifHint = BitmapUtil.zoomBitmap(gifHint, Math.round(bitmap.getWidth() * 1.0f / 3));
			
			canvas.drawBitmap(gifHint, (bitmap.getWidth() - gifHint.getWidth()) * 1.0f / 2, 
					(bitmap.getHeight() - gifHint.getHeight()) * 1.0f / 2, paint);
		}
		
		return bitmap;
	}

}
