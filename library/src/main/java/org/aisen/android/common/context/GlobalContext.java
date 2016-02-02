package org.aisen.android.common.context;

import java.io.File;
import java.util.concurrent.TimeUnit;

import android.app.Application;
import android.os.Handler;

import com.squareup.okhttp.OkHttpClient;

import org.aisen.android.common.setting.SettingUtility;
import org.aisen.android.common.utils.ActivityHelper;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.SdcardUtils;

public class GlobalContext extends Application {

	private static GlobalContext _context;

	public final static int CONN_TIMEOUT = 30000;
	public final static int READ_TIMEOUT = 30000;

	private OkHttpClient mOkHttpClient;

	@Override
	public void onCreate() {
		super.onCreate();
		
		_context = this;

		// 初始化OkHttpClient
		mOkHttpClient = new OkHttpClient();
		configOkHttpClient(CONN_TIMEOUT, READ_TIMEOUT);
		
		// 初始化ActivityHelper
		ActivityHelper.config(this);

		// 初始化设置
		SettingUtility.setSettingUtility();

		Logger.DEBUG = SettingUtility.getBooleanSetting("debug");
	}

	public static GlobalContext getInstance() {
		return _context;
	}
	
	public Handler getHandler() {
		return mHandler;
	}
	
	Handler mHandler = new Handler() {
		
	};
	
	/**
	 * 程序的文件目录，如果setting配置的是android，标志目录位于/sdcard/Application/PackageName/...下<br/>
	 * 否则，就是/sdcard/setting[root_path]/...目录
	 * 
	 * @return
	 */
	public String getAppPath() {
		if ("android".equals(SettingUtility.getStringSetting("root_path")))
			return getExternalCacheDir().getAbsolutePath() + File.separator;
		
		return SdcardUtils.getSdcardPath() + File.separator + SettingUtility.getStringSetting("root_path") + File.separator;
	}
	
	/**
	 * 关于程序所有的文件缓存根目录
	 * 
	 * @return
	 */
	public String getDataPath() {
		return getAppPath() + SettingUtility.getPermanentSettingAsStr("com_m_common_json", "data") + File.separator;
	}
	
	/**
	 * 图片缓存目录
	 * 
	 * @return
	 */
	public String getImagePath() {
		return getAppPath() + SettingUtility.getPermanentSettingAsStr("com_m_common_image", "image") + File.separator;
	}

	public OkHttpClient getOkHttpClient() {
		return mOkHttpClient;
	}

	public void configOkHttpClient(int connTimeout, int socketTimeout) {
		if (mOkHttpClient != null) {
			mOkHttpClient.setConnectTimeout(connTimeout, TimeUnit.MILLISECONDS);
			mOkHttpClient.setReadTimeout(socketTimeout, TimeUnit.MILLISECONDS);
		}
	}

}