package org.aisen.weibo.sina.ui.fragment.timeline;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.KeyGenerator;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.common.utils.ViewUtils;
import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.Group;
import org.aisen.weibo.sina.sinasdk.bean.PicUrls;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.support.utils.ThemeUtils;
import org.aisen.weibo.sina.support.utils.UMengUtil;
import org.aisen.weibo.sina.ui.activity.picture.PicsActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 分组微博列表
 *
 * Created by wangdan on 16/1/2.
 */
public class TimelineGroupsFragment extends ATimelineFragment {

    public static TimelineGroupsFragment newInstance(Group group) {
        TimelineGroupsFragment fragment = new TimelineGroupsFragment();

        Bundle args = new Bundle();
        args.putSerializable("group", group);
        fragment.setArguments(args);

        return fragment;
    }

    private Group group;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        group = savedInstanceState == null ? (Group) getArguments().getSerializable("group")
                                           : (Group) savedInstanceState.getSerializable("group");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("group", group);
    }

    @Override
    public void requestData(RefreshMode mode) {
        new GroupsTimelineTask(mode).execute();
    }

    class GroupsTimelineTask extends ATimelineTask {

        public GroupsTimelineTask(RefreshMode mode) {
            super(mode);
        }

        @Override
        public StatusContents getStatusContents(Params params) throws TaskException {
            params.addParameter("list_id", group.getIdstr());

            return SinaSDK.getInstance(AppContext.getAccount().getAccessToken(), getTaskCacheMode(this)).friendshipGroupsTimeline(params);
        }

    }

    @Override
    public boolean onToolbarDoubleClick() {
        requestDataDelaySetRefreshing(AppSettings.REQUEST_DATA_DELAY);
        getRefreshView().scrollToPosition(0);

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        UMengUtil.onPageStart(getActivity(), "好友分组微博页");
    }

    @Override
    public void onPause() {
        super.onPause();

        UMengUtil.onPageEnd(getActivity(), "好友分组微博页");
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        AisenUtils.showMenuDialog(TimelineGroupsFragment.this, view, new String[]{ "微博评论", "下载图片" }, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    TimelineGroupsFragment.super.onItemClick(parent, view, position, id);
                }
                else {
                    downloadPics(getAdapterItems().get(position).getId() + "");
                }
            }

        });
    }

    private void downloadPics(final String id) {
        new WorkTask<Void, String, Void>() {

            @Override
            protected void onPrepare() {
                super.onPrepare();

                ViewUtils.createProgressDialog(getActivity(), "开始准备下载图片...", ThemeUtils.getThemeColor()).show();
            }

            @Override
            public Void workInBackground(Void... voids) throws TaskException {
                final List<StatusContent> contents = new ArrayList<>();

                long statusId = 0;
                while (true) {
                    Params params = new Params();
                    params.addParameter("list_id", group.getIdstr());
                    if (statusId > 0) {
                        params.addParameter("max_id", statusId + "");
                    }
                    params.addParameter("count", String.valueOf(200));

                    final StatusContents statusContents = SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).friendshipGroupsTimeline(params);
                    if (statusContents != null && statusContents.getStatuses() != null && statusContents.getStatuses().size() > 0) {
                        contents.addAll(statusContents.getStatuses());

                        publishProgress("共 " + contents.size() + " 条微博");

                        statusId = statusContents.getStatuses().get(statusContents.getStatuses().size() - 1).getId() - 1;
                    }
                    else {
                        break;
                    }
                }

                for (int i = 0; i < contents.size(); i++) {
                    final int index = i;
                    StatusContent mBean = contents.get(i);

                    if (mBean.getRetweeted_status() == null) {
                        final PicUrls[] picUrlses = mBean.getPic_urls();
                        if (picUrlses != null && picUrlses.length > 0) {
                            publishProgress("共 " + contents.size() + " 条微博 , 当前第 " + (i + 1) + " 条, [" + picUrlses.length + " / " + 0 + "]");

                            final CountDownLatch countDownLatch = new CountDownLatch(picUrlses.length);
                            for (PicUrls picUrlse : picUrlses) {
                                try {
                                    final String image = picUrlse.getThumbnail_pic().replace("thumbnail", "large");

                                    final File file = new File(SystemUtils.getSdcardPath() + File.separator + "女神" + File.separator + mBean.getUser().getScreen_name() + File.separator + KeyGenerator.generateMD5(image) + ".jpg");

                                    if (!file.getParentFile().exists()) {
                                        file.getParentFile().mkdirs();
                                    }

                                    if (!file.exists()) {
                                        new Thread() {

                                            @Override
                                            public void run() {
                                                try {
                                                    File tempFile = new File(file.getAbsolutePath() + ".temp");
                                                    if (tempFile.exists()) {
                                                        tempFile.delete();
                                                    }
                                                    FileOutputStream out = new FileOutputStream(file);
                                                    Request request = new Request.Builder().url(image).build();
                                                    Response response = GlobalContext.getOkHttpClient().newCall(request).execute();
                                                    if (response.isSuccessful()) {
                                                        InputStream in = response.body().byteStream();

                                                        // 获取图片数据
                                                        byte[] buffer = new byte[1024 * 8];
                                                        int readLen = -1;
                                                        while ((readLen = in.read(buffer)) != -1) {
                                                            out.write(buffer, 0, readLen);
                                                        }
                                                        in.close();
                                                        out.close();
                                                    }
                                                    tempFile.renameTo(file);

//													ImageConfig config = new ImageConfig();
//													config.setId("Large");
//													BitmapLoader.BitmapBytesAndFlag bitmapBytesAndFlag = BitmapLoader.getInstance().doDownload(image, config);
//													byte[] bytes = bitmapBytesAndFlag.bitmapBytes;
//
//													FileUtils.writeFile(file, bytes);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }

                                                countDownLatch.countDown();
                                                publishProgress("共 " + contents.size() + " 条微博 , 当前第 " + (index + 1) + " 条, [" + picUrlses.length + " / " + (picUrlses.length - countDownLatch.getCount()) + "]");
//                                                    publishProgress("[" + picUrlses.length + " / " + (picUrlses.length - countDownLatch.getCount()) + "]");

                                                if (getActivity() != null) {
                                                    SystemUtils.scanPhoto(getActivity(), file);
                                                }
                                            }

                                        }.start();
                                    }
                                    else {
                                        countDownLatch.countDown();

                                        if (getActivity() != null) {
                                            SystemUtils.scanPhoto(getActivity(), file);
                                        }
                                    }
                                } catch (Throwable e) {
                                    countDownLatch.countDown();
                                }
                            }
                            try {
                                countDownLatch.await(2 * 60 * 1000, TimeUnit.SECONDS);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(String... values) {
                super.onProgressUpdate(values);

                if (values != null && values.length > 0) {
                    ViewUtils.updateProgressDialog(values[0]);
                }
            }

            @Override
            protected void onFinished() {
                super.onFinished();

                ViewUtils.dismissProgressDialog();
            }

        }.execute();
    }

}
