package org.aisen.weibo.sina.support.bean;

import org.aisen.android.support.bean.ResultBean;

import java.io.Serializable;
import java.util.List;

public class PhotosBean extends ResultBean implements Serializable {

	private static final long serialVersionUID = -5782864823784522733L;

	private List<PhotoBean> list;

	public List<PhotoBean> getList() {
		return list;
	}

	public void setList(List<PhotoBean> list) {
		this.list = list;
	}

}
