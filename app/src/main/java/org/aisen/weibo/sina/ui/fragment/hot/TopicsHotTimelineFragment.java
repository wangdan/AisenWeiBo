package org.aisen.weibo.sina.ui.fragment.hot;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;

import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.paging.IPaging;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;
import org.aisen.weibo.sina.sinasdk.bean.WebHotTopicsBean;
import org.aisen.weibo.sina.support.paging.HotPaging;
import org.aisen.weibo.sina.ui.fragment.timeline.ATimelineFragment;
import org.aisen.weibo.sina.ui.widget.AisenTextView;

/**
 * 热门话题微博
 *
 * Created by wangdan on 16/8/14.
 */
public class TopicsHotTimelineFragment extends ATimelineFragment {

    public static TopicsHotTimelineFragment newInstance(WebHotTopicsBean bean, Type type) {
        Bundle args = new Bundle();
        args.putSerializable("bean", bean);
        args.putString("type", type.toString());

        TopicsHotTimelineFragment fragment = new TopicsHotTimelineFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public enum Type {
        recommend, hot
    }

    private Type mType;
    private WebHotTopicsBean mBean;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("bean", mBean);
        outState.putString("type", mType.toString());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBean = savedInstanceState != null ? (WebHotTopicsBean) savedInstanceState.getSerializable("bean")
                                           : (WebHotTopicsBean) getArguments().getSerializable("bean");
        mType = savedInstanceState != null ? Type.valueOf(savedInstanceState.getString("type"))
                                           : Type.valueOf(getArguments().getString("type"));
    }

    @Override
    protected int timelineCount() {
        return 10;
    }

    @Override
    public void requestData(RefreshMode mode) {
        new HotTimelineTask(mode != RefreshMode.update ? RefreshMode.reset : mode).execute();
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        getSwipeRefreshLayout().setEnabled(false);
    }

    @Override
    protected IPaging<StatusContent, StatusContents> newPaging() {
        return new HotPaging();
    }

    class HotTimelineTask extends ATimelineTask {

        public HotTimelineTask(RefreshMode mode) {
            super(mode);
        }

        @Override
        protected StatusContents workInBackground(RefreshMode mode, String previousPage, String nextPage, Void... p) throws TaskException {
            // 系统繁忙的错误，多尝试几次接口拉取
            StatusContents result = null;
            int retry = 5;
            while (true) {
                try {
                    if (mType == Type.recommend) {
                        String uid = AppContext.getAccount().getUid();
                        result = SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).webGetHotTopicsRecommendStatus(uid, mBean.getOid(), nextPage);
                    }
                    else {
                        result = SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).webGetHotTopicsHotStatus(mBean.getOid(), nextPage);
                    }
                } catch (TaskException e) {
                    // 系统繁忙错误重试几次
                    if ("1040002".equals(e.getCode()) && --retry > 0) {
                        continue;
                    }

                    throw e;
                }

                break;
            }

            for (StatusContent content : result.getStatuses()) {
                AisenTextView.addText(content.getText());

                if (content.getRetweeted_status() != null) {
                    String reUserName = "";
                    if (content.getRetweeted_status().getUser() != null && !TextUtils.isEmpty(content.getRetweeted_status().getUser().getScreen_name()))
                        reUserName = String.format("@%s :", content.getRetweeted_status().getUser().getScreen_name());
                    AisenTextView.addText(reUserName + content.getRetweeted_status().getText());
                }
            }

            if (!result.isEndPaging())
                result.setEndPaging(result.getStatuses().size() <= 3);

            return result;
        }

        @Override
        public StatusContents getStatusContents(Params params) throws TaskException {
            throw new TaskException("", "不支持");
        }

    }

}
