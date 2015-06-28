package org.aisen.android.common.setting;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.ActivityHelper;
import org.aisen.android.common.utils.SdcardUtils;

public class SettingUtility {

	private static Map<String, Setting> settingMap;

	static {
		settingMap = new HashMap<String, Setting>();
	}

	private SettingUtility() {

	}

	public static void setSettingUtility() {
		addSettings("actions");
		addSettings("settings");

        if (SdcardUtils.hasSdcardAndCanWrite()) {
            File rootFile = new File(GlobalContext.getInstance().getAppPath());
            if (!rootFile.exists())
                rootFile.mkdirs();

            // 数据缓存目录设置
            File jsonFile = new File(rootFile.getAbsolutePath() + File.separator + getPermanentSettingAsStr("com_m_common_json", "files"));
            if (!jsonFile.exists())
                jsonFile.mkdirs();

            // 缓存目录设置
            File imageFile = new File(rootFile.getAbsolutePath() + File.separator + getPermanentSettingAsStr("com_m_common_image", "images"));
            if (!imageFile.exists())
                imageFile.mkdirs();
        }
	}

	/**
	 * 添加设置配置数据
	 * 
	 * @param settingsXmlName
	 */
	public static void addSettings(String settingsXmlName) {
		Map<String, Setting> newSettingMap = SettingsXmlParser.parseSettings(GlobalContext.getInstance(), settingsXmlName);

		Set<String> keySet = newSettingMap.keySet();
		for (String key : keySet)
			settingMap.put(key, newSettingMap.get(key));
	}

	public static boolean getBooleanSetting(String type) {
		if (settingMap.containsKey(type))
			return Boolean.parseBoolean(settingMap.get(type).getValue());

		return false;
	}

	public static int getIntSetting(String type) {
		if (settingMap.containsKey(type))
			return Integer.parseInt(settingMap.get(type).getValue());

		return -1;
	}

	public static String getStringSetting(String type) {
		if (settingMap.containsKey(type))
			return settingMap.get(type).getValue();

		return null;
	}

	public static Setting getSetting(String type) {
		if (settingMap.containsKey(type))
			return settingMap.get(type);

		return null;
	}

	public static void setPermanentSetting(String type, boolean value) {
		ActivityHelper.putBooleanShareData(type, value);
	}

	public static boolean getPermanentSettingAsBool(String type, boolean def) {
		return ActivityHelper.getBooleanShareData(type,
				settingMap.containsKey(type) ? Boolean.parseBoolean(settingMap.get(type).getValue()) : def);
	}

	public static void setPermanentSetting(String type, int value) {
		ActivityHelper.putIntShareData(type, value);
	}

	public static int getPermanentSettingAsInt(String type) {
		return ActivityHelper.getIntShareData(type,
				settingMap.containsKey(type) ? Integer.parseInt(settingMap.get(type).getValue()) : -1);
	}

	public static void setPermanentSetting(String type, String value) {
		ActivityHelper.putShareData(type, value);
	}

	public static String getPermanentSettingAsStr(String type, String def) {
		return ActivityHelper.getShareData(type, settingMap.containsKey(type) ? settingMap.get(type).getValue() : def);
	}

}
