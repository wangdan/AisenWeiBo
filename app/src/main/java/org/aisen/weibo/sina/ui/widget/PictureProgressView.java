package org.aisen.weibo.sina.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class PictureProgressView extends View {

	private int progress;
	private Rect mRect;
	private Paint mPaint;
	
	public PictureProgressView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public PictureProgressView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PictureProgressView(Context context) {
		super(context);
	}
	
	public void setProgress(int progress) {
		this.progress = progress;
		
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		canvas.drawColor(Color.parseColor("#ff000000"));
		
		if (mRect == null)
			mRect = new Rect();
		if (mPaint == null) {
			mPaint = new Paint();
			mPaint.setColor(Color.parseColor("#3b3b3b"));
		}
		
		mRect.left = 0;
		mRect.right = getWidth();
		mRect.top = 0;
		mRect.bottom = Math.round(getHeight() * progress * 1.0f / 100);
		
		canvas.drawRect(mRect, mPaint);
	}

}
