package org.sina.android.bean;

import java.io.Serializable;

import com.m.support.sqlite.annotation.Id;

/**
 * 微博用户信息
 * 
 * @author wangdan
 * 
 */
public class WeiBoUser implements Serializable {

	private static final long serialVersionUID = 7431379231256590182L;

	private String token;
	private String secret;
	private String recentStatusId;
	private byte[] photoData;

	/**
	 * 用户UID
	 */
	private String id;

	@Id(column = "idStr")
	private String idstr;

	/**
	 * 微博昵称
	 */
	private String screen_name;

	/**
	 * 友好显示名称，同微博昵称
	 */
	private String name;

	/**
	 * 省份编码（参考省份编码表）
	 */
	private Integer province;

	/**
	 * 城市编码（参考城市编码表）
	 */
	private Integer city;

	/**
	 * 地址
	 */
	private String location;

	/**
	 * 个人描述
	 */
	private String description;

	/**
	 * 用户博客地址
	 */
	private String url;

	/**
	 * 自定义图像
	 */
	private String profile_image_url;

	/**
	 * 用户个性化URL
	 */
	private String domain;

	/**
	 * 性别,m--男，f--女,n--未知
	 */
	private String gender;

	/**
	 * 粉丝数
	 */
	private Integer followers_count;

	/**
	 * 关注数
	 */
	private Integer friends_count;

	/**
	 * 微博数
	 */
	private Integer statuses_count;

	/**
	 * 收藏数
	 */
	private Integer favourites_count;

	/**
	 * 创建时间
	 */
	private String created_at;

	/**
	 * 是否已关注(此特性暂不支持)
	 */
	private Boolean following;

	/**
	 * 加V标示，是否微博认证用户
	 */
	private Boolean verified;

	/**
	 * 用户大头像地址
	 */
	private String avatar_large;

	/**
	 * 用户的在线状态，0：不在线、1：在线
	 */
	private Integer online_status;

	/**
	 * 用户的互粉数
	 */
	private Integer bi_followers_count;

	/**
	 * 是否允许所有人对我的微博进行评论
	 */
	private Boolean allow_all_comment;

	/**
	 * 是否允许带有地理信息
	 */
	private Boolean geo_enabled;

	/**
	 * 用户的最近一条微博信息字段
	 */
	private StatusContent status;

	/**
	 * 用户备注信息，只有在查询用户关系时才返回此字段
	 */
	private String remark;
	
	/**
	 * 认证类型
	 */
	private Integer verified_type;
	
	/**
	 * 认证原因
	 */
	private String verified_reason;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getRecentStatusId() {
		return recentStatusId;
	}

	public void setRecentStatusId(String recentStatusId) {
		this.recentStatusId = recentStatusId;
	}

	public byte[] getPhotoData() {
		return photoData;
	}

	public void setPhotoData(byte[] photoData) {
		this.photoData = photoData;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getScreen_name() {
		return screen_name;
	}

	public void setScreen_name(String screen_name) {
		this.screen_name = screen_name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getProvince() {
		return province;
	}

	public void setProvince(Integer province) {
		this.province = province;
	}

	public Integer getCity() {
		return city;
	}

	public void setCity(Integer city) {
		this.city = city;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getProfile_image_url() {
		return profile_image_url;
	}

	public void setProfile_image_url(String profile_image_url) {
		this.profile_image_url = profile_image_url;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Integer getFollowers_count() {
		return followers_count;
	}

	public void setFollowers_count(Integer followers_count) {
		this.followers_count = followers_count;
	}

	public Integer getFriends_count() {
		return friends_count;
	}

	public void setFriends_count(Integer friends_count) {
		this.friends_count = friends_count;
	}

	public Integer getStatuses_count() {
		return statuses_count;
	}

	public void setStatuses_count(Integer statuses_count) {
		this.statuses_count = statuses_count;
	}

	public Integer getFavourites_count() {
		return favourites_count;
	}

	public void setFavourites_count(Integer favourites_count) {
		this.favourites_count = favourites_count;
	}

	public String getCreated_at() {
		return created_at;
	}

	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}

	public Boolean getFollowing() {
		return following;
	}

	public void setFollowing(Boolean following) {
		this.following = following;
	}

	public Boolean getVerified() {
		return verified;
	}

	public void setVerified(Boolean verified) {
		this.verified = verified;
	}

	public String getAvatar_large() {
		return avatar_large;
	}

	public void setAvatar_large(String avatar_large) {
		this.avatar_large = avatar_large;
	}

	public Integer getOnline_status() {
		return online_status;
	}

	public void setOnline_status(Integer online_status) {
		this.online_status = online_status;
	}

	public Integer getBi_followers_count() {
		return bi_followers_count;
	}

	public void setBi_followers_count(Integer bi_followers_count) {
		this.bi_followers_count = bi_followers_count;
	}

	public Boolean getAllow_all_comment() {
		return allow_all_comment;
	}

	public void setAllow_all_comment(Boolean allow_all_comment) {
		this.allow_all_comment = allow_all_comment;
	}

	public Boolean getGeo_enabled() {
		return geo_enabled;
	}

	public void setGeo_enabled(Boolean geo_enabled) {
		this.geo_enabled = geo_enabled;
	}

	public StatusContent getStatus() {
		return status;
	}

	public void setStatus(StatusContent status) {
		this.status = status;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getIdstr() {
		return idstr;
	}

	public void setIdstr(String idstr) {
		this.idstr = idstr;
	}

	public String getVerified_reason() {
		return verified_reason;
	}

	public void setVerified_reason(String verified_reason) {
		this.verified_reason = verified_reason;
	}

	public Integer getVerified_type() {
		return verified_type;
	}

	public void setVerified_type(Integer verified_type) {
		this.verified_type = verified_type;
	}

}
