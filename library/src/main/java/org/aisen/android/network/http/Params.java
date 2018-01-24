package org.aisen.android.network.http;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Params implements Serializable {

	private static final long serialVersionUID = 5990125562753037686L;

	private Map<String, String> mParameters = new HashMap<String, String>();
	private List<String> mKeys = new ArrayList<String>();

	// 某些时候，参数列表不需要进行编码，例如将参数作为URL QUERY时，防止进行了二次编码导致服务器不能解析
	private boolean encodeAble = true;

	public Params() {

	}

	public Params(String[] keys, String[] values) {
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			mKeys.add(key);
			mParameters.put(key, values[i]);
		}

	}

	public Params(String key, String value) {
		mKeys.add(key);
		mParameters.put(key, value);
	}

	public int size() {
		return mKeys.size();
	}

	public boolean containsKey(String key) {
		return mKeys.contains(key);
	}

	public List<String> getKeys() {
		return mKeys;
	}

	public void addParameter(String key, String value) {
		if (!mKeys.contains(key)) {
			mKeys.add(key);
		}
		mParameters.put(key, value);
	}

	public String getParameter(String key) {
		return mParameters.get(key);
	}

	public Map<String, String> getVaules() {
		return mParameters;
	}

	public void remove(String key) {
		if (mKeys.contains(key)) {
			mKeys.remove(key);
			mParameters.remove(key);
		}
	}

	public void addParams(Params params) {
		for (String key : params.getKeys()) {
			if (!mKeys.contains(key)) {
				mKeys.add(key);
			}
			mParameters.put(key, params.getParameter(key));
		}
	}

	public boolean isEncodeAble() {
		return encodeAble;
	}

	public void setEncodeAble(boolean encodeAble) {
		this.encodeAble = encodeAble;
	}

	public void clearParams() {
		mParameters.clear();
		mKeys.clear();
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		for (String key : mKeys) {
			sb.append(key).append("=").append(getParameter(key)).append(",");
		}

		return sb.toString();
	}

}
