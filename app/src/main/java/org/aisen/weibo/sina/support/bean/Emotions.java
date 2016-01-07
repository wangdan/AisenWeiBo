package org.aisen.weibo.sina.support.bean;

import java.io.Serializable;
import java.util.List;

public class Emotions implements Serializable {

	private static final long serialVersionUID = 6179327495902944739L;

	private List<Emotion> emotions;

	public List<Emotion> getEmotions() {
		return emotions;
	}

	public void setEmotions(List<Emotion> emotions) {
		this.emotions = emotions;
	}
	
}
