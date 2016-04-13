package org.aisen.weibo.sina.sinasdk.bean;

import org.aisen.android.support.bean.ResultBean;

import java.io.Serializable;
import java.util.List;

public class Favorities extends ResultBean implements Serializable {

	private static final long serialVersionUID = 7517616720409609120L;

	private List<Favority> favorites;

	private int total_number;

	public List<Favority> getFavorites() {
		return favorites;
	}

	public void setFavorites(List<Favority> favorites) {
		this.favorites = favorites;
	}

	public int getTotal_number() {
		return total_number;
	}

	public void setTotal_number(int total_number) {
		this.total_number = total_number;
	}

}
