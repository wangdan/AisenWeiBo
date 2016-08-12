package org.aisen.weibo.sina.sinasdk.bean;

import org.aisen.android.component.orm.annotation.PrimaryKey;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;

import java.io.Serializable;

public class StatusComment implements Serializable, BizFragment.ILikeBean {

	private static final long serialVersionUID = -8876057032378860108L;

	/**
	 * 创建时间
	 */
	private String created_at;
	/**
	 * 评论ID
	 */
	@PrimaryKey(column = "id")
	private String id;

	/**
	 * 评论内容
	 */
	private String text;

	/**
	 * 评论来源
	 */
	private String source;

	/**
	 * 评论的MID
	 */
	private String mid;

	/**
	 * 作者信息
	 */
	private WeiBoUser user;

	/**
	 * 评论的微博信息字段
	 */
	private StatusContent status;

	private String statusId;// 支持评论点赞

	/**
	 * 回复的评论信息字段
	 */
	private StatusComment reply_comment;

	private long likedCount;

	private boolean picture;

	private UrlBean videoUrl;

	private boolean liked;

	public String getCreated_at() {
		return created_at;
	}

	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public WeiBoUser getUser() {
		return user;
	}

	public void setUser(WeiBoUser user) {
		this.user = user;
	}

	public StatusContent getStatus() {
		return status;
	}

	public void setStatus(StatusContent status) {
		this.status = status;
	}

	public StatusComment getReply_comment() {
		return reply_comment;
	}

	public void setReply_comment(StatusComment reply_comment) {
		this.reply_comment = reply_comment;
	}

	public boolean isPicture() {
		return picture;
	}

	public void setPicture(boolean picture) {
		this.picture = picture;
	}

	public UrlBean getVideoUrl() {
		return videoUrl;
	}

	public void setVideoUrl(UrlBean videoUrl) {
		this.videoUrl = videoUrl;
	}

	public boolean isLiked() {
		return liked;
	}

	public void setLiked(boolean liked) {
		this.liked = liked;
	}

	public long getLikedCount() {
		return likedCount;
	}

	public void setLikedCount(long likedCount) {
		this.likedCount = likedCount;
	}

	@Override
	public String getLikeId() {
		return getId();
	}

	public String getStatusId() {
		return statusId;
	}

	public void setStatusId(String statusId) {
		this.statusId = statusId;
	}

}
