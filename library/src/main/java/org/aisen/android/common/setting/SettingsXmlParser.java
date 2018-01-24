package org.aisen.android.common.setting;

import android.content.Context;
import android.content.res.Resources;
import android.util.Xml;

import com.alibaba.fastjson.JSON;

import org.aisen.android.common.utils.Logger;
import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsXmlParser {

	private static final String TAG = "AppSettingsXmlParser";

	private SettingsXmlParser() {

	}

	static Map<String, Setting> parseSettings(Context context, String fileName) {
		Map<String, Setting> settingMap = new HashMap<String, Setting>();
		List<SettingArray> settingArray = new ArrayList<SettingArray>();
		List<SettingExtra> settingExtras = null;

		Setting readSetting = null;
		SettingArray readSettingArray = null;
		SettingExtra readSettingExtra = null;

		XmlPullParser xmlResParser = null;
		try {
			String packageName = context.getPackageName();
			Resources resources = context.getPackageManager().getResourcesForApplication(packageName);

			Logger.d("read xml resource, filename = " + fileName);

			int resId = resources.getIdentifier(fileName, "raw", packageName);

			// 解析URL配置
			xmlResParser = Xml.newPullParser();
			xmlResParser.setInput(resources.openRawResource(resId), "utf-8");
			int eventType = xmlResParser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {

				switch (eventType) {
				case XmlPullParser.START_TAG:
					if ("setting-array".equals(xmlResParser.getName())) {
						readSettingArray = new SettingArray();
						readSettingArray.setType(xmlResParser.getAttributeValue(null, "type"));
						readSettingArray.setIndex(Integer.parseInt(xmlResParser.getAttributeValue(null, "index")));
					}

					if ("setting".equals(xmlResParser.getName())) {
						readSetting = new Setting();
						readSetting.setType(xmlResParser.getAttributeValue(null, "type"));
					}

					if ("extras".equals(xmlResParser.getName())) {
						settingExtras = new ArrayList<SettingExtra>();
					}

					if ("extra".equals(xmlResParser.getName())) {
						readSettingExtra = new SettingExtra();
						readSettingExtra.setType(xmlResParser.getAttributeValue(null, "type"));
					}

					if ("des".equals(xmlResParser.getName())) {
						if (readSettingExtra != null) {
							readSettingExtra.setDescription(xmlResParser.nextText());
						} else if (readSetting != null) {
							readSetting.setDescription(xmlResParser.nextText());
						} else if (readSettingArray != null) {
							readSettingArray.setDescription(xmlResParser.nextText());
						}
					}

					if ("value".equals(xmlResParser.getName())) {
						if (readSettingExtra != null) {
							readSettingExtra.setValue(xmlResParser.nextText());
						} else if (readSetting != null) {
							readSetting.setValue(xmlResParser.nextText());
						}
					}

					break;
				case XmlPullParser.END_TAG:
					if ("setting".equals(xmlResParser.getName())) {
						if (readSetting != null) {
							if (readSettingArray != null) {
								readSettingArray.getSettingArray().add(readSetting);
							} else {
								settingMap.put(readSetting.getType(), readSetting);
							}
						}
						Logger.d(TAG, String.format("parse new setting --->%s", JSON.toJSONString(readSetting)));
						readSetting = null;
					}

					if ("setting-array".equals(xmlResParser.getName())) {
						settingArray.add(readSettingArray);
						Logger.d(TAG, String.format("parse new settingArray --->%s", JSON.toJSONString(readSettingArray)));
						readSettingArray = null;
					}

					if ("extras".equals(xmlResParser.getName())) {
						if (readSetting != null)
							for (SettingExtra extra : settingExtras)
								readSetting.getExtras().put(extra.getType(), extra);

						settingExtras = null;
					}

					if ("extra".equals(xmlResParser.getName())) {
						settingExtras.add(readSettingExtra);
						Logger.d(TAG, String.format("parse new settingExtra --->%s", JSON.toJSONString(settingExtras)));
						readSettingExtra = null;
					}
				}

				eventType = xmlResParser.next();
			}
		} catch (Exception e) {
			Logger.printExc(SettingsXmlParser.class, e);
		} finally {
//			if (xmlResParser != null)
//				xmlResParser.close();
		}

		for (SettingArray array : settingArray) {
			if (array.getSettingArray().size() > array.getIndex()) {
				Setting setting = array.getSettingArray().get(array.getIndex());
				setting.setType(array.getType());
				settingMap.put(setting.getType(), setting);
			}
		}

		return settingMap;
	}

}
