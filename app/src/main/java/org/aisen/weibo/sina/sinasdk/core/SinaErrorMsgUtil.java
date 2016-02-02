package org.aisen.weibo.sina.sinasdk.core;

import android.text.TextUtils;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.network.task.IExceptionDeclare;
import org.aisen.android.network.task.TaskException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SinaErrorMsgUtil implements IExceptionDeclare {

	private static final Map<String, String> errorMap = new HashMap<String, String>();

	/**
	 * 根据服务器返回的错误代码，返回错误信息
	 *
	 * @param errorMsg
	 * @return
	 */
	private static final String[][] errorMsgs = new String[][] { { "10002", "服务暂停" }, { "10009", "任务过多，系统繁忙" }, { "10010", "任务超时" }, { "10017", "分组名称长度超过限制" },
			{ "10022", "IP请求频次超过上限" }, { "10023", "用户请求频次超过上限,请重新授权" }, { "10024", "用户请求特殊接口频次超过上限" }, { "10025", "备注长度不正确，应为0～30个字符" },
			{ "20019", "不能发布相同内容" }, { "20008", "内容不能为空" }, { "20003", "用户不存在" }, { "20006", "图片太大" }, { "20012", "字数超过140限制" },
			{ "20015", "账号、APP或者IP异常，请稍后再试。(sina有毛病)" }, { "20016", "发布内容过于频繁" }, { "20017", "提交相似的信息" }, { "20018", "包含非法网址" },
			{ "20020", "包含广告信息" }, { "20021", "包含非法内容" }, { "20031", "需要验证码,操作太频繁" },
			{ "20104", "不合法的微博" }, { "20101", "该微博已经删除" },
			{ "20203", "该评论已被删除" }, { "20207", "Ta设置了不允许你评论他的微博" }, { "20202", "不合法的评论" }, { "20206", "仅Ta的好友能回复Ta" }, { "20508", "根据对方的设置，你不能进行此操作" },
			{ "20512", "你已经把此用户加入黑名单，加关注前请先解除" }, { "20513", "你的关注人数已达上限" }, { "20506", "已经关注了" }, { "20522", "还没有关注该用户" },
			{ "20521", "hi 超人，你今天已经取消关注很多喽，接下来的时间想想如何让大家都来关注你吧！如有问题，请联系新浪客服：400 690 0000" }, { "20603", "分组不存在" }, { "20608", "分组名不能重复" },
			{ "21602", "含有敏感词" }, { "20704", "该微博已经收藏过了" }, { "20705", "还没有收藏该微博" }, { "21301", "授权过期，请重新授权" }, { "21321", "应用请求超过API限制了" },
			{ "21317", "授权被取消，请重新授权" }, { "21324", "安全方面考虑，开发人员重置了应用秘钥，当前应用被视为盗版，请重新从应用市场下载!" },
			{ "21327", "授权过期，请重新授权" }, { "21332", "授权过期，请重新授权" }, {"21923", "没有找到相关位置信息"} };

	static {
		for (String[] errorArr : errorMsgs) {
			errorMap.put(errorArr[0], errorArr[1]);
		}
		errorMap.put("invalid_access_token", "无效授权，请稍后尝试重新授权");
	}

	@Override
	public void checkResponse(String response) throws TaskException {
		Logger.w(response);

		if (!TextUtils.isEmpty(response)) {
			if (response.indexOf("that page doesn’t exist!") != -1)
				throw new TaskException("", "渣浪TMD的神经病，请稍后重试...");
		}

		String code = null;
		String msg = null;

		if (!TextUtils.isEmpty(response)) {
			JSONObject jsonMsg;
			try {
				jsonMsg = new JSONObject(response);
				if (jsonMsg.has("error")) {
					if (jsonMsg.has("error_code")) {
						msg = jsonMsg.getString("error");
						if ("invalid_access_token".equals(msg)) {
							code = "21327";
						}
						else {
							code = jsonMsg.getString("error_code");
						}
						if (errorMap.containsKey(code))
							msg = errorMap.get(code);
					}
				}
			} catch (Exception e) {
			}
		}

		if (!TextUtils.isEmpty(msg))
			throw new TaskException(code, msg);

		throw new TaskException(code);
	}

	@Override
	public String checkCode(String code) {
		if (errorMap.containsKey(code))
			return errorMap.get(code);;

		return null;
	}

}
