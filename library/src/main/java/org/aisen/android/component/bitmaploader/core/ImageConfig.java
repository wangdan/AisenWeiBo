package org.aisen.android.component.bitmaploader.core;

import org.aisen.android.component.bitmaploader.display.Displayer;
import org.aisen.android.component.bitmaploader.display.FadeInDisplayer;
import org.aisen.android.component.bitmaploader.download.DownloadProcess;
import org.aisen.android.component.bitmaploader.download.Downloader;
import org.aisen.android.component.bitmaploader.download.WebDownloader;

public class ImageConfig {

	private String id;// 图片缓存id,区分相同图片显示在不同地方的时候作用

	private int maxWidth = 0;// 图片最大宽度

	private int maxHeight = 0;// 图片最大高度
	
	private int corner;// 圆角

	private DownloadProcess progress;

	private Class<? extends Downloader> downloaderClass;

	private Class<? extends IBitmapCompress> bitmapCompress;

	private Displayer displayer;

	private int loadingRes;
	
	private int loadfaildRes;

    private boolean cacheEnable = true;// 是否保存图片文件到缓存文件，当加载自sd卡或者contentprovider时，可配置这个属性

	private boolean compressCacheEnable = true;// 是否保存图片文件到压缩缓存文件，当加载自sd卡或者contentprovider时，可配置这个属性
	
	public ImageConfig() {
		downloaderClass = WebDownloader.class;
		bitmapCompress = BitmapCompress.class;
		displayer = new FadeInDisplayer();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public DownloadProcess getProgress() {
		return progress;
	}

	public void setProgress(DownloadProcess progress) {
		this.progress = progress;
	}

	public Class<? extends Downloader> getDownloaderClass() {
		return downloaderClass;
	}

	public void setDownloaderClass(Class<? extends Downloader> downloaderClass) {
		this.downloaderClass = downloaderClass;
	}

	public Displayer getDisplayer() {
		return displayer;
	}

	public void setDisplayer(Displayer displayer) {
		this.displayer = displayer;
	}

	public Class<? extends IBitmapCompress> getBitmapCompress() {
		return bitmapCompress;
	}

	public void setBitmapCompress(Class<? extends IBitmapCompress> bitmapCompress) {
		this.bitmapCompress = bitmapCompress;
	}

	public int getMaxWidth() {
		return maxWidth;
	}

	public void setMaxWidth(int maxWidth) {
		this.maxWidth = maxWidth;
	}

	public int getMaxHeight() {
		return maxHeight;
	}

	public void setMaxHeight(int maxHeight) {
		this.maxHeight = maxHeight;
	}

	public int getLoadingRes() {
		return loadingRes;
	}

	public void setLoadingRes(int loadingRes) {
		this.loadingRes = loadingRes;
	}

	public int getLoadfaildRes() {
		return loadfaildRes;
	}

	public void setLoadfaildRes(int loadfaildRes) {
		this.loadfaildRes = loadfaildRes;
	}

	public int getCorner() {
		return corner;
	}

	public void setCorner(int corner) {
		this.corner = corner;
	}

    public boolean isCacheEnable() {
        return cacheEnable;
    }

	public void setCacheEnable(boolean cacheEnable) {
        this.cacheEnable = cacheEnable;
		this.compressCacheEnable = cacheEnable;
    }

	public boolean isCompressCacheEnable() {
		return compressCacheEnable;
	}

	public void setCompressCacheEnable(boolean compressCacheEnable) {
		this.compressCacheEnable = compressCacheEnable;
	}
}
