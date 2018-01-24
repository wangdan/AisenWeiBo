package org.aisen.android.component.bitmaploader;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.ImageView;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.KeyGenerator;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.component.bitmaploader.core.BitmapCache;
import org.aisen.android.component.bitmaploader.core.BitmapOwner;
import org.aisen.android.component.bitmaploader.core.BitmapProcess;
import org.aisen.android.component.bitmaploader.core.BitmapTask;
import org.aisen.android.component.bitmaploader.core.ImageConfig;
import org.aisen.android.component.bitmaploader.core.MyBitmap;
import org.aisen.android.component.bitmaploader.view.MyDrawable;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author Jeff.Wang
 *
 * @date 2014年9月17日
 */
public class BitmapLoader {

	public static final String TAG = "BitmapLoader";

	private Map<WeakReference<BitmapOwner>, List<WeakReference<MyBitmapLoaderTask>>> ownerMap;
	private Map<String, WeakReference<MyBitmapLoaderTask>> taskCache;

	private String imageCachePath;// 图片缓存路径

	private BitmapProcess bitmapProcess;

	private BitmapCache mImageCache;// 图片缓存

	private Context mContext;
	
	private BitmapLoader(Context mContext) {
		this.mContext = mContext;
	}

	private static BitmapLoader imageLoader;

	static BitmapLoader newInstance(Context mContext) {
		imageLoader = new BitmapLoader(mContext);
		return imageLoader;
	}

	public static BitmapLoader newInstance(Context mContext, String imageCachePath) {
		BitmapLoader loader = newInstance(mContext);
		if (TextUtils.isEmpty(imageCachePath))
			imageCachePath = SystemUtils.getSdcardPath() + File.separator + "aisenImage" + File.separator;

		loader.imageCachePath = imageCachePath;
		loader.init();
		
		return loader;
	}
	
	public static BitmapLoader getInstance() {
		return imageLoader;
	}

	public void destory() {
	}

