package org.aisen.weibo.sina.support.bean;

import java.io.Serializable;

import org.sina.android.bean.SuggestionAtUser;
import org.sina.android.bean.WeiBoUser;

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
