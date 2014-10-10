package org.aisen.weibo.sina.support.bean;

import java.io.Serializable;

import com.m.common.settings.SettingUtility;
import com.m.support.sqlite.annotation.Id;

public class AppSettingsBean implements Serializable {

	private static final long serialVersionUID = 7318833630371789478L;

	@Id(column = "beanId")
	private String beanId = "APP_SETTINGS";
	
	private String adFlag;// banner设置
	
	private String splashAdFlag;// splash设置
	
	private String wallAdFlag;// 应用墙广告设置
	
	private int minAdExhibitionCount;// 最少展示这个数目的广告显示CloseBtn
	
	private boolean splashADEnable;// 是否开启开屏广告
	
	private boolean removeAdWhenClosed;// 关闭广告时，移除AD，如果不移除，会持续获取广告
	
	private boolean visiableAD;// 广告可以获取，是否显示视图，如果为false，则正常拉取广告，但是不显示视图
	
	private int adInterval;// 多个广告时，间隔播放的时间
	
	private String extraJson;
	
	public AppSettingsBean() {
		adFlag = SettingUtility.getPermanentSettingAsStr("ad_flag", null);
		
		wallAdFlag = SettingUtility.getPermanentSettingAsStr("wall_flag", null);
		
		splashAdFlag = SettingUtility.getPermanentSettingAsStr("splash_ad_flag", null);
		
		minAdExhibitionCount = 5;
		
		splashADEnable = false;
		
		removeAdWhenClosed = true;
		
		visiableAD = true;
		
		adInterval = 15 * 1000;
	}
	
	public String getBeanId() {
		return beanId;
	}

	public void setBeanId(String beanId) {
		this.beanId = beanId;
	}

	public String getAdFlag() {
		return adFlag;
	}

	public void setAdFlag(String adFlag) {
		this.adFlag = adFlag;
	}

	public int getMinAdExhibitionCount() {
		return minAdExhibitionCount;
	}

	public void setMinAdExhibitionCount(int minAdExhibitionCount) {
		this.minAdExhibitionCount = minAdExhibitionCount;
	}

	public boolean isSplashADEnable() {
		return splashADEnable;
	}

	public void setSplashADEnable(boolean splashADEnable) {
		this.splashADEnable = splashADEnable;
	}

	public String getSplashAdFlag() {
		return splashAdFlag;
	}

	public void setSplashAdFlag(String splashAdFlag) {
		this.splashAdFlag = splashAdFlag;
	}

	public boolean isRemoveAdWhenClosed() {
		return removeAdWhenClosed;
	}

	public void setRemoveAdWhenClosed(boolean removeAdWhenClosed) {
		this.removeAdWhenClosed = removeAdWhenClosed;
	}

	public boolean isVisiableAD() {
		return visiableAD;
	}

	public void setVisiableAD(boolean visiableAD) {
		this.visiableAD = visiableAD;
	}

	public int getAdInterval() {
		return adInterval;
	}

	public void setAdInterval(int adInterval) {
		this.adInterval = adInterval;
	}

	public String getWallAdFlag() {
		return wallAdFlag;
	}

	public void setWallAdFlag(String wallAdFlag) {
		this.wallAdFlag = wallAdFlag;
	}

	public String getExtraJson() {
		return extraJson;
	}

	public void setExtraJson(String extraJson) {
		this.extraJson = extraJson;
	}

}
