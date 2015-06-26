package org.aisen.weibo.sina.support.bean;

import java.io.Serializable;

import org.aisen.orm.annotation.PrimaryKey;

/**
 * Created by wangdan on 15/5/3.
 */
public class OfflineCommentBean implements Serializable {

    @PrimaryKey(column = "statusId")
    private String statusId;// 微博id

    private String groupId;// 分组id，如果一条微博存在于多个分组，那么只会加载一次

    public String getStatusId() {
        return statusId;
    }

    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
