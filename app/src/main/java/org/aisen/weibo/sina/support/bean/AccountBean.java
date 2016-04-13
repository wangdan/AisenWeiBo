package org.aisen.weibo.sina.support.bean;

import org.aisen.android.component.orm.annotation.PrimaryKey;
import org.aisen.weibo.sina.sinasdk.bean.AccessToken;
import org.aisen.weibo.sina.sinasdk.bean.BaseSinaBean;
import org.aisen.weibo.sina.sinasdk.bean.Groups;
import org.aisen.weibo.sina.sinasdk.bean.UnreadCount;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;

/**
 * Created by wangdan on 15/12/31.
 */
public class AccountBean extends BaseSinaBean {

    private static final long serialVersionUID = -8974889891999245818L;

    @PrimaryKey(column = "uid")
    private String uid;

    private String account;

    private String password;

    private AccessToken accessToken;

    private WeiBoUser user;

    private Groups groups;

    private AccessToken advancedToken;

    private UnreadCount unreadCount = new UnreadCount();

    private String cookie;// 网页授权的cookie

    public AccessToken getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(AccessToken accessToken) {
        this.accessToken = accessToken;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public WeiBoUser getUser() {
        return user;
    }

    public void setUser(WeiBoUser user) {
        this.user = user;
    }

    public Groups getGroups() {
        return groups;
    }

    public void setGroups(Groups groups) {
        this.groups = groups;
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

    public AccessToken getAdvancedToken() {
        return advancedToken;
    }

    public void setAdvancedToken(AccessToken advancedToken) {
        this.advancedToken = advancedToken;
    }

    public UnreadCount getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(UnreadCount unreadCount) {
        this.unreadCount = unreadCount;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }
}
