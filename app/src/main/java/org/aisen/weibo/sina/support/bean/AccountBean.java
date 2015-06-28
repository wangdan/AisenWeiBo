package org.aisen.weibo.sina.support.bean;

import java.io.Serializable;

import org.aisen.weibo.sina.sinasdk.bean.AccessToken;
import org.aisen.weibo.sina.sinasdk.bean.Groups;
import org.aisen.weibo.sina.sinasdk.bean.Token;
import org.aisen.weibo.sina.sinasdk.bean.TokenInfo;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;

import org.aisen.orm.annotation.PrimaryKey;

public class AccountBean implements Serializable {

	private static final long serialVersionUID = -6805443927915693862L;

	@PrimaryKey(column = "userId")
	private String userId;
	
	private WeiBoUser user;// 用户的个人信息
	
	private Groups groups;// 用户的分组信息
	
	private AccessToken advancedToken;// 高级授权的Token

	private AccessToken token;// 应用授权的token

    private String account;// 用户的账号

    private String password;// 用户的密码

    private String cookie;// 网页授权的cookie

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

	public AccessToken getToken() {
		return token;
	}

	public void setToken(AccessToken token) {
		this.token = token;
	}

	public Groups getGroups() {
		return groups;
	}

	public void setGroups(Groups groups) {
		this.groups = groups;
	}

//	public static boolean isExpired(AccountBean account) {
//		TokenInfo tokenInfo = account.getTokenInfo();
//		if (tokenInfo != null) {
//			long validSecond = Long.parseLong(tokenInfo.getCreate_at()) + Long.parseLong(tokenInfo.getExpire_in());
//			if (System.currentTimeMillis() > validSecond * 1000) {
//
//				return true;
//			}
//		}

//		return false;
//	}

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
