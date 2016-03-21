package org.aisen.weibo.sina.sinasdk;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.Html;
import android.text.TextUtils;
import android.webkit.WebView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.setting.Setting;
import org.aisen.android.common.setting.SettingUtil;
import org.aisen.android.common.setting.SettingUtility;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.network.biz.ABizLogic;
import org.aisen.android.network.http.HttpConfig;
import org.aisen.android.network.http.IHttpUtility;
import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.weibo.sina.sinasdk.bean.AccessToken;
import org.aisen.weibo.sina.sinasdk.bean.DirectMessages;
import org.aisen.weibo.sina.sinasdk.bean.DmMessages;
import org.aisen.weibo.sina.sinasdk.bean.Favorities;
import org.aisen.weibo.sina.sinasdk.bean.Favority;
import org.aisen.weibo.sina.sinasdk.bean.Friendship;
import org.aisen.weibo.sina.sinasdk.bean.FriendshipShow;
import org.aisen.weibo.sina.sinasdk.bean.Group;
import org.aisen.weibo.sina.sinasdk.bean.GroupMemberListed;
import org.aisen.weibo.sina.sinasdk.bean.GroupSortResult;
import org.aisen.weibo.sina.sinasdk.bean.Groups;
import org.aisen.weibo.sina.sinasdk.bean.PicUrls;
import org.aisen.weibo.sina.sinasdk.bean.SearchsResultUser;
import org.aisen.weibo.sina.sinasdk.bean.SetCount;
import org.aisen.weibo.sina.sinasdk.bean.SinaLocationMap;
import org.aisen.weibo.sina.sinasdk.bean.StatusComment;
import org.aisen.weibo.sina.sinasdk.bean.StatusComments;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;
import org.aisen.weibo.sina.sinasdk.bean.StatusRepost;
import org.aisen.weibo.sina.sinasdk.bean.StatusesIds;
import org.aisen.weibo.sina.sinasdk.bean.SuggestionAtUser;
import org.aisen.weibo.sina.sinasdk.bean.SuggestionsUser;
import org.aisen.weibo.sina.sinasdk.bean.Token;
import org.aisen.weibo.sina.sinasdk.bean.TokenInfo;
import org.aisen.weibo.sina.sinasdk.bean.TrendsBean;
import org.aisen.weibo.sina.sinasdk.bean.UnreadCount;
import org.aisen.weibo.sina.sinasdk.bean.UploadPictureResultBean;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;
import org.aisen.weibo.sina.sinasdk.http.HttpsUtility;
import org.aisen.weibo.sina.support.utils.AisenUtils;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 新浪微博SDK
 * 
 * @author wangdan
 * 
 */
public class SinaSDK extends ABizLogic {

	private Token token;

	@Override
	protected IHttpUtility configHttpUtility() {
		return new HttpsUtility();
	}

	@Override
	protected HttpConfig configHttpConfig() {
		HttpConfig httpConfig = new HttpConfig();
		httpConfig.baseUrl = getSetting(BASE_URL).getValue();
		if (token != null) {
			httpConfig.addHeader("Authorization", "OAuth2 " + token.getToken());
		}
		return httpConfig;
	}

	private SinaSDK(Token token) {
		super();

		this.token = token;
	}

	private SinaSDK(Token token, CacheMode cacheMode) {
		super(cacheMode);
		this.token = token;
	}

	public static SinaSDK getInstance(Token token) {
		return new SinaSDK(token);
	}

	public static SinaSDK getInstance(Token token, CacheMode cacheMode) {
		return new SinaSDK(token, cacheMode);
	}
	
	public static String getAppKey() {
		return getSetting("app_key").getValue();
	}

	public String getAppSecret() {
		return getSetting("app_secret").getValue();
	}

	public String getAppCallback() {
		return getSetting("callback_url").getValue();
	}

	private Params configParams(Params params) {
		if (params == null) {
			params = new Params();
		}

		if (!params.containsKey("source"))
			params.addParameter("source", getAppKey());
		if (token != null)
			params.addParameter("access_token", token.getToken());

		return params;
	}

	/**
	 * 通过验证码获得access token
	 * 
	 * @param verifier
	 * @return
	 * @throws TaskException
	 */
	public AccessToken getAccessToken(String verifier) throws TaskException {
		Params params = new Params();
		params.addParameter("code", verifier);
		params.addParameter("client_id", getAppKey());
		params.addParameter("client_secret", getAppSecret());
		params.addParameter("grant_type", "authorization_code");
		params.addParameter("redirect_uri", getAppCallback());

		return doPost(getHttpConfig(), getSetting("access_token"), null, configParams(params), null, AccessToken.class);
	}

    public AccessToken getWeicoAccessToken(String verifier) throws TaskException {
        Params params = new Params();
        params.addParameter("code", verifier);
        params.addParameter("client_id", SettingUtility.getStringSetting("weico_key"));
        params.addParameter("client_secret", SettingUtility.getStringSetting("weico_screet"));
        params.addParameter("grant_type", "authorization_code");
        params.addParameter("redirect_uri", SettingUtility.getStringSetting("weico_callback"));

        return doPost(getHttpConfig(), getSetting("access_token"), null, configParams(params), null, AccessToken.class);
    }
	
	/**
	 * 查询用户access_token的授权相关信息，包括授权时间，过期时间和scope权限
	 * 
	 * @param token
	 * @return
	 * @throws TaskException
	 */
	public TokenInfo getTokenInfo(String token) throws TaskException {
		Params params = new Params();
		params.addParameter("access_token", token);
		
		return doPost(getHttpConfig(), getSetting("token_info"), null, configParams(params), null, TokenInfo.class);
	}

	// https://api.weibo.com/oauth2/default.html
	public void doWebRequest(WebView webView) {
		String url = String
				.format("https://api.weibo.com/oauth2/authorize?client_id=%s&scope=friendships_groups_read,friendships_groups_write,statuses_to_me_read,follow_app_official_microblog&redirect_uri=%s&display=mobile&forcelogin=true",
						getAppKey(), getAppCallback());
		webView.loadUrl(url);
	}
	
	/**
	 * 用户名账号密码登录
	 * 
	 * @param username
	 * @param pwd
	 * @param appId
	 * @param appSecret
	 * @return
	 * @throws Exception
	 */
	public AccessToken accountLogin(String username, String pwd, String appId, String appSecret) throws TaskException {
		Params params = new Params();
		params.addParameter("username", username);
		params.addParameter("password", pwd);
		params.addParameter("client_id", appId);
		params.addParameter("client_secret", appSecret);
		params.addParameter("grant_type", "password");
		
		return doPost(getHttpConfig(), getSetting("accountLogin"), null, params, null, AccessToken.class);
	}
	
