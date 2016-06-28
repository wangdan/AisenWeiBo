package org.aisen.weibo.sina.ui.fragment.secondgroups;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.FileUtils;
import org.aisen.android.common.utils.KeyGenerator;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppSettings;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

/**
 * 用来下载壁纸<br/>
 * 或者设置壁纸<br/>
 * 最多同时下载3个壁纸，其他队列一次下载，可以点击下载进度条停止下载任务
 *
 * @author wangdan
 */
public class WallpaperDownloadTask extends WorkTask<Void, Long, Boolean> {
    static final String TAG = "WallpaperTask";


    private final static int PUBLISH_INTERVAL_TIME = 100;

    private static int DEFAULT_CONNECT_TIMEOUT = 15000;
    private static int DEFAULT_SO_TIMEOUT = 20000;

    // 下载墙纸
    public static synchronized void download(Context context, String url, String id, OnProgressCallback callback) {
        Logger.d(TAG, "触发一次壁纸下载");

        handleWallpaper(context, url, id, callback, Type.download);
    }

    // 下载并设置强制
    public static synchronized void settingWallpaper(Context context, String url, String id, OnProgressCallback callback) {
        Logger.d(TAG, "触发一次壁纸设置");

        handleWallpaper(context, url, id, callback, Type.setting);
    }

    // 停止下载
    public static synchronized void cancelTask(Context context, String url, String id, OnProgressCallback callback) {
        String key = KeyGenerator.generateMD5(url);

        // 如果已经有了线程在运行
        if (runningTask.containsKey(key)) {
            WallpaperDownloadTask task = runningTask.get(key).get();
            Logger.v(TAG, task + "");
            if (task == null) {
                runningTask.remove(key);
            }
            // 已经有线程在运行
            else {
                runningTask.remove(key);

                if (callback != null) {
                    callback.onCanceled(url);
                }

                task.cancel(true);
            }
        }
    }

    // 每次初始化View的时候，重置一下状态
    public static synchronized void bindWallpaper(Context context, String url, String id, OnProgressCallback callback) {
        handleWallpaper(context, url, id, callback, Type.bindview);
    }

