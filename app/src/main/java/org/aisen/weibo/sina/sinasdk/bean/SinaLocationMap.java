package org.aisen.weibo.sina.sinasdk.bean;

import java.io.Serializable;

public class SinaLocationMap implements Serializable {

	private static final long serialVersionUID = 8514193431800290063L;

	private SinaLocation location;

	public SinaLocation getLocation() {
		return location;
	}

	public void setLocation(SinaLocation location) {
		this.location = location;
	}
	
}