	/**
	 * 按用户ID或昵称返回用户资料以及用户的最新发布的一条微博消息。<br>
	 * <br>
	 * 
	 * 
	 * @param uid
	 *            (true):必须设置
	 * @param screenName
	 *            (false):微博昵称，主要是用来区分用户UID跟微博昵称，当二者一样而产生歧义的时候，建议使用该参数
	 * @return
	 */
	public WeiBoUser userShow(String uid, String screenName) throws TaskException {
		Params params = new Params();
		if (!TextUtils.isEmpty(uid))
			params.addParameter("uid", uid);
		if (!TextUtils.isEmpty(screenName))
			params.addParameter("screen_name", screenName);
		params.setEncodeAble(false);
		
		WeiBoUser user = doGet(getSetting("usershow"), configParams(params), WeiBoUser.class);
		if (user != null) {
			// Sina真的是有毛病啊
			if (!TextUtils.isEmpty(user.getDescription()) && 
					user.getDescription().toLowerCase().endsWith("-weibo")) {
				user.setDescription(user.getDescription().substring(0, user.getDescription().length() - 6));
			}
			if (!TextUtils.isEmpty(user.getVerified_reason()) && 
					user.getVerified_reason().toLowerCase().endsWith("-weibo")) {
				user.setVerified_reason(user.getVerified_reason().substring(0, user.getVerified_reason().length() - 6));
			}
		}
		return user;
	}

	/**
	 * 
	 * @param list_id
	 *            需要查询的好友分组ID，建议使用返回值里的idstr，当查询的为私有分组时，则当前登录用户必须为其所有者
	 * @param since_id
	 *            若指定此参数，则返回ID比since_id大的微博（即比since_id时间晚的微博），默认为0
	 * @param max_id
	 *            若指定此参数，则返回ID小于或等于max_id的微博，默认为0
	 * @param count
	 *            单页返回的记录条数，最大不超过200，默认为50
	 * @param page
	 *            返回结果的页码，默认为1
	 * @param token
	 * @return
	 */
	public StatusContents friendshipGroupsTimeline(Params params) throws TaskException {
        if (!params.containsKey("count"))
            params.addParameter("count", getPageCount(getSetting("friendshipGroupsTimeline")));

        return doGet(getSetting("friendshipGroupsTimeline"), configParams(params), StatusContents.class);
	}

    public StatusContents offlineFriendshipGroupsTimeline(Params params) throws TaskException {
        if (!params.containsKey("count"))
            params.addParameter("count", getPageCount(getSetting("offlineFriendshipGroupsTimeline")));

        return doGet(getSetting("offlineFriendshipGroupsTimeline"), configParams(params), StatusContents.class);
    }

	/**
	 * 返回最新的20条公共微博。<br>
	 * 返回结果非完全实时，最长会缓存60秒<br>
	 * <br>
	 * 
	 * @param count
	 *            (false):单页返回的记录条数。(默认值20，最大200)
	 * @param base_app
	 *            (false):是否基于当前应用来获取数据。1为限制本应用微博，0为不做限制
	 * @return
	 */
	public StatusContents statusesPublicTimeLine(Params params) throws TaskException {
		if (!params.containsKey("count"))
			params.addParameter("count", getPageCount(getSetting("statusesPublicTimeLine")));

		return doGet(getSetting("statusesPublicTimeLine"), configParams(params), StatusContents.class);
	}

	/**
	 * 获取当前登录用户及其所关注用户的最新微博消息。<br>
	 * 和用户登录 http://t.sina.com.cn 后在“我的首页”中看到的内容相同。<br>
	 * 别名statuses/home_timeline<br>
	 * <br>
	 * 如果:id、user_id、screen_name三个参数均未指定，则返回当前登录用户最近发表的微博消息列表。<br>
	 * <br>
	 * 
	 * @param since_id
	 *            (false):若指定此参数，则只返回ID比since_id大的评论（比since_id发表时间晚）
	 * @param max_id
	 *            (false):若指定此参数，则返回ID小于或等于max_id的评论
	 * @param count
	 *            (false):单页返回的记录条数。(默认值20，最大200)
	 * @param page
	 *            (false):返回结果的页码。注意：有分页限制。
	 * @param base_app
	 *            (false):是否基于当前应用来获取数据。1为限制本应用微博，0为不做限制
	 * @param feature
	 *            (false):微博类型，0全部，1原创，2图片，3视频，4音乐. 返回指定类型的微博信息内容
	 * @return
	 */
	public StatusContents statusesFriendsTimeLine(Params params) throws TaskException {
		if (!params.containsKey("count"))
			params.addParameter("count", getPageCount(getSetting("statusesFriendsTimeLine")));

		return doGet(getSetting("statusesFriendsTimeLine"), configParams(params), StatusContents.class);
	}

	public StatusContents statusesHomeTimeLine(Params params) throws TaskException {
		if (!params.containsKey("count"))
			params.addParameter("count", getPageCount(getSetting("statusesHomeTimeLine")));

		return doGet(getSetting("statusesHomeTimeLine"), configParams(params), StatusContents.class);
	}

	/**
	 * 返回用户最新发表的微博消息列表<br>
	 * 建议使用该参数<br>
	 * <br>
	 * 如果:id、user_id、screen_name三个参数均未指定，则返回当前登录用户最近发表的微博消息列表。<br>
	 * <br>
	 * 
	 * @param user_id
	 *            (false):用户ID，主要是用来区分用户ID跟微博昵称。当微博昵称为数字导致和用户ID产生歧义，
	 *            特别是当微博昵称和用户ID一样的时候
	 * @param since_id
	 *            (false):若指定此参数，则只返回ID比since_id大的评论（比since_id发表时间晚）
	 * @param max_id
	 *            (false):若指定此参数，则返回ID小于或等于max_id的评论
	 * @param count
	 *            (false):单页返回的记录条数。(默认值20，最大200)
	 * @param page
	 *            (false):返回结果的页码。注意：有分页限制。
	 * @param base_app
	 *            (false):是否基于当前应用来获取数据。1为限制本应用微博，0为不做限制
	 * @param feature
	 *            (false):微博类型，0全部，1原创，2图片，3视频，4音乐. 返回指定类型的微博信息内容
	 * @return
	 */
	public StatusContents statusesUserTimeLine(Params params) throws TaskException {
		if (!params.containsKey("count"))
			params.addParameter("count", getPageCount(getSetting("statusesUserTimeLine")));
		params.setEncodeAble(false);

		return doGet(getSetting("statusesUserTimeLine"), configParams(params), StatusContents.class);
	}

	/**
	 * 获取双向关注用户的最新微博
	 * 
	 * @param since_id
	 *            (false):若指定此参数，则只返回ID比since_id大的评论（比since_id发表时间晚）
	 * @param max_id
	 *            (false):若指定此参数，则返回ID小于或等于max_id的评论
	 * @param count
	 *            (false):单页返回的记录条数。(默认值20，最大200)
	 * @param page
	 *            (false):返回结果的页码。注意：有分页限制。
	 * @param base_app
	 *            (false):是否基于当前应用来获取数据。1为限制本应用微博，0为不做限制
	 * @param feature
	 *            (false):微博类型，0全部，1原创，2图片，3视频，4音乐. 返回指定类型的微博信息内容
	 * @param trim_user
	 *            (false):返回值中user字段开关，0：返回完整user字段、1：user字段仅返回user_id，默认为0
	 * @return
	 */
	public StatusContents statusesBilateralTimeLine(Params params) throws TaskException {
		if (!params.containsKey("count"))
			params.addParameter("count", getPageCount(getSetting("statusesBilateralTimeLine")));

		return doGet(getSetting("statusesBilateralTimeLine"), configParams(params), StatusContents.class);
	}

	/**
	 * 获取某条原创微博的最新转发微博
	 * 
	 * @param params
	 * @param token
	 * @return
	 * @throws WeiboException
	 */
	public StatusRepost statusRepostTimeline(Params params) throws TaskException {
		if (!params.containsKey("count"))
			params.addParameter("count", getPageCount(getSetting("statusRepostTimeline")));

		return doGet(getSetting("statusRepostTimeline"), configParams(params), StatusRepost.class);
	}

