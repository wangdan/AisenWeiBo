package org.aisen.android.component.eventbus;

/**
 * 对象型消息订阅者接口
 * @author wenbiao.xie
 * 
 * @param <T>
 */
public interface Subscriber<T>
{
	/**
      * Handle a published event. 
      *
      * @param event The Object that is being published.
    **/
	void onEvent(T event);

}
