package org.aisen.weibo.sina.support.bean;

import java.io.Serializable;

import org.sina.android.bean.Groups;
import org.sina.android.bean.Token;
import org.sina.android.bean.TokenInfo;
import org.sina.android.bean.WeiBoUser;

import com.m.support.sqlite.annotation.Id;

public class AccountBean implements Serializable {

	private static final long serialVersionUID = -6805443927915693862L;

	@Id(column = "userId")
	private String userId;
	
	private String _token;
	
	private String secret;

	private WeiBoUser user;
	
	private Groups groups;
	
	private TokenInfo tokenInfo;

	public String get_token() {
		return _token;
	}

	public void set_token(String _token) {
		this._token = _token;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public WeiBoUser getUser() {
		return user;
	}

	public void setUser(WeiBoUser user) {
		this.user = user;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	private Token token;

	public Token getToken() {
		if (token == null)
			token = new Token();
		token.setToken(_token);
		token.setSecret(secret);
		return token;
	}

	public Groups getGroups() {
		return groups;
	}

	public void setGroups(Groups groups) {
		this.groups = groups;
	}

	public TokenInfo getTokenInfo() {
		return tokenInfo;
	}

	public void setTokenInfo(TokenInfo tokenInfo) {
		this.tokenInfo = tokenInfo;
	}
	
	public static boolean isExpired(AccountBean account) {
		TokenInfo tokenInfo = account.getTokenInfo();
		if (tokenInfo != null) {
			long validSecond = Long.parseLong(tokenInfo.getCreate_at()) + Long.parseLong(tokenInfo.getExpire_in());
			if (System.currentTimeMillis() > validSecond * 1000) {
				
				return true;
			}
		}
		
		return false;
	}

}