	/**
	 * 转发一条微博消息。请求必须用POST方式提交。
	 * 
	 * @param id
	 *            (true)要转发的微博ID
	 * @param status
	 *            (false)添加的转发文本。必须做URLEncode,信息内容不超过140个汉字。如不填则默认为“转发微博”
	 * @param is_comment
	 *            (false)是否在转发的同时发表评论。0表示不发表评论，1表示发表评论给当前微博，2表示发表评论给原微博，3是1、2都发表
	 *            。默认为0
	 * @return
	 */
	public StatusContent statusesReport(Params params) throws TaskException {
		return doPost(getHttpConfig(), getSetting("statusesReport"), null, params, null, StatusContent.class);
	}

	/**
	 * 获取最新的提到登录用户的微博列表，即@我的微博
	 * 
	 * @param since_id
	 *            若指定此参数，则返回ID比since_id大的微博（即比since_id时间晚的微博），默认为0
	 * @param max_id
	 *            若指定此参数，则返回ID小于或等于max_id的微博，默认为0
	 * @param count
	 *            单页返回的记录条数，默认为50
	 * @param page
	 *            返回结果的页码，默认为1
	 * @param filter_by_author
	 *            作者筛选类型，0：全部、1：我关注的人、2：陌生人，默认为0
	 * @param filter_by_source
	 *            来源筛选类型，0：全部、1：来自微博的评论、2：来自微群的评论，默认为0
	 * @param filter_by_type
	 *            原创筛选类型，0：全部微博、1：原创的微博，默认为0
	 * @return
	 */
	public StatusContents statusesMentions(Params params) throws TaskException {
		return doGet(getSetting("statusesMentions"), configParams(params), StatusContents.class);
	}

	/**
	 * 获取当前登录用户关注的人发给其的定向微博
	 * 
	 * @param params
	 * @param token
	 * @return
	 */
	public StatusContents statusesToMe(Params params) throws TaskException {
		if (!params.containsKey("count"))
			params.addParameter("count", getPageCount(getSetting("statusesToMe")));

		return doGet(getSetting("statusesToMe"), configParams(params), StatusContents.class);
	}
	
	/**
	 * 屏蔽某条微博
	 * 
	 * @param id
	 * @return
	 * @throws TaskException
	 */
	public GroupSortResult statusFilterCreate(String id) throws TaskException {
		Params params = new Params();
		params.addParameter("id", id);

		return doPost(configHttpConfig(), getSetting("statusFilterCreate"), null, configParams(params), null, GroupSortResult.class);
	}
	
	/**
	 * 屏蔽某个@到我的微博以及后续由对其转发引起的@提及
	 * 
	 * @param id 需要屏蔽的@提到我的微博ID。此ID必须在statuses/mentions列表中
	 * @return
	 * @throws TaskException
	 */
	public GroupSortResult statusMentionsShield(String id) throws TaskException {
		Params params = new Params();
		params.addParameter("id", id);
		
		return doPost(configHttpConfig(), getSetting("statusMentionsShield"), null, configParams(params), null, GroupSortResult.class);
	}

	/**
	 * 获取好友的分组信息
	 * 
	 * @return
	 */
	public Groups friendshipGroups() throws TaskException {
        Groups groups = doGet(getSetting("friendshipGroups"), configParams(null), Groups.class);

        Map<String, String> groupMap = new HashMap<String, String>();
        List<Group> groupList = new ArrayList<Group>();
        // fuck 有两个特别关注
        for (Group group : groups.getLists()) {
            if (!groupMap.containsKey(group.getIdstr())) {
                groupList.add(group);
                groupMap.put(group.getIdstr(), group.getIdstr());
            }
        }
        groups.setLists(groupList);

		return groups;
	}

	/**
	 * 
	 * @param id
	 *            (true) 需要查询的微博ID
	 * @param since_id
	 *            (false) 若指定此参数，则返回ID比since_id大的评论（即比since_id时间晚的评论），默认为0
	 * @param max_id
	 *            (false) 若指定此参数，则返回ID小于或等于max_id的评论，默认为0
	 * @param count
	 *            (false) 单页返回的记录条数，默认为50
	 * @param page
	 *            (false) 返回结果的页码，默认为1
	 * @param filter_by_author
	 *            (false) 作者筛选类型，0：全部、1：我关注的人、2：陌生人，默认为0
	 * @return
	 */
	public StatusComments commentsShow(Params params) throws TaskException {
		if (!params.containsKey("count"))
			params.addParameter("count", getPageCount(getSetting("commentsShow")));

		return doGet(getSetting("commentsShow"), configParams(params), StatusComments.class);
	}

	/**
	 * 对一条微博信息进行评论。请求必须用POST方式提交。
	 * 
	 * @param id
	 *            (true) 要评论的微博消息ID
	 * @param comment
	 *            (true) 评论内容。必须做URLEncode,信息内容不超过140个汉字。
	 * @param comment_ori
	 *            (false) 当评论一条转发微博时，是否评论给原微博。0:不评论给原微博。1：评论给原微博。默认0
	 * @return
	 */
	public StatusComment commentCreate(Params params) throws TaskException {
		return doPost(getHttpConfig(), getSetting("commentCreate"), null, params, null, StatusComment.class);
	}

	/**
	 * 获取两个用户之间的详细关注关系情况
	 * 
	 * @param params
	 *            source_id 源用户的UID following true 代表source关注了target
	 * @param params
	 *            target_id 目标用户的UID following true 代表target关注了source
	 * @return
	 */
	public FriendshipShow friendshipsShow(String source_id, String target_id) throws TaskException {
		Params params = new Params();
		params.addParameter("source_id", source_id);
		params.addParameter("target_id", target_id);

		return doGet(getSetting("friendshipsShow"), configParams(params), FriendshipShow.class);
	}

	/**
	 * 取消关注一个用户
	 * 
	 * @param uid
	 *            (true) 需要取消关注的用户ID
	 * @return
	 */
	public WeiBoUser friendshipsDestroy(String uid) throws TaskException {
		Params params = new Params("uid", uid);

		return doPost(getHttpConfig(), getSetting("friendshipsDestroy"), null, configParams(params), null, WeiBoUser.class);
	}

	/**
	 * 关注一个用户
	 * 
	 * @param uid
	 *            (true) 需要关注的用户ID
	 * @return
	 */
	public WeiBoUser friendshipsCreate(String uid) throws TaskException {
		Params params = new Params("uid", uid);

		return doPost(getHttpConfig(), getSetting("friendshipsCreate"), null, configParams(params), null, WeiBoUser.class);
	}

	/**
	 * 更新当前登录用户所关注的某个好友的备注信息
	 * 
	 * @param uid
	 * @param remark
	 * @param token
	 * @return
	 */
	public WeiBoUser friendshipsRemarkUpdate(String uid, String remark) throws TaskException {
		Params params = new Params("uid", uid);
		params.addParameter("remark", remark);

		return doPost(getHttpConfig(), getSetting("friendshipsRemarkUpdate"), null, configParams(params), null, WeiBoUser.class);
	}