	/**
	 * 在设置完参数属性后，必须调用此方法进行初始化
	 * 
	 * @return
	 */
	BitmapLoader init() {

		ownerMap = new HashMap<WeakReference<BitmapOwner>, List<WeakReference<MyBitmapLoaderTask>>>();
		taskCache = new HashMap<String, WeakReference<MyBitmapLoaderTask>>();

		int memCacheSize = 1024 * 1024 * ((ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
		memCacheSize = memCacheSize / 3;

		Logger.i(TAG, "memCacheSize = " + (memCacheSize / 1024 / 1024) + "MB");

		bitmapProcess = new BitmapProcess(imageCachePath);

		mImageCache = new BitmapCache(memCacheSize);

		return this;
	}
	
	public void display(BitmapOwner owner, String url, ImageView imageView, ImageConfig imageConfig) {
		if (TextUtils.isEmpty(url)) {
			setImageFaild(imageView, imageConfig);
            return;
        }

		if (bitmapHasBeenSet(imageView, url))
			return;

		MyBitmap myBitmap = mImageCache.getBitmapFromMemCache(url, imageConfig);
		// 内存缓存存在图片，且未释放
		if (myBitmap != null && imageView != null) {
			imageView.setImageDrawable(new MyDrawable(mContext.getResources(), myBitmap, imageConfig, null));
		}
		// 开启线程拉取图片
		else {
			// 线程不存在，获取已经存在的线程不是加载当前的url，如果存在线程，则停止之前存在的线程
			if (!checkTaskExistAndRunning(url, imageView, imageConfig)) {
				// 2014-08-29 修改为当视图在滚动的时候，不加载图片
				boolean canLoad = owner == null || owner.canDisplay() ? true : false;
				
				if (!canLoad) {
					Logger.d(TAG, "视图在滚动，显示默认图片");
					
					setImageLoading(imageView, null, imageConfig);
				}
				else {
					// 开启新的线程加载图片
					MyBitmapLoaderTask newTask = new MyBitmapLoaderTask(url, imageView, this, imageConfig);
					WeakReference<MyBitmapLoaderTask> taskReference = new WeakReference<MyBitmapLoaderTask>(newTask);
					taskCache.put(KeyGenerator.generateMD5(getKeyByConfig(url, imageConfig)), taskReference);
					
					setImageLoading(imageView, url, imageConfig);
					
					newTask.executrOnImageExecutor();
					
					// 添加到fragment当中，当fragment在Destory的时候，清除task列表
					if (owner != null)
						getTaskCache(owner).add(new WeakReference<MyBitmapLoaderTask>(newTask));
					
					newTask = null;
				}
			}
		}
	}

	/**
	 * 当前加载的图片已经绑定在ImageView上了
	 * 
	 * @param imageView
	 * @param url
	 * @return
	 */
	public boolean bitmapHasBeenSet(ImageView imageView, String url) {
		if (imageView != null) {
			Drawable drawable = imageView.getDrawable();
			if (drawable != null) {
				if (drawable instanceof TransitionDrawable) {
					TransitionDrawable transitionDrawable = (TransitionDrawable) drawable;
					drawable = transitionDrawable.getDrawable(1);
				}
				if (drawable instanceof MyDrawable) {
					MyDrawable myDrawable = (MyDrawable) drawable; 
					if (myDrawable.getMyBitmap() != null && url.equals(myDrawable.getMyBitmap().getUrl())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public BitmapCache getImageCache() {
		return mImageCache;
	}

    public BitmapProcess getBitmapProcess() {
        return bitmapProcess;
    }

	private List<WeakReference<MyBitmapLoaderTask>> getTaskCache(BitmapOwner owner) {
		List<WeakReference<MyBitmapLoaderTask>> taskWorkInOwner = null;

		Set<WeakReference<BitmapOwner>> set = ownerMap.keySet();
		for (WeakReference<BitmapOwner> key : set)
			if (key != null && key.get() == owner)
				taskWorkInOwner = ownerMap.get(key);

		if (taskWorkInOwner == null) {
			taskWorkInOwner = new ArrayList<WeakReference<MyBitmapLoaderTask>>();
			ownerMap.put(new WeakReference<BitmapOwner>(owner), taskWorkInOwner);
		}

		return taskWorkInOwner;
	}

	private boolean checkTaskExistAndRunning(String url, ImageView imageView, ImageConfig config) {
		if (imageView == null)
			return false;

		// 线程缓存中是否已经有线程在运行，如果有，就将ImageView关联Task
		WeakReference<MyBitmapLoaderTask> loader = taskCache.get(KeyGenerator.generateMD5(BitmapLoader.getKeyByConfig(url, config)));
		if (loader != null) {
			MyBitmapLoaderTask task = loader.get();
			if (task != null) {
				if (!task.isCancelled() && !task.isCompleted && task.imageUrl.equals(url)) {
					try {
						setImageLoading(imageView, url, config);
						task.imageViewsRef.add(new WeakReference<ImageView>(imageView));
						Logger.d(TAG, String.format("ImageView加载的图片已有线程在运行，url = %s", url));
					} catch (OutOfMemoryError e) {
						e.printStackTrace();
					}
					return true;
				}
			} else {
				taskCache.remove(KeyGenerator.generateMD5(BitmapLoader.getKeyByConfig(url, config)));
			}
		}
		// 还没有线程，判断ImageView是否已经绑定了线程，如果绑定了，就将已存在的线程cancel(false)掉
		MyBitmapLoaderTask task = getWorkingTask(imageView);
		if (task != null && !task.imageUrl.equals(url) && task.imageViewsRef.size() == 1) {
			Logger.d(TAG, String.format("停止一个图片加载，如果还没有运行 url = %s", url));
			task.cancel(false);
		}
		return false;
	}

	public void cancelPotentialTask(BitmapOwner owner) {
		if (owner == null)
			return;

		List<WeakReference<MyBitmapLoaderTask>> taskWorkInFragment = getTaskCache(owner);
		if (taskWorkInFragment != null)
			for (WeakReference<MyBitmapLoaderTask> taskRef : taskWorkInFragment) {
				MyBitmapLoaderTask task = taskRef.get();
				if (task != null) {
					task.cancel(true);
					Logger.d(TAG, String.format("fragemnt销毁，停止线程 url = %s", task.imageUrl));
				}
			}

        for (WeakReference<BitmapOwner> key : ownerMap.keySet())
            if (key != null && key.get() == owner) {
                ownerMap.remove(key);

                Logger.w(TAG, "移除一个owner --->" + owner.toString());

                break;
            }

        Logger.w(TAG, "owner %d 个" + ownerMap.size());
	}

	private MyBitmapLoaderTask getWorkingTask(ImageView imageView) {
		if (imageView == null)
			return null;

		Drawable drawable = imageView.getDrawable();
		if (drawable != null && drawable instanceof MyDrawable) {
			WeakReference<MyBitmapLoaderTask> loader = ((MyDrawable) drawable).getTask();
			if (loader != null && loader.get() != null)
				return loader.get();
		}
		return null;
	}

	public File getCacheFile(String url) {
		return bitmapProcess.getOirgFile(url);
	}
	
	public File getCompressCacheFile(String url, String imageId) {
		return bitmapProcess.getCompressFile(url, imageId);
	}

    public Bitmap getBitmapFromMemory(String url, ImageConfig config) {
        MyBitmap bitmap = mImageCache.getBitmapFromMemCache(url, config);
        if (bitmap != null)
            return bitmap.getBitmap();

        return null;
    }

	public MyBitmap getMyBitmapFromMemory(String url, ImageConfig config) {
		MyBitmap bitmap = mImageCache.getBitmapFromMemCache(url, config);
		if (bitmap != null)
			return bitmap;

		return null;
	}

	public static String getKeyByConfig(String url, ImageConfig config) {
		String path = url;
		
		if (config == null || TextUtils.isEmpty(config.getId()))
			return path;

		return path + config.getId();
	}

	/**
	 * 清除缓存
	 */
	public void clearCache() {
		new CacheExecutecTask().execute(CacheExecutecTask.MESSAGE_CLEAR);
	}

	public void clearHalfCache() {
		new CacheExecutecTask().execute(CacheExecutecTask.MESSAGE_HALF_CLEAR);
	}

	public class MyBitmapLoaderTask extends BitmapTask<Void, Void, MyBitmap> {

		private final String imageUrl;

		private List<WeakReference<ImageView>> imageViewsRef;

		private final ImageConfig config;

		boolean isCompleted = false;

		public String getKey() {
			return KeyGenerator.generateMD5(getKeyByConfig(imageUrl, config));
		}

		public MyBitmapLoaderTask(String imageUrl, ImageView imageView, BitmapLoader bitmapLoader, ImageConfig config) {
			this.imageUrl = imageUrl;
			imageViewsRef = new ArrayList<WeakReference<ImageView>>();
			if (imageView != null)
				imageViewsRef.add(new WeakReference<ImageView>(imageView));
			this.config = config;
		}

		@Override
		public MyBitmap workInBackground(Void... params) throws Exception {
			try {
				BitmapBytesAndFlag bitmapBytesAndFlag = doDownload(imageUrl, config);
				byte[] bitmapBytes = bitmapBytesAndFlag.bitmapBytes;
				int flag = bitmapBytesAndFlag.flag;

				if (!isCancelled() && checkImageBinding()) {
					// 如果图片不是拉取至二级缓存，判断是否需要处理
					MyBitmap bitmap = bitmapProcess.compressBitmap(mContext, bitmapBytes, imageUrl, flag, config);

					if (bitmap != null && bitmap.getBitmap() != null) {
						mImageCache.addBitmapToMemCache(imageUrl, config, bitmap);
						return bitmap;
					} else {
						// 如果本地有缓存数据，解析图片失败，则删除图片文件
						bitmapProcess.deleteFile(imageUrl, config);
					}
				}
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			}
			throw new Exception("task canceled or failed, bitmap is null, url = " + imageUrl);
		}

		@Override
		protected void onTaskSuccess(MyBitmap bitmap) {
			super.onTaskSuccess(bitmap);

			setImageBitmap(bitmap);
		}

		@Override
		protected void onTaskFailed(Exception exception) {
			super.onTaskFailed(exception);

			if (config.getLoadfaildRes() > 0)
				setImageBitmap(new MyBitmap(mContext, config.getLoadfaildRes()));
		}

		private boolean checkImageBinding() {
			for (int i = 0; i < imageViewsRef.size(); i++) {

				ImageView imageView = imageViewsRef.get(i).get();
				if (imageView != null) {

					Drawable drawable = imageView.getDrawable();
					if (drawable != null && drawable instanceof MyDrawable) {

						MyDrawable aisenDrawable = (MyDrawable) drawable;
						if (imageUrl.equals(aisenDrawable.getMyBitmap().getUrl())) {
							return true;
						}
					}
				}
			}
			return false;
		}

		void setImageBitmap(MyBitmap bitmap) {
			for (int i = 0; i < imageViewsRef.size(); i++) {
				ImageView imageView = imageViewsRef.get(i).get();
				if (imageView != null) {
					Drawable drawable = imageView.getDrawable();
					if (drawable != null && drawable instanceof MyDrawable) {
						MyDrawable aisenDrawable = (MyDrawable) drawable;
						if (imageUrl.equals(aisenDrawable.getMyBitmap().getUrl())) {
							MyDrawable myDrawable = new MyDrawable(mContext.getResources(), bitmap, config, null);
							config.getDisplayer().loadCompletedisplay(imageView, myDrawable);
						}
					}
				}
			}
		}

		@Override
		protected void onTaskComplete() {
			super.onTaskComplete();

			isCompleted = true;

			taskCache.remove(KeyGenerator.generateMD5(BitmapLoader.getKeyByConfig(imageUrl, config)));
		}

	}

	/**
	 * 加载图片，该方法不是异步执行的方法
	 * 
	 * @param imageUrl
	 * @param config
	 * @return
	 * @throws Exception
	 */
	public BitmapBytesAndFlag doDownload(String imageUrl, final ImageConfig config) throws Exception {
		byte[] bitmapBytes = null;
		int flag = 0x00;

		// 判断二级缓存数据
		bitmapBytes = bitmapProcess.getBitmapFromCompDiskCache(imageUrl, config);
		if (bitmapBytes != null) {
			Logger.v(TAG, "load the picture through the compress disk, url = " + imageUrl);
			flag = flag | 0x01;
		}

		// 判断原始缓存数据
		if (bitmapBytes == null) {
			bitmapBytes = bitmapProcess.getBitmapFromOrigDiskCache(imageUrl, config);
			if (bitmapBytes != null) {
				Logger.v(TAG, "load the data through the original disk, url = " + imageUrl);
				flag = flag | 0x02;
			}
		}

		// 网络加载
		if (bitmapBytes == null) {
			bitmapBytes = config.getDownloaderClass().newInstance().downloadBitmap(mContext, imageUrl, config);
			if (bitmapBytes != null) {
				Logger.v(TAG, "load the data through the network, url = " + imageUrl);
				Logger.v(TAG, "downloader = " + config.getDownloaderClass().getSimpleName());
				flag = flag | 0x04;
			}

			// 数据写入原始缓存
			if (bitmapBytes != null && config.isCacheEnable())
				bitmapProcess.writeBytesToOrigDisk(bitmapBytes, imageUrl);
		}

		if (bitmapBytes != null && config.getProgress() != null)
			config.getProgress().sendFinishedDownload(bitmapBytes);

		if (bitmapBytes != null) {
			BitmapBytesAndFlag bitmapBytesAndFlag = new BitmapBytesAndFlag();
			bitmapBytesAndFlag.bitmapBytes = bitmapBytes;
			bitmapBytesAndFlag.flag = flag;
			return bitmapBytesAndFlag;
		}

		throw new Exception("download faild : " + imageUrl);
	}

	public static class BitmapBytesAndFlag {
		public byte[] bitmapBytes;

		public int flag;
	}

	private class CacheExecutecTask extends AsyncTask<Object, Void, Void> {
		public static final int MESSAGE_CLEAR = 0;
		public static final int MESSAGE_HALF_CLEAR = 4;

		@Override
		protected Void doInBackground(Object... params) {
			switch ((Integer) params[0]) {
			case MESSAGE_CLEAR:
				clearMemCacheInternal();
				break;
			case MESSAGE_HALF_CLEAR:
				clearMemHalfCacheInternal();
				break;
			}
			return null;
		}
	}

	private void clearMemCacheInternal() {
		Logger.d(TAG, "clearMemCacheInternal");
		if (mImageCache != null) {
			mImageCache.clearMemCache();
		}
	}

	public void clearMemHalfCacheInternal() {
		if (mImageCache != null) {
			mImageCache.clearMemHalfCache();
		}
	}

	public static Drawable getLoadingDrawable(Context context, ImageView imageView) {
		Drawable drawable = imageView.getDrawable();
		if (drawable != null && context != null) {
			if (drawable instanceof TransitionDrawable) {
				TransitionDrawable transitionDrawable = (TransitionDrawable) drawable;
				drawable = transitionDrawable.getDrawable(1);
			}
			if (drawable instanceof MyDrawable) {
				MyDrawable myDrawable = (MyDrawable) drawable;
				ImageConfig config = myDrawable.getConfig();
				if (config != null) {
					if (config.getLoadingRes() > 0)
						return new BitmapDrawable(context.getResources(), new MyBitmap(context, config.getLoadingRes()).getBitmap());
				}
			}
		}
		
		return new ColorDrawable(Color.parseColor("#fff2f2f2"));
	}
	
	private void setImageFaild(ImageView imageView, ImageConfig imageConfig) {
		if (imageView != null && imageConfig.getLoadfaildRes() > 0)
			imageView.setImageDrawable(
						new MyDrawable(mContext.getResources(), new MyBitmap(mContext, imageConfig.getLoadfaildRes()), imageConfig, null));
	}
	
	private void setImageLoading(ImageView imageView, String url, ImageConfig imageConfig) {
		if (imageView != null && imageConfig.getLoadingRes() > 0)
			imageView.setImageDrawable(
						new MyDrawable(mContext.getResources(), new MyBitmap(mContext, imageConfig.getLoadingRes(), url), imageConfig, null));
	}

}
