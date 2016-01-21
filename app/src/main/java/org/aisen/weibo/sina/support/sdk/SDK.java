package org.aisen.weibo.sina.support.sdk;

import org.aisen.android.network.biz.ABizLogic;
import org.aisen.android.network.http.HttpConfig;
import org.aisen.android.network.http.IHttpUtility;
import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.weibo.sina.support.bean.PictureSize;

public class SDK extends ABizLogic {

    public static SDK newInstance() {
        return new SDK();
    }

	@Override
	protected HttpConfig configHttpConfig() {
		HttpConfig httpConfig = new HttpConfig();
		return httpConfig;
	}

    @Override
    protected IHttpUtility configHttpUtility() {
        return super.configHttpUtility();
    }

    public PictureSize getPictureSize(String url) throws TaskException {
        Params params = new Params();
        params.addParameter("path", url);

        return doGet(getSetting("getPictureSize"), params, PictureSize.class);
    }

}
