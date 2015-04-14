package org.aisen.weibo.sina.support.bean;

import com.m.component.sqlite.annotation.PrimaryKey;

import org.sina.android.bean.AccessToken;
import org.sina.android.bean.Groups;
import org.sina.android.bean.Token;
import org.sina.android.bean.TokenInfo;
import org.sina.android.bean.WeiBoUser;

import java.io.Serializable;

public class AccountBean implements Serializable {

	private static final long serialVersionUID = -6805443927915693862L;

	@PrimaryKey(column = "userId")
	private String userId;
	
	private String _token;
	
	private String secret;

	private WeiBoUser user;
	
	private Groups groups;
	
	private TokenInfo tokenInfo;
	
	private String advancedAppKey;// 高级授权
	
	private String advancedAppScreet;// 高级授权
	
	private AccessToken advancedToken;// 高级授权的Token

    private String account;

    private String password;

    private String cookie;

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

	public String getAdvancedAppKey() {
		return advancedAppKey;
	}

	public void setAdvancedAppKey(String advancedAppKey) {
		this.advancedAppKey = advancedAppKey;
	}

	public String getAdvancedAppScreet() {
		return advancedAppScreet;
	}

	public void setAdvancedAppScreet(String advancedAppScreet) {
		this.advancedAppScreet = advancedAppScreet;
	}

	public AccessToken getAdvancedToken() {
		return advancedToken;
	}

	public void setAdvancedToken(AccessToken advancedToken) {
		this.advancedToken = advancedToken;
	}

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }
}
