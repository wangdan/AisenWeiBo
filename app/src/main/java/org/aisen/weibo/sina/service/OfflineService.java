package org.aisen.weibo.sina.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.ActivityHelper;
import org.aisen.android.common.utils.KeyGenerator;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.service.notifier.OfflineNotifier;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.Group;
import org.aisen.weibo.sina.sinasdk.bean.PicUrls;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;
import org.aisen.weibo.sina.support.bean.OfflinePictureBean;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.ui.activity.base.MainActivity;
import org.aisen.weibo.sina.ui.fragment.timeline.TimelineMainFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
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

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "OfflineTask #" + mCount.getAndIncrement());
        }
    };

    private static Executor OFFLINE_EXECUTOR;

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

    private WeiBoUser loggedIn;

    @Override
    public void onCreate() {
        super.onCreate();

        mNotifier = new OfflineNotifier(this);

        loggedIn = AppContext.getAccount().getUser();
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
            if (OFFLINE_EXECUTOR != null)
                ((ExecutorService) OFFLINE_EXECUTOR).shutdownNow();

            mStatus = OfflineStatus.cancel;

            if (offlinePictureCount > 0 && mNotifier != null)
                mNotifier.notifyPictureSuccess(offlinePictureCount, offlinePictureLengh);

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

        // 正在运行
        if (BaseActivity.getRunningActivity() instanceof MainActivity) {
            TimelineMainFragment.sendBroadcast();
        }
        else {
            setOfflineFinished(loggedIn, true);
        }

        // 清理缓存数据
        mPictureMap.clear();

        // 微博更新完了
        mNotifier.notifyStatusSuccess(mGroups.size(), offlineStatusCount, offlineStatusLengh);

        // 只有WIFI情况才离线图片
        if (isCanceled()) {
            return;
        }

        // 没有图片
        if (mPictures.size() == 0) {
            stopSelf();
        }
        else {
            // 开始下载队列里的图片
            if (OFFLINE_EXECUTOR == null) {
                OFFLINE_EXECUTOR = Executors.newFixedThreadPool(AppSettings.offlinePicTaskSize(), sThreadFactory);
            }

            // 新建线程队列，开始下载图片
            List<LoadPictureTask> taskList = new ArrayList<>();
            int taskSize = AppSettings.offlinePicTaskSize() > mPictures.size() ? mPictures.size() : AppSettings.offlinePicTaskSize();
            for (int i = 0; i < taskSize; i++) {
                OfflinePictureBean bean = pollPicture();
                if (bean != null)
                    taskList.add(new LoadPictureTask(bean));
                else {
                    break;
                }
            }
            for (LoadPictureTask task : taskList) {
                task.executeOnExecutor(OFFLINE_EXECUTOR);
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
            // 发生异常了重复加载几次
            int repeat = 3;

            // 离线的大小
            int count = AppSettings.getOfflineStatusSize();

//            String max_id = null;
            while (--repeat >= 0) {
                if (isCanceled())
                    break;

                try {
                    Params params = new Params();
                    params.addParameter("list_id", group.getIdstr());
//                    if (!TextUtils.isEmpty(max_id))
//                        params.addParameter("max_id", max_id);
                    params.addParameter("count", String.valueOf(count));

                    // 这里会自动的清理现有缓存，更新新的缓存
                    StatusContents statusContents = SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).friendshipGroupsTimeline(params);
                    if (statusContents.getStatuses().size() > 0) {
                        Logger.d(TAG, "分组%s当次加载微博%d条，节省流量%s", group.getName(), count, AisenUtils.getUnit(statusContents.getLength()));

                        TimelineMainFragment.clearLastRead(group, loggedIn);

                        // 统计流量
                        offlineStatusLengh += statusContents.getLength();
                        // 统计数量
                        offlineStatusCount += statusContents.getStatuses().size();

                        // 处理微博的图片
                        List<OfflinePictureBean> pictureList = new ArrayList<>();
                        for (StatusContent status : statusContents.getStatuses()) {
                            // 处理微博头像
                            WeiBoUser user = status.getUser();
                            if (user != null) {
                                if (!mPictureMap.containsKey(KeyGenerator.generateMD5(AisenUtils.getUserPhoto(user))) &&
                                        !BitmapLoader.getInstance().getCacheFile(AisenUtils.getUserPhoto(user)).exists()) {
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
                                        !BitmapLoader.getInstance().getCacheFile(AisenUtils.getUserPhoto(user)).exists()) {
                                    OfflinePictureBean picture = new OfflinePictureBean();
                                    picture.setThumb(AisenUtils.getUserPhoto(user));
                                    pictureList.add(picture);

                                    mPictureMap.put(KeyGenerator.generateMD5(AisenUtils.getUserPhoto(user)), AisenUtils.getUserPhoto(user));
                                }
                            }

                            // 微博配图
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
            }
        }

        @Override
        protected void onFinished() {
            super.onFinished();

            unOfflineGroups.remove(group);

            preparePicture();
        }
    }

    int taskCount = 0;
    int taskRunningCount = 0;
    class LoadPictureTask extends WorkTask<Void, Void, Void> {

        OfflinePictureBean bean;
        LoadPictureTask(OfflinePictureBean bean) {
            this.bean = bean;

            taskCount++;
//            Logger.d(TAG, "图片线程数%d", taskCount);
        }

        @Override
        public Void workInBackground(Void... params) throws TaskException {
            taskRunningCount++;
//            Logger.d(TAG, "图片运行线程数%d", taskRunningCount);

            // 下载缩略图
            String url = bean.getThumb();// .replace("thumbnail", "bmiddle");
            downloadPicture(url, bean);

            // 2015-07-07 新增中图离线选项
            if (AppSettings.offlineMidPic()) {
                String mid_url = url.replace("thumbnail", "bmiddle");
                downloadPicture(mid_url, bean);
            }

            offlinePictureLengh += bean.getLength();

            return null;
        }

        private void downloadPicture(String url, OfflinePictureBean mBean) throws TaskException {
            File file = BitmapLoader.getInstance().getCacheFile(url);
            File fileTemp = new File(file.getPath() + ".tmp");
            if (!file.exists()) {
                int repeat = 3;
                while(--repeat > 0) {
                    if (isCanceled())
                        break;

                    try {
//                        Logger.v(TAG, "开始离线图片 ---> %s", url);

                        Request request = new Request.Builder().url(url).build();

                        Response response = GlobalContext.getOkHttpClient().newCall(request).execute();

                        InputStream in = response.body().byteStream();

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

                        mBean.setLength(mBean.getLength() + file.length());
//                        Logger.d(TAG, "离线图片成功，url = %s", url);

                        repeat = 0;
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();

                        if (repeat == 1) {
                            Logger.e(TAG, "离线图片失败" + e.getMessage() + "" + e);

                            throw new TaskException("");
                        }
                    }
                }

//                if (mBean.getLength() > 0)
//                    Logger.v(TAG, "离线图片成功，url = %s", url);
            }
            else {
                mBean.setLength(mBean.getLength() + file.length());
            }
        }

        @Override
        protected void onFinished() {
            super.onFinished();

            offlinePictureCount++;

            taskCount--;
            taskRunningCount--;

            if (!isCanceled())
                notifyPictureProgress();

            if (!isCanceled()) {
                OfflinePictureBean bean = pollPicture();
                if (bean != null) {
                    new LoadPictureTask(bean).executeOnExecutor(OFFLINE_EXECUTOR);
                }
                else {
                    if (taskCount == 0) {
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

    private boolean isCanceled() {
        return mStatus == OfflineStatus.cancel || mStatus == OfflineStatus.finished;
    }

    private boolean stopSelfIfCan() {
        if (isCanceled()) {
            stopSelf();

            return true;
        }

        if (SystemUtils.getNetworkType(this) != SystemUtils.NetWorkType.wifi) {
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

        if (MainActivity.getInstance() != null) {
            MainActivity.getInstance().invalidateOptionsMenu();
        }
    }

    public static void setOfflineFinished(WeiBoUser user, boolean finished) {
        ActivityHelper.putBooleanShareData(GlobalContext.getInstance(), user.getIdstr() + "Offline_finished", finished);
    }

    public static boolean isOfflineFinished(WeiBoUser user) {
        return ActivityHelper.getBooleanShareData(GlobalContext.getInstance(), user.getIdstr() + "Offline_finished", false);
    }

}
