package org.aisen.weibo.sina.support.bean;

import java.io.Serializable;
import java.util.List;

public class MentionSuggestionBeans implements Serializable {

	private static final long serialVersionUID = -1780582484661436918L;

	private List<MentionSuggestionBean> list;

	public List<MentionSuggestionBean> getList() {
		return list;
	}

	public void setList(List<MentionSuggestionBean> list) {
		this.list = list;
	}
	
}
