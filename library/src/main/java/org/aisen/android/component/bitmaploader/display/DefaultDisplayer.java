package org.aisen.android.component.bitmaploader.display;

import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

public class DefaultDisplayer implements Displayer {

	@Override
	public void loadCompletedisplay(ImageView imageView, BitmapDrawable drawable) {
		imageView.setImageDrawable(drawable);
	}

	@Override
	public void loadFailDisplay(ImageView imageView, BitmapDrawable drawable) {
		imageView.setImageDrawable(drawable);
	}

}
