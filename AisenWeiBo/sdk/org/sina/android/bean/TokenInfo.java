package org.sina.android.bean;

import java.io.Serializable;

public class TokenInfo implements Serializable {

	private static final long serialVersionUID = -7790263696180422692L;

	private String uid;
	
	private String appkey;
	
	private String scope;
	
	private String create_at;
	
	private String expire_in;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getAppkey() {
		return appkey;
	}

	public void setAppkey(String appkey) {
		this.appkey = appkey;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getCreate_at() {
		return create_at;
	}

	public void setCreate_at(String create_at) {
		this.create_at = create_at;
	}

	public String getExpire_in() {
		return expire_in;
	}

	public void setExpire_in(String expire_in) {
		this.expire_in = expire_in;
	}
	
}
