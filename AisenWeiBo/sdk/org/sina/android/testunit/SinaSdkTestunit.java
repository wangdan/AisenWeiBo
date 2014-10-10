package org.sina.android.testunit;

import java.util.List;

import org.sina.android.SinaSDK;
import org.sina.android.bean.StatusContent;
import org.sina.android.bean.Token;

import com.alibaba.fastjson.JSON;
import com.m.common.context.GlobalContext;
import com.m.common.utils.FileUtility;
import com.m.common.utils.Logger;

import android.test.AndroidTestCase;

public class SinaSdkTestunit extends AndroidTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testGetLocation() throws Throwable {
		try {
			try {
				Thread.sleep(2000);
			} catch (Exception e) {
			}
			SinaSDK.getInstance(getToken()).locationMobileGetLocation();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void testFastjson() throws Throwable {
		try {
			String json = FileUtility.readAssetsFile("status.txt", GlobalContext.getInstance());

			StatusContent status = JSON.parseObject(json, StatusContent.class);
			Logger.d(status);
			
			json = FileUtility.readAssetsFile("statusArr.txt", GlobalContext.getInstance());
			
			List<StatusContent> statusArr = JSON.parseArray(json, StatusContent.class);
			Logger.d(statusArr);
		} catch (Exception e) {
		}
	}
	
	private Token getToken() {
		Token token = new Token();
		token.setToken("2.00GhFSiCu_WsZCc797b6b3cflUMsjC");
		return token;
	}
	
}
