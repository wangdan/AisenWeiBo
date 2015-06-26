package org.aisen.weibo.sina.sinasdk.bean;

import java.io.Serializable;
import java.util.List;

public class DirectMessages implements Serializable {

	private static final long serialVersionUID = -5260906504391469073L;

	private List<DirectMessage> direct_messages;

	public List<DirectMessage> getDirect_messages() {
		return direct_messages;
	}

	public void setDirect_messages(List<DirectMessage> direct_messages) {
		this.direct_messages = direct_messages;
	}
	
}
