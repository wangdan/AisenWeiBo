package org.aisen.weibo.sina.sinasdk.bean;

import org.aisen.android.component.orm.annotation.PrimaryKey;

import java.io.Serializable;

/**
 * Created by wangdan on 15/6/29.
 */
public class UploadPictureBean implements Serializable {

    private static final long serialVersionUID = 6074139799176006180L;

    @PrimaryKey(column = "key")
    private String key;

    private String pic_id;// 服务端返回的id

    private String error;// 是否发生了异常

    private String path;// 本地路径

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPic_id() {
        return pic_id;
    }

    public void setPic_id(String pic_id) {
        this.pic_id = pic_id;
    }

}
