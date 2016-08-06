package org.aisen.weibo.sina.ui.widget.span;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.style.ImageSpan;

import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.weibo.sina.service.VideoService;

/**
 * Created by wangdan on 16/8/6.
 */
public class WebURLEmotionSpan extends ImageSpan {

    private final Paint mBackgroundPaint;
    private final String mURL;
    private final String mReplaceText;
    private final int mType;
    private int size;
    private boolean clickDown;
    private final int mColor;
    private Drawable mDrawable = null;

    public WebURLEmotionSpan(Context context, Bitmap b, String url, int type, int verticalAlignment) {
        super(context, b, verticalAlignment);

        int[] attrs = new int[] { org.aisen.android.R.attr.colorPrimary };
        Activity activity = BaseActivity.getRunningActivity();
        if (activity != null) {
            TypedArray ta = activity.obtainStyledAttributes(attrs);
            mColor = ta.getColor(0, Color.BLUE);
        }
        else {
            mColor = Color.BLUE;
        }
        mType = type;
        mURL = url;
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(Color.parseColor("#33969696"));
        if (mType == VideoService.TYPE_VIDEO_SINA ||
                mType == VideoService.TYPE_VIDEO_WEIPAI) {
            mReplaceText = "秒拍视频";
        }
        else {
            mReplaceText = "网页链接";
        }
    }

    public void setClickDown(boolean down) {
        clickDown = down;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
//        int bitmapSize = super.getSize(paint, text, start, end, fm);
        int bitmapSize = getDrawable().getBounds().right;

        int textSize = Math.round(paint.measureText(mReplaceText));

        size = bitmapSize + textSize;

        return size;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        Drawable drawable = getDrawable();
        canvas.save();

        if (clickDown) {
            RectF rect = new RectF(x, top, x + size, bottom);
            canvas.drawRect(rect, mBackgroundPaint);
        }

        int oldColor = paint.getColor();
        paint.setColor(mColor);
        Rect bounds = new Rect();
        paint.getTextBounds(mReplaceText, 0, mReplaceText.length(), bounds);
        canvas.drawText(mReplaceText, 0, mReplaceText.length(), x + drawable.getBounds().right, y, paint);

        int transY = bottom - drawable.getBounds().bottom;
        if (mVerticalAlignment == ALIGN_BASELINE) {
            transY -= paint.getFontMetricsInt().descent / 2;
        }

        canvas.translate(x, transY);
        drawable.draw(canvas);
        canvas.restore();
        paint.setColor(oldColor);
    }

    @Override
    public Drawable getDrawable() {
        if (mDrawable != null) {
            return mDrawable;
        }

        Drawable drawable = super.getDrawable();

        if (drawable != null) {
            if (mDrawable == null) {
                mDrawable = drawable;
                ColorStateList mIconTints = new ColorStateList(
                        new int[][]{ { android.R.attr.state_selected },
                                { } },
                        new int[]{ mColor, mColor });
                if (mIconTints != null) {
                    DrawableCompat.setTintList(mDrawable, mIconTints);
                }

                drawable = mDrawable;
            }
        }

        return drawable;
    }
}
