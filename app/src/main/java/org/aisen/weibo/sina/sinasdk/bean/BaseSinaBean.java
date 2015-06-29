package org.aisen.weibo.sina.sinasdk.bean;

import java.io.Serializable;

/**
 * Created by wangdan on 15/6/29.
 */
public class BaseSinaBean implements Serializable {

    private long error_code;

    private String error;

    public long getError_code() {
        return error_code;
    }

    public void setError_code(long error_code) {
        this.error_code = error_code;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
