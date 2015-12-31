package org.aisen.weibo.sina.sinasdk.bean;

import org.aisen.android.support.bean.ResultBean;

import java.io.Serializable;
import java.util.List;

public class StatusRepost extends ResultBean implements Serializable {

	private static final long serialVersionUID = 1388689503057498611L;

	private List<StatusContent> reposts;
	
	public StatusRepost() {
		
	}
	
	public StatusRepost(List<StatusContent> reposts) {
		this.reposts = reposts;
	}

	public List<StatusContent> getReposts() {
		return reposts;
	}

	public void setReposts(List<StatusContent> reposts) {
		this.reposts = reposts;
	}
}