	/**
	 * 获取用户的关注列表
	 * 
	 * @param uid
	 *            (true) 需要查询的用户UID
	 * @param count
	 *            (false) 单页返回的记录条数，默认为50，最大不超过200
	 * @param cursor
	 *            (false) 返回结果的游标，下一页用返回值里的next_cursor，上一页用previous_cursor，默认为0
	 * @return
	 */
	public Friendship friendshipsFriends(String uid, String screenName, String cursor, int count) throws TaskException {
		Params params = new Params();
		if (!TextUtils.isEmpty(uid))
			params.addParameter("uid", uid);
		if (!TextUtils.isEmpty(screenName))
			params.addParameter("screen_name", screenName);
		params.addParameter("cursor", cursor);
        if (count == 0)
		    params.addParameter("count", getPageCount(getSetting("friendshipsFriends")));
        else
            params.addParameter("count", String.valueOf(count));
		params.setEncodeAble(false);

		return doGet(getSetting("friendshipsFriends"), configParams(params), Friendship.class);
	}

	/**
	 * 获取用户的粉丝列表
	 * 
	 * @param uid
	 *            (true) 需要查询的用户UID
	 * @param count
	 *            (false) 单页返回的记录条数，默认为50，最大不超过200
	 * @param cursor
	 *            (false) 返回结果的游标，下一页用返回值里的next_cursor，上一页用previous_cursor，默认为0
	 * @return
	 */
	public Friendship friendshipsFollowers(String uid, String screenName, String cursor) throws TaskException {
		Params params = new Params();
		if (!TextUtils.isEmpty(uid))
			params.addParameter("uid", uid);
		if (!TextUtils.isEmpty(screenName))
			params.addParameter("screen_name", screenName);
		params.addParameter("cursor", cursor);
		params.addParameter("count", getPageCount(getSetting("friendshipsFollowers")));
		params.setEncodeAble(false);

		return doGet(getSetting("friendshipsFollowers"), configParams(params), Friendship.class);
	}

	/**
	 * 获取用户的互粉列表
	 * 
	 * @param params
	 *            (true) 需要查询的用户UID
	 * @param params
	 *            (false) 单页返回的记录条数，默认为50，最大不超过200
	 * @param params
	 *            (false) 返回结果的游标，下一页用返回值里的next_cursor，上一页用previous_cursor，默认为0
	 * @return
	 */
	public Friendship friendshipsIncommon(String uid, String cursor) throws TaskException {
		Params params = new Params();
		params.addParameter("uid", uid);
		params.addParameter("cursor", cursor);
		params.addParameter("count", getPageCount(getSetting("friendshipsIncommon")));

		return doGet(getSetting("friendshipsIncommon"), configParams(params), Friendship.class);
	}

	/**
	 * 获取用户的彼此关注列表
	 * 
	 * @param params
	 *            (true) 需要查询的用户UID
	 * @param params
	 *            (false) 单页返回的记录条数，默认为50，最大不超过200
	 * @param params
	 *            (false) 返回结果的游标，下一页用返回值里的next_cursor，上一页用previous_cursor，默认为0
	 * @return
	 */
	public Friendship friendshipsBilateral(String uid, String cursor) throws TaskException {
		Params params = new Params();
		params.addParameter("uid", uid);
		params.addParameter("cursor", cursor);
		params.addParameter("count", getPageCount(getSetting("friendshipsBilateral")));

		return doGet(getSetting("friendshipsBilateral"), configParams(params), Friendship.class);
	}

	/**
	 * 移除当前登录用户的粉丝
	 * 
	 * @param uid
	 * @param token
	 * @return
	 */
	public WeiBoUser friendshipsFollowersDestory(String uid) throws TaskException {
		Params params = new Params("uid", uid);

		return doPost(getHttpConfig(), getSetting("friendshipsFollowersDestory"), null, configParams(params), null, WeiBoUser.class);
	}

	/**
	 * 获取当前登录用户所发出的评论列表
	 * 
	 * @param since_id
	 *            若指定此参数，则返回ID比since_id大的微博（即比since_id时间晚的微博），默认为0
	 * @param max_id
	 *            若指定此参数，则返回ID小于或等于max_id的微博，默认为0
	 * @param count
	 *            单页返回的记录条数，默认为50
	 * @param page
	 *            返回结果的页码，默认为1
	 * @param filter_by_source
	 *            来源筛选类型，0：全部、1：来自微博的评论、2：来自微群的评论，默认为0
	 * @return
	 */
	public StatusComments commentsByMe(String sinceId, String maxId, String count) throws TaskException {
		Params params = new Params();
		if (!TextUtils.isEmpty(sinceId))
			params.addParameter("since_id", sinceId);
		if (!TextUtils.isEmpty(maxId))
			params.addParameter("max_id", maxId);
		if (!TextUtils.isEmpty(count))
			params.addParameter("count", count);
		else 
			params.addParameter("count", getPageCount(getSetting("commentsByMe")));

		return doGet(getSetting("commentsByMe"), configParams(params), StatusComments.class);
	}

	/**
	 * 获取当前登录用户所接收到的评论列表
	 * 
	 * @param since_id
	 *            若指定此参数，则返回ID比since_id大的微博（即比since_id时间晚的微博），默认为0
	 * @param max_id
	 *            若指定此参数，则返回ID小于或等于max_id的微博，默认为0
	 * @param count
	 *            单页返回的记录条数，默认为50
	 * @param page
	 *            返回结果的页码，默认为1
	 * @param filter_by_author
	 *            作者筛选类型，0：全部、1：我关注的人、2：陌生人，默认为0
	 * @param filter_by_source
	 *            来源筛选类型，0：全部、1：来自微博的评论、2：来自微群的评论，默认为0
	 * @return
	 */
	public StatusComments commentsToMe(String sinceId, String maxId, String filterByAuthor, String count) throws TaskException {
		Params params = new Params();
		if (!TextUtils.isEmpty(sinceId))
			params.addParameter("since_id", sinceId);
		if (!TextUtils.isEmpty(maxId))
			params.addParameter("max_id", maxId);
		if (!TextUtils.isEmpty(filterByAuthor))
			params.addParameter("filter_by_author", filterByAuthor);
		if (!TextUtils.isEmpty(count))
			params.addParameter("count", count);
		else 
			params.addParameter("count", getPageCount(getSetting("commentsToMe")));

		return doGet(getSetting("commentsToMe"), configParams(params), StatusComments.class);
	}

	/**
	 * 获取最新的提到当前登录用户的评论，即@我的评论
	 * 
	 * @param since_id
	 *            若指定此参数，则返回ID比since_id大的微博（即比since_id时间晚的微博），默认为0
	 * @param max_id
	 *            若指定此参数，则返回ID小于或等于max_id的微博，默认为0
	 * @param count
	 *            单页返回的记录条数，默认为50
	 * @param page
	 *            返回结果的页码，默认为1
	 * @param filter_by_author
	 *            作者筛选类型，0：全部、1：我关注的人、2：陌生人，默认为0
	 * @param filter_by_source
	 *            来源筛选类型，0：全部、1：来自微博的评论、2：来自微群的评论，默认为0
	 * @return
	 */
	public StatusComments commentsMentions(String sinceId, String maxId, String filterByAuthor, String count) throws TaskException {
		Params params = new Params();
		if (!TextUtils.isEmpty(sinceId))
			params.addParameter("since_id", sinceId);
		if (!TextUtils.isEmpty(maxId))
			params.addParameter("max_id", maxId);
		if (!TextUtils.isEmpty(filterByAuthor))
			params.addParameter("filter_by_author", filterByAuthor);
		if (!TextUtils.isEmpty(count))
			params.addParameter("count", count);
		else
			params.addParameter("count", getPageCount(getSetting("commentsMentions")));

		return doGet(getSetting("commentsMentions"), configParams(params), StatusComments.class);
	}

