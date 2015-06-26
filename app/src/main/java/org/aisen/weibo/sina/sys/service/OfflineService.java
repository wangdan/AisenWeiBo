package org.aisen.weibo.sina.sys.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.support.bean.GroupOfflineStatus;
import org.aisen.weibo.sina.support.bean.OfflinePictureBean;
import org.aisen.weibo.sina.support.db.SinaDB;
import org.aisen.weibo.sina.support.notifier.OfflineNotifier;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.support.utils.OfflineUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.Group;
import org.aisen.weibo.sina.sinasdk.bean.PicUrls;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
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
import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.orm.utils.FieldUtils;

/**
 * 离线数据的服务
 *
 * Created by wangdan on 15/5/3.
 */
public class OfflineService extends Service {

    public static final String TAG = "Offline-Service";

    public static final String ACTION_TOGGLE = "org.aisen.weibo.sina.ACTION_TOGGLE";// 开始离线

    public static final String ACTION_STATUS_OFFLINE = "org.aisen.weibo.sina.ACTION_STATUS_OFFLINE";//分组离线完成

    private static final int MAC_PICTURE_SIZE = 300;

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

    public enum  OfflineStatus {
        init, prepare, loadStatus, loadPicture, cancel, finished
    }

    // 当前服务状态
    private OfflineStatus mStatus = OfflineStatus.init;
    // 当次离线任务的分组
    private List<Group> mGroups;
    private LinkedBlockingQueue<OfflinePictureBean> mPictures = new LinkedBlockingQueue<>();
    private List<GroupOfflineStatus> mOfflineGroupStatus;

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

