package org.aisen.weibo.sina.sinasdk.bean;

import org.aisen.android.support.bean.ResultBean;

import java.io.Serializable;

/**
 * Created by wangdan on 15/6/29.
 */
public class BaseSinaBean extends ResultBean implements Serializable {

    private static final long serialVersionUID = 5080818027671237851L;

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
