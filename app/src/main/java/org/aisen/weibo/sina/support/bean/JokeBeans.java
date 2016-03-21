package org.aisen.weibo.sina.support.bean;

import org.aisen.android.support.bean.ResultBean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wangdan on 16/3/14.
 */
public class JokeBeans extends ResultBean implements Serializable {

    private static final long serialVersionUID = 7448242204807892779L;

    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {

        private List<JokeBean> contents;

        public List<JokeBean> getContents() {
            return contents;
        }

        public void setContents(List<JokeBean> contents) {
            this.contents = contents;
        }
    }

}
