package org.aisen.weibo.sina.support.utils;

import android.app.Activity;
import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.md.MDHelper;
import org.aisen.android.common.setting.SettingUtility;
import org.aisen.android.common.utils.DateUtils;
import org.aisen.android.common.utils.FileUtils;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.common.utils.Utils;
import org.aisen.android.common.utils.ViewUtils;
import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.component.bitmaploader.core.BitmapDecoder;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.android.ui.fragment.APagingFragment;
import org.aisen.android.ui.fragment.ATabsFragment;
import org.aisen.android.ui.fragment.ATabsTabLayoutFragment;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.GroupSortResult;
import org.aisen.weibo.sina.sinasdk.bean.StatusComment;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;
import org.aisen.weibo.sina.ui.activity.base.MainActivity;
import org.aisen.weibo.sina.ui.activity.base.SinaCommonActivity;
import org.aisen.weibo.sina.ui.activity.profile.UserProfileActivity;
import org.aisen.weibo.sina.ui.activity.publish.PublishActivity;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;
import org.aisen.weibo.sina.ui.fragment.comment.TimelineCommentFragment;
import org.aisen.weibo.sina.ui.fragment.comment.TimelineDetailPagerFragment;
import org.aisen.weibo.sina.ui.fragment.timeline.ATimelineFragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import fr.castorflex.android.circularprogressbar.CircularProgressDrawable;

/**
 * Created by wangdan on 15/4/12.
 */
public class AisenUtils {

    public static int getThemeColor(Context context) {
        final int materialBlue = Color.parseColor("#ff0000");
        int themeColor = MDHelper.resolveColor(context, R.attr.themeColor, materialBlue);
        return themeColor;
    }

    public static String getUserScreenName(WeiBoUser user) {
        if (AppSettings.isShowRemark() && !TextUtils.isEmpty(user.getRemark()))
            return user.getRemark();

        return user.getScreen_name();
    }

    public static String getUserKey(String key, WeiBoUser user) {
        return key + "-" + user.getIdstr();
    }

    public static File getUploadFile(Context context, File source) {
        Logger.w("原图图片大小" + (source.length() / 1024) + "KB");

        if (source.getName().toLowerCase().endsWith(".gif")) {
            Logger.w("上传图片是GIF图片，上传原图");
            return source;
        }

        File file = null;

        String imagePath = GlobalContext.getInstance().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + File.separator +
                                        SettingUtility.getStringSetting("draft") + File.separator;

        int sample = 1;
        int maxSize = 0;

        int type = AppSettings.getUploadSetting();
        // 自动，WIFI时原图，移动网络时高
        if (type == 0) {
            if (SystemUtils.getNetworkType(context) == SystemUtils.NetWorkType.wifi)
                type = 1;
            else
                type = 2;
        }
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(source.getAbsolutePath(), opts);
        switch (type) {
            // 原图
            case 1:
                Logger.w("原图上传");
                file = source;
                break;
            // 高
            case 2:
                sample = BitmapDecoder.calculateInSampleSize(opts, 1920, 1080);
                Logger.w("高质量上传");
                maxSize = 700 * 1024;
                imagePath = imagePath + "高" + File.separator + source.getName();
                file = new File(imagePath);
                break;
            // 中
            case 3:
                Logger.w("中质量上传");
                sample = BitmapDecoder.calculateInSampleSize(opts, 1280, 720);
                maxSize = 300 * 1024;
                imagePath = imagePath + "中" + File.separator + source.getName();
                file = new File(imagePath);
                break;
            // 低
            case 4:
                Logger.w("低质量上传");
                sample = BitmapDecoder.calculateInSampleSize(opts, 1280, 720);
                maxSize = 100 * 1024;
                imagePath = imagePath + "低" + File.separator + source.getName();
                file = new File(imagePath);
                break;
            default:
                break;
        }

        // 压缩图片
        if (type != 1 && !file.exists()) {
            Logger.w(String.format("压缩图片，原图片 path = %s", source.getAbsolutePath()));
            byte[] imageBytes = FileUtils.readFileToBytes(source);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                out.write(imageBytes);
            } catch (Exception e) {
            }

            Logger.w(String.format("原图片大小%sK", String.valueOf(imageBytes.length / 1024)));
            if (imageBytes.length > maxSize) {
                // 尺寸做压缩
                BitmapFactory.Options options = new BitmapFactory.Options();

                if (sample > 1) {
                    options.inSampleSize = sample;
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, options);
                    Logger.w(String.format("压缩图片至大小：%d*%d", bitmap.getWidth(), bitmap.getHeight()));
                    out.reset();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    imageBytes = out.toByteArray();
                }

                options.inSampleSize = 1;
                if (imageBytes.length > maxSize) {
                    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, options);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, options);

