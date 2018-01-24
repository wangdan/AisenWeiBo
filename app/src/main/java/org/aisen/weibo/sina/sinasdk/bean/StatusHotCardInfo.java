package org.aisen.weibo.sina.sinasdk.bean;

import java.io.Serializable;

/**
 * Created by wangdan on 16/8/10.
 */
public class StatusHotCardInfo implements Serializable {

    private static final long serialVersionUID = -1683135546660048135L;

    private int since_id;

    public int getSince_id() {
        return since_id;
    }

    public void setSince_id(int since_id) {
        this.since_id = since_id;
    }

}
