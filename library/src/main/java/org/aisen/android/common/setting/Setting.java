package org.aisen.android.common.setting;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Setting extends SettingBean implements Serializable {

	private static final long serialVersionUID = 4801654811733634325L;
	
	private Map<String, SettingExtra> extras;

	public Setting() {
		extras = new HashMap<String, SettingExtra>();
	}

	public Map<String, SettingExtra> getExtras() {
		return extras;
	}

	public void setExtras(Map<String, SettingExtra> extras) {
		this.extras = extras;
	}
	
}
