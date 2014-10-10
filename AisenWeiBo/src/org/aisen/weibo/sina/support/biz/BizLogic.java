package org.aisen.weibo.sina.support.biz;

import org.aisen.weibo.sina.support.bean.ApkInfo;
import org.aisen.weibo.sina.support.bean.AppSettingsBean;

import com.m.common.context.GlobalContext;
import com.m.common.params.Params;
import com.m.common.settings.SettingUtility;
import com.m.support.bizlogic.ABaseBizlogic;
import com.m.support.network.HttpConfig;
import com.m.support.task.TaskException;

public class BizLogic extends ABaseBizlogic {

	@Override
	protected HttpConfig configHttpConfig() {
		HttpConfig httpConfig = new HttpConfig();
		httpConfig.baseUrl = getSetting("meizt_base_url").getValue();
		httpConfig.contentType = "application/x-www-form-urlencoded";
		httpConfig.cookie = String.format("pck=%s;", GlobalContext.getInstance().getPackageName().replace(".", "_"));
		return httpConfig;
	}

	private BizLogic() {

	}
	
	public BizLogic(CacheMode cacheMode) {
		super(cacheMode);
	}

	public static BizLogic newInstance() {
		return new BizLogic();
	}
	
	public static BizLogic newInstance(CacheMode cacheMode) {
		return new BizLogic(cacheMode);
	}
	
	/**
	 * 获取版本信息
	 * 
	 * @return
	 * @throws TaskException
	 */
	public ApkInfo getApkInfo() throws TaskException {
		return doGet(SettingUtility.getSetting("getApkInfo"), null, ApkInfo.class);
	}
	
	/**
	 * 获取配置信息
	 * 
	 * @return
	 * @throws TaskException
	 */
	public AppSettingsBean getSettings() throws TaskException {
		Params params = new Params();
		params.addParameter("beanId", "APP_SETTINGS");
		
		return doGet(SettingUtility.getSetting("getSettings"), params, AppSettingsBean.class);
	}

}