                    int quality = 90;
                    out.reset();
                    Logger.w(String.format("压缩图片至原来的百分之%d大小", quality));
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
                    while (out.toByteArray().length > maxSize) {
                        out.reset();
                        quality -= 10;
                        Logger.w(String.format("压缩图片至原来的百分之%d大小", quality));
                        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
                    }
                }

            }

            try {
                if (!file.getParentFile().exists())
                    file.getParentFile().mkdirs();

                Logger.w(String.format("最终图片大小%sK", String.valueOf(out.toByteArray().length / 1024)));
                FileOutputStream fo = new FileOutputStream(file);
                fo.write(out.toByteArray());
                fo.flush();
                fo.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return file;
    }

    public static void showMenuDialog(ABaseFragment fragment, final View targetView,
                                      String[] menuArr, DialogInterface.OnClickListener onItemClickListener) {
        new AlertDialogWrapper.Builder(fragment.getActivity())
                .setItems(menuArr, onItemClickListener)
                .show();
    }

    public static String getFirstId(@SuppressWarnings("rawtypes") List datas) {
        int size = datas.size();
        if (size > 0)
            return getId(datas.get(0));
        return null;
    }

    public static String getLastId(@SuppressWarnings("rawtypes") List datas) {
        int size = datas.size();
        if (size > 0)
            return getId(datas.get(size - 1));
        return null;
    }

    public static String getId(Object t) {
        try {
            Field idField = t.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            return idField.get(t).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    public static String convDate(String time) {
        try {
            Context context = GlobalContext.getInstance();
            Resources res = context.getResources();

            StringBuffer buffer = new StringBuffer();

            Calendar createCal = Calendar.getInstance();

            if (time.length() == 13) {
                try {
                    createCal.setTimeInMillis(Long.parseLong(time));
                } catch (Exception e) {
                    createCal.setTimeInMillis(Date.parse(time));
                }
            }
            else {
                createCal.setTimeInMillis(Date.parse(time));
            }

            Calendar currentcal = Calendar.getInstance();
            currentcal.setTimeInMillis(System.currentTimeMillis());

            long diffTime = (currentcal.getTimeInMillis() - createCal.getTimeInMillis()) / 1000;

            // 同一月
            if (currentcal.get(Calendar.MONTH) == createCal.get(Calendar.MONTH)) {
                // 同一天
                if (currentcal.get(Calendar.DAY_OF_MONTH) == createCal.get(Calendar.DAY_OF_MONTH)) {
                    if (diffTime < 3600 && diffTime >= 60) {
                        buffer.append((diffTime / 60) + res.getString(R.string.msg_few_minutes_ago));
                    } else if (diffTime < 60) {
                        buffer.append(res.getString(R.string.msg_now));
                    } else {
                        buffer.append(res.getString(R.string.msg_today)).append(" ").append(DateUtils.formatDate(createCal.getTimeInMillis(), "HH:mm"));
                    }
                }
                // 前一天
                else if (currentcal.get(Calendar.DAY_OF_MONTH) - createCal.get(Calendar.DAY_OF_MONTH) == 1) {
                    buffer.append(res.getString(R.string.msg_yesterday)).append(" ").append(DateUtils.formatDate(createCal.getTimeInMillis(), "HH:mm"));
                }
            }

            if (buffer.length() == 0) {
                buffer.append(DateUtils.formatDate(createCal.getTimeInMillis(), "MM-dd HH:mm"));
            }

            String timeStr = buffer.toString();
            if (currentcal.get(Calendar.YEAR) != createCal.get(Calendar.YEAR)) {
                timeStr = createCal.get(Calendar.YEAR) + " " + timeStr;
            }

            return timeStr;
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return time;
    }


    public static String getGender(WeiBoUser user) {
        Resources res = GlobalContext.getInstance().getResources();
        if (user != null) {
            if ("m".equals(user.getGender())) {
                return res.getString(R.string.msg_male);
            } else if ("f".equals(user.getGender())) {
                return res.getString(R.string.msg_female);
            } else if ("n".equals(user.getGender())) {
                return res.getString(R.string.msg_gender_unknow);
            }
        }
        return "";
    }

    public static String convCount(int count) {
        if (count < 10000) {
            return count + "";
        } else {
            Resources res = GlobalContext.getInstance().getResources();
            String result = new DecimalFormat("#.0").format(count * 1.0f / 10000) + res.getString(R.string.msg_ten_thousand);
            return result;
        }
    }

    public static String getCounter(int count, String append) {
        Resources res = GlobalContext.getInstance().getResources();

        if (count < 10000)
            return String.valueOf(count) + append;
        else if (count < 100 * 10000)
            return new DecimalFormat("#.0").format(count * 1.0f / 10000) + append + res.getString(R.string.msg_ten_thousand);
        else
            return new DecimalFormat("#").format(count * 1.0f / 10000) + append + res.getString(R.string.msg_ten_thousand);
    }

    public static String getCounter(int count) {
        return getCounter(count, "");
    }

    /**
     * 显示高清头像
     *
     * @param user
     * @return
     */
    public static String getUserPhoto(WeiBoUser user) {
        if (user == null)
            return "";

        if (AppSettings.isLargePhoto() && !TextUtils.isEmpty(user.getAvatar_large())) {
            return user.getAvatar_large();
        }

        return user.getProfile_image_url();
    }

    public static void setImageVerified(ImageView imgVerified, WeiBoUser user) {
        // 2014-08-27 新增判断，VerifiedType存在为null的情况
        if (user == null || user.getVerified_type() == null) {
            imgVerified.setVisibility(View.GONE);
            return;
        }

        // 黄V
        if (user.getVerified_type() == 0) {
            imgVerified.setImageResource(R.drawable.avatar_vip);
        }
        // 200:初级达人 220:高级达人
        else if (user.getVerified_type() == 200 || user.getVerified_type() == 220) {
            imgVerified.setImageResource(R.drawable.avatar_grassroot);
        }
        // 蓝V
        else if (user.getVerified_type() > 0) {
            imgVerified.setImageResource(R.drawable.avatar_enterprise_vip);
        }
        if (user.getVerified_type() >= 0)
            imgVerified.setVisibility(View.VISIBLE);
        else
            imgVerified.setVisibility(View.GONE);
    }

    public static void timelineMenuSelected(final ABaseFragment fragment, String selectedItem, final StatusContent status) {
        final String[] timelineMenuArr = GlobalContext.getInstance().getResources().getStringArray(R.array.timeline_menus);

        try {
            int position = 0;
            for (int i = 0; i < timelineMenuArr.length; i++) {
                if (timelineMenuArr[i].equals(selectedItem)) {
                    position = i;
                    break;
                }
            }

            switch (position) {
                // 原微博
                case 0:
                    TimelineDetailPagerFragment.launch(fragment.getActivity(), status.getRetweeted_status());
                    break;
                // 复制
                case 1:
                    AisenUtils.copyToClipboard(status.getText());

                    if (fragment.getActivity() != null)
                        ViewUtils.showMessage(fragment.getActivity(), R.string.msg_text_copyed);
                    break;
                // 转发
                case 2:
                    BizFragment.createBizFragment(fragment).statusRepost(status);
                    break;
                // 评论
                case 3:
                    BizFragment.createBizFragment(fragment).commentCreate(status);
                    break;
                // 收藏
                case 4:
                    BizFragment.createBizFragment(fragment).favorityCreate(status.getId() + "", null);
                    break;
                // 取消收藏
                case 5:
                    BizFragment.createBizFragment(fragment).favorityDestory(status.getId() + "", null);
                    break;
                // 删除微博
                case 6:
                    deleteStatus(fragment, status);
                    break;
                // 屏蔽微博
                case 7:
                    shieldStatus(fragment, status);
                    break;
                // 围观
                case 8:
                    PublishActivity.publishStatusRepostAndWeiguan(fragment.getActivity(), null, status);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void deleteStatus(final ABaseFragment fragment, final StatusContent status) {
        new AlertDialogWrapper.Builder(fragment.getActivity())
                .setMessage(R.string.msg_del_status_remind)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BizFragment.createBizFragment(fragment).statusDestory(status.getId() + "", new BizFragment.OnStatusDestoryCallback() {

                            @SuppressWarnings({"rawtypes"})
                            @Override
                            public void onStatusDestory(StatusContent status) {
                                if (fragment instanceof ATimelineFragment) {
                                    APagingFragment aRefreshFragment = ((APagingFragment) fragment);
                                    for (Object so : aRefreshFragment.getAdapterItems()) {
                                        StatusContent s = (StatusContent) so;
                                        if (String.valueOf(s.getId()).equals(String.valueOf(status.getId()))) {
                                            aRefreshFragment.getAdapterItems().remove(s);
                                            aRefreshFragment.getAdapter().notifyDataSetChanged();
                                            break;
                                        }
                                    }
                                } else {
                                    if (fragment.getActivity() != null && fragment instanceof TimelineCommentFragment) {
                                        Intent data = new Intent();
                                        data.putExtra("status", status.getId());

                                        fragment.getActivity().setResult(Activity.RESULT_OK, data);
                                        fragment.getActivity().finish();
                                    }

                                    if (fragment.getActivity() != null)
                                        ViewUtils.showMessage(fragment.getActivity(), R.string.delete_success);
                                }
                            }

                            @Override
                            public boolean onFaild(TaskException e) {
                                if (fragment.getActivity() != null)
                                    ViewUtils.showMessage(fragment.getActivity(), R.string.delete_faild);

                                return true;
                            }
                        });
                    }
                })
                .show();
    }

    private static void shieldStatus(final ABaseFragment fragment, final StatusContent status) {
        new AlertDialogWrapper.Builder(fragment.getActivity())
                .setMessage(R.string.msg_shield_remind)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new WorkTask<Void, Void, GroupSortResult>() {

                            @Override
                            protected void onPrepare() {
                                super.onPrepare();

                                Resources res = GlobalContext.getInstance().getResources();
                                ViewUtils.createProgressDialog(fragment.getActivity(), res.getString(R.string.processing), ThemeUtils.getThemeColor()).show();
                            }

                            @Override
                            protected void onFinished() {
                                super.onFinished();

                                ViewUtils.dismissProgressDialog();
                            }

                            @Override
                            protected void onFailure(TaskException exception) {
                                super.onFailure(exception);

                                if (fragment.getActivity() != null)
                                    ViewUtils.showMessage(fragment.getActivity(), exception.getMessage());
                            }

                            @Override
                            protected void onSuccess(GroupSortResult result) {
                                super.onSuccess(result);

                                if (fragment.getActivity() != null) {
                                    if ("true".equals(result.getResult()))
                                        ViewUtils.showMessage(fragment.getActivity(), R.string.msg_shield_success);
                                    else
                                        ViewUtils.showMessage(fragment.getActivity(), R.string.msg_shield_faild);
                                }
                            }

                            @Override
                            public GroupSortResult workInBackground(Void... params) throws TaskException {
                                return SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).statusMentionsShield(status.getId() + "");
                            }

                        }.execute();
                    }
                })
                .show();
    }

    public static String getCommentText(String text) {
        if (TextUtils.isEmpty(text))
            return "";

        try {
            if (text.startsWith("回覆") || text.startsWith("回复")) {
                if (text.indexOf(":") != -1) {
                    text = text.substring(text.indexOf(":") + 1, text.length());
                }
                else if (text.indexOf("：") != -1) {
                    text = text.substring(text.indexOf("：") + 1, text.length());
                }
            }
        } catch (Exception e) {
        }

        return text.trim();
    }

    public static void setTextSize(TextView textView) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, AppSettings.getTextSize());
    }

    public static int getStrLength(String content) {
        int length = 0;
        int tempLength = 0;
        for (int i = 0; i < content.length(); i++) {
            String temp = content.charAt(i) + "";
            if (temp.getBytes().length == 3) {
                length++;
            } else {
                tempLength++;
            }
        }
        length += tempLength / 2 + ((tempLength % 2) == 0 ? 0 : 1);
        return length;
    }

    public static void commentMenuSelected(final ABaseFragment fragment, String selectedItem, final StatusComment comment) {
        final String[] commentMenuArr = GlobalContext.getInstance().getResources().getStringArray(R.array.cmt_menus);

        try {
            int position = 0;
            for (int i = 0; i < commentMenuArr.length; i++) {
                if (commentMenuArr[i].equals(selectedItem)) {
                    position = i;
                    break;
                }
            }

            switch (position) {
                // 复制
                case 0:
                    AisenUtils.copyToClipboard(comment.getText());

                    if (fragment.getActivity() != null)
                        ViewUtils.showMessage(fragment.getActivity(), R.string.msg_text_copyed);
                    break;
                // 转发
                case 1:
                    BizFragment.createBizFragment(fragment).commentRepost(comment);
                    break;
                // 删除
                case 2:
                    new AlertDialogWrapper.Builder(fragment.getActivity()).setMessage(R.string.msg_del_cmt_remind)
                            .setNegativeButton(R.string.cancel, null)
                            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    BizFragment.createBizFragment(fragment).commentDestory(comment, new BizFragment.OnCommentDestoryCallback() {

                                        @SuppressWarnings("unchecked")
                                        @Override
                                        public void onCommentDestory(StatusComment commnet) {
                                            if (fragment instanceof APagingFragment) {
                                                @SuppressWarnings("rawtypes")
                                                APagingFragment aRefreshFragment = ((APagingFragment) fragment);
                                                for (Object so : aRefreshFragment.getAdapterItems()) {
                                                    StatusComment s = (StatusComment) so;
                                                    if (s.getId().equals(commnet.getId())) {
                                                        aRefreshFragment.getAdapterItems().remove(s);
                                                        aRefreshFragment.getAdapter().notifyDataSetChanged();
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    });
                                }
                            })
                            .show();
                    break;
                // 评论
                case 3:
                    BizFragment.createBizFragment(fragment).replyComment(comment.getStatus(), comment);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyToClipboard(String text) {
        // 得到剪贴板管理器
        try {
            ClipboardManager cmb = (ClipboardManager) GlobalContext.getInstance().getSystemService(Context.CLIPBOARD_SERVICE);
            cmb.setPrimaryClip(ClipData.newPlainText(null, text.trim()));
        } catch (Exception e) {
        }
    }

    public static void launchBrowser(Activity from, String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        from.startActivity(intent);
    }

    public static String getStatusMulImage(Context context, String thumbImage) {
        switch (AppSettings.getPictureMode()) {
            // MODE_AUTO
            case 2:
                if (SystemUtils.getNetworkType(context) == SystemUtils.NetWorkType.wifi)
                    return thumbImage.replace("thumbnail", "bmiddle");

                return thumbImage;
            // MODE_ALWAYS_ORIG
            case 1:
                return thumbImage.replace("thumbnail", "bmiddle");
            case 3:
                return thumbImage.replace("thumbnail", "bmiddle");
            // MODE_ALWAYS_THUMB
            case 0:
                return thumbImage;
            default:
                return thumbImage;
        }
    }

    public static void onMenuClicked(ABaseFragment fragment, int menuId, StatusContent status) {
        switch (menuId) {
            case R.id.comment:
                BizFragment.createBizFragment(fragment).commentCreate(status);
                break;
            case R.id.repost:
                BizFragment.createBizFragment(fragment).statusRepost(status);
                break;
            case R.id.fav:
                BizFragment.createBizFragment(fragment).favorityCreate(status.getId() + "", null);
                break;
            case R.id.fav_destory:
                BizFragment.createBizFragment(fragment).favorityDestory(status.getId() + "", null);
                break;
            case R.id.copy:
                copyToClipboard(status.getText());

                if (fragment.getActivity() != null)
                    ViewUtils.showMessage(fragment.getActivity(), R.string.msg_text_copyed);
                break;
            case R.id.delete:
                deleteStatus(fragment, status);
                break;
            case R.id.weiguan:
                PublishActivity.publishStatusRepostAndWeiguan(fragment.getActivity(), null, status);
                break;
            case R.id.share:

                break;
        }
    }

    public static void setStatusShareMenu(MenuItem shareItem, StatusContent status) {
        String url = null;

        if (status.getPic_urls() != null && status.getPic_urls().length > 0) {
            url = status.getPic_urls()[0].getThumbnail_pic();
        }
        else if (!TextUtils.isEmpty(status.getThumbnail_pic())) {
            url = status.getThumbnail_pic();
        }
        if (!TextUtils.isEmpty(url)) {
            File file = BitmapLoader.getInstance().getCacheFile(url.replace("thumbnail", "large"));
            if (file.exists()) {
                url = url.replace("thumbnail", "large");
            } else {
                file = BitmapLoader.getInstance().getCacheFile(url.replace("thumbnail", "bmiddle"));
                if (file.exists()) {
                    url = url.replace("thumbnail", "bmiddle");
                }
            }
        }

        Intent shareIntent = Utils.getShareIntent(status.getText(), status.getText(), url);

        ShareActionProvider shareProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        shareProvider.setShareHistoryFileName("channe_share.xml");
        shareProvider.setShareIntent(shareIntent);
    }

    public static boolean isLoggedUser(WeiBoUser user) {
        return user.getIdstr().equalsIgnoreCase(AppContext.getAccount().getUser().getIdstr());
    }

    public static String getUnit(long length) {
        String sizeStr;
        if (length * 1.0f / 1024 / 1024 > 1)
            sizeStr = String.format("%s M", new DecimalFormat("#.00").format(length * 1.0d / 1024 / 1024));
        else
            sizeStr = String.format("%s Kb", new DecimalFormat("#.00").format(length * 1.0d / 1024));
        return sizeStr;
    }

    public static Drawable getProgressBarDrawable() {
        if (BaseActivity.getRunningActivity() != null) {
            Activity context = BaseActivity.getRunningActivity();

            int color = context.getResources().getColor(ThemeUtils.themeColorArr[AppSettings.getThemeColor()][0]);

            return new CircularProgressDrawable.Builder(context).color(color).build();
        }

        return null;
    }

    public static void setStatusBar(Activity activity) {
//        if (true) return;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );//| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Utils.resolveColor(activity, R.attr.theme_statusbar_color, Color.BLUE));
            window.setStatusBarColor(Color.parseColor("#20000000"));
            window.setNavigationBarColor(activity.getResources().getColor(ThemeUtils.themeColorArr[AppSettings.getThemeColor()][1]));
//            window.setNavigationBarColor(Utils.resolveColor(activity, R.attr.theme_color, Color.BLUE));

//            Window window = activity.getWindow();
//            window.requestFeature(Window.FEATURE_NO_TITLE);
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
//                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
//            window.setNavigationBarColor(Utils.resolveColor(activity, R.attr.theme_statusbar_color, Color.TRANSPARENT));
        }
    }

    public static void setPicStatusBar(Activity activity) {
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            activity.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
//            Window window = activity.getWindow();
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
//                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
//        }
    }

    public static boolean checkTabsFragmentCanRequestData(Fragment checkedFragment) {
        if (checkedFragment.getActivity() == null)
            return false;

        ABaseFragment aFragment = null;
        if (checkedFragment.getActivity() instanceof SinaCommonActivity) {
            aFragment = (ABaseFragment) checkedFragment.getActivity().getFragmentManager().findFragmentByTag(SinaCommonActivity.FRAGMENT_TAG);
        }
        else if (checkedFragment.getActivity() instanceof MainActivity) {
            aFragment = (ABaseFragment) checkedFragment.getActivity().getFragmentManager().findFragmentByTag("MainFragment");
        }
        else if (checkedFragment.getActivity() instanceof UserProfileActivity) {
            aFragment = (ABaseFragment) checkedFragment.getActivity().getFragmentManager().findFragmentByTag(SinaCommonActivity.FRAGMENT_TAG);
        }

        if (aFragment != null && aFragment instanceof ATabsFragment) {
            ATabsFragment fragment = (ATabsFragment) aFragment;
            return fragment.getCurrentFragment() == checkedFragment;
        }

        return false;
    }

    public static void setTabsText(Fragment fragment, int index, String text) {
        if (fragment.getActivity() == null)
            return;

        ABaseFragment aFragment = null;
        if (fragment.getActivity() instanceof SinaCommonActivity) {
            aFragment = (ABaseFragment) fragment.getActivity().getFragmentManager().findFragmentByTag(SinaCommonActivity.FRAGMENT_TAG);
        }
        else if (fragment.getActivity() instanceof MainActivity) {
            aFragment = (ABaseFragment) fragment.getActivity().getFragmentManager().findFragmentByTag("MainFragment");
        }

        if (aFragment != null && aFragment instanceof ATabsTabLayoutFragment) {
            ATabsTabLayoutFragment tabsFragment = (ATabsTabLayoutFragment) aFragment;

            tabsFragment.getTablayout().getTabAt(index).setText(text);
        }
    }

    public static String convertUnicode(String ori) {
        char aChar;
        int len = ori.length();
        StringBuffer outBuffer = new StringBuffer(len);
        for (int x = 0; x < len; ) {
            aChar = ori.charAt(x++);
            if (aChar == '\\') {
                aChar = ori.charAt(x++);
                if (aChar == 'u') {
                    // Read the xxxx
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = ori.charAt(x++);
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Malformed   \\uxxxx   encoding.");
                        }
                    }
                    outBuffer.append((char) value);
                } else {
                    if (aChar == 't')
                        aChar = '\t';
                    else if (aChar == 'r')
                        aChar = '\r';
                    else if (aChar == 'n')
                        aChar = '\n';
                    else if (aChar == 'f')
                        aChar = '\f';
                    outBuffer.append(aChar);
                }
            } else
                outBuffer.append(aChar);

        }
        return outBuffer.toString();
    }

    public static void gotoSettings(Context context) {
        //Goto settings details
        final Uri packageURI = Uri.parse("package:" + context.getPackageName());
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
//            e.printStackTrace();

            //加入Launcher报错
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

}
