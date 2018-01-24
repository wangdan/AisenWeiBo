package org.aisen.weibo.sina.sinasdk.http;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.aisen.android.network.task.TaskException;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;
import org.aisen.weibo.sina.support.utils.AisenUtils;

import java.util.ArrayList;
import java.util.List;

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
                throw new TaskException(result.getString("error_code"), result.getString("error"));
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
                        JSONObject card = cards.getJSONObject(i);
                        if (card.containsKey("card_group")) {
//                            if ("timeline".equals(card.getString("_cur_filter")) || "".equals(card.getString("_cur_filter"))) {
//
//                            }
                            if (card.containsKey("_cur_filter")) {
                                card_group = cards.getJSONObject(i).getJSONArray("card_group");
                                break;
                            }
                        }
                    }

                    if (card_group.size() == 0) {
                        beans = new StatusContents();
                        beans.setStatuses(new ArrayList<StatusContent>());
                        beans.setEndPaging(true);

                        return (T) beans;
                    }

                    List<String> idList = new ArrayList<>();
                    for (int i = 0; i < card_group.size(); i++) {
                        JSONObject group = card_group.getJSONObject(i);
                        if (group.containsKey("mblog")) {
                            idList.add(group.getJSONObject("mblog").getString("id"));
                        }
                    }
                    String[] ids = new String[idList.size()];
                    for (int i = 0; i < idList.size(); i++) {
                        ids[i] = idList.get(i);
                    }
                    beans = SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).statusShowBatch(ids); ;
                }
                else {
                    beans = new StatusContents();
                    beans.setStatuses(new ArrayList<StatusContent>());
                }

                if (result.containsKey("cardlistInfo")) {
                    JSONObject cardlistInfo = result.getJSONObject("cardlistInfo");
                    beans.setSince_id(cardlistInfo.getString("since_id"));

                    // 是否加载完了
                    int total = cardlistInfo.getInteger("total");
                    beans.setEndPaging(beans.getStatuses().size() == total);
                }
                else if (result.containsKey("pageInfo")) {
                    JSONObject cardlistInfo = result.getJSONObject("pageInfo");
                    beans.setSince_id(cardlistInfo.getString("since_id"));
                }

                return (T) beans;
            }
            else {
                throw  new TaskException("", "未知错误，请稍后再试");
            }
        } catch (Exception e) {
            if (e instanceof TaskException) {
                throw e;
            }
            else if (!TextUtils.isEmpty(resultStr) && resultStr.indexOf("你要找的页面") != -1) {
                throw new TaskException("", "啊哦，你要找的页面不见啦！");
            }

            throw  new TaskException(TaskException.TaskError.resultIllegal.toString());
        }
    }

}
