package org.aisen.android.component.bitmaploader.display;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.widget.ImageView;

public class FadeInDisplayer implements Displayer {

	@Override
	public void loadCompletedisplay(ImageView imageView, BitmapDrawable drawable) {
		// PhotoView不设置
		if (imageView.getClass().getSimpleName().indexOf("PhotoView") != -1)
			return;

		if (imageView.getDrawable() != null) {
			final TransitionDrawable td = new TransitionDrawable(new Drawable[] { imageView.getDrawable(), drawable });
			imageView.setImageDrawable(td);
			td.startTransition(300);
		}
		else {
			imageView.setImageDrawable(drawable);
		}
	}

	@Override
	public void loadFailDisplay(ImageView imageView, BitmapDrawable drawable) {
		imageView.setImageDrawable(drawable);
	}

}
