package org.aisen.weibo.sina.support.utils;

import java.util.List;
import java.util.Locale;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.bean.AppSettingsBean;
import org.aisen.weibo.sina.support.bean.SettingExtraBean;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.m.common.context.GlobalContext;
import com.m.common.settings.SettingUtility;
import com.m.common.utils.ActivityHelper;
import com.m.common.utils.SystemUtility;
import com.m.common.utils.SystemUtility.NetWorkType;
import com.m.support.sqlite.SqliteUtility;

public class AppSettings {

	public static int getPublishDelay() {
		return 5 * 1000;
	}
	
	private static int[] txtSizeResArr = new int[]{ R.dimen.txt_small, R.dimen.txt_mid, R.dimen.txt_large, R.dimen.txt_huge };
	
	/**
	 * 分享照片时旋转90度
	 * 
	 * @return
	 */
	public static boolean isRotatePic() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
		return prefs.getBoolean("pRotatePic", false);
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
	 * 是否显示备注
	 * 
	 * @return
	 */
	public static boolean isShowRemark() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
		return prefs.getBoolean("pShowRemark", true);
	}
	
	/**
	 * 首页是否显示默认分组微博
	 * 
	 * @return
	 */
	public static boolean isShowDefGroup() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
		return prefs.getBoolean("pShowDefGroup", true);
	}
	
	/**
	 * 是否显示快速滚动条
	 * 
	 * @return
	 */
	public static boolean isFastScrollbarEnable() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
		return prefs.getBoolean("pFastScrollBar", true);
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
	 * 刷新微博音效反馈
	 * 
	 * @return
	 */
	public static boolean isRefreshSound() {
		if (isDebug())
			return true;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
		return prefs.getBoolean("pRefreshSound", false);
	}
	
	/**
	 * 正文字体大小
	 * 
	 * @return
	 */
	public static int getTextSize() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
		int value = Integer.parseInt(prefs.getString("pTextSize", "1"));
		return GlobalContext.getInstance().getResources().getDimensionPixelSize(txtSizeResArr[value]);
	}
	
	/**
	 * 语言设置
	 * 
	 * @return
	 */
	public static int getLanguage() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
		return Integer.parseInt(prefs.getString("pLanguage", "0"));
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
	 * 显示高清头像
	 * 
	 * @return
	 */
	public static boolean isLargePhoto() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
		return prefs.getBoolean("pLargePhoto", true);
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
	 * 提醒设置
	 * 
	 * @return
	 */
	public static boolean isNotifyEnable() {
		return ActivityHelper.getInstance().getBooleanShareData("org.aisen.weibo.sina.NOTIFICATION", true);
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
	 * 崩溃日志上传
	 * 
	 * @return
	 */
	public static boolean isCrashLogUpload() {
//		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
//		return prefs.getBoolean("pCrashLog", true);
		return true;
	}
	
	static final int[] countArr = {20, 50, 100};
	
	/**
	 * 微博加载数量
	 * 
	 * @return
	 */
	public static int getTimelineCount() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
		int index = Integer.parseInt(prefs.getString("pTimelineCount", "3"));
		
		int count = 50;
		if (index == 3) {
			if (SystemUtility.getNetworkType() == NetWorkType.wifi) 
				count = 100;
		}
		else {
			count = countArr[index];
		}
		
		return count;
	}
	
	/**
	 * 高清图片排版模式
	 * 
	 * @return 0:自动排版 1:9宫格
	 */
	public static int getPicLargeMode() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
		int index = Integer.parseInt(prefs.getString("pPicLargeMode", "0"));
		
		return index;
	}
	
	/**
	 * 评论加载数量
	 * 
	 * @return
	 */
	public static int getCommentCount() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
		int index = Integer.parseInt(prefs.getString("pCommentCount", "3"));
		
		int count = 50;
		if (index == 3) {
			if (SystemUtility.getNetworkType() == NetWorkType.wifi) 
				count = 100;
		}
		else {
			count = countArr[index];
		}
		
		return count;
	}

	// 服务器配置信息
	private static AppSettingsBean mSettingsBean;
	
	public static AppSettingsBean getSettings() {
		if (mSettingsBean == null) {
			List<AppSettingsBean> beanList = SqliteUtility.getInstance().selectAll(AppSettingsBean.class);
			if (beanList.size() > 0) {
				mSettingsBean = beanList.get(beanList.size() - 1);
			}
			else {
				mSettingsBean = new AppSettingsBean();
			}
		}
		
		return mSettingsBean;
	}
	
	public static void setSettings(AppSettingsBean bean) {
		mSettingsBean = bean;
	}
	
	public static SettingExtraBean getSettingExtra() {
		if (AppSettings.getSettings() != null && !TextUtils.isEmpty(AppSettings.getSettings().getExtraJson()))
			return JSON.parseObject(AppSettings.getSettings().getExtraJson(), SettingExtraBean.class);
		
		SettingExtraBean extraBean = new SettingExtraBean();
		
		extraBean.setRecommentText(SettingUtility.getStringSetting("recommend_text"));
		extraBean.setRecommentImage(SettingUtility.getStringSetting("recommend_image"));
		extraBean.setAboutURL(SettingUtility.getStringSetting("about_url"));
		extraBean.setHelpURL(SettingUtility.getStringSetting("help_url"));
		
		return extraBean;
	}

	public static String getImageSavePath() {
		return ActivityHelper.getInstance().getShareData("org.aisen.weibo.sina.Images", "Images");
	}
	
	public static void setImageSavePath(String path) {
		ActivityHelper.getInstance().putShareData("org.aisen.weibo.sina.Images", path);
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
		int value = Integer.parseInt(prefs.getString("pCacheValidity", "2"));
		
		switch (value) {
		case 0:
			return 30 * 1000;
		case 1:
			return 1 * 60 * 60 * 1000;
		case 2:
			return 4 * 60 * 60 * 1000;
		case 3:
			return 8 * 60 * 60 * 1000;
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
	 * 列表刷新控件
	 * 
	 * @return
	 */
	public static int getRefreshType() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
		int value = Integer.parseInt(prefs.getString("pRefreshViewType", "0"));
		
		switch (value) {
		case 0:
			return 0;
		case 1:
			return 1;
		default:
			return 0;
		}
	}
	
	/**
	 * 系统语言或者用户设置是否为繁体
	 * 
	 * @return
	 */
	public static boolean isTraditional() {
		// 转换成繁体
		boolean convert = false;
		// 繁体
		if (AppSettings.getLanguage() == 2) {
			convert = true;
		}
		// 简体
		else if (AppSettings.getLanguage() == 1) {
			
		}
		// 随系统
		else {
			String language = Locale.getDefault().getLanguage();
			String country = Locale.getDefault().getCountry();
			if ("zh".equals(language) && ("TW".equals(country) || "HK".equals(country))) {
				convert = true;
			}
		}
		
		return convert;
	}
	
}
