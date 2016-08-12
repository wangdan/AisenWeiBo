package org.aisen.weibo.sina.sinasdk.http;

import android.text.Html;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.network.task.TaskException;
import org.aisen.weibo.sina.sinasdk.bean.StatusComment;
import org.aisen.weibo.sina.sinasdk.bean.StatusComments;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;

import java.util.ArrayList;

/**
 * Created by wangdan on 16/7/21.
 */
public class CommentsHotHttpUtility extends TimelineCommentHttpUtility {

    @Override
    protected <T> T parseResponse(String resultStr, Class<T> responseCls) throws TaskException {
        try {
            JSONArray jsonArray = JSONArray.parseArray(resultStr);

            StatusComments comments = new StatusComments();
            comments.setComments(new ArrayList<StatusComment>());

            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                if (jsonObject.containsKey("card_group")) {
                    JSONArray card_group = jsonObject.getJSONArray("card_group");

                    for (int j = 0; j < card_group.size(); j++) {
                        try {
                            JSONObject card = card_group.getJSONObject(j);

                            StatusComment comment = new StatusComment();
                            comment.setId(card.getString("id"));
                            comment.setText(Html.fromHtml(card.getString("text")).toString());
                            comment.setCreated_at(card.getString("created_at"));
                            comment.setSource(card.getString("source"));
                            comment.setLikedCount(card.getLong("like_counts"));
                            comment.setLiked(card.getBoolean("liked"));


                            JSONObject user = card.getJSONObject("user");
                            comment.setUser(new WeiBoUser());
                            comment.getUser().setInfoAll(false);
                            comment.getUser().setId(user.getString("id"));
                            comment.getUser().setScreen_name(user.getString("screen_name"));
                            comment.getUser().setProfile_image_url(user.getString("profile_image_url"));

                            comments.getComments().add(comment);
                        } catch (Exception e) {
                            Logger.printExc(CommentsHotHttpUtility.class, e);
                        }
                    }
                }
            }

            return (T) comments;
        } catch (Exception e) {
            throw  new TaskException(TaskException.TaskError.resultIllegal.toString());
        }
    }

}
