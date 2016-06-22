package org.aisen.weibo.sina.base;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.ActivityHelper;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.weibo.sina.R;

/**
 * Created by wangdan on 15/4/12.
 */
public class AppSettings {

    static final int[] countArr = { 20, 50, 100 };

    public static final int REQUEST_DATA_DELAY = 500;

    public static int getPublishDelay() {
        return 5 * 1000;
    }

    /**
     * 网络请求延迟
     *
     * @return
     */
    public static boolean isNetworkDelay() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pNetworkDelay", false);
    }

    /**
     * 提醒设置
     *
     * @return
     */
    public static boolean isNotifyEnable() {
        return ActivityHelper.getBooleanShareData(GlobalContext.getInstance(), "org.aisen.weibo.sina.NOTIFICATION", true);
    }

    /**
     * 提及评论提醒
     *
     * @return
     */
    public static boolean isNotifyCommentMention() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pCommentMention", true);
    }

    /**
     * 提及微博提醒
     *
     * @return
     */
    public static boolean isNotifyStatusMention() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pStatusMention", true);
    }

    /**
     * 粉丝提醒
     *
     * @return
     */
    public static boolean isNotifyFollower() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pFollower", true);
    }

    /**
     * 评论提醒
     *
     * @return
     */
    public static boolean isNotifyComment() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pComment", true);
    }

    /**
     * 私信提醒
     *
     * @return
     */
    public static boolean isNotifyDm() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pDm", true);
    }

    /**
     * 声音提醒
     *
     * @return
     */
    public static boolean isNotifySound() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pNotifySound", true);
    }

    /**
     * 振动提醒
     *
     * @return
     */
    public static boolean isNotifyVibrate() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pNotifyVibrate", true);
    }

    /**
     * LED提醒
     *
     * @return
     */
    public static boolean isNotifyLED() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pNotifyLED", true);
    }

    /**
     * 夜间不扰
     *
     * @return
     */
    public static boolean isNotifyNightClose() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pNightClose", true);
    }

    /**
     * 发送成功后的震动反馈
     *
     * @return
     */
    public static boolean isSendVibrate() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pSendVibrate", true);
    }

    /**
     * 未读小时间隔时间
     *
     * @return
     */
    public static int getUnreadInterval() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        int value = Integer.parseInt(prefs.getString("pInterval", "0"));
        String[] valueArr = GlobalContext.getInstance().getResources().getStringArray(R.array.prefValues);

        int interval = 60;

        switch (Integer.parseInt(valueArr[value])) {
            case 0:
                interval = 60;
                break;
            case 1:
                interval = 60 * 5;
                break;
            case 2:
                interval = 60 * 15;
                break;
            case 3:
                interval = 60 * 60;
                break;
        }

        return interval;
    }

    /**
     * 上传图片质量设置
     *
     * @return
     */
    public static int getUploadSetting() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        int value = Integer.parseInt(prefs.getString("pUploadSetting", "0"));
        return value;
    }

    /**
     * 应用常驻内存
     *
     * @return
     */
    public static boolean isAppResident() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pAppResident", true);
    }

    /**
     * 微博加载数量
     *
     * @return
     */
    public static int getTimelineCount() {
//        if (true) return 5;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        int index = Integer.parseInt(prefs.getString("pTimelineCount", "0"));

        int count = countArr[0];
        if (index == 3 && GlobalContext.getInstance() != null) {
            if (SystemUtils.getNetworkType(GlobalContext.getInstance()) == SystemUtils.NetWorkType.wifi)
                count = 100;
        }
        else {
            count = countArr[index];
        }

        return count;
    }

    /**
     * 评论加载数量
     *
     * @return
     */
    public static int getCommentCount() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        int index = Integer.parseInt(prefs.getString("pCommentCount", "0"));

        int count = 50;
        if (index == 3 && GlobalContext.getInstance() != null) {
            if (SystemUtils.getNetworkType(GlobalContext.getInstance()) == SystemUtils.NetWorkType.wifi)
                count = 100;
        }
        else {
            count = countArr[index];
        }

        return count;
    }

    /**
     * 图片加载模式
     *
     * @return
     */
    public static int getPictureMode() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        int value = Integer.parseInt(prefs.getString("pPicMode", "2"));
        return value;
    }

    /**
     * 默认加载原图
     *
     * @return
     */
    public static boolean isLoadOrigPic() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pLoadOrigPic", false);
    }

    /**
     * 是否使用内置浏览器
     *
     * @return
     */
    public static boolean isInnerBrower() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pInnerBrowser", true);
    }

    /**
     * 正文字体大小
     *
     * @return
     */
    private static int[] txtSizeResArr = new int[]{ R.dimen.sp_12, R.dimen.sp_13, R.dimen.sp_14, R.dimen.sp_15,
            R.dimen.sp_16, R.dimen.sp_17, R.dimen.sp_18, R.dimen.sp_19,
            R.dimen.sp_20 };
    public static int getTextSize() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        int value = Integer.parseInt(prefs.getString("pTextSize", "4"));
        return GlobalContext.getInstance().getResources().getDimensionPixelSize(txtSizeResArr[value]);
    }

    /**
     * 无图模式
     *
     * @return
     */
    public static boolean isPicNone() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pNonePic", false);
    }

    /**
     * 是否显示备注
     *
     * @return
     */
    public static boolean isShowRemark() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pShowRemark", true);
    }

    /**
     * 显示高清头像
     *
     * @return
     */
    public static boolean isLargePhoto() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pLargePhoto", true);
    }

    /**
     * 如果是获取历史数据，则历史数据的时间大于这个时间，将缓存刷新
     *
     * @return
     */
    public static int getRefreshInterval() {
        if (isDebug())
            return 30 * 1000;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        int value = Integer.parseInt(prefs.getString("pCacheValidity", "1"));

        switch (value) {
            case 0:
                return 30 * 1000;
            case 1:
                return 20 * 60 * 1000;
            case 2:
                return 12 * 60 * 60 * 1000;
            case 3:
                return 24 * 60 * 60 * 1000;
            case 4:
                return Integer.MAX_VALUE;
//		case 1:
//			return 1 * 60 * 60 * 1000;
//		case 2:
//			return 4 * 60 * 60 * 1000;
//		case 3:
//			return 8 * 60 * 60 * 1000;
            default:
                return 1 * 60 * 60 * 1000;
        }
    }

    /**
     * 开发者测试模式
     *
     * @return
     */
    public static boolean isDebug() {
        // 自动刷新时间间隔为30秒
        // 屏幕旋转
        // 打开音效

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pDebug", false);
    }

    /**
     * 关闭缓存
     *
     * @return
     */
    public static boolean isDisableCache() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pDisableCache", false);
    }

    /**
     * 撤销发布
     *
     * @return
     */
    public static boolean isSendDelay() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pSendDelay", false);
    }

    /**
     * 分享照片时旋转90度
     *
     * @return
     */
    public static boolean isRotatePic() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pRotatePic", false);
    }

    public static String getImageSavePath() {
        return ActivityHelper.getShareData(GlobalContext.getInstance(), "org.aisen.weibo.sina.Images", "Images");
    }

    public static void setImageSavePath(String path) {
        ActivityHelper.putShareData(GlobalContext.getInstance(), "org.aisen.weibo.sina.Images", path);
    }

    /**
     * 手势返回方向设置
     *
     * @return
     */
    public static int getSwipebackEdgeMode() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return Integer.parseInt(prefs.getString("pSwipebackEdgeMode", "0"));
    }

    /**
     * 首页fab按钮功能
     *
     * @return
     */
    public static int getFabBtnType() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return Integer.parseInt(prefs.getString("pFabType", "1"));
    }

    /**
     * 首页fab按钮位置
     *
     * @return
     */
    public static int getFabBtnPosition() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return Integer.parseInt(prefs.getString("pFabPosition", "1"));
    }

    public static int getThemeColor() {
        return ActivityHelper.getIntShareData(GlobalContext.getInstance(), "Theme_index", -1);
    }

    public static void setThemeColor(int theme) {
        ActivityHelper.putIntShareData(GlobalContext.getInstance(), "Theme_index", theme);
    }

    /**
     * 开启高清图已下载提示
     *
     * @return
     */
    public static boolean midPicHint() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pMidPicHint", true);
    }

    /**
     * 离线微博大小
     *
     * @return
     */
    public static int getOfflineStatusSize() {
        int[] values = new int[]{ 50, 100, 200 };

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return values[Integer.parseInt(prefs.getString("pOfflineStatusSize", "0"))];
    }

    /**
     * 同时离线中图
     *
     * @return
     */
    public static boolean offlineMidPic() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pLoadMidPicture", true);
    }

    /**
     * 同时离线图片线程大小
     *
     * @return
     */
    public static int offlinePicTaskSize() {
        int[] values = new int[]{ 10, 20, 30, 50 };

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return values[Integer.parseInt(prefs.getString("pOfflinePicTaskCount", "2"))];
    }

    /**
     * 崩溃日志上传
     *
     * @return
     */
    public static boolean isCrashLogUpload() {
//		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
//		return prefs.getBoolean("pCrashLog", true);
        return true;
    }

    /**
     * 屏幕旋转
     *
     * @return
     */
    public static boolean isScreenRotate() {
        if (isDebug())
            return true;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
        return prefs.getBoolean("pScreenRotate", false);
    }

    /**
     * 测试用，验证通知模块时，不自动清除数据
     *
     * @return
     */
    public static boolean ignoreUnread() {
        return false;
    }

}