	/**
	 * 获取当前登录用户的收藏列表
	 * 
	 * @param count
	 *            单页返回的记录条数，默认为50
	 * @param page
	 *            返回结果的页码，默认为1
	 * @param token
	 * @return
	 */
	public Favorities favorites(String page, String count) throws TaskException {
		Params params = new Params();
		params.addParameter("page", page);
		if (!TextUtils.isEmpty(count)) 
			params.addParameter("count", count);
		else
			params.addParameter("count", getPageCount(getSetting("favorites")));

		return doGet(getSetting("favorites"), configParams(params), Favorities.class);
	}

	/**
	 * 添加一条微博到收藏里
	 * 
	 * @param id
	 *            (true) 要收藏的微博ID
	 * @return
	 */
	public Favority favoritesCreate(String id) throws TaskException {
		Params params = new Params("id", id);
		return doPost(getHttpConfig(), getSetting("favoritesCreate"), null, params, null, Favority.class);
	}

	/**
	 * 取消收藏一条微博
	 * 
	 * @param id
	 * @param token
	 * @return
	 */
	public Favority favoritesDestory(String id) throws TaskException {
		Params params = new Params("id", id);
		return doPost(getHttpConfig(), getSetting("favoritesDestory"), null, params, null, Favority.class);
	}

	/**
	 * 删除一条微博
	 * 
	 * @param id
	 * @param token
	 * @return
	 */
	public StatusContent statusDestroy(String id) throws TaskException {
		Params params = new Params("id", id);
		return doPost(getHttpConfig(), getSetting("statusDestroy"), null, params, null, StatusContent.class);
	}

	/**
	 * 搜索某一话题下的微博
	 * 
	 * @param count
	 *            单页返回的记录条数，默认为50
	 * @param page
	 *            返回结果的页码，默认为1
	 * @param token
	 * @return
	 */
	public StatusContents searchTopics(String page, String topics, String count) throws TaskException {
		Params params = new Params();
		params.addParameter("page", page);
		if (!TextUtils.isEmpty(count))
			params.addParameter("count", count);
		else 
			params.addParameter("count", getPageCount(getSetting("searchTopics")));
		params.addParameter("q", topics);
		params.setEncodeAble(false);

		return doGet(getSetting("searchTopics"), configParams(params), StatusContents.class);
	}

	/**
	 * 发表带图片的微博。必须用POST方式提交pic参数，且Content-Type必须设置为multipart/form-data。图片大小<5M。
	 * <p>
	 * 图片格式定义为png<br>
	 * 
	 * @param status
	 *            (true)要发布的微博文本内容。
	 * @param pic
	 *            (true)要上传的图片数据。仅支持JPEG、GIF、PNG格式，为空返回400错误。图片大小<5M。
	 * @param lat
	 *            (false)纬度。有效范围：-90.0到+90.0，+表示北纬。
	 * @param long (false)经度。有效范围：-180.0到+180.0，+表示东经。
	 * @return
	 */
	public StatusContent statusesUpload(Params params, File file) throws TaskException {
		MultipartFile[] files = file == null ? null : new MultipartFile[] { new MultipartFile("image/jpge", "pic", file) };

		StatusContent s = doPostFiles(getHttpConfig(), getSetting("statusesUpload"), null, params, files, StatusContent.class);
//		tempFile.delete();
		return s;
	}

	/**
	 * 指定一个图片URL地址抓取后上传并同时发布一条新微博
	 * 
	 * @return
	 */
	public StatusContent statusesUploadUrlText(Params params) throws TaskException {
		StatusContent s = doPost(getHttpConfig(), getSetting("statusesUploadUrlText"), null, params, null, StatusContent.class);
		return s;
	}
	
	/**
	 * 发布一条微博信息。也可以同时转发某条微博。请求必须用POST方式提交。 <br>
	 * <br>
	 * 注意：lat和long参数需配合使用，用于标记发表微博消息时所在的地理位置，只有用户设置中geo_enabled=true时候地理位置信息才有效。<br>
	 * 
	 * @param status
	 *            (true) 要发布的微博消息文本内容
	 * @param in_reply_to_status_id
	 *            (false) 要转发的微博消息ID
	 * @param lat
	 *            (false) 纬度。有效范围：-90.0到+90.0，+表示北纬。
	 * @param long (false) 经度。有效范围：-180.0到+180.0，+表示东经。
	 * @param annotations
	 *            (false)
	 * @return
	 */
	public StatusContent statusesUpdate(Params params) throws TaskException {
		return doPost(getHttpConfig(), getSetting("statusesUpdate"), null, configParams(params), null, StatusContent.class);
	}

	/**
	 * 回复一条评论
	 * 
	 * @param cid
	 *            需要回复的评论ID
	 * @param id
	 *            需要评论的微博ID
	 * @param comment
	 *            回复评论内容，必须做URLencode，内容不超过140个汉字
	 * @param without_mention
	 *            回复中是否自动加入“回复@用户名”，0：是、1：否，默认为0
	 * @param comment_ori
	 *            当评论转发微博时，是否评论给原微博，0：否、1：是，默认为0
	 * @return
	 */
	public StatusComment commentsReply(Params params) throws TaskException {

		return doPost(getHttpConfig(), getSetting("commentsReply"), null, params, null, StatusComment.class);
	}

	/**
	 * 删除一条评论
	 * 
	 * @param commentId
	 *            要删除的评论ID，只能删除登录用户自己发布的评论
	 * @param token
	 * @return
	 */
	public StatusComment commentsDestory(String commentId) throws TaskException {
		Params params = new Params();
		params.addParameter("cid", commentId);

		return doPost(getHttpConfig(), getSetting("commentsDestory"), null, params, null, StatusComment.class);
	}

	/**
	 * at用户时的联想建议
	 * 
	 * @param q
	 *            搜索的关键字，必须做URLencoding
	 * @param count
	 *            返回的记录条数，默认为10，粉丝最多1000，关注最多2000
	 * @param type
	 *            联想类型，0：关注、1：粉丝
	 * @param range
	 *            联想范围，0：只联想关注人、1：只联想关注人的备注、2：全部，默认为2
	 * @param token
	 * @return
	 */
	public SuggestionAtUser[] searchSuggestionsAtUsers(String q) throws TaskException {
		Params params = new Params();
		params.addParameter("q", q);
		params.addParameter("count", "25");
		params.addParameter("type", "0");
		params.addParameter("range", "2");
		params.setEncodeAble(false);

		return doGet(getSetting("searchSuggestionsAtUsers"), params, SuggestionAtUser[].class);
	}

	/**
	 * 获取当前用户Web主站未读消息数，包括：<br>
	 * 新提及我的评论数<br>
	 * 最新提到我的微博数<br>
	 * 新评论数<br>
	 * 新粉丝数。<br>
	 * 
	 * @param uid
	 *            当前登录用户
	 * @return
	 */
	public UnreadCount remindUnread(String uid) throws TaskException {
		Params params = new Params("uid", uid);

		return doGet(getSetting("remindUnread"), params, UnreadCount.class);
	}

