package org.aisen.weibo.sina.sinasdk.bean;

import org.aisen.android.component.orm.annotation.PrimaryKey;

import java.io.Serializable;

public class SuggestionAtUser implements Serializable {

	private static final long serialVersionUID = -1295044472748529056L;

	@PrimaryKey(column = "id")
	private String uid;

	private String nickname;

	private String remark;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

}
