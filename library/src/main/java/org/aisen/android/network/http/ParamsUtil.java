package org.aisen.android.network.http;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ParamsUtil {
	public static char[] base64Encode(byte[] data) {
		final char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".toCharArray();
		char[] out = new char[((data.length + 2) / 3) * 4];
		for (int i = 0, index = 0; i < data.length; i += 3, index += 4) {
			boolean quad = false;
			boolean trip = false;
			int val = (0xFF & (int) data[i]);
			val <<= 8;
			if ((i + 1) < data.length) {
				val |= (0xFF & (int) data[i + 1]);
				trip = true;
			}
			val <<= 8;
			if ((i + 2) < data.length) {
				val |= (0xFF & (int) data[i + 2]);
				quad = true;
			}
			out[index + 3] = alphabet[(quad ? (val & 0x3F) : 64)];
			val >>= 6;
			out[index + 2] = alphabet[(trip ? (val & 0x3F) : 64)];
			val >>= 6;
			out[index + 1] = alphabet[val & 0x3F];
			val >>= 6;
			out[index + 0] = alphabet[val & 0x3F];
		}
		return out;
	}

	/**
	 * 拼凑成key="value",key="value"的格式
	 * 
	 * @param params
	 * @return
	 */
	public static String appendParams(Params params) {
		StringBuffer paramsBuffer = new StringBuffer();
		for (String key : params.getKeys()) {
			if (paramsBuffer.length() != 0) {
				paramsBuffer.append(",");
			}

			paramsBuffer.append(key + "=");

			try {
				paramsBuffer.append("\"" + encode(params.getParameter(key)) + "\"");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return paramsBuffer.toString();
	}

	public static String encodeParams(Params params, String splitStr, boolean encode) {
		StringBuffer paramsBuffer = new StringBuffer();
		for (String key : params.getKeys()) {
			if (params.getParameter(key) == null)
				continue;

			if (paramsBuffer.length() != 0) {
				paramsBuffer.append(splitStr);
			}

			paramsBuffer.append(key + "=");
			paramsBuffer.append(encode && params.isEncodeAble() ? encode(params.getParameter(key)) : params.getParameter(key));
		}
		return paramsBuffer.toString();
	}

	public static String encodeToURLParams(Params params) {
		return encodeParams(params, "&", true);
	}

	public static String encodeParamsToJson(Params params) {
		JSONObject json = new JSONObject();

		for (String key : params.getKeys()) {
			if (params.getParameter(key) == null)
				continue;

			try {
				json.put(key, encode(params.getParameter(key)));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return json.toString();
	}

//	public static String encodeParams(Params params, boolean encode) {
//		StringBuffer paramsBuffer = new StringBuffer();
//		for (String key : params.getKeys()) {
//			if (paramsBuffer.length() != 0) {
//				paramsBuffer.append("&");
//			}
//
//			try {
//				paramsBuffer.append(encode(key) + "=");
//
//				if (encode) {
//					paramsBuffer.append(encode(params.getParameter(key)));
//				} else {
//					paramsBuffer.append(params.getParameter(key));
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//
//		}
//		return paramsBuffer.toString();
//	}

	public static String encode(String value) {
		if (value == null)
			return "";

		String encoded = null;
		try {
			encoded = URLEncoder.encode(value, "UTF-8");
		} catch (UnsupportedEncodingException ignore) {
		}
		StringBuffer buf = new StringBuffer(encoded.length());
		char focus;
		for (int i = 0; i < encoded.length(); i++) {
			focus = encoded.charAt(i);
			if (focus == '*') {
				buf.append("%2A");
			} else if (focus == '+') {
				buf.append("%20");
			} else if (focus == '%' && (i + 1) < encoded.length() && encoded.charAt(i + 1) == '7' && encoded.charAt(i + 2) == 'E') {
				buf.append('~');
				i += 2;
			} else {
				buf.append(focus);
			}
		}
		return buf.toString();
	}

	public static Params deCodeUrl(String content) {
		Params params = new Params();

		try {
			String decodeSource = "";
			if (content.indexOf("?") != -1) {
				decodeSource = content.substring(content.indexOf("?") + 1, content.length());
			} else {
				decodeSource = content;
			}
			String[] decodeParams = decodeSource.split("&");

			for (String keyValues : decodeParams) {
				String[] keyValue = keyValues.split("=");
				params.addParameter(keyValue[0], keyValue[1]);
			}
		} catch (Exception e) {
		}

		return params;
	}

	public static String encodeUrl(String url, Params params) {
		StringBuffer urlBuffer = new StringBuffer();

		urlBuffer.append(url + "?");

		urlBuffer.append(encodeParams(params, "&", true));

		return urlBuffer.toString();
	}
}
