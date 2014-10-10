package org.sina.android.bean;

import java.io.Serializable;

import com.m.support.sqlite.annotation.Id;

/**
 * 好友分组信息
 * 
 * @author wangdan
 * 
 */
public class Group implements Serializable {

	private static final long serialVersionUID = 1364226754711881171L;

	@Id(column = "id")
	private String id;

	private String idstr;

	private String name;

	private String mode;

	private Integer visible;

	private Integer like_count;

	private Integer member_count;

	private String description;

	private String profile_image_url;

	private WeiBoUser user;

	private boolean offline = false;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIdstr() {
		return idstr;
	}

	public void setIdstr(String idstr) {
		this.idstr = idstr;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public Integer getVisible() {
		return visible;
	}

	public void setVisible(Integer visible) {
		this.visible = visible;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getProfile_image_url() {
		return profile_image_url;
	}

	public void setProfile_image_url(String profile_image_url) {
		this.profile_image_url = profile_image_url;
	}

	public WeiBoUser getUser() {
		return user;
	}

	public void setUser(WeiBoUser user) {
		this.user = user;
	}

	public Integer getLike_count() {
		return like_count;
	}

	public void setLike_count(Integer like_count) {
		this.like_count = like_count;
	}

	public Integer getMember_count() {
		return member_count;
	}

	public void setMember_count(Integer member_count) {
		this.member_count = member_count;
	}

	public boolean isOffline() {
		return offline;
	}

	public void setOffline(boolean offline) {
		this.offline = offline;
	}

}
