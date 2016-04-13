package org.aisen.weibo.sina.sinasdk.bean;

import org.aisen.android.support.bean.ResultBean;

import java.io.Serializable;
import java.util.List;

public class StatusComments extends ResultBean implements Serializable {

	private static final long serialVersionUID = 2420923134169920046L;
	
	private List<StatusComment> comments;

	public StatusComments() {

	}

	public StatusComments(List<StatusComment> comments) {
		this.comments = comments;
	}

	public List<StatusComment> getComments() {
		return comments;
	}

	public void setComments(List<StatusComment> comments) {
		this.comments = comments;
	}

}
