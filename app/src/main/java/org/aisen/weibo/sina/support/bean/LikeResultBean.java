package org.aisen.weibo.sina.support.bean;

import java.io.Serializable;

/**
 * Created by wangdan on 15-3-7.
 */
public class LikeResultBean implements Serializable {

    private static final long serialVersionUID = -3410609361540356405L;

    private int ok;

    private String msg;

    public int getOk() {
        return ok;
    }

    public void setOk(int ok) {
        this.ok = ok;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
