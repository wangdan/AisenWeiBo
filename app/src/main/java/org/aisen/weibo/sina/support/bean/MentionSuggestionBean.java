package org.aisen.weibo.sina.support.bean;

import org.aisen.weibo.sina.sinasdk.bean.SuggestionAtUser;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;

import java.io.Serializable;

public class MentionSuggestionBean implements Serializable {

	private static final long serialVersionUID = -1834749268568390229L;

	private WeiBoUser user;
	
	private SuggestionAtUser suggestUser;

	public WeiBoUser getUser() {
		return user;
	}

	public void setUser(WeiBoUser user) {
		this.user = user;
	}

	public SuggestionAtUser getSuggestUser() {
		return suggestUser;
	}

	public void setSuggestUser(SuggestionAtUser suggestUser) {
		this.suggestUser = suggestUser;
	}
	
}
