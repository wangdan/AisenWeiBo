package org.aisen.weibo.sina.support.utils;

import org.aisen.weibo.sina.R;
import org.android.loader.core.ImageConfig;

public class ImageConfigUtils {

	public static ImageConfig getPhotoConfig() {
		ImageConfig config = new ImageConfig();
		
		config.setLoadingBitmapRes(R.drawable.user_placeholder);
		config.setLoadfaildBitmapRes(R.drawable.user_placeholder);
		
		return config;
	}
	
	public static ImageConfig getLargePhotoConfig() {
		ImageConfig config = new ImageConfig();
		
		config.setId("large");
		config.setLoadingBitmapRes(R.drawable.user_placeholder);
		config.setLoadfaildBitmapRes(R.drawable.user_placeholder);
//		config.setMaxWidth(GlobalContext.getInstance().getResources().getDimensionPixelSize(R.dimen.menu_icon));
		
		return config;
	}
	
}
