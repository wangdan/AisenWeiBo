package org.aisen.weibo.sina.support.bean;

import java.io.Serializable;

public class ApkInfo implements Serializable {

	private static final long serialVersionUID = 1333864970402517932L;

	private String versionName;

	private int versionCode;

	private String des;
	
	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	public String getDes() {
		return des;
	}

	public void setDes(String des) {
		this.des = des;
	}

}
