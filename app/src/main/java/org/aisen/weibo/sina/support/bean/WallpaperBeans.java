package org.aisen.weibo.sina.support.bean;

import org.aisen.android.support.bean.ResultBean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wangdan on 16/3/23.
 */
public class WallpaperBeans extends ResultBean implements Serializable {

    private static final long serialVersionUID = 7798837539105164175L;

    private Data item;

    private String msg;

    private int status;

    public Data getItem() {
        return item;
    }

    public void setItem(Data item) {
        this.item = item;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public static class Data implements Serializable {

        private static final long serialVersionUID = 8023177893663617357L;

        private List<WallpaperBean> wallpaperList;

        private int count;

        public List<WallpaperBean> getWallpaperList() {
            return wallpaperList;
        }

        public void setWallpaperList(List<WallpaperBean> wallpaperList) {
            this.wallpaperList = wallpaperList;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

    }

}
