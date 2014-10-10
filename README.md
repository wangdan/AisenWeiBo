# Aisen微博

**注意** 工程依赖[AisenBase](https://github.com/wangdan/com.m)

[![Get it on Google Play](http://www.android.com/images/brand/get_it_on_play_logo_small.png)](http://play.google.com/store/apps/details?id=org.aisen.weibo.sina)

## 说明
Aisen微博是新浪微博的第三方客户端，遵循Android Design，Holo主题风格，设计从界面简约清爽操作简单易用出发，力争为Android用户提供更好以及特别的微博体验。同时使用AisenBase框架开发，基于Aisen微博这个范例可以学习使用AisenBase快速搭建Android项目工程快速开发。

## 基本功能
 
 * 多账号授权管理
 * 侧边栏抽屉菜单
 * 图片高清阅读（9宫格、智能排版风格）
 * 分组左右划屏切换阅读，可自由排序
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

## 文档
请在actions.xml文件中替换成你的appKey

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

## 须知
 * 私信、多图上传、点赞功能没有权限
 * 用户粉丝、关注只能获取30%的数据
 * 用户搜索限制：v用户、粉丝500以上的达人、粉丝600以上的普通用户
 * 用户微博列表只能是当前授权登录用户

## License

Copyright (c) 2014 Jeff Wang

Licensed under the [Apache License, Version 3.0](http://opensource.org/licenses/GPL-3.0)


