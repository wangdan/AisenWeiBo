package org.aisen.weibo.sina.sinasdk.bean;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.component.orm.annotation.PrimaryKey;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class AccessToken extends Token implements Serializable {

	private static final long serialVersionUID = 1L;

	private String verifier;

	private String uid;
	
	private String access_token;

	private long expires_in;

    @PrimaryKey(column = "appKey")
    private String appKey;

    private String appScreet;

    private long create_at = System.currentTimeMillis();

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

    public long getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(long expires_in) {
        this.expires_in = expires_in;
    }

    public long getCreate_at() {
        return create_at;
    }

    public void setCreate_at(long create_at) {
        this.create_at = create_at;
    }

    public boolean isExpired() {
        String days = String.valueOf(TimeUnit.SECONDS.toDays(getExpires_in()));
        Logger.w("Logger", "%s还有%s天失效", uid, days);

        return System.currentTimeMillis() - create_at >= expires_in * 1000;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getAppScreet() {
        return appScreet;
    }

    public void setAppScreet(String appScreet) {
        this.appScreet = appScreet;
    }
}
