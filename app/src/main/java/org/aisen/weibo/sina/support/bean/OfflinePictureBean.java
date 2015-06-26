package org.aisen.weibo.sina.support.bean;

import java.io.Serializable;

import org.aisen.orm.annotation.PrimaryKey;

/**
 * Created by wangdan on 15/5/3.
 */
public class OfflinePictureBean implements Serializable {

    @PrimaryKey(column = "beanId")
    private String beanId;

    private String thumb;// 缩略图

    private String groupId;

    private int status;// 0:未下载,1:下载失败,2:正在下载,10:下完成

    private int version;

    private long length;// 图片大小

    public String getBeanId() {
        return beanId;
    }

    public void setBeanId(String beanId) {
        this.beanId = beanId;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }
}