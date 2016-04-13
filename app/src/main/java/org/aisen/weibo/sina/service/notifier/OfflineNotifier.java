package org.aisen.weibo.sina.service.notifier;

import android.app.Notification;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.sinasdk.bean.Group;
import org.aisen.weibo.sina.support.utils.AisenUtils;

/**
 * Created by wangdan on 15/5/3.
 */
public class OfflineNotifier extends Notifier {

    public OfflineNotifier(Context context) {
        super(context);
    }

    public void notifyStatus(Group group, long offlineLengh) {
        String title = String.format("离线分组[%s]", group.getName());
        String content = String.format("节省流量%s", AisenUtils.getUnit(offlineLengh));


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setSmallIcon(R.drawable.statusbar_ic_send_success)
                                .setContentTitle(title)
                                .setAutoCancel(true)
                                .setContentText(content);

        notify(OfflineStatus, 0, builder);
    }

    public void notifyStatusSuccess(int groupSize, int statusSize, long statusLength) {
        String title = String.format("%d个分组离线完成", groupSize);
        String content = String.format("共%d条微博，节省流量%s", statusSize, AisenUtils.getUnit(statusLength));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setSmallIcon(R.drawable.statusbar_ic_send_success)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setContentText(content);

        notify(OfflineStatus, 0, builder);
    }

    public void notifyPictureSuccess(int picSize, long picLength) {
        String title = "图片离线完成";
        String content = String.format("%s张图片，节省流量%s", String.valueOf(picSize) , AisenUtils.getUnit(picLength));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setSmallIcon(R.drawable.statusbar_ic_send_success)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setContentText(content);

        OfflineNotifier.this.notify(OfflinePicture, 0, builder);
    }

    public void notify(int request, int status, NotificationCompat.Builder builder) {
        Notification notification = builder.build();
        notify(request, notification);
    }

}
