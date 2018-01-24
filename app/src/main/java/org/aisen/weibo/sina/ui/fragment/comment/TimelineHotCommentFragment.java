package org.aisen.weibo.sina.ui.fragment.comment;

import android.os.Bundle;
import android.text.TextUtils;

import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.paging.IPaging;
import org.aisen.android.support.paging.PageIndexPaging;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.StatusComment;
import org.aisen.weibo.sina.sinasdk.bean.StatusComments;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.ui.widget.AisenTextView;

/**
 * 微博评论
 *
 * Created by wangdan on 16/1/7.
 */
public class TimelineHotCommentFragment extends TimelineCommentFragment {

    public static TimelineHotCommentFragment newInstance(StatusContent status) {
        Bundle arts = new Bundle();
        arts.putSerializable("status", status);

        TimelineHotCommentFragment fragment = new TimelineHotCommentFragment();
        fragment.setArguments(arts);
        return fragment;
    }

    @Override
    protected IPaging<StatusComment, StatusComments> newPaging() {
        return new PageIndexPaging<>();
    }

    @Override
    public void requestData(RefreshMode mode) {
        new HotCommentTask(mode != RefreshMode.update ? RefreshMode.reset : mode).execute();
    }

    class HotCommentTask extends CommentTask {

        public HotCommentTask(RefreshMode mode) {
            super(mode);
        }

        @Override
        protected StatusComments workInBackground(RefreshMode mode, String previousPage, String nextPage, Void... p) throws TaskException {
            String statusId = mStatusContent.getId() + "";
            int page = 1;

            if (mode == RefreshMode.update && !TextUtils.isEmpty(nextPage))
                page = Integer.parseInt(nextPage);

            StatusComments statusComments = SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).commentsHotShow(statusId, page);
            statusComments.setEndPaging(statusComments.getComments().size() <= 5);

            for (StatusComment content : statusComments.getComments()) {
                AisenTextView.addText(content.getText());
            }

            return statusComments;
        }

    }

}
