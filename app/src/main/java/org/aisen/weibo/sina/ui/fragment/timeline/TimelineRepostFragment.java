package org.aisen.weibo.sina.ui.fragment.timeline;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import org.aisen.android.component.container.FragmentArgs;
import org.aisen.android.component.container.FragmentContainerActivity;
import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.adapter.ABaseAdapter.AbstractItemView;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import com.melnykov.fab.FloatingActionButton;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.support.utils.FabBtnUtils;
import org.aisen.weibo.sina.ui.fragment.comment.CommentsHeaderView;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;
import org.aisen.weibo.sina.sinasdk.bean.StatusRepost;

/**
 * 某条原创微博的转发微博
 * 
 * @author wangdan
 *
 */
public class TimelineRepostFragment extends ATimelineFragment {

    public static void launch(Activity from, StatusContent bean) {
        FragmentArgs args = new FragmentArgs();
        args.add("status", bean);

        FragmentContainerActivity.launch(from, TimelineRepostFragment.class, args);
    }

    @ViewInject(id = R.id.fab, click = "onFabBtnClicked")
    FloatingActionButton fab;

    private View headerView;

    private StatusContent mStatusContent;

    @Override
    public void onCreate(Bundle savedInstanceSate) {
        super.onCreate(savedInstanceSate);

        mStatusContent = savedInstanceSate == null ? (StatusContent) getArguments().getSerializable("status")
                                                    : (StatusContent) savedInstanceSate.getSerializable("status");
    }

    @Override
    protected int inflateContentView() {
        return R.layout.as_ui_timeline_repost;
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        BaseActivity baseActivity = (BaseActivity) getActivity();
        baseActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        baseActivity.setTitle(R.string.cmts_repost_title);

        setHasOptionsMenu(true);

        FabBtnUtils.setFabBtn(getActivity(), fab, R.drawable.ic_retweet, getRefreshView());
    }

    @Override
    protected String loadDisabledLabel() {
        return getString(R.string.disable_status);
    }

    @Override
    protected String loadingLabel() {
        return String.format(getString(R.string.loading_status), AppSettings.getCommentCount());
    }

    @Override
    protected void setInitSwipeRefresh(ListView listView, SwipeRefreshLayout swipeRefreshLayout, Bundle savedInstanceState) {
        super.setInitSwipeRefresh(listView, swipeRefreshLayout, savedInstanceState);

        CommentsHeaderView timelineItem = new CommentsHeaderView(this, true);
        View view = View.inflate(getActivity(), timelineItem.inflateViewId(), null);
        timelineItem.bindingView(view);
        view.setTag(timelineItem);
        listView.addHeaderView(view);

        headerView = view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("status", mStatusContent);
    }

    @Override
    public void onResume() {
        super.onResume();

        CommentsHeaderView timelineItem = (CommentsHeaderView) headerView.getTag();
        if (timelineItem != null)
            timelineItem.bindingData(headerView, mStatusContent);
    }

    @Override
    public void onPause() {
        super.onPause();

        CommentsHeaderView timelineItem = (CommentsHeaderView) headerView.getTag();
        if (timelineItem != null)
            timelineItem.layPicturs.release();
    }

    @Override
    protected AbstractItemView<StatusContent> newItemView() {
        return new TimelineItemView(this, mStatusContent, false);
    }

    @Override
    protected void requestData(RefreshMode mode) {
        new RepostTimeTask(mode).execute();
    }

    class RepostTimeTask extends TimelineTask {

        public RepostTimeTask(RefreshMode mode) {
            super(mode);
        }

        @Override
        protected StatusContents workInBackground(RefreshMode mode, String previousPage, String nextPage,
                                                  Void... p) throws TaskException {
            Params params = new Params();

            if (mode == RefreshMode.refresh && !TextUtils.isEmpty(previousPage))
                params.addParameter("since_id", previousPage);

            if (mode == RefreshMode.update && !TextUtils.isEmpty(nextPage))
                params.addParameter("max_id", nextPage);

            String statusId = mStatusContent.getId() + "";
            params.addParameter("id", statusId);

            params.addParameter("count", String.valueOf(AppSettings.getTimelineCount()));

            StatusRepost statusRepost = SinaSDK.getInstance(AppContext.getToken()).statusRepostTimeline(params);
            if (statusRepost != null) {
                for (StatusContent status : statusRepost.getReposts()) {
                    status.setRetweeted_status(null);
                }

                return new StatusContents(statusRepost.getReposts());
            }

            return null;
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_cmts, menu);
        menu.removeItem(R.id.repost);
        if (mStatusContent.getUser() == null ||
                !mStatusContent.getUser().getIdstr().equalsIgnoreCase(AppContext.getUser().getIdstr()))
            menu.removeItem(R.id.delete);

        AisenUtils.setStatusShareMenu(menu.findItem(R.id.share), mStatusContent);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AisenUtils.onMenuClicked(this, item.getItemId(), mStatusContent);

        return super.onOptionsItemSelected(item);
    }

    void onFabBtnClicked(View v) {
        AisenUtils.onMenuClicked(this, R.id.repost, mStatusContent);
    }

}
