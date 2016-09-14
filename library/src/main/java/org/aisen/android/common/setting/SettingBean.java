package org.aisen.android.common.setting;

import java.io.Serializable;

class SettingBean implements Serializable {

	private static final long serialVersionUID = -3694407301270573142L;

	private String description;

	private String type;

	private String value;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
