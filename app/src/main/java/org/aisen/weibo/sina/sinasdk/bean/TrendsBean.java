package org.aisen.weibo.sina.sinasdk.bean;

import org.aisen.android.network.biz.IResult;
import org.aisen.android.support.bean.ResultBean;

import java.io.Serializable;
import java.util.List;

public class TrendsBean extends ResultBean implements Serializable {

	private static final long serialVersionUID = -1880799989137644825L;

	private List<TrendBean> list;
	
	public List<TrendBean> getList() {
		return list;
	}

	public void setList(List<TrendBean> list) {
		this.list = list;
	}

}
