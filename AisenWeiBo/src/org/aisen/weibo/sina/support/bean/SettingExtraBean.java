package org.aisen.weibo.sina.support.bean;

import java.io.Serializable;

public class SettingExtraBean implements Serializable {

	private static final long serialVersionUID = 1775304199790933730L;

	private String recommentText;
	
	private String recommentImage;
	
	private String aboutURL;
	
	private String helpURL;

	public String getRecommentText() {
		return recommentText;
	}

	public void setRecommentText(String recommentText) {
		this.recommentText = recommentText;
	}

	public String getRecommentImage() {
		return recommentImage;
	}

	public void setRecommentImage(String recommentImage) {
		this.recommentImage = recommentImage;
	}

	public String getAboutURL() {
		return aboutURL;
	}

	public void setAboutURL(String aboutURL) {
		this.aboutURL = aboutURL;
	}

	public String getHelpURL() {
		return helpURL;
	}

	public void setHelpURL(String helpURL) {
		this.helpURL = helpURL;
	}
	
}
