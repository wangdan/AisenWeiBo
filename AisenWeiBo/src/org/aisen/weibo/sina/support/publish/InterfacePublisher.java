package org.aisen.weibo.sina.support.publish;

import org.aisen.weibo.sina.support.bean.PublishBean;


public interface InterfacePublisher {

//	public void publishTimeline(boolean isDraft, String text, String visible, String list_id, String[] statusImages, double lat, double lng);
//
//	public void publishComment(boolean isDraft, String text, StatusContent status, boolean comment_ori, boolean forward);
//
//	public void publishCommentsReply(boolean isDraft, String cid, String id, String comment, String without_mention, String comment_ori,
//			boolean forward);
//
//	public void publishStatusRepost(boolean isDraft, StatusContent status, String text, String is_comment);
	
	public void publish(PublishBean data);

}
