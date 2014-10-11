# Aisen微博

**注意** 工程依赖[AisenForAndroid](https://github.com/wangdan/AisenForAndroid)

[![Get it on Google Play](http://www.android.com/images/brand/get_it_on_play_logo_small.png)](http://play.google.com/store/apps/details?id=org.aisen.weibo.sina) [![Get it on 应用宝](http://ww1.sinaimg.cn/large/94389574gw1el706ontuxj203u019q2r.jpg)](http://android.myapp.com/myapp/detail.htm?apkName=org.aisen.weibo.sina) [![Get it on 安卓市场](http://ww3.sinaimg.cn/mw690/94389574gw1el706s6zhjj202p019wea.jpg)](http://apk.hiapk.com/appinfo/org.aisen.weibo.sina)


## 说明
Aisen微博是新浪微博的第三方客户端，遵循Android Design，Holo主题风格，设计从界面简约清爽操作简单易用出发，力争为Android用户提供更好以及特别的微博体验。基于[AisenForAndroid](https://github.com/wangdan/AisenForAndroid)(Aisen)框架开发，Aisen框架是一个android快速开发框架，包含ORM、IOC、BitmapLoader等开发组件，四层结构：UI层、业务接口层、持久层、数据通讯层。

## 基本功能
 
 * 多账号授权管理
 * 侧边栏抽屉菜单
 * 图片高清阅读（9宫格、智能排版风格）
 * 分组左右划屏切换阅读，可自由排序
 * 聚合评论阅读
 * 用户、微博搜索
 * 发布内容可设置延迟
 * 多语言支持
  * 中文简体
  * 中文繁体
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

 * 账号管理
 
 ![github](http://ww4.sinaimg.cn/mw1024/94389574gw1el6hpgvc4vg20ae0gz7wi.gif)  

 * [Aisen](https://github.com/wangdan/AisenForAndroid)支持三种主流刷新控件
 
 ![github](http://ww4.sinaimg.cn/bmiddle/94389574gw1el668azuwag20b20ghqkh.gif)  

 * 分组左右切屏阅读，可以编辑、排序
 
 ![github](http://ww1.sinaimg.cn/bmiddle/94389574gw1el6f6mj8hgg20ae0ghqv7.gif)

 * 内容离线编辑，可以保存到草稿箱，可以设置定时发布
 
 ![github](http://ww4.sinaimg.cn/bmiddle/94389574gw1el6gpodcwwg20ae0gzqmj.gif)

 * 图片高清阅读，支持自动排版、9宫格模式
 
 ![github](http://ww1.sinaimg.cn/bmiddle/94389574gw1el6g2t8hepg20ae0ghqvc.gif)

## 须知
 * 私信、多图上传、点赞功能没有权限
 * 用户粉丝、关注只能获取30%的数据
 * 用户搜索限制：v用户、粉丝500以上的达人、粉丝600以上的普通用户
 * 用户微博列表只能是当前授权登录用户

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


