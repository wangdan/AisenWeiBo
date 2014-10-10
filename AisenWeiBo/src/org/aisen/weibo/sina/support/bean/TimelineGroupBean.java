package org.aisen.weibo.sina.support.bean;

import com.m.ui.fragment.ATabTitlePagerFragment.TabTitlePagerBean;

public class TimelineGroupBean extends TabTitlePagerBean {

	private static final long serialVersionUID = -6257057073847009248L;

	/**
	 * 分组: 0-默认分组, 1-好友分组
	 */
	private String group;

	public TimelineGroupBean() {
		
	}
	
	public TimelineGroupBean(String type, String title) {
		this.group = type;
		setType(type);
		setTitle(title);
	}
	
	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}
	
}
