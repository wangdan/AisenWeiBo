package org.sina.android.bean;

import java.io.Serializable;

/**
 * oauth授权token
 * 
 * @author wangdan
 * 
 */
public class Token implements Serializable {

	private static final long serialVersionUID = 1L;

	private String token;

	private String secret;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("oauthToken=").append(token).append("\n");
		buffer.append("oauthTokenSecret=").append(secret).append("\n");

		return buffer.toString();
	}

}
