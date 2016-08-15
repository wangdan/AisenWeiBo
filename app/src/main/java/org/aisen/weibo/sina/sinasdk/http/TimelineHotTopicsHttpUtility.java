package org.aisen.weibo.sina.sinasdk.http;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.aisen.android.network.task.TaskException;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;
import org.aisen.weibo.sina.support.utils.AisenUtils;

import java.util.ArrayList;

/**
 * Created by wangdan on 16/7/21.
 */
public class TimelineHotTopicsHttpUtility extends HttpsUtility {

    @Override
    protected <T> T parseResponse(String resultStr, Class<T> responseCls) throws TaskException {
        try {
            AisenUtils.checkWebResult(resultStr);

            JSONObject result = JSONObject.parseObject(resultStr);

            if (result.containsKey("error")) {
                throw new TaskException("", result.getString("error"));
            }

            if (result.containsKey("ok")) {
                if (result.getInteger("ok") == 0 && result.containsKey("msg")) {
                    throw new TaskException("", result.getString("msg"));
                }

                JSONArray cards = result.getJSONArray("cards");
                StatusContents beans;
                if (cards.size() > 0) {
                    JSONArray card_group = null;
                    for (int i = 0; i < cards.size(); i++) {
                        if (cards.getJSONObject(i).containsKey("card_group")) {
                            card_group = cards.getJSONObject(i).getJSONArray("card_group");
                            break;
                        }
                    }

                    String[] ids = new String[card_group.size()];
                    for (int i = 0; i < card_group.size(); i++) {
                        ids[i] = card_group.getJSONObject(i).getJSONObject("mblog").getString("id");
                    }
                    beans = SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).statusShowBatch(ids); ;
                }
                else {
                    beans = new StatusContents();
                    beans.setStatuses(new ArrayList<StatusContent>());
                }

                JSONObject cardlistInfo = result.getJSONObject("cardlistInfo");
                beans.setSince_id(cardlistInfo.getString("since_id"));

                return (T) beans;
            }
            else {
                throw  new TaskException("", "未知错误，请稍后再试");
            }
        } catch (Exception e) {
            if (e instanceof TaskException)
                throw e;

            throw  new TaskException(TaskException.TaskError.resultIllegal.toString());
        }
    }

}
