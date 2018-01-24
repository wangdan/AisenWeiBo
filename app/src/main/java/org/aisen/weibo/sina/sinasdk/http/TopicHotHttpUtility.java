package org.aisen.weibo.sina.sinasdk.http;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.aisen.android.network.task.TaskException;
import org.aisen.weibo.sina.sinasdk.bean.WebHotTopicsBean;
import org.aisen.weibo.sina.sinasdk.bean.WebHotTopicssBean;
import org.aisen.weibo.sina.support.utils.AisenUtils;

import java.util.ArrayList;

/**
 * Created by wangdan on 16/7/21.
 */
public class TopicHotHttpUtility extends HttpsUtility {

    @Override
    protected <T> T parseResponse(String resultStr, Class<T> responseCls) throws TaskException {
        try {
            AisenUtils.checkWebResult(resultStr);

            WebHotTopicssBean beans = new WebHotTopicssBean();
            beans.setList(new ArrayList<WebHotTopicsBean>());

            JSONObject result = JSONObject.parseObject(resultStr);

            if (result.containsKey("error")) {
                throw new TaskException("", result.getString("error"));
            }

            if (result.containsKey("ok")) {
                if (result.getInteger("ok") == 0 && result.containsKey("msg")) {
                    throw new TaskException("", result.getString("msg"));
                }

                JSONObject cardlistInfo = result.getJSONObject("cardlistInfo");
                if (cardlistInfo.containsKey("since_id")) {
                    beans.setSince_id(cardlistInfo.getString("since_id"));
                }
                else if (cardlistInfo.containsKey("page")) {
                    beans.setPage(cardlistInfo.getInteger("page"));
                }
                else {
                    beans.setEndPaging(true);

                    return (T) beans;
                }

                JSONArray cards = result.getJSONArray("cards");

                if (cards.size() > 0) {
                    JSONArray card_group = null;
                    for (int i = 0; i < cards.size(); i++) {
                        if (cards.getJSONObject(i).containsKey("card_group")) {
                            card_group = cards.getJSONObject(i).getJSONArray("card_group");
                            break;
                        }
                    }

                    if (card_group != null) {
                        for (int i = 0; i < card_group.size(); i++) {
                            JSONObject group = card_group.getJSONObject(i);

                            WebHotTopicsBean bean = new WebHotTopicsBean();
                            bean.setCard_type(group.getInteger("card_type"));
                            // 普通样式
                            if (bean.getCard_type() == 8) {
                                bean.setPic(group.getString("pic"));
                                bean.setTitle_sub(group.getString("title_sub"));
                                bean.setCard_type_name(group.getString("card_type_name"));
                                bean.setDesc1(group.getString("desc1"));
                                bean.setDesc2(group.getString("desc2"));
                                bean.setOid(group.getJSONObject("actionlog").getString("oid"));
                                bean.setFid(group.getJSONObject("actionlog").getString("fid"));
                            }
                            // 多个图片、暂不支持
                            else if (bean.getCard_type() == 3) {
                                continue;
                            }
                            else {
                                continue;
                            }

                            beans.getList().add(bean);
                        }
                    }
                }

                return (T) beans;
            }
            else {
                return (T) beans;
            }
        } catch (Exception e) {
            if (e instanceof TaskException)
                throw e;

            throw  new TaskException(TaskException.TaskError.resultIllegal.toString());
        }
    }

}
