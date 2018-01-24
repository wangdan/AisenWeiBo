package org.aisen.weibo.sina.ui.widget.span;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

/**
 * Created by wangdan on 16/8/6.
 */
public class BackgroundImageSpan extends ImageSpan {

    private Paint mPain;

    public BackgroundImageSpan(Context context, Bitmap b, int verticalAlignment) {
        super(context, b, verticalAlignment);

        mPain = new Paint();
        mPain.setColor(Color.parseColor("#33969696"));
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        Drawable b = getDrawable();
        canvas.save();

        canvas.drawRect(x, y - (b.getBounds().bottom - b.getBounds().top), x + (b.getBounds().right - b.getBounds().left), bottom, mPain);

        int transY = bottom - b.getBounds().bottom;
        if (mVerticalAlignment == ALIGN_BASELINE) {
            transY -= paint.getFontMetricsInt().descent;
        }

        canvas.translate(x, transY);
//        b.draw(canvas);
        canvas.restore();

//        super.draw(canvas, text, start, end, x, top, y, bottom, paint);
    }

}
