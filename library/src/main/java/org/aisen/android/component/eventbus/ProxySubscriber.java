package org.aisen.android.component.eventbus;


/**
 * An interface that can be implemented when proxies are used for subscription, not needed in normal usage.  When an
 * unsubscribe method is called on an EventService, the EventService is required to check if any of subscribed objects
 * are ProxySubscribers and if the object to be unsubscribed is the ProxySubscriber's proxiedSubscriber. If so, the
 * EventService proxy is unsubscribed and the ProxySubscriber's proxyUnsubscribed() method is called to allow the proxy
 * to perform any cleanup if necessary.  ProxySubscribers should set their references to their proxied objects to null
 * for strong subscriptions to allow garbage collection.
 *
 * @author wenbiao.xie
 */
public interface ProxySubscriber {

   /** @return the object this proxy is subscribed on behalf of */
   Object getProxiedSubscriber();

   /**
    * Called by EventServices to inform the proxy that it is unsubscribed.  The ProxySubscriber should null the
    * reference to it's proxied subscriber
    */
   void proxyUnsubscribed();

   /**
    * @return the reference strength from this proxy to the proxied subscriber
    */
   ReferenceStrength getReferenceStrength();

}
