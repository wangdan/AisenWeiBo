package org.aisen.android.component.bitmaploader.core;

import android.text.TextUtils;

import org.aisen.android.common.setting.SettingUtility;
import org.aisen.android.common.utils.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class FileDisk {

	private String filePath;

	private final String IMG_SUFFIX;

	public FileDisk(String filePath) {
		if (!TextUtils.isEmpty(SettingUtility.getStringSetting("image_suffix")))
			IMG_SUFFIX = SettingUtility.getStringSetting("image_suffix");
		else
			IMG_SUFFIX = "is";
		File file = new File(filePath);
		if (!file.exists())
			file.mkdirs();

		this.filePath = filePath;
	}

	public void writeOutStream(byte[] datas, String url, String key) throws Exception {
		ByteArrayInputStream in = new ByteArrayInputStream(datas);
		File file = new File(filePath + File.separator + key + "." + getImageSuffix(url));
		if (file.getParentFile().exists())
			file.getParentFile().mkdirs();
		FileOutputStream out = new FileOutputStream(file);
		byte[] buffer = new byte[8 * 1024];
		int len = -1;
		while ((len = in.read(buffer)) != -1) {
			out.write(buffer, 0, len);
		}
		out.flush();
		out.close();
		in.close();
	}

	public File getFile(String url, String key) {
		return new File(filePath + File.separator + key + "." + getImageSuffix(url));
	}

	public FileInputStream getInputStream(String url, String key) throws Exception {
		File file = getFile(url, key);

		if (file.exists()) {
			if (file.length() == 0) {
				file.delete();
				Logger.w("文件已损坏，url = " + url);
				
				return null;
			}
			
			return new FileInputStream(file);
		}
		else
			Logger.d("getInputStream(String key) not exist");

		return null;
	}

	public OutputStream getOutputStream(String url, String key) throws Exception {
		
		Logger.d("getOutputStream(String key)" + filePath + File.separator + key + "." + getImageSuffix(url) + ".temp");

		File file = new File(filePath + File.separator + key + "." + getImageSuffix(url) + ".temp");
		if (!file.getParentFile().exists())
			file.getParentFile().mkdirs();
		
		return new FileOutputStream(file);
	}

	public void deleteFile(String url, String key) {
		
		File file = new File(filePath + File.separator + key + "." + getImageSuffix(url));
		if (file.exists())
			file.delete();
	}

	public void renameFile(String url, String key) {
		
		File file = new File(filePath + File.separator + key + "." + getImageSuffix(url) + ".temp");
		File newFile = new File(filePath + File.separator + key + "." + getImageSuffix(url));
		if (file.exists() && file.length() != 0) {
            // 2015-03-03 这里发现了一个bug，如果文件已经存在，且文件已经保存过了，但是保存的文件是异常的文件，如果再rename的话
            // rename后的文件还是之前异常的文件，所以操作前简单的判断一下文件是否一致
            if (newFile.exists() && newFile.length() != file.length()) {
                Logger.v(String.format("原文件已存在不匹配，先删除目标文件，临时文件长度%s, 目标文件长度%s", file.length() + "", newFile.length() + ""));
                newFile.delete();
            }

            boolean renameResult = file.renameTo(newFile);
        }

	}
	
	private String getImageSuffix(String url) {
		return getImageSuffix(url, IMG_SUFFIX);
	}
	
	public static String getImageSuffix(String url, String suffix) {
		if ("auto".equals(suffix)) {
			try {
				String temp = url;
				temp = temp.toLowerCase();
				if (temp.endsWith(".gif") || temp.endsWith(".jpg") || temp.endsWith(".jpeg")
						|| temp.endsWith(".bmp") || temp.endsWith(".png")) {
					return url.substring(url.lastIndexOf(".") + 1, url.length());
				}
				else {
					return "jpg";
				}
			} catch (Exception e) {
			}
		}
		
		return suffix;
	}
	
}
