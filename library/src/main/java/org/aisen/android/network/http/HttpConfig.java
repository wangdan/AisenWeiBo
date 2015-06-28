package org.aisen.android.network.http;

public class HttpConfig {

	public String cookie;

	public String baseUrl;// 服务器地址
	
	public String contentType = "application/json";
	
	public String authrization = "";

	@Override
	public HttpConfig clone() throws CloneNotSupportedException {
		HttpConfig httpConfig = new HttpConfig();
		httpConfig.cookie = cookie;
		httpConfig.baseUrl = baseUrl;
		httpConfig.contentType = contentType;
		httpConfig.authrization = authrization;
		return httpConfig;
	}
	
}
