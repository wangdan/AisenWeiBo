package org.aisen.weibo.sina.ui.fragment.hot;

import android.app.Activity;
import android.os.Bundle;

import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.weibo.sina.sinasdk.bean.WebHotTopicsBean;
import org.aisen.weibo.sina.ui.activity.base.SinaCommonActivity;
import org.aisen.weibo.sina.ui.fragment.timeline.ATimelineFragment;

/**
 * 热门话题
 *
 * Created by wangdan on 16/8/14.
 */
public class TopicsHotTimelineFragment extends ATimelineFragment {

    public static void launch(Activity from) {
        SinaCommonActivity.launch(from , TopicsHotFragment.class, null);
    }

    private WebHotTopicsBean mBean;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("bean", mBean);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        BaseActivity activity = (BaseActivity) getActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setTitle("热门话题");

        mBean = getArguments() == null ? (WebHotTopicsBean) savedInstanceState.getSerializable("bean")
                                       : (WebHotTopicsBean) getArguments().getSerializable("bean");
    }

    @Override
    public void requestData(RefreshMode mode) {

    }

}
