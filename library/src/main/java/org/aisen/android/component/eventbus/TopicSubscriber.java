package org.aisen.android.component.eventbus;
/**
 * 主题型消息订阅接口
 * @author wenbiao.xie
 * 
 * @param <T>
 */
public interface TopicSubscriber<T>
{
	/**
	 * Handle an event published on a topic.
	 * 
	 * @param topic
	 *            the name of the topic published on
	 * @param data
	 *            the data object published on the topic
	 */
	void onEvent(String topic, T data);

}
