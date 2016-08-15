package org.aisen.weibo.sina.sinasdk.bean;

import org.aisen.android.support.bean.ResultBean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wangdan on 16/8/14.
 */
public class WebHotTopicssBean extends ResultBean implements Serializable {

    private static final long serialVersionUID = 6557233609789759518L;

    private List<WebHotTopicsBean> list;

    private String since_id;// 下一页分页

    private int page = -1;// 下一页分页

    public List<WebHotTopicsBean> getList() {
        return list;
    }

    public void setList(List<WebHotTopicsBean> list) {
        this.list = list;
    }

    public String getSince_id() {
        return since_id;
    }

    public void setSince_id(String since_id) {
        this.since_id = since_id;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

}
