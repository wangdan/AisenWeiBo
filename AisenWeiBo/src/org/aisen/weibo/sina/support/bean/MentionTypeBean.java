package org.aisen.weibo.sina.support.bean;

import com.m.ui.fragment.ATabTitlePagerFragment.TabTitlePagerBean;

public class MentionTypeBean extends TabTitlePagerBean {

	private static final long serialVersionUID = 8995671306485265842L;
	
	/**
	 * 0:提及的微博,1:提及的评论
	 */
	private String type;
	
	public MentionTypeBean() {
		
	}
	
	public MentionTypeBean(String type, String title) {
		this.type = type;
		setTitle(title);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