	/**
	 * 对当前登录用户某一种消息未读数清零<br/>
	 * 
	 * @param type
	 *            follower:新粉丝数、cmt:新评论数、mention_status:新提及微博、mention_cmt:新提及评论
	 * 
	 * @return
	 */
	public SetCount remindSetCount(String type) throws TaskException {
		return doGet(getSetting("remindSetCount"), new Params("type", type), SetCount.class);
	}

	/**
	 * 批量获取某些用户在当前登录用户指定好友分组中的收录信息
	 * 
	 * @param uids
	 *            需要获取好友分组信息的用户UID列表，多个之间用逗号分隔，每次不超过50个
	 * @param token
	 * @return
	 */
	public GroupMemberListed[] friendshipGroupsListed(String uids) throws TaskException {
		Params params = new Params("uids", uids);
		params.setEncodeAble(false);

		return doGet(getSetting("friendshipGroupsListed"), params, GroupMemberListed[].class);
	}

	/**
	 * 创建好友分组
	 * 
	 * @param name
	 *            要创建的好友分组的名称，不超过10个汉字，20个半角字符
	 * @param description
	 *            要创建的好友分组的描述，不超过70个汉字，140个半角字符
	 * @param tags
	 *            要创建的好友分组的标签，多个之间用逗号分隔，最多不超过10个，每个不超过7个汉字，14个半角字符
	 * @param token
	 * @return
	 */
	public Group friendshipsGroupsCreate(String name, String description, String tags) throws TaskException {
		Params params = new Params();
		params.addParameter("name", name);
		if (!TextUtils.isEmpty(description))
			params.addParameter("description", description);
		if (!TextUtils.isEmpty(tags))
			params.addParameter("tags", tags);

		return doPost(getHttpConfig(), getSetting("friendshipsGroupsCreate"), null, params, null, Group.class);
	}

	/**
	 * 删除好友分组
	 * 
	 * @param list_id
	 *            要删除的好友分组ID，建议使用返回值里的idstr
	 * @return
	 */
	public Group friendshipGroupsDestory(String list_id) throws TaskException {
		Params params = new Params();
		params.addParameter("list_id", list_id);

		return doPost(getHttpConfig(), getSetting("friendshipGroupsDestory"), null, params, null, Group.class);
	}

	/**
	 * 添加关注用户到好友分组
	 * 
	 * @param uid
	 * @param list_id
	 * @param token
	 * @return
	 */
	public Group friendshipsGroupdMembersAdd(String uid, String list_id) throws TaskException {
		Params params = new Params();
		params.addParameter("uid", uid);
		params.addParameter("list_id", list_id);

		return doPost(getHttpConfig(), getSetting("friendshipsGroupdMembersAdd"), null, params, null, Group.class);
	}

	/**
	 * 删除好友分组内的关注用户
	 * 
	 * @param uid
	 * @param list_id
	 * @param token
	 * @return
	 */
	public Group friendshipsGroupdMembersDestory(String uid, String list_id) throws TaskException {
		Params params = new Params();
		params.addParameter("uid", uid);
		params.addParameter("list_id", list_id);

		return doPost(getHttpConfig(), getSetting("friendshipsGroupdMembersDestory"), null, params, null, Group.class);
	}

	/**
	 * 更新好友分组
	 * 
	 * @param list_id
	 *            需要更新的好友分组ID，建议使用返回值里的idstr，只能更新当前登录用户自己创建的分组
	 * @param name
	 *            好友分组更新后的名称，不超过8个汉字，16个半角字符
	 * @param description
	 *            好友分组更新后的描述，不超过70个汉字，140个半角字符
	 * @param tags
	 *            好友分组更新后的标签，多个之间用逗号分隔，最多不超过10个，每个不超过7个汉字，14个半角字符
	 * @param token
	 * @return
	 */
	public Group friendshipGroupsUpdate(String list_id, String name, String description, String tags) throws TaskException {
		Params params = new Params("list_id", list_id);
		params.addParameter("name", name);

		if (description != null)
			params.addParameter("description", description);
		if (tags != null)
			params.addParameter("tags", tags);

		return doPost(getHttpConfig(), getSetting("friendshipGroupsUpdate"), null, params, null, Group.class);
	}

	/**
	 * 获取某一好友分组下的成员列表
	 * 
	 * @param list_id
	 *            好友分组ID，建议使用返回值里的idstr
	 * @param count
	 *            单页返回的记录条数，默认为50，最大不超过200
	 * @param cursor
	 *            分页返回结果的游标，下一页用返回值里的next_cursor，上一页用previous_cursor，默认为0
	 * @param token
	 * @return
	 */
	public Friendship friendshipGroupsMembers(String list_id, String cursor) throws TaskException {
		Params params = new Params("list_id", list_id);
		params.addParameter("count", getPageCount(getSetting("friendshipGroupsMembers")));
		params.addParameter("cursor", cursor);

		return doGet(getSetting("friendshipGroupsMembers"), params, Friendship.class);
	}
	
	/**
	 * 调整当前登录用户的好友分组顺序
	 * 
	 * @param groupList
	 * @return
	 * @throws TaskException
	 */
	public GroupSortResult friendshipGroupsOrder(List<Group> groupList) throws TaskException {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < groupList.size(); i++) {
			sb.append(groupList.get(i).getIdstr());
			if (i != groupList.size() - 1)
				sb.append(",");
		}
		
		Params params = new Params();
		params.addParameter("count", String.valueOf(groupList.size()));
		params.addParameter("list_ids", sb.toString());
		
