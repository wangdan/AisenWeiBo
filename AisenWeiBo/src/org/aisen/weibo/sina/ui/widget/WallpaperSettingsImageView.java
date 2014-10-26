package org.aisen.weibo.sina.ui.widget;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.bean.WallpaperBean;
import org.android.loader.BitmapLoader;
import org.android.loader.core.ImageConfig;
import org.android.loader.download.AssetsDownloader;
import org.android.loader.download.SdcardDownloader;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

public class WallpaperSettingsImageView extends ImageView {

	private WallpaperBean wallpaper;
	
	private boolean set;
	
	private ImageConfig imageConfig;
	
	public WallpaperSettingsImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public WallpaperSettingsImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public WallpaperSettingsImageView(Context context) {
		super(context);
	}
	
	public void setWallpaper(WallpaperBean wallpaper) {
		this.wallpaper = wallpaper;
		
		set = false;
		
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if (!set && getWidth() > 0 && getHeight() > 0 && wallpaper != null) {
			set = true;
			
			if (imageConfig == null)
				imageConfig = new ImageConfig();
			
			if ("1".equals(wallpaper.getType())) {
				imageConfig.setId("def_wallpaper");
				imageConfig.setDownloaderClass(AssetsDownloader.class);
				imageConfig.setLoadingBitmapRes(R.drawable.bg_timeline_loading);
				imageConfig.setLoadfaildBitmapRes(R.drawable.bg_timeline_loading);
				imageConfig.setMaxWidth(getWidth());
				imageConfig.setMaxHeight(getHeight());
				
				BitmapLoader.getInstance().display(null, wallpaper.getPath(), this, imageConfig);
			}
			else if ("100".equals(wallpaper.getType())) {
				imageConfig.setId("github_def_wallpaper");
				imageConfig.setDownloaderClass(SdcardDownloader.class);
				imageConfig.setLoadingBitmapRes(R.drawable.bg_timeline_loading);
				imageConfig.setLoadfaildBitmapRes(R.drawable.bg_timeline_loading);
				imageConfig.setMaxWidth(getWidth());
				imageConfig.setMaxHeight(getHeight());
				
				BitmapLoader.getInstance().display(null, wallpaper.getPath(), this, imageConfig);
			}
		}
	}

}
