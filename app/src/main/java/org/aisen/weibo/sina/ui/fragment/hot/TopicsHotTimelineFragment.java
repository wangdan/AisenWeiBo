package org.aisen.weibo.sina.ui.fragment.hot;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;

import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.paging.IPaging;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.activity.container.FragmentArgs;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;
import org.aisen.weibo.sina.sinasdk.bean.WebHotTopicsBean;
import org.aisen.weibo.sina.support.paging.HotPaging;
import org.aisen.weibo.sina.ui.activity.base.SinaCommonActivity;
import org.aisen.weibo.sina.ui.fragment.timeline.ATimelineFragment;
import org.aisen.weibo.sina.ui.widget.AisenTextView;

/**
 * 热门话题微博
 *
 * Created by wangdan on 16/8/14.
 */
public class TopicsHotTimelineFragment extends ATimelineFragment {

    public static void launch(Activity from, WebHotTopicsBean bean) {
        FragmentArgs args = new FragmentArgs();
        args.add("bean", bean);

        SinaCommonActivity.launch(from , TopicsHotTimelineFragment.class, args);
    }

    private WebHotTopicsBean mBean;

//    @Override
//    public int inflateContentView() {
//        return -1;
//    }
//
//    @Override
//    public int inflateActivityContentView() {
//        return R.layout.ui_hottopics_timeline;
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        InjectUtility.initInjectedView(getActivity(), this, ((BaseActivity) getActivity()).getRootView());
//        layoutInit(inflater, savedInstanceState);
//
//        return null;
//    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("bean", mBean);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBean = savedInstanceState != null ? (WebHotTopicsBean) savedInstanceState.getSerializable("bean")
                                           : (WebHotTopicsBean) getArguments().getSerializable("bean");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        BaseActivity activity = (BaseActivity) getActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setTitle(mBean.getCard_type_name());
    }

    @Override
    public void requestData(RefreshMode mode) {
        new HotTimelineTask(mode != RefreshMode.update ? RefreshMode.reset : mode).execute();
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
            String uid = AppContext.getAccount().getUid();
            StatusContents result = SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).webGetHotTopicsStatus(uid, mBean.getOid(), nextPage);

            for (StatusContent content : result.getStatuses()) {
                AisenTextView.addText(content.getText());

                if (content.getRetweeted_status() != null) {
                    String reUserName = "";
                    if (content.getRetweeted_status().getUser() != null && !TextUtils.isEmpty(content.getRetweeted_status().getUser().getScreen_name()))
                        reUserName = String.format("@%s :", content.getRetweeted_status().getUser().getScreen_name());
                    AisenTextView.addText(reUserName + content.getRetweeted_status().getText());
                }
            }

            return result;
        }

        @Override
        public StatusContents getStatusContents(Params params) throws TaskException {
            throw new TaskException("", "不支持");
        }

    }

}
