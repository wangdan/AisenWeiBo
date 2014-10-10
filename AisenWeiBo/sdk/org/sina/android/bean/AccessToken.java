package org.sina.android.bean;

import java.io.Serializable;

public class AccessToken extends Token implements Serializable{

	private static final long serialVersionUID = 1L;

	private String verifier;

	private String uid;
	
	private String access_token;
	
	public String getVerifier() {
		return verifier;
	}

	public void setVerifier(String verifier) {
		this.verifier = verifier;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
	
	@Override
	public String getToken() {
		return getAccess_token();
	}
	
	public String getAccess_token() {
		return access_token;
	}

	public void setAccess_token(String access_token) {
		this.access_token = access_token;
		setToken(access_token);
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("oautUid=").append(uid).append("\n");
		buffer.append("oauthVerifier=").append(verifier).append("\n");

		return buffer.toString();
	}
}
