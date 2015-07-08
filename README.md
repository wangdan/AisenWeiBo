# Aisen微博

Aisen微博是新浪微博的第三方客户端，UI遵循Material Design。

[![Get it on Google Play](http://www.android.com/images/brand/get_it_on_play_logo_small.png)](http://play.google.com/store/apps/details?id=org.aisen.weibo.sina)

你也可以在应用市场下载应用体验

[豌豆荚](http://www.wandoujia.com/apps/org.aisen.weibo.sina)

[酷安](http://coolapk.com/apk/org.aisen.weibo.sina)

[最新测试版](https://github.com/wangdan/AisenWeibo/raw/master/resource/v5_0_6.apk)

## 说明
新浪目前已经限制了第三方微博的很多API接口，加上平常时间不够，所以后续可能不会面向产品的去维护Aisen，不过也有了一些新的方向，例如引入最新[Android-support-library](https://blog.leancloud.cn/3306/)，在一个完整的APP项目中示例最新的玩意儿，如果你也想以Aisen作为示例项目切入自己的Library库或者UI控件实现某部分的功能，可以联系我。

## 基本功能
 
 * 遵循Material Design
 * 发布多图、离线下载、私信（触屏版）
 * 颜色主题切换
 * 手势返回，4.4、5.0状态栏变色
 * 离线编辑，定时发布
 * 多图、gif、长微博预览
 * 等等

## 应用截图

![](https://github.com/wangdan/AisenWeibo/raw/master/resource/aisen1.gif) 

![](https://github.com/wangdan/AisenWeibo/raw/master/resource/aisen2.gif) 

![](https://github.com/wangdan/AisenWeibo/raw/master/resource/aisen3.gif) 

![](https://github.com/wangdan/AisenWeibo/raw/master/resource/aisen4.gif) 

![](https://github.com/wangdan/AisenWeibo/raw/master/resource/aisen5.gif) 

![](https://github.com/wangdan/AisenWeibo/raw/master/resource/aisen6.gif) 

![](https://github.com/wangdan/AisenWeibo/raw/master/resource/aisen7.gif) 

![](https://github.com/wangdan/AisenWeibo/raw/master/resource/aisen8.gif) 

![](https://github.com/wangdan/AisenWeibo/raw/master/resource/git_2.png)

![](https://github.com/wangdan/AisenWeibo/raw/master/resource/git_6.png) 

![](https://github.com/wangdan/AisenWeibo/raw/master/resource/git_4.png) 

![](https://github.com/wangdan/AisenWeibo/raw/master/resource/git_5.png) 

![](https://github.com/wangdan/AisenWeibo/raw/master/resource/git_3.png) 

![](https://github.com/wangdan/AisenWeibo/raw/master/resource/git_7.png) 

![](https://github.com/wangdan/AisenWeibo/raw/master/resource/git_1.png) 

## 须知
 * 私信、热门微博、热门话题、微博头条都采用内置官方触屏版实现
 * 多图上传、点赞功能没有权限
 * 用户粉丝、关注只能获取30%的数据
 * 用户搜索限制：v用户、粉丝500以上的达人、粉丝600以上的普通用户

## 配置
 * 导入[AisenForAndroid](https://github.com/wangdan/AisenForAndroid)，已包含所有依赖项目；
 * 配置action.xml，设置你的appkey等相关参数
```java	
	<setting type="app_key">
		<des>APP应用授权key</des>
		<value></value>
	</setting>

	<setting type="app_secret">
		<des>APP应用授权secret</des>
		<value></value>
	</setting>

	<setting type="callback_url">
		<des>授权回调地址</des>
		<value></value>
	</setting>
```

## License

Copyright (c) 2014 Jeff Wang

Licensed under the [Apache License, Version 3.0](http://opensource.org/licenses/GPL-3.0)

