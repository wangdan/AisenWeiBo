package org.aisen.weibo.sina.sinasdk.bean;

import java.io.Serializable;

public class DirectMessage implements Serializable {

	private static final long serialVersionUID = 4265562508290170834L;

	private String id;
	
	private String idstr;
	
	private String created_at;
	
	private String text;
	
	private String sys_type;
	
	private String sender_id;
	
	private String recipient_id;
	
	private String sender_screen_name;
	
	private String recipient_screen_name;
	
	private WeiBoUser sender;
	
	private WeiBoUser recipient;
	
	private String mid;
	
	private boolean isLargeDm;
	
	private String source;
	
	private String status_id;
	
	private Geo geo;

	private String dm_type;

	private String media_type;

	private String ip;

	private boolean matchKeyword;

	private boolean topublic;

	private boolean pushToMPS;

	private String oriImageId;

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

	public String getCreated_at() {
		return created_at;
	}

	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getSys_type() {
		return sys_type;
	}

	public void setSys_type(String sys_type) {
		this.sys_type = sys_type;
	}

	public String getSender_id() {
		return sender_id;
	}

	public void setSender_id(String sender_id) {
		this.sender_id = sender_id;
	}

	public String getRecipient_id() {
		return recipient_id;
	}

	public void setRecipient_id(String recipient_id) {
		this.recipient_id = recipient_id;
	}

	public String getSender_screen_name() {
		return sender_screen_name;
	}

	public void setSender_screen_name(String sender_screen_name) {
		this.sender_screen_name = sender_screen_name;
	}

	public String getRecipient_screen_name() {
		return recipient_screen_name;
	}

	public void setRecipient_screen_name(String recipient_screen_name) {
		this.recipient_screen_name = recipient_screen_name;
	}

	public WeiBoUser getSender() {
		return sender;
	}

	public void setSender(WeiBoUser sender) {
		this.sender = sender;
	}

	public WeiBoUser getRecipient() {
		return recipient;
	}

	public void setRecipient(WeiBoUser recipient) {
		this.recipient = recipient;
	}

	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public boolean isLargeDm() {
		return isLargeDm;
	}

	public void setLargeDm(boolean isLargeDm) {
		this.isLargeDm = isLargeDm;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getStatus_id() {
		return status_id;
	}

	public void setStatus_id(String status_id) {
		this.status_id = status_id;
	}

	public Geo getGeo() {
		return geo;
	}

	public void setGeo(Geo geo) {
		this.geo = geo;
	}

	public String getDm_type() {
		return dm_type;
	}

	public void setDm_type(String dm_type) {
		this.dm_type = dm_type;
	}

	public String getMedia_type() {
		return media_type;
	}

	public void setMedia_type(String media_type) {
		this.media_type = media_type;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public boolean isMatchKeyword() {
		return matchKeyword;
	}

	public void setMatchKeyword(boolean matchKeyword) {
		this.matchKeyword = matchKeyword;
	}

	public boolean isTopublic() {
		return topublic;
	}

	public void setTopublic(boolean topublic) {
		this.topublic = topublic;
	}

	public boolean isPushToMPS() {
		return pushToMPS;
	}

	public void setPushToMPS(boolean pushToMPS) {
		this.pushToMPS = pushToMPS;
	}

	public String getOriImageId() {
		return oriImageId;
	}

	public void setOriImageId(String oriImageId) {
		this.oriImageId = oriImageId;
	}
	
}
