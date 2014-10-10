package org.sina.android.bean;

import java.io.Serializable;

import com.m.support.sqlite.annotation.Id;

public class StatusComment implements Serializable {

	private static final long serialVersionUID = -8876057032378860108L;

	/**
	 * 创建时间
	 */
	private String created_at;
	/**
	 * 评论ID
	 */
	@Id(column = "id")
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

	/**
	 * 回复的评论信息字段
	 */
	private StatusComment reply_comment;

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

}
