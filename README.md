# Aisen微博

**注意** 工程依赖[AisenForAndroid](https://github.com/wangdan/AisenForAndroid)

[![Get it on Google Play](http://www.android.com/images/brand/get_it_on_play_logo_small.png)](http://play.google.com/store/apps/details?id=org.aisen.weibo.sina)

## 说明
Aisen微博是新浪微博的第三方客户端，UI遵循Material Design。

## 基本功能
 
 * 私信功能(采用内置官方触屏版实现，可发图片，4.4.1、4.4.2暂不支持发图)
 * 热门微博、热门话题、微博头条、搜索(用户和微博)
 * 删除粉丝、屏蔽微博、分享图片链接等高级权限功能
 * 多样化主题(可以色盘自主选择主题颜色)，炫酷程序壁纸设置
 * 所有列表界面没有刷新按钮，请双击顶部操作栏空白处置顶（可设置同时刷新）
 * 手势返回，4.4以上完美沉浸
 * 好友分组标签栏，可禁用，右侧抽屉分组列表
 * 可以对好友分组排序
 * 多账户支持、草稿箱、内置浏览器等基本功能
 * 侧边栏抽屉菜单
 * 聚合评论阅读
 * 发布内容可设置延迟
 * 草稿箱
  * 离线编辑
  * 定时发布
  * 发布失败的内容
 * 其他功能
  * 字体大小设置
  * 图片浏览设置，WIFI展示高清，移动网络展示小图，或者自行设置
  * 支持GIF浏览，上传
  * 分组编辑功能，支持排序

## 应用截图

![](https://github.com/wangdan/AisenWeibo/raw/master/resource/aisen_5.gif) 

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