    private static synchronized void handleWallpaper(Context context, final String url, final String id, final OnProgressCallback callback, Type type) {
        String key = KeyGenerator.generateMD5(url);

        boolean isRunning = false;

        // 如果已经有了线程在运行
        if (runningTask.containsKey(key)) {
            WallpaperDownloadTask task = runningTask.get(key).get();
            Logger.v(TAG, task + "");
            if (task == null) {
                runningTask.remove(key);
            }
            // 已经有线程在运行
            else {
                isRunning = true;

                Logger.d(TAG, "已经有线程在运行了，重置一下线程, oldType = %s, newType = %s", task.mType.toString(), type.toString());
                task.reset(type, callback);
            }
        }

        // 如果没有运行
        if (!isRunning) {
            if (type == Type.bindview) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        callback.setInit(url);
                    }

                });
            } else {
                WallpaperDownloadTask task = new WallpaperDownloadTask(context, url, id);
                // 如果是设置壁纸，且已经下载，就不回调UI
                File file = getWallpaperSaveFile(url);
                // 如果是设置壁纸，而且壁纸已经存在本地了，
                if (type == Type.setting && file.exists()) {
                    task.callbackRef = new WeakReference<OnProgressCallback>(callback);
                    task.mType = type;
                    if (type == Type.download)
                        task.mTypeFlag = task.mTypeFlag | 0x01;
                    if (type == Type.setting)
                        task.mTypeFlag = task.mTypeFlag | 0x02;
                } else {
                    task.reset(type, callback);
                }
                task.executeOnExecutor(OFFLINE_EXECUTOR);

                Logger.d(TAG, "开启壁纸下载线程");

                runningTask.put(key, new WeakReference<WallpaperDownloadTask>(task));

            }
        }
    }

    private static Hashtable<String, WeakReference<WallpaperDownloadTask>> runningTask = new Hashtable<String, WeakReference<WallpaperDownloadTask>>();

    private enum Type {
        download, // 下载
        setting, // 设置壁纸
        bindview // 绑定一下view
    }

    private Context mContext;

    private Type mType;

    private int mTypeFlag = 0x00;// 0x01:下载,0x02:设置壁纸,0x03:下载+设置壁纸

    private WeakReference<OnProgressCallback> callbackRef;

    private String mImageUrl;

    private String mImageId;

    private long total = -1;

    private long progress = -1;

    private static OkHttpClient httpClient = new OkHttpClient();
    private final static int CONN_TIMEOUT = 30000;
    private final static int READ_TIMEOUT = 30000;

    static {
        httpClient.setConnectTimeout(CONN_TIMEOUT, TimeUnit.MILLISECONDS);
        httpClient.setReadTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    private Call mCall;

    public WallpaperDownloadTask(Context context, String url, String imageId) {
        this.mImageUrl = url;
        this.mContext = context;
        this.mImageId = imageId;
    }

    public void reset(Type type, final OnProgressCallback callback) {
        if (type != null && type != Type.bindview)
            this.mType = type;

        if (type == Type.download) {
            mTypeFlag = mTypeFlag | 0x01;
        }
        if (type == Type.setting) {
            mTypeFlag = mTypeFlag | 0x02;
        }

        this.callbackRef = new WeakReference<OnProgressCallback>(callback);

        callback.onProgressUpdate(mImageUrl, progress, total, mTypeFlag);
    }

    @Override
    public Boolean workInBackground(Void... params) throws TaskException {

        // 先看本地相册是否已经存了这个图片，如果存了就不下载了
        File file = getWallpaperSaveFile(mImageUrl);
        // ImageLoader也未下载该图片
        if (file != null && !file.exists())
            file = BitmapLoader.getInstance().getCacheFile(mImageUrl);
        // 文件不存在，先下载
        if (file != null && !file.exists())
            file = doDownload(file);

        // 不管是下载还是设置，都首先保存壁纸
        saveWallpaper(file);

        // 是否是设置壁纸
        if ((mTypeFlag & 0x02) > 0)
            setWallpaper(mContext, file, callbackRef.get());

        return true;
    }

    @Override
    protected void onProgressUpdate(Long... values) {
        super.onProgressUpdate(values);

        OnProgressCallback callback = callbackRef.get();
        if (callback != null && values != null && values.length > 1) {
            long total = values[1];
            long progress = values[0];

            callback.onProgressUpdate(mImageUrl, progress, total, mTypeFlag);
        }
    }

    @Override
    protected void onSuccess(Boolean result) {
        super.onSuccess(result);

        OnProgressCallback callback = callbackRef.get();

        if (mType == Type.setting) {
            Logger.d(TAG, "设置壁纸成功 ---> " + callback + "");

            if (callback != null)
                callback.showMessage(mImageUrl, mContext.getResources().getString(R.string.txt_set_wallpaper_suc));
        } else if (mType == Type.download) {
            Logger.d(TAG, "下载壁纸成功 ---> " + callback + "");

            if (callback != null)
                callback.showMessage(mImageUrl, mContext.getResources().getString(R.string.txt_save_wallpaper_suc));
        }
    }


    @Override
    protected void onFailure(TaskException exception) {
        super.onFailure(exception);

        OnProgressCallback callback = callbackRef.get();
        // 如果是取消下载，
        if (callback != null) {
            if ("-100".equals(exception.getCode())) {
                if (isRunning()) {
                    Logger.d(TAG, "取消下载");

                    callback.onCanceled(mImageUrl);
                }
            } else {
                Logger.d(TAG, "下载异常 ---> %s", exception.getMessage() + "");

                callback.showMessage(mImageUrl, exception.getMessage());
            }
        }
    }

    private boolean isRunning() {
        if (isCancelled()) {
            return false;
        }

        String key = KeyGenerator.generateMD5(mImageUrl);
        if (runningTask != null && runningTask.containsKey(key)) {
            return runningTask.get(key).get() == this;
        }

        return false;
    }

    @Override
    protected void onFinished() {
        super.onFinished();

        runningTask.remove(KeyGenerator.generateMD5(mImageUrl));

        OnProgressCallback callback = callbackRef.get();
        if (callback != null) {
            callback.setInit(mImageUrl);
        }
    }

    // 设置壁纸
    public static void setWallpaper(Context context, File file, final OnProgressCallback callback) throws TaskException {
        long time = System.currentTimeMillis();
        try {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(GlobalContext.getInstance());

            try {
                DisplayMetrics dm = new DisplayMetrics();
                WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                windowManager.getDefaultDisplay().getMetrics(dm);
                int width = dm.widthPixels;
                int height = dm.heightPixels;

                int navigationBarHeight = SystemUtils.getNavigationBarHeight(context);

                int wallpaperWidth = width;
                int wallpaperHeight = height;

                Options opts = new Options();
                opts.inJustDecodeBounds = true;
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), opts);

                boolean decode = false;

                if (opts.outHeight > height + navigationBarHeight) {
                    decode = true;
                }

                Logger.d(TAG, "image height = %d, screen height = %d", opts.outHeight, height + navigationBarHeight);

                if (decode) {
                    // docode的时间会稍微长一点，idol3测试在1S内，所以先显示一个99%的进度
//                    if (progress <= 0) {
//                        progress = 999l;
//                        total = 1000l;
//                        publishProgress(progress, total);
//                    }

                    opts.inJustDecodeBounds = false;
                    bitmap = BitmapFactory.decodeStream(new FileInputStream(file));

                    Matrix matrix = new Matrix();
                    float scale = wallpaperHeight * 1.0f / bitmap.getHeight();
                    matrix.setScale(scale, scale);

                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
                    wallpaperManager.setStream(in);

                    if (callback != null) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {

                            @Override
                            public void run() {
                                callback.onSetWallpaper(true);
                            }

                        });
                    }

                    Logger.d(TAG, "设置处理后的壁纸耗时 : " + (System.currentTimeMillis() - time));

                    return;
                }
            } catch (Throwable e) {
                Logger.printExc(WallpaperDownloadTask.class, e);
            }

            wallpaperManager.setStream(new FileInputStream(file));

            Logger.d(TAG, "设置原壁纸耗时 : " + (System.currentTimeMillis() - time));

            if (callback != null) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        callback.onSetWallpaper(true);
                    }

                });
            }
        } catch (Exception e) {
            Logger.printExc(WallpaperDownloadTask.class, e);

            if (callback != null) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        callback.onSetWallpaper(false);
                    }

                });
            }

            throw new TaskException("", context.getResources().getString(R.string.txt_set_wallpaper_fail));
        }
    }

    // 保存壁纸到相册
    private File saveWallpaper(File file) throws TaskException {
        File savedFile = getWallpaperSaveFile(mImageUrl);

        if (savedFile.exists())
            return savedFile;

        if (file.exists()) {
            try {
                FileUtils.copyFile(file, savedFile);
                Logger.v("保存成功!:" + savedFile.getAbsolutePath());

                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
                    // 解决在部分机器缓存更新不及时问题
                    mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"
                            + Environment.getExternalStorageDirectory())));
                }

                try {
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri uri = Uri.fromFile(savedFile);
                    intent.setData(uri);
                    mContext.sendBroadcast(intent);
                } catch (Exception e) {
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return savedFile;
        }

        return null;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        // 停止网络请求，如果不为空
        try {
            if (mCall != null) {
                mCall.cancel();
            }
        } catch (Throwable e) {
        }

        return super.cancel(mayInterruptIfRunning);
    }

    // 下载壁纸
    private File doDownload(File file) throws TaskException {
        if (!isRunning())
            throw new TaskException("-100", "");

        //判断网络是否连接
        if(SystemUtils.getNetworkType(GlobalContext.getInstance()) == SystemUtils.NetWorkType.none){
            throw new TaskException("", mContext.getResources().getString(org.aisen.android.R.string.comm_error_noneNetwork));
        }

        // 先下载一个临时文件
        File tempFile = new File(file.getAbsolutePath() + ".tmp");

        // 如果图片没有content-length
        final int defaultLength = 8 * 1024 * 1024;

        // 开始下载图片
        try {
            total = 0;
            progress = 0;
            // 发布进度
            publishProgress(progress, total);

            Logger.d(TAG, "开始下载壁纸 ---> %s", mImageUrl);
            Request request = new Request.Builder().get().url(mImageUrl).build();


            mCall = httpClient.newCall(request);
            Response response = mCall.execute();
            if (response == null) {
                throw new TaskException(TaskException.TaskError.failIOError.toString());
            }
            int statusCode = response.code();
            if (!(statusCode == HttpURLConnection.HTTP_OK || statusCode == HttpURLConnection.HTTP_PARTIAL)) {
                throw new TaskException(TaskException.TaskError.failIOError.toString());
            }
            InputStream imageStream = null;

            // 写临时文件
            FileOutputStream out = new FileOutputStream(tempFile);

            try {

                String encoding = response.header("Content-Encoding");
                if (encoding != null && !TextUtils.isEmpty(encoding) &&
                        "gzip".equals(encoding)) {

                    imageStream = new GZIPInputStream(response.body().byteStream());
                    Logger.w(TAG, "解压gzip文件, 解压前大小:");
                } else {
                    imageStream = response.body().byteStream();
                }


                try {
                    total = response.body().contentLength();
                } catch (Exception e) {
                    // 容错处理，如果未读到大小，默认为8M
                    total = defaultLength;
                }
                Logger.d(TAG, "Content-Length = " + total);
                if (total < 0)
                    total = defaultLength;

                // 获取图片数据
                byte[] buffer = new byte[1024 * 8];
                int readLen = -1;
                long lastPublishTime = 0l;
                while ((readLen = imageStream.read(buffer)) != -1) {
                    if (!isRunning())
                        throw new TaskException("-100", "");

                    progress += readLen;
                    out.write(buffer, 0, readLen);

                    Logger.v(TAG, "下载进度, %s / %s", getUnit(progress), getUnit(total));

                    // 发布进度
                    long now = System.currentTimeMillis();
                    if (now - lastPublishTime > PUBLISH_INTERVAL_TIME) {
                        lastPublishTime = now;
                        publishProgress(progress, total);
                    }
                }

                publishProgress(progress, progress);
                Logger.d(TAG, "total : " + total + "   readLen : " + readLen);

                out.flush();
            } catch (IOException e) {
                Logger.printExc(WallpaperDownloadTask.class, e);

                throw e;
            } finally {
                out.close();
                if (imageStream != null)
                    imageStream.close();
            }

//            // 验证一下是否GZip压缩
//            try {
//                String encoding = response.header("Content-Encoding");
//                if (encoding != null && !TextUtils.isEmpty(encoding) &&
//                        "gzip".equals(encoding)) {
//                    File ttf = tempFile;
//                    tempFile = decodeGZipFile(tempFile);
//                    ttf.delete();
//                    Logger.w(TAG, "解压gzip文件, 解压前大小:" + total + ", 解压后:" + tempFile.length());
//                }
//            } catch (Throwable e) {
//                Logger.printExc(WallpaperDownloadTask.class, e);
//            }
        } catch (Throwable e) {
            Logger.printExc(WallpaperDownloadTask.class, e);

            throw new TaskException("", mContext.getResources().getString(R.string.down_faild));
        }

        Logger.d(TAG, "File-Length = " + tempFile.length());
        Logger.d(TAG, "下载文件成功，path = " + tempFile.getAbsolutePath());

        // 重命名之前，对临时文件，做一次图片校验，用BitmapFactory解析一下
        Options opts = new Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(tempFile.getAbsolutePath(), opts);
        // 如果解析的宽高度小于1，默认为非图片
        Logger.d(TAG, "图片解析尺寸 %s x %s", opts.outWidth, opts.outHeight);
        if (opts.outWidth < 1 && opts.outHeight < 1) {
            throw new TaskException("", mContext.getResources().getString(R.string.down_faild));
        }

        Logger.d(TAG, "下载壁纸完成");
        if (!tempFile.renameTo(file))
            throw new TaskException("", mContext.getResources().getString(R.string.down_faild));

        return file;
    }

    // 壁纸的本地保存文件
    public static File getWallpaperSaveFile(String imageUrl) {
        String name = KeyGenerator.generateMD5(imageUrl);
        String suffix = ".jpg";
        File savedFile = new File(SystemUtils.getSdcardPath() + File.separator + AppSettings.getImageSavePath() +
                                            File.separator + "Wallpaper" + File.separator + name + suffix);

        try {
            if (!savedFile.getParentFile().exists())
                savedFile.getParentFile().mkdirs();
        } catch (Throwable e) {
            Logger.printExc(WallpaperDownloadTask.class, e);
        }

        return savedFile;
    }

    public interface OnProgressCallback {

        // 回调下载进度
        void onProgressUpdate(String image, long progress, long total, int flag);

        // 设置壁纸成功
        void onSetWallpaper(boolean success);

        // 回调弹框消息
        void showMessage(String image, String text);

        // 取消下载
        void onCanceled(String image);

        // 未下载，初始化视图
        void setInit(String image);

    }

    // 限制同时下载3个的线程池
    private static Executor OFFLINE_EXECUTOR = Executors.newFixedThreadPool(3, new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "WallpaperTask #" + mCount.getAndIncrement());
        }
    });

    // 仅用于日志使用
    private String getUnit(long length) {
        String sizeStr;
        if (length * 1.0f / 1024 / 1024 > 1)
            sizeStr = String.format("%s M", new DecimalFormat("#.00").format(length * 1.0d / 1024 / 1024));
        else
            sizeStr = String.format("%s Kb", new DecimalFormat("#.00").format(length * 1.0d / 1024));
        return sizeStr;
    }


    public static File decodeGZipFile(File file) throws Throwable {
        GZIPInputStream gzipIn = null;
        FileOutputStream gzipOut = null;
        try {
            gzipIn = new GZIPInputStream(new FileInputStream(file));
            file = new File(file.getAbsolutePath() + ".gziptemp");
            gzipOut = new FileOutputStream(file);
            byte[] buf = new byte[1024 * 8];
            int num = -1;
            while ((num = gzipIn.read(buf, 0, buf.length)) != -1) {
                gzipOut.write(buf, 0, num);
            }
        } finally {
            if (gzipIn != null)
                gzipIn.close();
            if (gzipOut != null)
                gzipOut.close();
        }

        return file;
    }

}