		HttpConfig config = getHttpConfig();
		return doPost(config, getSetting("friendshipGroupsOrder"), null, params, null, GroupSortResult.class);
	}
	
	/**
	 * 根据移动基站WIFI等数据获取当前位置信息
	 * 
	 * @return
	 * @throws TaskException
	 */
	public SinaLocationMap locationMobileGetLocation() throws TaskException {
		Map<String, Object> requestMap = new HashMap<String, Object>();
		
		// 请求版本信息
		requestMap.put("version", "2.0");
		// 请求地址信息
		requestMap.put("host", "api.weibo.com");
		// 请求类型
		requestMap.put("radio_type", "gsm");
		// 是否需要返回详细地址，可选，默认：false
		requestMap.put("request_address", "true");
		// 返回坐标是否偏移处理，偏移后坐标适合在新浪地图上使用（http://map.sina.com.cn），
		// 不适用于百度地图（各地图偏移量不同，请谨慎处理）；可选，默认：false
		requestMap.put("decode_pos", "true");
		
		TelephonyManager mTelNet = (TelephonyManager) GlobalContext.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
		GsmCellLocation location = null;
		if (mTelNet != null)
			location = (GsmCellLocation) mTelNet.getCellLocation();
		if (location != null) {
			String operator = mTelNet.getNetworkOperator();
			int mcc = Integer.parseInt(operator.substring(0, 3));
			int mnc = Integer.parseInt(operator.substring(3));
			
			List<NeighboringCellInfo> cellInfoList = mTelNet.getNeighboringCellInfo();
			
			ArrayList<Map<String, Object>> cellMapList = new ArrayList<Map<String,Object>>();
			for (NeighboringCellInfo cellInfo : cellInfoList) {
				// 基站信息
				Map<String, Object> cellMap = new HashMap<String, Object>();
				// 基站号
				cellMap.put("cell_id", location.getCid());
				// 小区号
				cellMap.put("location_area_code", cellInfo.getLac());
				// 地区代码
				cellMap.put("mobile_country_code", mcc);
				// 运营商号
				cellMap.put("mobile_network_code", mnc);
				// 信号强度
				cellMap.put("signal_strength", cellInfo.getRssi());
				
				cellMapList.add(cellMap);
			}
			if (cellMapList.size() > 0)
				requestMap.put("cell_towers", cellMapList);
		}
		
		WifiManager wm = (WifiManager) GlobalContext.getInstance().getSystemService(Context.WIFI_SERVICE);
		List<ScanResult> scanResultList = wm.getScanResults();
		if (scanResultList != null && scanResultList.size() > 0) {
			
			ArrayList<Map<String, Object>> wifiMapList = new ArrayList<Map<String,Object>>();
			for (ScanResult scanResult : scanResultList) {
				// WIFI信息
				Map<String, Object> wifiMap = new HashMap<String, Object>();
				wifiMapList.add(wifiMap);
				wifiMap.put("mac_address", scanResult.BSSID);
				wifiMap.put("mac_name", scanResult.SSID);
				// 信号强度
				wifiMap.put("signal_strength", scanResult.level);
			}
			if (wifiMapList.size() > 0)
				requestMap.put("wifi_towers", wifiMapList);
		}
		
		HttpConfig config = getHttpConfig();

		Params params = new Params();
		params.addParameter("json", JSON.toJSONString(requestMap));
		
		return doPost(config, getSetting("locationMobileGetLocation"), null, params, null, SinaLocationMap.class);
	}
	
	/**
	 * 搜索用户时的联想搜索建议
	 * 
	 * @param q
	 * @param count
	 * @return
	 * @throws TaskException
	 */
	public SuggestionsUser[] searchSuggestionsUsers(String q, int count) throws TaskException {
		Params params = new Params();
		params.addParameter("q", q);
		if (count > 0)
			params.addParameter("count", String.valueOf(count));
		params.setEncodeAble(false);
		
		return doGet(getSetting("searchSuggestionsUsers"), params, SuggestionsUser[].class);
	}
	
	/**
	 * 返回最近一小时内的热门话题
	 * 
	 * @return
	 * @throws TaskException
	 */
	public TrendsBean trendsHourly() throws TaskException {
		
		return doGet(getSetting("trendsHourly"), configParams(null), TrendsBean.class);
	}
	
	/**
	 * 返回最近一天内的热门话题
	 * 
	 * @return
	 * @throws TaskException
	 */
	public TrendsBean trendsDaily() throws TaskException {
		
		return doGet(getSetting("trendsDaily"), configParams(null), TrendsBean.class);
	}
	
	/**
	 * 返回最近一周内的热门话题
	 * 
	 * @return
	 * @throws TaskException
	 */
	public TrendsBean trendsWeekly() throws TaskException {
		
		return doGet(getSetting("trendsWeekly"), configParams(null), TrendsBean.class);
	}
	
	/**
	 * 获取私信列表
	 * 
	 * @return
	 * @throws TaskException
	 */
	public DmMessages getDmMessage(String cursor, String source) throws TaskException {
		Params params = new Params();
		params.addParameter("count", "20");
		params.addParameter("cursor", cursor);
		params.addParameter("source", source);
		
		DmMessages dms = doGet(getSetting("getDmMessage"), params, DmMessages.class);
		return dms;
	}
	
	/**
	 * 获取私信对话
	 * 
	 * @param cursor
	 * @param source
	 * @return
	 * @throws TaskException
	 */
	public DirectMessages getDmConversation(String uid, int page, String source) throws TaskException {
		Params params = new Params();
		params.addParameter("uid", uid);
		params.addParameter("count", "30");
		params.addParameter("source", source);
		params.addParameter("page", String.valueOf(page));
		
		DirectMessages dms = doGet(getSetting("getDmConversation"), params, DirectMessages.class);
		return dms;
	}
	
	/**
	 * 发送私信
	 *
	 * @param uid
	 * @param text
	 * @return
	 * @throws TaskException
	 */
	public Boolean sendDmMessage(String uid, String text) throws TaskException {
		Params params = new Params();
		params.addParameter("uid", uid);
		params.addParameter("text", text);

		return doPost(getHttpConfig(), getSetting("sendDmMessage"), null, params, null, Boolean.class);
	}

	/**
	 * 根据id获取某条微博
	 * 
	 * @param id
	 * @return
	 * @throws TaskException
	 */
	public StatusContent statusesShow(String id) throws TaskException {
		Params params = new Params();
		params.addParameter("id", id);
		
		return doGet(getSetting("statusesShow"), params, StatusContent.class);
	}
	
	/**
	 * 获取当前登录用户及其所关注用户的最新微博的ID
	 * 
	 * @return
	 * @throws TaskException
	 */
	public StatusesIds getFriendsTimelineIds(String sinceId) throws TaskException {
		Params params = new Params();
		if (!TextUtils.isEmpty(sinceId))
			params.addParameter("since_id", sinceId);
		params.addParameter("count", "100");
		
		return doGet(getSetting("getFriendsTimelineIds"), params, StatusesIds.class);
	}
	
	/**
	 * 获取当前登录用户及其所关注用户的最新微博的ID
	 * 
	 * @return
	 * @throws TaskException
	 */
	public StatusesIds getGroupsTimelineIds(String sinceId, String listId) throws TaskException {
		Params params = new Params();
		params.addParameter("since_id", sinceId);
		params.addParameter("list_id", listId);
		params.addParameter("count", "101");
		
		return doGet(getSetting("getGroupsTimelineIds"), params, StatusesIds.class);
	}

	// {"bmiddle_pic":"http://ww1.sinaimg.cn/bmiddle/94389574jw1etl94fy67qj21kw0w0qjd.jpg","original_pic":"http://ww1.sinaimg.cn/large/94389574jw1etl94fy67qj21kw0w0qjd.jpg","pic_id":"94389574jw1etl94fy67qj21kw0w0qjd","thumbnail_pic":"http://ww1.sinaimg.cn/thumbnail/94389574jw1etl94fy67qj21kw0w0qjd.jpg"}
	/**
	 * 上传一张图片<br/>
	 * 只支持高级权限
	 *
	 * @param file
	 * @return
	 * @throws TaskException
	 */
	public UploadPictureResultBean uploadPicture(File file) throws TaskException {
		Params params = new Params();

		MultipartFile[] files = file == null ? null : new MultipartFile[] { new MultipartFile("image/jpge", "pic", file) };

		return doPostFiles(configHttpConfig(), getSetting("publishUploadPicture"), null, configParams(params), files, UploadPictureResultBean.class);
	}

	/**
	 * 使用H5页面的接口拉取数据
	 *
	 * @param q
	 * @param cookies
	 * @return
	 * @throws TaskException
	 */
	public String[] searchsSuggest(String q, String cookies) throws TaskException {
		// http://m.weibo.cn/searchs/suggest?count=10&q=ann
		Setting action = newSetting("searchsSuggest", "searchs/suggest", "获取搜索建议");
		action.getExtras().put(BASE_URL, newSettingExtra(BASE_URL, "http://m.weibo.cn/", ""));

		Params params = new Params();
		params.addParameter("q", q);
		params.addParameter("count", "5");

		HttpConfig config = configHttpConfig();
		config.cookie = cookies;
		config.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		config.addHeader("Referer", "http://m.weibo.cn/searchs");
		try {

			// ["ana",["anastasia","T-ANA小芹","anastasia 修容","广安门医院官方微博","anastasia 高光"]]
			String response = doPost(config, action, null, params, null, String.class);
			response = AisenUtils.convertUnicode(response);

			Logger.d("SinaSDK", response);

			if (response.toLowerCase().indexOf("<html>") != -1) {
				throw new TaskException("cookieinvalid", "网页版登录失效了");
			}

			// {"request":"/search/suggestions/all.php","error_code":"21405","error":"Operation timed out after 300 milliseconds with 0 bytes received url:http://i.api.weibo.com/users/show_batch.json"}
			if (response.indexOf("error_code") != -1 && response.indexOf("error") != -1) {
				JSONObject jsonRespone = JSONObject.parseObject(response);
				throw new TaskException(jsonRespone.getString("error_code"), jsonRespone.getString("error"));
			}
			// {"ok":-100,"msg":"请先登录","url":"https://passport.weibo.cn/signin/welcome?entry=mweibo&r=http%3A%2F%2Fm.weibo.cn%2Fsearchs%2Fsuggest"}
			else if (response.indexOf("ok") != -1 && response.indexOf("msg") != -1) {
				JSONObject jsonRespone = JSONObject.parseObject(response);
				throw new TaskException(jsonRespone.getString("ok"), jsonRespone.getString("msg"));
			}

			JSONArray jsonArray = JSON.parseArray(response);
			JSONArray resultArray = jsonArray.getJSONArray(1);
			String[] result = new String[resultArray.size()];
			for (int i = 0; i < resultArray.size(); i++) {
				result[i] = resultArray.getString(i);
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();

			if (e instanceof TaskException) {
				throw e;
			}
		}

		return new String[0];
	}

	/**
	 * 使用H5页面查询用户
	 *
	 * @param q
	 * @param cookies
	 * @return
	 * @throws TaskException
	 */
	public ArrayList<SearchsResultUser> searchsResultUsers(String q, String cookies) throws TaskException {
		ArrayList<SearchsResultUser> resultUsers = new ArrayList<>();

		Setting action = newSetting("searchsResultUsers", "page/pageJson", "获取用户");
		action.getExtras().put(BASE_URL, newSettingExtra(BASE_URL, "http://m.weibo.cn/", ""));

		Params params = new Params();
//		params.addParameter("containerid", "100103");
//		params.addParameter("type", "3");
//		params.addParameter("page", "1");
//		params.addParameter("q", q);
		// 我也搞不懂这个脑残接口到底怎么玩的
		// http://m.weibo.cn/page/pageJson?containerid=100103type%3D3%26q%3Dwang&page=1
		params.addParameter("containerid", "100103type%3D3%26q%3D" + q + "&page=1");

		try {
			HttpConfig config = getHttpConfig();
//			config.cookie = cookies;
//			config.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
//			config.addHeader("Referer", "http://m.weibo.cn/searchs");
			String response = doGet(config, action, params, String.class);

			JSONObject responseJSON = JSONObject.parseObject(response);
			int ok = responseJSON.getInteger("ok");
			if (ok == 1) {
				JSONArray cardsArray = responseJSON.getJSONArray("cards");
				if (cardsArray.size() > 0) {
					JSONObject cardGroupsObject = cardsArray.getJSONObject(1);
					JSONArray cardGroupArray = cardGroupsObject.getJSONArray("card_group");
					for (int i = 0; i < cardGroupArray.size(); i++) {
						JSONObject cardGroup = cardGroupArray.getJSONObject(i);

						SearchsResultUser user = new SearchsResultUser();
						user.setDesc1(cardGroup.getString("desc1"));
						user.setDesc2(cardGroup.getString("desc2"));
						JSONObject userJSON = cardGroup.getJSONObject("user");
						user.setId(userJSON.getString("id"));
						user.setFollowing(userJSON.getBoolean("following"));
						user.setFollow_me(userJSON.getBoolean("follow_me"));
						user.setFansNum(userJSON.getString("fansNum"));
						user.setScreen_name(userJSON.getString("screen_name"));
						user.setDescription(userJSON.getString("description"));
						user.setProfile_image_url(userJSON.getString("profile_image_url"));
						user.setStatuses_count(userJSON.getInteger("statuses_count"));
						user.setGender(userJSON.getString("gender"));
						user.setRemark(userJSON.getString("remark"));

						resultUsers.add(user);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();

			if (e instanceof TaskException) {
				throw e;
			}
		}

		return resultUsers;
	}

	/**
	 * H5接口搜索微博
	 *
	 * @param q
	 * @param page
	 * @param cookies
	 * @return
	 * @throws TaskException
	 */
	public ArrayList<StatusContent> searchsResultStatuss(String q, int page, String cookies) throws TaskException {
		ArrayList<StatusContent> resultUsers = new ArrayList<>();

		Setting action = newSetting("searchsResultUsers", "page/pageJson", "获取用户");
		action.getExtras().put(BASE_URL, newSettingExtra(BASE_URL, "http://m.weibo.cn/", ""));

		Params params = new Params();
		params.addParameter("containerid", "100103type%3D2%26q%3D" + q + "&page=" + page);

		try {
			String response = doGet(getHttpConfig(), action, params, String.class);

			JSONObject responseJSON = JSONObject.parseObject(response);
			int ok = responseJSON.getInteger("ok");
			if (ok == 1) {
				JSONArray cardsArray = responseJSON.getJSONArray("cards");
				for (int i = 0; i < cardsArray.size(); i++) {
					JSONObject cardGroupsObject = cardsArray.getJSONObject(i);
					JSONArray cardGroupArray = cardGroupsObject.getJSONArray("card_group");
					for (int j = 0; j < cardGroupArray.size(); j++) {
						JSONObject cardGroup = cardGroupArray.getJSONObject(j);

						JSONObject mblogObject = cardGroup.getJSONObject("mblog");

						StatusContent content = JSON.parseObject(mblogObject.toJSONString(), StatusContent.class);
						// 图片
						if (mblogObject.containsKey("pics")) {
							JSONArray picsArray = mblogObject.getJSONArray("pics");
							if (picsArray != null && picsArray.size() > 0) {
								PicUrls picUrls = new PicUrls();
								picUrls.setThumbnail_pic(picsArray.getJSONObject(0).getString("url"));
								content.setPic_urls(new PicUrls[]{ picUrls });
							}
						}
						// 把Html5文本转换一下
						content.setText(Html.fromHtml(content.getText()).toString());
						if (content.getRetweeted_status() != null) {
							content.getRetweeted_status().setText(Html.fromHtml(content.getRetweeted_status().getText()).toString());
						}
						// 把时间转换一下
						try {
							Calendar calendar = Calendar.getInstance();
							int year = calendar.get(Calendar.YEAR);
							SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
							calendar.setTimeInMillis(format.parse(content.getCreated_at()).getTime());
							calendar.set(Calendar.YEAR, year);
							content.setCreated_at(calendar.getTimeInMillis() + "");
						} catch (ParseException e) {
							try {
								SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
								content.setCreated_at(format.parse(content.getCreated_at()).getTime() + "");
							} catch (ParseException ewe) {
							}
						}

						resultUsers.add(content);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();

			if (e instanceof TaskException) {
				throw (TaskException) e;
			}
		}

		return resultUsers;
	}

	protected String getPageCount(Setting setting) {
		return SettingUtil.getSettingValue(setting, "page_count");
	}
	
}
