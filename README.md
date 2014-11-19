# Aisen微博

**注意** 工程依赖[AisenForAndroid](https://github.com/wangdan/AisenForAndroid)

[![Get it on Google Play](http://www.android.com/images/brand/get_it_on_play_logo_small.png)](http://play.google.com/store/apps/details?id=org.aisen.weibo.sina)


## 说明
Aisen微博是新浪微博的第三方客户端，设计界面简约清爽，操作简单易用，多样化的颜色主题，炫酷的程序壁纸设置，4.4以上完美沉浸，极致流畅的列表动画，力争为Android用户提供更好以及特别的微博体验。Aisen微博基于[AisenForAndroid](https://github.com/wangdan/AisenForAndroid)(Aisen)框架开发，Aisen框架是一个android快速开发框架，包含ORM、IOC、BitmapLoader等开发组件，四层结构：UI层、业务接口层、持久层、数据通讯层。

## 基本功能
 
 * 多样化主题，炫酷壁纸
 * 手势返回，4.4以上完全沉浸
 * 好友分组标签栏，可设置随手势自动隐藏
 * 可以对好友分组排序
 * 热门话题，搜索用户，以及搜索话题，删除粉丝、屏蔽微博、分享图片链接等高级权限功能
 * 所有列表界面没有刷新按钮，请双击顶部操作栏空白处置顶（可设置同时刷新）
 * 多账户支持、草稿箱、内置浏览器等基本功能
 * 侧边栏抽屉菜单
 * 聚合评论阅读
 * 用户、微博搜索
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

![](https://github.com/wangdan/AisenForOSC/raw/master/screenshot/1_git.jpg) 
![](https://github.com/wangdan/AisenForOSC/raw/master/screenshot/2_git.jpg) 
![](https://github.com/wangdan/AisenForOSC/raw/master/screenshot/3_git.jpg) 
![](https://github.com/wangdan/AisenForOSC/raw/master/screenshot/4_git.jpg) 
![](https://github.com/wangdan/AisenForOSC/raw/master/screenshot/5_git.jpg) 

## 须知
 * 私信、多图上传、点赞功能没有权限
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


