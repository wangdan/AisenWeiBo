package org.aisen.weibo.weex.adapter;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.taobao.weex.InitConfig;
import com.taobao.weex.WXEnvironment;
import com.taobao.weex.WXSDKEngine;
import com.taobao.weex.WXSDKManager;
import com.taobao.weex.bridge.WXBridgeManager;
import com.taobao.weex.common.WXException;

import org.aisen.weibo.weex.component.RichText;
import org.aisen.weibo.weex.component.WXMask;
import org.aisen.weibo.weex.component.WXParallax;
import org.aisen.weibo.weex.module.*;

public class Init {

    public static void setup(Application context) {
        /**
         * Set up for fresco usage.
         * Set<RequestListener> requestListeners = new HashSet<>();
         * requestListeners.add(new RequestLoggingListener());
         * ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this)
         *     .setRequestListeners(requestListeners)
         *     .build();
         * Fresco.initialize(this,config);
         **/
//    initDebugEnvironment(true, false, "DEBUG_SERVER_HOST");
        WXBridgeManager.updateGlobalConfig("wson_on");
        WXEnvironment.setOpenDebugLog(true);
        WXEnvironment.setApkDebugable(true);
        WXSDKEngine.addCustomOptions("appName", "WXSample");
        WXSDKEngine.addCustomOptions("appGroup", "WXApp");
        WXSDKEngine.initialize(context,
                new InitConfig.Builder()
                        //.setImgAdapter(new FrescoImageAdapter())// use fresco adapter
                        .setImgAdapter(new ImageAdapter())
//                        .setWebSocketAdapterFactory(new DefaultWebSocketAdapterFactory())
//                        .setJSExceptionAdapter(new JSExceptionAdapter())
//                        .setHttpAdapter(new InterceptWXHttpAdapter())
                        .build()
        );

        WXSDKManager.getInstance().setAccessibilityRoleAdapter(new DefaultAccessibilityRoleAdapter());

        try {
//            WXSDKEngine.registerComponent("synccomponent", WXComponentSyncTest.class);
            WXSDKEngine.registerComponent(WXParallax.PARALLAX, WXParallax.class);

            WXSDKEngine.registerComponent("richtext", RichText.class);
            WXSDKEngine.registerModule("render", RenderModule.class);
            WXSDKEngine.registerModule("event", WXEventModule.class);
            WXSDKEngine.registerModule("syncTest", SyncTestModule.class);

            WXSDKEngine.registerComponent("mask", WXMask.class);
            WXSDKEngine.registerModule("myModule", MyModule.class);
            WXSDKEngine.registerModule("geolocation", GeolocationModule.class);

            WXSDKEngine.registerModule("titleBar", WXTitleBar.class);

            WXSDKEngine.registerModule("wsonTest", WXWsonTestModule.class);


            /**
             * override default image tag
             * WXSDKEngine.registerComponent("image", FrescoImageComponent.class);
             */

            //Typeface nativeFont = Typeface.createFromAsset(getAssets(), "font/native_font.ttf");
            //WXEnvironment.setGlobalFontFamily("bolezhusun", nativeFont);

        } catch (WXException e) {
            e.printStackTrace();
        }

        context.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                // The demo code of calling 'notifyTrimMemory()'
                if (false) {
                    // We assume that the application is on an idle time.
                    WXSDKManager.getInstance().notifyTrimMemory();
                }
                // The demo code of calling 'notifySerializeCodeCache()'
                if (false) {
                    WXSDKManager.getInstance().notifySerializeCodeCache();
                }
            }
        });
    }

}
