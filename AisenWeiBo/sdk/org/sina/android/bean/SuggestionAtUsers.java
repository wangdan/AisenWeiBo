package org.sina.android.bean;

import java.io.Serializable;
import java.util.List;

public class SuggestionAtUsers implements Serializable {

	private static final long serialVersionUID = 3844626402117393141L;

	private List<SuggestionAtUser> suggesstionUsers;

	public List<SuggestionAtUser> getSuggesstionUsers() {
		return suggesstionUsers;
	}

	public void setSuggesstionUsers(List<SuggestionAtUser> suggesstionUsers) {
		this.suggesstionUsers = suggesstionUsers;
	}

}
