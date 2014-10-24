package org.aisen.weibo.sina.support.bean;

import java.io.Serializable;

import com.m.support.sqlite.annotation.Id;

public class WallpaperBean implements Serializable {

	private static final long serialVersionUID = -6886432578977226567L;

	@Id(column = "beanId")
	private String beanId;
	
	private String type;// 壁纸类型(0:手机壁纸, 1:自定义壁纸)
	
	private String path;// 壁纸路径

	public String getBeanId() {
		return beanId;
	}

	public void setBeanId(String beanId) {
		this.beanId = beanId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
}