        return super.onStartCommand(intent, flags, startId);
    }

    // 准备离线
    private void prepareOffline() {
        mStatus = OfflineStatus.prepare;
        new WorkTask<Void, Void, Void>() {

            @Override
            public Void workInBackground(Void... params) throws TaskException {
                int version = OfflineUtils.getLastVersion();

                String selection = String.format(" %s = ? and %s = ? ", FieldUtils.OWNER, "version");
                String[] selectionArgs = new String[]{ AppContext.getUser().getIdstr(), version + "" };
                List<GroupOfflineStatus> statusList = SinaDB.getOfflineSqlite().select(GroupOfflineStatus.class, selection, selectionArgs);
                // 最后一次离线时，是否所有的分组都离线完成，如果是，说明这次数据都离线完成了，开启一次新的离线
                boolean newVersion = true;
                if (statusList.size() > 0) {
                    for (GroupOfflineStatus status : statusList) {
                        // 这个分组没有完全离线
                        if ((status.getStatus() & 0x10) == 0) {
                            newVersion = false;
                            break;
                        }
                    }
                }
                newVersion = true;
                if (newVersion) {
                    OfflineUtils.newVersion();

                    statusList = new ArrayList<GroupOfflineStatus>();
                    for (Group group : mGroups) {
                        GroupOfflineStatus newStatus = new GroupOfflineStatus();
                        newStatus.setGroupId(group.getIdstr());
                        newStatus.setVersion(OfflineUtils.getLastVersion());
                        statusList.add(newStatus);

                        SinaDB.getOfflineSqlite().insertOrReplace(OfflineUtils.getLoggedExtra(null), newStatus);
                    }
                }
                else {
                    Logger.d(TAG, "离线version未更改");
                }

                // 将失败离线的图片状态改成未下载
                ContentValues values = new ContentValues();
                values.put("status", 0);
                String whereClause = String.format("%s = ? and %s < ? ", FieldUtils.OWNER, "status");
                String[] whereArgs = new String[]{ AppContext.getUser().getIdstr(), "10" };
                int size = SinaDB.getOfflineSqlite().update(OfflinePictureBean.class, values, whereClause, whereArgs);
                Logger.d(TAG, "将%d张图片状态修改成未下载", size);
                List<OfflinePictureBean> beans = SinaDB.getOfflineSqlite().select(OfflineUtils.getLoggedExtra(null), OfflinePictureBean.class);
                for (OfflinePictureBean bean : beans)
                if (bean.getStatus() != 10) {
                    Logger.i(TAG, bean);
                }

                mOfflineGroupStatus = statusList;
                Logger.d(TAG, "准备离线");
                Logger.d(TAG, "====================");
                Logger.d(TAG, "当次 version = %d" , OfflineUtils.getLastVersion());
                for (GroupOfflineStatus status : mOfflineGroupStatus) {
                    status.setStatus(status.getStatus() & 0x0FF);// 把失败的状态干掉
                    Logger.d(TAG, status);
                }
                Logger.d(TAG, "====================");
                Logger.d(TAG, "结束准备");

                checkStatusOffline();

                return null;
            }

        }.execute();
    }

    // 是否还有微博离线
    private void checkStatusOffline() {
        boolean none = true;
        for (GroupOfflineStatus status : mOfflineGroupStatus) {
            if ((status.getStatus() & 0x100) > 0) {
                Logger.d(TAG, "分组%s下载失败，排除当次下载", status.getGroupId());

                continue;
            }

            if ((status.getStatus() & 0x02) == 0) {
                new LoadStatusTask(status).execute();

                none = false;

                break;
            }
        }

        if (none) {
            mNotifier.notifyStatusSuccess(mOfflineGroupStatus);

            int count = 0;
            for (GroupOfflineStatus status : mOfflineGroupStatus)
                count += status.getStatusCount();
            Logger.d(TAG, "当次离线所有微博加载完了，共离线%d条微博", count);

            preparePicture();
        }
    }

    private synchronized void preparePicture() {
        if (SystemUtils.getNetworkType() != SystemUtils.NetWorkType.wifi) {
            stopSelf();

            return;
        }

        if (mPictures.size() == 0) {
            int result = 0;
            // 每次离线300个
            String selection = String.format(" %s = ? and %s = ? ", "version", "status");
            String[] selectionArgs = new String[]{ OfflineUtils.getLastVersion() + "", "0" };
            List<OfflinePictureBean> list = SinaDB.getOfflineSqlite()
                    .select(OfflinePictureBean.class, selection, selectionArgs,
                            null, null, null, "" + MAC_PICTURE_SIZE);
            if (list.size() > 0) {
                for (OfflinePictureBean bean : list) {
                    mPictures.add(bean);
                    // 将图片的状态修改成正在下载
                    bean.setStatus(2);
                }

                SinaDB.getOfflineSqlite().update(OfflineUtils.getLoggedExtra(null), list);

                Logger.w(TAG, "从db找到未下载的图片%d张", list.size());

                result = 1;
            }

            if (result == 1) {
                preparePicture();
            }
            else {
                mNotifier.notifyPictureSuccess(mOfflineGroupStatus);

                stopSelf();
            }
        }
        else {
            // 开始下载队列里的图片
            downloadPicture();
        }
    }

    // 开始下载图片
    private void downloadPicture() {
        if (IMAGE_POOL_EXECUTOR == null) {
            IMAGE_POOL_EXECUTOR = Executors.newFixedThreadPool(AppSettings.offlinePicTaskSize(), sThreadFactory);
        }

        for (int i = 0; i < AppSettings.offlinePicTaskSize(); i++) {
            OfflinePictureBean bean = pollPicture();
            if (bean != null)
                new LoadPictureTask(bean).executeOnExecutor(IMAGE_POOL_EXECUTOR);
            else {
                break;
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    LoadStatusTask mStatusTask;
    class LoadStatusTask extends WorkTask<Void, Void, Boolean> {

        GroupOfflineStatus status;
        Group group;

        LoadStatusTask(GroupOfflineStatus status) {
            mStatusTask = this;

            this.status = status;
            this.group = getGroupById(status.getGroupId());

            Logger.d(TAG, "开始离线分组%s", group.getName());

            mNotifier.notifyStatus(mOfflineGroupStatus, group);
        }

        @Override
        public Boolean workInBackground(Void... p) throws TaskException {
            int repeat = 3;

            int count = AppSettings.getOfflineStatusSize();

            String max_id = null;
            while (--repeat >= 0) {
                try {
                    Params params = new Params();
                    params.addParameter("list_id", group.getIdstr());
                    if (!TextUtils.isEmpty(max_id))
                        params.addParameter("max_id", max_id);
                    params.addParameter("count", String.valueOf(count));

                    StatusContents statusContents = SinaSDK.getInstance(AppContext.getToken()).friendshipGroupsTimeline(params);
                    if (statusContents.getStatuses().size() > 0) {
                        Logger.d(TAG, "分组%s当次加载微博%d条，节省流量%s", group.getName(), count, AisenUtils.getUnit(statusContents.getLength()));

                        // 处理微博的图片
                        handlerPicture(status, statusContents.getStatuses(), group);

                        status.setStatusLength(status.getStatusLength() + statusContents.getLength());
                        status.setStatusCount(statusContents.getStatuses().size() + status.getStatusCount());
                    }
                    else {
                        Logger.d(TAG, "分组%s加载0条微博", group.getName());
                    }

                    status.setStatus(status.getStatus() | 0x02);
                    updateStatus(status);

                    // 离线完成一个，就广播
                    Intent intent = new Intent();
                    intent.setAction(ACTION_STATUS_OFFLINE);
                    intent.putExtra("id", group.getIdstr());
                    sendBroadcast(intent);

                    return true;
                } catch (Exception e) {
                }
            }

            status.setStatus(status.getStatus() | 0x100);// 当次加载失败，排除掉当次下载

            return null;
        }

        @Override
        protected void onSuccess(Boolean aBoolean) {
            super.onSuccess(aBoolean);

            checkStatusOffline();
        }

    }

    class LoadPictureTask extends WorkTask<Void, Void, Void> {

        Group group;
        GroupOfflineStatus status;
        OfflinePictureBean bean;
        LoadPictureTask(OfflinePictureBean bean) {
            this.bean = bean;
            this.status = getStatusById(bean.getGroupId());
            this.group = getGroupById(bean.getGroupId());
        }

        @Override
        public Void workInBackground(Void... params) throws TaskException {
            String url = bean.getThumb().replace("thumbnail", "bmiddle");

            Logger.v(TAG, "开始离线图片%s", url);

            File file = BitmapLoader.getInstance().getCacheFile(url);
            if (!file.exists()) {
                try {
                    if (!file.getParentFile().exists())
                        file.getParentFile().mkdirs();

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

                    FileOutputStream out = new FileOutputStream(file);
                    // 获取图片数据
                    byte[] buffer = new byte[1024 * 8];
                    int readLen = -1;
                    while ((readLen = in.read(buffer)) != -1) {
                        out.write(buffer, 0, readLen);
                    }
                    out.flush();
                    in.close();
                    out.close();

                    bean.setLength(length);

                    Logger.v(TAG, "离线图片成功，url = %s", url);
                } catch (Exception e) {
                    e.printStackTrace();

                    Logger.e(TAG, "离线图片失败" + e.getMessage() + "" + e);

                    bean.setStatus(1);
                    SinaDB.getOfflineSqlite().insertOrReplace(OfflineUtils.getLoggedExtra(null), bean);

                    throw new TaskException("");
                }
            }
            else {
                bean.setLength(file.length());
            }

            bean.setStatus(10);
            updateStatus(status);
            SinaDB.getOfflineSqlite().insertOrReplace(OfflineUtils.getLoggedExtra(null), bean);

            return null;
        }

        @Override
        protected void onFinished() {
            super.onFinished();

//            mNotifier.notifyPicture(mOfflineGroupStatus);
            notifyPictureProgress();

            if (SystemUtils.getNetworkType() == SystemUtils.NetWorkType.wifi) {
                OfflinePictureBean bean = pollPicture();
                if (bean != null)
                    new LoadPictureTask(bean).executeOnExecutor(IMAGE_POOL_EXECUTOR);
                else
                    preparePicture();
            }
            else {
                if (mStatus != OfflineStatus.finished) {
                    mStatus = OfflineStatus.cancel;

                    stopSelf();
                }
            }
        }
    }


    NotificationCompat.Builder progressNuilder;
    long refreshTime = 0;
    public void notifyPictureProgress() {
        if (refreshTime == 0)
            refreshTime = System.currentTimeMillis();

        if (System.currentTimeMillis() - refreshTime >= 1000) {
            refreshTime = System.currentTimeMillis();

            String whereClause = String.format(" %s = ? ", "version");
            String[] whereArgs = new String[]{ String.valueOf(OfflineUtils.getLastVersion()) };
            long totalCount = SinaDB.getOfflineSqlite().count(OfflinePictureBean.class, whereClause, whereArgs);
            whereClause = String.format(" %s = ? and %s = ? ", "version", "status");
            whereArgs = new String[]{ String.valueOf(OfflineUtils.getLastVersion()), String.valueOf(10) };
            long downloadCount = SinaDB.getOfflineSqlite().count(OfflinePictureBean.class, whereClause, whereArgs);

            String title = String.format("正在离线图片");
            if (progressNuilder == null) {
                progressNuilder = new NotificationCompat.Builder(this);

                progressNuilder.setSmallIcon(R.drawable.statusbar_ic_send_success)
                        .setContentTitle(title)
                        .setContentInfo(String.format("%s/%s", String.valueOf(downloadCount), String.valueOf(totalCount)))
                        .setOnlyAlertOnce(true)
                        .setProgress(Math.round(totalCount), Math.round(downloadCount), false);
            }

            mNotifier.notify(OfflineNotifier.OfflinePicture, 0, progressNuilder);
        }
    }

    private synchronized OfflinePictureBean pollPicture() {
        return mPictures.poll();
    }

    private void handlerPicture(GroupOfflineStatus offlineStatus, List<StatusContent> statuses, Group group) {
        List<OfflinePictureBean> pictureBeans = new ArrayList<>();

        for (StatusContent status : statuses) {
            if (status.getRetweeted_status() != null)
                status = status.getRetweeted_status();

            if (status.getPic_urls() != null && status.getPic_urls().length > 0) {
                for (PicUrls pic : status.getPic_urls()) {
                    File file = BitmapLoader.getInstance().getCacheFile(pic.getThumbnail_pic());
                    if (file.exists()) {
                        continue;
                    }

                    OfflinePictureBean bean = new OfflinePictureBean();
                    bean.setBeanId(KeyGenerator.generateMD5(pic.getThumbnail_pic()));
                    bean.setThumb(pic.getThumbnail_pic());
                    bean.setVersion(OfflineUtils.getLastVersion());
                    bean.setGroupId(group.getIdstr());

                    if (mPictures.size() < MAC_PICTURE_SIZE) {
                        bean.setStatus(2);
                        mPictures.add(bean);
                    }
                    pictureBeans.add(bean);
                }
            }
        }

        Logger.i(TAG, "存入%d条需要加载的图片", pictureBeans.size());
        SinaDB.getOfflineSqlite().insertOrReplace(OfflineUtils.getLoggedExtra(null), pictureBeans);
    }

    // 更新状态
    private synchronized void updateStatus(GroupOfflineStatus status) {
        SinaDB.getOfflineSqlite().insertOrReplace(OfflineUtils.getLoggedExtra(null), status);
    }

    private Group getGroupById(String groupId) {
        for (Group group : mGroups) {
            if (group.getId().equalsIgnoreCase(groupId))
                return group;
        }

        return null;
    }

    private GroupOfflineStatus getStatusById(String groupId) {
        for (GroupOfflineStatus group : mOfflineGroupStatus) {
            if (group.getGroupId().equalsIgnoreCase(groupId))
                return group;
        }

        return null;
    }

    public interface OfflineLength {

        public void setLength(long length);

    }

    public static abstract class OfflineBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && ACTION_STATUS_OFFLINE.equalsIgnoreCase(intent.getAction())) {
                onReceiveStatusOfflined(intent.getStringExtra("id"));
            }
        }

        abstract protected void onReceiveStatusOfflined(String groupId);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (AppSettings.offlinePicture() && mStatus == OfflineStatus.cancel)
            mNotifier.notifyPictureSuccess(mOfflineGroupStatus);

        mStatus = OfflineStatus.finished;

        Logger.d(TAG, "离线服务停止");
    }

}
