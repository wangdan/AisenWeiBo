package org.aisen.weibo.sina.sinasdk.http;

import android.text.Html;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.aisen.android.network.task.TaskException;
import org.aisen.weibo.sina.sinasdk.bean.StatusComment;
import org.aisen.weibo.sina.sinasdk.bean.StatusComments;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;

import java.util.ArrayList;

/**
 * Created by wangdan on 16/7/21.
 */
public class CommentsHotHttpUtility extends HttpsUtility {

    @Override
    protected <T> T parseResponse(String resultStr, Class<T> responseCls) throws TaskException {
        try {
            JSONArray jsonArray = JSONArray.parseArray(resultStr);

            StatusComments comments = new StatusComments();
            comments.setComments(new ArrayList<StatusComment>());

            if (jsonArray.size() >= 2) {
                JSONObject jsonObject = jsonArray.getJSONObject(1);

                if (jsonObject.containsKey("mod_type") && "mod/empty".equals(jsonObject.getString("mod_type"))) {
                    comments.setEndPaging(true);
                }
                else if (!jsonObject.containsKey("card_group")) {
                    comments.setEndPaging(true);
                }
                else {
                    // 是否接受翻页
                    int page = jsonObject.getInteger("page");
                    int maxPage = jsonObject.getInteger("maxPage");
                    comments.setEndPaging(page == maxPage);

                    JSONArray card_group = jsonObject.getJSONArray("card_group");
                    for (int i = 0; i < card_group.size(); i++) {
                        JSONObject card = card_group.getJSONObject(i);

                        StatusComment comment = new StatusComment();
                        comment.setId(card.getString("id"));
                        comment.setText(Html.fromHtml(card.getString("text")).toString());
                        comment.setCreated_at(card.getString("created_at"));
                        comment.setSource(card.getString("source"));

                        JSONObject user = card.getJSONObject("user");
                        comment.setUser(new WeiBoUser());
                        comment.getUser().setAll(false);
                        comment.getUser().setId(user.getString("id"));
                        comment.getUser().setScreen_name(user.getString("screen_name"));
                        comment.getUser().setProfile_image_url(user.getString("profile_image_url"));

                        comments.getComments().add(comment);
                    }
                }
            }

            return (T) comments;
        } catch (Exception e) {
            throw  new TaskException(TaskException.TaskError.resultIllegal.toString());
        }
    }

}
