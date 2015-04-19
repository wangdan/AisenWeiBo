package org.aisen.weibo.sina.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;

import org.aisen.weibo.sina.R;

public class GifHintImageView extends ImageView {

	private boolean showGif = false;
    
    private boolean cut = false;
	
	public static Bitmap gif = null;
    public static Bitmap cutBitmap = null;
	
	private Paint paint;

	public GifHintImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public GifHintImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GifHintImageView(Context context) {
		super(context);
	}
	
	public void setHint(String url) {
		showGif = !TextUtils.isEmpty(url) && url.toLowerCase().endsWith(".gif");
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if (getWidth() > 0 && getHeight() > 0) {
            if (showGif) {
                if (gif == null) {
                    gif = BitmapFactory.decodeResource(getResources(), R.drawable.ic_gif);
                    paint = new Paint();
                }

                canvas.drawBitmap(gif, getWidth() - gif.getWidth(), getHeight() - gif.getHeight(), paint);
            }
            else if (cut){
                if (cutBitmap == null) {
                    cutBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_cut);
                    paint = new Paint();
                }

                canvas.drawBitmap(cutBitmap, getWidth() - cutBitmap.getWidth(), getHeight() - cutBitmap.getHeight(), paint);
            }
		}
	}

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);

        if (getParent() instanceof TimelinePicsView)
            ((TimelinePicsView) getParent()).checkPicSize();
    }

    public void setCut(boolean cut) {
        this.cut = cut;
    }

}
