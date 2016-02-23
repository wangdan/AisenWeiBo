package org.aisen.android.network.http;

import org.aisen.android.common.setting.Setting;
import org.aisen.android.network.task.TaskException;

import java.io.File;

public interface IHttpUtility {

	<T> T doGet(HttpConfig config, Setting action, Params params, Class<T> responseCls) throws TaskException;

	<T> T doPost(HttpConfig config, Setting action, Params params, Class<T> responseCls, Object requestObj) throws TaskException;

	<T> T uploadFile(HttpConfig config, Setting action, Params params, MultipartFile[] files, Params headers, Class<T> responseCls) throws TaskException;

	class MultipartFile {

		private String contentType;

		private File file;

		private String key;

		private byte[] bytes;

		public MultipartFile(String contentType, String key, File file) {
			this.key = key;
			this.contentType = contentType;
			this.file = file;
		}

		public MultipartFile(String contentType, String key, byte[] bytes) {
			this.key = key;
			this.contentType = contentType;
			this.bytes = bytes;
		}

		public String getContentType() {
			return contentType;
		}

		public void setContentType(String contentType) {
			this.contentType = contentType;
		}

		public File getFile() {
			return file;
		}

		public void setFile(File file) {
			this.file = file;
		}

		public byte[] getBytes() {
			return bytes;
		}

		public void setBytes(byte[] bytes) {
			this.bytes = bytes;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

	}

}
