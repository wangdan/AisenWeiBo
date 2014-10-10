package org.sina.android.bean;

import java.io.Serializable;
import java.util.UUID;

import com.m.support.sqlite.annotation.Id;

/**
 * 用户的各种消息未读数
 * 
 * @author wangdan
 * 
 */
public class UnreadCount implements Serializable {

	private static final long serialVersionUID = 3633461417848980465L;

	@Id(column = "id")
	private String id = UUID.randomUUID().toString();

	/**
	 * 新微博未读数
	 */
	private int status;

	/**
	 * 新粉丝数
	 */
	private int follower;

	/**
	 * 新评论数
	 */
	private int cmt;

	/**
	 * 新私信数
	 */
	private int dm;// privateMessage

	/**
	 * 新提及我的微博数
	 */
	private int mention_status;

	/**
	 * 新提及我的评论数
	 */
	private int mention_cmt;

	/**
	 * 微群消息未读数
	 */
//	private int group;

	/**
	 * 私有微群消息未读数
	 */
	private int private_group;

	/**
	 * 新通知未读数 新邀请未读数
	 */
	private int notice;

	/**
	 * 新邀请未读数
	 */
	private int invite;

	/**
	 * 新勋章数
	 */
	private int badge;

	/**
	 * 相册消息未读数
	 */
	private int photo;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getFollower() {
		return follower;
	}

	public void setFollower(int follower) {
		this.follower = follower;
	}

	public int getCmt() {
		return cmt;
	}

	public void setCmt(int cmt) {
		this.cmt = cmt;
	}

	public int getDm() {
		return dm;
	}

	public void setDm(int dm) {
		this.dm = dm;
	}

	public int getMention_status() {
		return mention_status;
	}

	public void setMention_status(int mention_status) {
		this.mention_status = mention_status;
	}

	public int getMention_cmt() {
		return mention_cmt;
	}

	public void setMention_cmt(int mention_cmt) {
		this.mention_cmt = mention_cmt;
	}

	public int getPrivate_group() {
		return private_group;
	}

	public void setPrivate_group(int private_group) {
		this.private_group = private_group;
	}

	public int getNotice() {
		return notice;
	}

	public void setNotice(int notice) {
		this.notice = notice;
	}

	public int getInvite() {
		return invite;
	}

	public void setInvite(int invite) {
		this.invite = invite;
	}

	public int getBadge() {
		return badge;
	}

	public void setBadge(int badge) {
		this.badge = badge;
	}

	public int getPhoto() {
		return photo;
	}

	public void setPhoto(int photo) {
		this.photo = photo;
	}
	
}
