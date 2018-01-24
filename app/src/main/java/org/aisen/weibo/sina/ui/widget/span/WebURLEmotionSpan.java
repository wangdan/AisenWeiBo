package org.aisen.weibo.sina.ui.widget.span;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Browser;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.View;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.support.textspan.MyURLSpan;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.widget.MToast;
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
        else if (mType == VideoService.TYPE_VIDEO_MEIPAI) {
            mReplaceText = "美拍视频";
        }
        else if (mType == VideoService.TYPE_PHOTO) {
            mReplaceText = "查看图片";
        }
        else {
            mReplaceText = "网页链接";
        }
    }

    public void setClickDown(boolean down) {
        clickDown = down;
    }

    public boolean isClickDown() {
        return clickDown;
    }

    public String getURL() {
        return mURL;
    }

    public void onClick(View widget) {
        Logger.v(MyURLSpan.class.getSimpleName(), String.format("the link(%s) was clicked ", getURL()));

        String url = getURL();
        if (VideoService.isVideo(mType)) {
            if (url.startsWith("http")) {
                url = "aisen_video://" + url;
            }
            else {
                url = url.replaceAll("aisen://", "videoshort://");
            }
        }
        else if (mType == VideoService.TYPE_PHOTO && GlobalContext.getInstance() != null) {
            if (url.startsWith("http")) {
                url = "timeline_pic://" + url;
            }
            else {
                url = url.replaceAll("aisen://", "timeline_pic://");
            }
        }

        Uri uri = Uri.parse(url);
        Context context = widget.getContext();
        if (uri.getScheme().startsWith("http")) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            intent.setData(uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
            context.startActivity(intent);
        }
    }

    public void onLongClick(View widget) {
        Uri data = Uri.parse(getURL());
        if (data != null) {
            String d = data.toString();
            String newValue = "";
            if (d.startsWith("org.aisen.android.ui")) {
                int index = d.lastIndexOf("/");
                newValue = d.substring(index + 1);
            } else if (d.startsWith("http")) {
                newValue = d;
            }
            if (!TextUtils.isEmpty(newValue)) {
                ClipboardManager cm = (ClipboardManager) widget.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(ClipData.newPlainText("ui", newValue));

                MToast.showMessage(widget.getContext(), String.format(widget.getContext().getString(org.aisen.android.R.string.comm_hint_copied), newValue));
            }
        }
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

        int bh = drawable.getBounds().bottom - drawable.getBounds().top;
        int transY = 0;
        if (mType == VideoService.TYPE_VIDEO_NONE) {
            transY = top + ((bottom - top) - bh) / 2;
        }
        else {
            transY = bottom - drawable.getBounds().bottom;
            if (mVerticalAlignment == ALIGN_BASELINE) {
                transY -= paint.getFontMetricsInt().descent;
            }
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
