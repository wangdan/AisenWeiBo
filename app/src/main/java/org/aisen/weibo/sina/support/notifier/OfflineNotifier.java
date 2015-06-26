package org.aisen.weibo.sina.support.notifier;

import android.app.Notification;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.bean.GroupOfflineStatus;
import org.aisen.weibo.sina.support.bean.OfflinePictureBean;
import org.aisen.weibo.sina.support.db.SinaDB;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.support.utils.OfflineUtils;
import org.aisen.weibo.sina.sinasdk.bean.Group;

import java.util.List;

/**
 * Created by wangdan on 15/5/3.
 */
public class OfflineNotifier extends Notifier {

    public OfflineNotifier(Context context) {
        super(context);
    }

    public void notifyStatus(List<GroupOfflineStatus> statuses, Group group) {
        long lenght = 0;
        for (GroupOfflineStatus status : statuses)
            lenght += status.getStatusLength();

        String title = String.format("正在离线 %s", group.getName());
        String content = String.format("节省流量%s", AisenUtils.getUnit(lenght));


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setSmallIcon(R.drawable.statusbar_ic_send_success)
                                .setContentTitle(title)
                                .setAutoCancel(true)
                                .setContentText(content);

        notify(OfflineStatus, 0, builder);
    }

    public void notifyStatusSuccess(List<GroupOfflineStatus> statuses) {
        long lenght = 0;
        int count = 0;
        for (GroupOfflineStatus status : statuses) {
            lenght += status.getStatusLength();
            count += status.getStatusCount();
        }

        String title = String.format("%d个分组离线完成", statuses.size());
        String content = String.format("%d条微博，节省流量%s", count, AisenUtils.getUnit(lenght));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setSmallIcon(R.drawable.statusbar_ic_send_success)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setContentText(content);

        notify(OfflineStatus, 0, builder);
    }

    public void notifyCmt(List<GroupOfflineStatus> statuses, Group group) {
        long lenght = 0;
        for (GroupOfflineStatus status : statuses)
            lenght += status.getCmtLength();

        long count = 0;
        for (GroupOfflineStatus status : statuses)
            count += status.getCmtCount();

        String title = String.format("正在离线分组 %s 的评论", group.getName());
        String content = String.format("%d个评论, 节省流量%s", count, AisenUtils.getUnit(lenght));


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setSmallIcon(R.drawable.statusbar_ic_send_success)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setContentText(content);

        notify(OfflineCmt, 0, builder);
    }

    public void notifyCmtSuccess(List<GroupOfflineStatus> statuses) {
        long lenght = 0;
        for (GroupOfflineStatus status : statuses)
            lenght += status.getCmtLength();

        long count = 0;
        for (GroupOfflineStatus status : statuses)
            count += status.getCmtCount();

        String title = String.format("%d个分组评论离线完成", statuses.size());
        String content = String.format("%d个评论，节省流量%s", count, AisenUtils.getUnit(lenght));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setSmallIcon(R.drawable.statusbar_ic_send_success)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setContentText(content);

        notify(OfflineCmt, 0, builder);
    }

    public void notifyPicture(List<GroupOfflineStatus> statuses) {

//        String title = String.format("正在离线图片");
//        String content = String.format("%d/%d, 节省流量%s", downloadCount, count, AisenUtils.getUnit(lenght));
//
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
//
//        builder.setSmallIcon(R.drawable.statusbar_ic_send_success)
//                .setContentTitle(title)
//                .setContentInfo(String.format("%d/%d", downloadCount, count))
//                .setOnlyAlertOnce(true)
//                .setProgress(10000, Math.round(downloadCount * 1.0f / count * 10000), false);
//
//        notify(OfflinePicture, 0, builder);
    }

    public void notifyPictureSuccess(List<GroupOfflineStatus> statuses) {
        new WorkTask<Void, Void, long[]>() {

            @Override
            public long[] workInBackground(Void... params) throws TaskException {
                long[] result = new long[2];
                String whereClause = String.format(" %s = ? ", "version");
                String[] whereArgs = new String[]{ String.valueOf(OfflineUtils.getLastVersion()) };

                result[0] = SinaDB.getOfflineSqlite().count(OfflinePictureBean.class, whereClause, whereArgs);
                result[1] = SinaDB.getOfflineSqlite().sum(OfflinePictureBean.class, "length", whereClause, whereArgs);

                return result;
            }

            @Override
            protected void onSuccess(long[] longs) {
                super.onSuccess(longs);

                String title = "图片离线完成";
                String content = String.format("%s张图片，节省流量%s", String.valueOf(longs[0]) ,AisenUtils.getUnit(longs[1]));

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

                builder.setSmallIcon(R.drawable.statusbar_ic_send_success)
                        .setContentTitle(title)
                        .setAutoCancel(true)
                        .setContentText(content);

                OfflineNotifier.this.notify(OfflinePicture, 0, builder);
            }

        }.execute();
    }

    public void notify(int request, int status, NotificationCompat.Builder builder) {
        Notification notification = builder.build();
        notify(request, notification);
    }

}
