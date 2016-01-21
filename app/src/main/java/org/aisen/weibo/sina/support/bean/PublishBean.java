package org.aisen.weibo.sina.support.bean;


import org.aisen.android.component.orm.annotation.PrimaryKey;
import org.aisen.android.network.http.Params;
import org.aisen.weibo.sina.sinasdk.bean.StatusComment;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;

import java.io.Serializable;
import java.util.UUID;

public class PublishBean implements Serializable {

	private static final long serialVersionUID = -9160054733168344496L;
	
	public enum PublishStatus {
		// 新建
		create,
		// 发布失败
		faild, 
		// 草稿
		draft, 
		// 正在发布
		sending,
		// 等待发布
		waiting
	}
	
	private PublishStatus status;
	
	PublishType type;

	// 定时发布，单位为秒
	long timing = 0;
	
	long delay = 0;
	
	@PrimaryKey(column = "id")
	String id = UUID.randomUUID().toString();
	
	String text;
	
	String errorMsg;
	
	Params params;
	
	Params extras = new Params();
	
	StatusContent statusContent;

	StatusComment statusComment;

    String[] pics;

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

	public PublishType getType() {
		return type;
	}

	public void setType(PublishType type) {
		this.type = type;
	}

	public PublishStatus getStatus() {
		return status;
	}

	public void setStatus(PublishStatus status) {
		this.status = status;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public Params getParams() {
		return params;
	}

	public void setParams(Params params) {
		this.params = params;
	}

	public Params getExtras() {
		return extras;
	}

	public void setExtras(Params extras) {
		this.extras = extras;
	}

	public StatusContent getStatusContent() {
		return statusContent;
	}

	public void setStatusContent(StatusContent statusContent) {
		this.statusContent = statusContent;
	}

	public StatusComment getStatusComment() {
		return statusComment;
	}

	public void setStatusComment(StatusComment statusComment) {
		this.statusComment = statusComment;
	}

	public long getTiming() {
		return timing;
	}

	public void setTiming(long timing) {
		this.timing = timing;
	}

	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

    public String[] getPics() {
        return pics;
    }

    public void setPics(String[] pics) {
        this.pics = pics;
    }
}
