package org.aisen.weibo.sina.support.bean;

import java.io.Serializable;

import org.aisen.orm.annotation.PrimaryKey;

/**
 * Created by wangdan on 15/5/3.
 */
public class GroupOfflineStatus implements Serializable {

    @PrimaryKey(column = "groupId")
    private String groupId;// 离线的分组

    private int version;// 当次离线的版本，用于程序崩溃或者意外停止，重启时检测是否继续离线

    private int statusCount;// 已离线微博数目

    private int cmtCount;// 已离线评论数量

    private long statusLength;// 已离线微博流量

    private long cmtLength;// 已离线评论流量

    // 11 1111
    private int status;// 0:未离线,0x01:正在离线,0x02:离线微博完成,0x04:离线评论完成,0x08:离线图片完成,0x10:全部离线完成,0x100:失败

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getStatusCount() {
        return statusCount;
    }

    public void setStatusCount(int statusCount) {
        this.statusCount = statusCount;
    }

    public int getCmtCount() {
        return cmtCount;
    }

    public void setCmtCount(int cmtCount) {
        this.cmtCount = cmtCount;
    }

    public long getStatusLength() {
        return statusLength;
    }

    public void setStatusLength(long statusLength) {
        this.statusLength = statusLength;
    }

    public long getCmtLength() {
        return cmtLength;
    }

    public void setCmtLength(long cmtLength) {
        this.cmtLength = cmtLength;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
