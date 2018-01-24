package org.aisen.android.common.setting;

public class SettingUtil {

	public static String getSettingValue(Setting setting, String type) {
		if (setting.getExtras().get(type) != null)
			return setting.getExtras().get(type).getValue();
		else {
			return SettingUtility.getStringSetting(type);
		}
	}

	/**
	 * 首先在指定的Setting的Extra中寻找type值，如果没有，则在所有的Setting中寻找
	 * 
	 * @param setting
	 * @param type
	 * @return
	 */
	public static int getSettingValueAsInt(Setting setting, String type) {
		if (setting.getExtras().containsKey(type))
			return Integer.parseInt(setting.getExtras().get(type).getValue());
		else {
			return SettingUtility.getIntSetting(type);
		}
	}

	/**
	 * 获取缓存有效时间
	 * 
	 * @param setting
	 * @return 单位:(s)
	 */
	public static int getValidTime(Setting setting) {
		int validTime = 0;

		String validTimeStr = null;
		if (setting.getExtras().containsKey("cache_validtime"))
			validTimeStr = setting.getExtras().get("cache_validtime").getValue();
		else
			validTimeStr = SettingUtility.getStringSetting("cache_validtime");
		if ("max_date".equals(validTimeStr))
			validTime = Integer.MAX_VALUE;
		else
			validTime = Integer.parseInt(validTimeStr);

		return validTime;
	}

}
