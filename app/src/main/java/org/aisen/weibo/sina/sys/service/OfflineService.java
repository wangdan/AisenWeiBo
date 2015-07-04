package org.aisen.weibo.sina.sys.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.KeyGenerator;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.component.bitmaploader.core.ImageConfig;
import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.Group;
import org.aisen.weibo.sina.sinasdk.bean.PicUrls;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;
import org.aisen.weibo.sina.support.bean.OfflinePictureBean;
import org.aisen.weibo.sina.support.notifier.OfflineNotifier;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.ui.activity.basic.MainActivity;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 离线数据的服务
 *
 * Created by wangdan on 15/5/3.
 */
public class OfflineService extends Service {

    public static final String TAG = "Offline-Service";

    public static final String ACTION_TOGGLE = "org.aisen.weibo.sina.ACTION_TOGGLE";// 开始离线

    public static final String ACTION_STOP = "org.aisen.weibo.sina.ACTION_STOP";// 停止离线

    public static final String ACTION_STATUS_FINISHED = "org.aisen.weibo.sina.ACTION_STATUS_FINISHED";// 分组离线完成

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "OfflineTask #" + mCount.getAndIncrement());
        }
    };

    private static Executor IMAGE_POOL_EXECUTOR;

    // 开始离线，根据分组
    public static void startOffline(ArrayList<Group> groups) {
        Logger.d(TAG, "开始离线，离线%d个分组", groups.size());

        if (groups == null || groups.size() == 0)
            return;

        Intent intent = new Intent(GlobalContext.getInstance(), OfflineService.class);
        intent.setAction(ACTION_TOGGLE);
        intent.putExtra("groups", groups);
        GlobalContext.getInstance().startService(intent);
    }

    public static void stopOffline() {
        if (getInstance() != null) {
            Intent intent = new Intent(GlobalContext.getInstance(), OfflineService.class);
            intent.setAction(ACTION_STOP);
            GlobalContext.getInstance().startService(intent);
        }
    }

    public static OfflineService getInstance() {
        if (mServiceRef != null)
            return mServiceRef.get();

        return null;
    }

    public static WeakReference<OfflineService> mServiceRef;

    public enum  OfflineStatus {
        init, prepare, loadStatus, loadPicture, cancel, finished
    }

    // 当前服务状态
    private OfflineStatus mStatus = OfflineStatus.init;

    private List<Group> mGroups;// 当次离线任务的分组
    private List<Group> unOfflineGroups;
    private int offlineStatusCount;// 离线的微博数量

    private Map<String, String> mPictureMap = new HashMap<>();// 用来去重
    private LinkedBlockingQueue<OfflinePictureBean> mPictures = new LinkedBlockingQueue<>();// 线程安全队列
    private int offlinePictureSize = 0;
    private int offlinePictureCount = 0;// 离线的图片数量

    private long offlineStatusLengh = 0;// 离线的微博总流量大小
    private long offlinePictureLengh = 0;// 离线的图片总流量大小

    private OfflineNotifier mNotifier;

    @Override
    public void onCreate() {
        super.onCreate();

        mNotifier = new OfflineNotifier(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || TextUtils.isEmpty(intent.getAction()))
            return super.onStartCommand(intent, flags, startId);

        String action = intent.getAction();
        if (ACTION_TOGGLE.equalsIgnoreCase(action)) {
            if (mStatus == OfflineStatus.init) {
                mGroups = (ArrayList<Group>) intent.getSerializableExtra("groups");

                prepareOffline();
            }
            else {
                Logger.d(TAG, "正在离线，忽略这次请求");
            }
        }
        else if (ACTION_STOP.equalsIgnoreCase(action)) {
            stopSelf();
        }

        mServiceRef = new WeakReference<OfflineService>(this);

        return super.onStartCommand(intent, flags, startId);
    }

    // 准备离线
    private void prepareOffline() {
        mStatus = OfflineStatus.prepare;

        mNotifier.cancelNotification(OfflineNotifier.OfflineStatus);
        mNotifier.cancelNotification(OfflineNotifier.OfflinePicture);

        // 开始离线微博
        unOfflineGroups = new ArrayList<>();
        unOfflineGroups.addAll(mGroups);
        for (Group group : mGroups) {
            new LoadStatusTask(group).executeOnSerialExecutor();
        }
    }

    private synchronized void preparePicture() {
        // 等微博离线完了再离线图片
        if (unOfflineGroups.size() > 0)
            return;

        // 清理缓存数据
        mPictureMap.clear();

        // 微博更新完了
        mNotifier.notifyStatusSuccess(mGroups.size(), offlineStatusCount, offlineStatusLengh);

        // 只有WIFI情况才离线图片
        if (stopSelfIfCan()) {
            return;
        }

        // 没有图片
        if (mPictures.size() == 0) {
            stopSelf();
        }
        else {
            // 开始下载队列里的图片
            if (IMAGE_POOL_EXECUTOR == null) {
                IMAGE_POOL_EXECUTOR = Executors.newFixedThreadPool(AppSettings.offlinePicTaskSize(), sThreadFactory);
            }

            // 新建线程队列，开始下载图片
            List<LoadPictureTask> taskList = new ArrayList<>();
            for (int i = 0; i < 30; i++) {
                OfflinePictureBean bean = pollPicture();
                if (bean != null)
                    taskList.add(new LoadPictureTask(bean));
                else {
                    break;
                }
            }
            for (LoadPictureTask task : taskList) {
                task.executeOnExecutor(IMAGE_POOL_EXECUTOR);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class LoadStatusTask extends WorkTask<Void, Void, Boolean> {

        Group group;

        LoadStatusTask(Group group) {
            this.group = group;

            Logger.d(TAG, "开始离线分组%s", group.getName());

            mNotifier.notifyStatus(group, offlineStatusLengh);
        }

        @Override
        public Boolean workInBackground(Void... p) throws TaskException {
            if (mStatus == OfflineStatus.finished || mStatus == OfflineStatus.cancel) {
                return false;
            }

            // 发生异常了重复加载几次
            int repeat = 3;

            // 离线的大小
            int count = AppSettings.getOfflineStatusSize();

//            String max_id = null;
            while (--repeat >= 0) {
                try {
                    Params params = new Params();
                    params.addParameter("list_id", group.getIdstr());
//                    if (!TextUtils.isEmpty(max_id))
//                        params.addParameter("max_id", max_id);
                    params.addParameter("count", String.valueOf(count));

                    // 这里会自动的清理现有缓存，更新新的缓存
                    StatusContents statusContents = SinaSDK.getInstance(AppContext.getToken()).friendshipGroupsTimeline(params);
                    if (statusContents.getStatuses().size() > 0) {
                        Logger.d(TAG, "分组%s当次加载微博%d条，节省流量%s", group.getName(), count, AisenUtils.getUnit(statusContents.getLength()));

                        // 统计流量
                        offlineStatusLengh += statusContents.getLength();
                        // 统计数量
                        offlineStatusCount += statusContents.getStatuses().size();

                        // 处理微博的图片
                        List<OfflinePictureBean> pictureList = new ArrayList<>();
                        for (StatusContent status : statusContents.getStatuses()) {
                            if (status.getRetweeted_status() != null)
                                status = status.getRetweeted_status();

                            if (status.getPic_urls() != null && status.getPic_urls().length > 0) {
                                for (PicUrls picUrl : status.getPic_urls()) {
                                    OfflinePictureBean picture = new OfflinePictureBean();
                                    picture.setThumb(picUrl.getThumbnail_pic());

                                    if (!mPictureMap.containsKey(KeyGenerator.generateMD5(picture.getThumb()))
                                            && !BitmapLoader.getInstance().getCacheFile(picture.getThumb()).exists()) {
                                        pictureList.add(picture);

                                        mPictureMap.put(KeyGenerator.generateMD5(picture.getThumb()), picture.getThumb());
                                    }
                                }
                            }

                            // 处理微博头像
                            WeiBoUser user = status.getUser();
                            if (user != null) {
                                if (!mPictureMap.containsKey(KeyGenerator.generateMD5(AisenUtils.getUserPhoto(user))) &&
                                        BitmapLoader.getInstance().getCacheFile(AisenUtils.getUserPhoto(user)).exists()) {
                                    OfflinePictureBean picture = new OfflinePictureBean();
                                    picture.setThumb(AisenUtils.getUserPhoto(user));
                                    pictureList.add(picture);

                                    mPictureMap.put(KeyGenerator.generateMD5(AisenUtils.getUserPhoto(user)), AisenUtils.getUserPhoto(user));
                                }
                            }
                            // 转发微博头像
                            if (status.getRetweeted_status() != null && status.getRetweeted_status().getUser() != null) {
                                user = status.getRetweeted_status().getUser();
                                if (!mPictureMap.containsKey(KeyGenerator.generateMD5(AisenUtils.getUserPhoto(user))) &&
                                        BitmapLoader.getInstance().getCacheFile(AisenUtils.getUserPhoto(user)).exists()) {
                                    OfflinePictureBean picture = new OfflinePictureBean();
                                    picture.setThumb(AisenUtils.getUserPhoto(user));
                                    pictureList.add(picture);

                                    mPictureMap.put(KeyGenerator.generateMD5(AisenUtils.getUserPhoto(user)), AisenUtils.getUserPhoto(user));
                                }
                            }
                        }

                        // 放到图片队列
                        mPictures.addAll(pictureList);
                        offlinePictureSize = mPictures.size();
                        Logger.d(TAG, "分组%s新增%d张待下载图片", group.getName(), pictureList.size());
                    }
                    else {
                        Logger.d(TAG, "分组%s加载0条微博", group.getName());
                    }

                    return true;
                } catch (Exception e) {
                }
            }
            return false;
        }

        @Override
        protected void onSuccess(Boolean result) {
            super.onSuccess(result);

            // 更新广播
            if (result) {
                mNotifier.notifyStatus(group, offlineStatusLengh);

                Intent intent = new Intent();
                intent.setAction(ACTION_STATUS_FINISHED);
                intent.putExtra("id", group.getId());
                sendBroadcast(intent);
            }
        }

        @Override
        protected void onFinished() {
            super.onFinished();

            unOfflineGroups.remove(group);

            preparePicture();
        }
    }

    int count = 0;
    class LoadPictureTask extends WorkTask<Void, Void, Void> {

        OfflinePictureBean bean;
        LoadPictureTask(OfflinePictureBean bean) {
            this.bean = bean;

            count++;
        }

        @Override
        public Void workInBackground(Void... params) throws TaskException {
            if (stopSelfIfCan()) {
                return null;
            }

            // 下载缩略图
            String url = bean.getThumb();// .replace("thumbnail", "bmiddle");

            Logger.v(TAG, "开始离线图片 ---> %s", url);

            File file = BitmapLoader.getInstance().getCacheFile(url);
            File fileTemp = new File(file.getPath() + ".tmp");
            if (!file.exists()) {
                try {
                    HttpGet httpGet = new HttpGet(url);
                    DefaultHttpClient httpClient = null;
                    BasicHttpParams httpParameters = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParameters, 10 * 1000);
                    HttpConnectionParams.setSoTimeout(httpParameters, 20 * 1000);
                    httpClient = new DefaultHttpClient(httpParameters);
                    // 设置网络代理
                    HttpHost proxy = SystemUtils.getProxy();
                    if (proxy != null)
                        httpClient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);
                    HttpResponse response = httpClient.execute(httpGet);
                    // 图片大小
                    int length = 0;
                    Header header = response.getFirstHeader("Content-Length");
                    length = Integer.parseInt(header.getValue());

                    InputStream in = response.getEntity().getContent();

                    FileOutputStream out = new FileOutputStream(fileTemp);
                    // 获取图片数据
                    byte[] buffer = new byte[1024 * 8];
                    int readLen = -1;
                    while ((readLen = in.read(buffer)) != -1) {
                        out.write(buffer, 0, readLen);
                    }
                    out.flush();
                    in.close();
                    out.close();

                    fileTemp.renameTo(file);

                    bean.setLength(length);

                    Logger.v(TAG, "离线图片成功，url = %s", url);
                } catch (Exception e) {
                    e.printStackTrace();

                    Logger.e(TAG, "离线图片失败" + e.getMessage() + "" + e);

                    throw new TaskException("");
                }
            }
            else {
                bean.setLength(file.length());
            }

            offlinePictureLengh += bean.getLength();

            return null;
        }

        @Override
        protected void onFinished() {
            super.onFinished();

            offlinePictureCount++;

            count--;

            if (SystemUtils.getNetworkType() == SystemUtils.NetWorkType.wifi)
                notifyPictureProgress();

            if ((mStatus != OfflineStatus.finished && mStatus != OfflineStatus.cancel) &&
                    SystemUtils.getNetworkType() == SystemUtils.NetWorkType.wifi) {
                OfflinePictureBean bean = pollPicture();
                if (bean != null) {
                    new LoadPictureTask(bean).executeOnExecutor(IMAGE_POOL_EXECUTOR);
                }
                else {
                    if (count == 0) {
                        stopSelf();
                    }
                }
            }

            stopSelfIfCan();
        }
    }


    NotificationCompat.Builder progressNuilder;
    long refreshTime = 0;
    public void notifyPictureProgress() {
        if (refreshTime == 0)
            refreshTime = System.currentTimeMillis();

        if (System.currentTimeMillis() - refreshTime >= 500 || offlinePictureCount == offlinePictureSize) {
            refreshTime = System.currentTimeMillis();

            String title = String.format("正在离线图片");
            if (progressNuilder == null) {
                progressNuilder = new NotificationCompat.Builder(this);

                progressNuilder.setSmallIcon(R.drawable.statusbar_ic_send_success)
                        .setContentTitle(title)
                        .setOnlyAlertOnce(true);
            }
            progressNuilder.setContentInfo(String.format("%s/%s", String.valueOf(offlinePictureCount), String.valueOf(offlinePictureSize)));
            progressNuilder.setProgress(Math.round(offlinePictureSize), Math.round(offlinePictureCount), false);

            mNotifier.notify(OfflineNotifier.OfflinePicture, 0, progressNuilder);
        }
    }

    private synchronized OfflinePictureBean pollPicture() {
        return mPictures.poll();
    }

    public interface OfflineLength {

        public void setLength(long length);

    }

    public static abstract class OfflineBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && ACTION_STATUS_FINISHED.equalsIgnoreCase(intent.getAction())) {
                onReceiveStatusOfflined(intent.getStringExtra("id"));
            }
        }

        abstract protected void onReceiveStatusOfflined(String groupId);

    }

    private boolean stopSelfIfCan() {
        if (mStatus == OfflineStatus.cancel) {
            stopSelf();

            return true;
        }

        if (SystemUtils.getNetworkType() != SystemUtils.NetWorkType.wifi) {
            stopSelf();

            mStatus = OfflineStatus.cancel;

            return true;
        }

        return false;
    }

    public OfflineStatus getStatus() {
        return mStatus;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mStatus == OfflineStatus.cancel) {
            mNotifier.notifyPictureSuccess(offlinePictureCount, offlinePictureLengh);
        } else {
            mNotifier.notifyPictureSuccess(offlinePictureSize, offlinePictureLengh);
        }


        mStatus = OfflineStatus.finished;

        Logger.d(TAG, "离线服务停止");

        if (MainActivity.getInstance() != null)
            MainActivity.getInstance().invalidateOptionsMenu();
    }

}
