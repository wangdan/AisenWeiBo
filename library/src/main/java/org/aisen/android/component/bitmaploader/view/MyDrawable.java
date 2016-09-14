package org.aisen.android.component.bitmaploader.view;

import java.lang.ref.WeakReference;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import org.aisen.android.component.bitmaploader.BitmapLoader.MyBitmapLoaderTask;
import org.aisen.android.component.bitmaploader.core.ImageConfig;
import org.aisen.android.component.bitmaploader.core.MyBitmap;

public class MyDrawable extends BitmapDrawable {

	private MyBitmap myBitmap;
	private ImageConfig config;
	private WeakReference<MyBitmapLoaderTask> task;

	public MyDrawable(Resources res, MyBitmap myBitmap, ImageConfig config, WeakReference<MyBitmapLoaderTask> task) {
		this(res, myBitmap.getBitmap());
		this.myBitmap = myBitmap;
		this.config = config;
		this.task = task;
	}

	public MyBitmap getMyBitmap() {
		return myBitmap;
	}

	public void setMyBitmap(MyBitmap myBitmap) {
		this.myBitmap = myBitmap;
	}

	public MyDrawable(Resources res, Bitmap bitmap) {
		super(res, bitmap);
	}

	public ImageConfig getConfig() {
		return config;
	}

	public void setConfig(ImageConfig config) {
		this.config = config;
	}

	public WeakReference<MyBitmapLoaderTask> getTask() {
		return task;
	}

	public void setTask(WeakReference<MyBitmapLoaderTask> task) {
		this.task = task;
	}

}
