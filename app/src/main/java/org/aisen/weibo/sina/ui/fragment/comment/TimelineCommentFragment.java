package org.aisen.weibo.sina.ui.fragment.comment;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.component.container.FragmentArgs;
import org.aisen.android.component.container.FragmentContainerActivity;
import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.adapter.ABaseAdapter;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.support.paging.IPaging;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.ASwipeRefreshListFragment;
import com.melnykov.fab.FloatingActionButton;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.support.paging.CommentsPagingProcessor;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.support.utils.FabBtnUtils;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.StatusComment;
import org.aisen.weibo.sina.sinasdk.bean.StatusComments;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;

import java.util.ArrayList;
import java.util.List;

/**
 * 微博评论列表
 *
 * Created by wangdan on 15/4/15.
 */
public class TimelineCommentFragment extends ASwipeRefreshListFragment<StatusComment, StatusComments> {

    public static void launch(Activity from, StatusContent bean) {
        FragmentArgs args = new FragmentArgs();
        args.add("bean", bean);

        FragmentContainerActivity.launch(from, TimelineCommentFragment.class, args);
    }

    @ViewInject(id = R.id.fab, click = "onFabBtnClicked")
    FloatingActionButton fab;

    private StatusContent mStatusContent;

    private View headerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mStatusContent = savedInstanceState == null ? (StatusContent) getArguments().getSerializable("bean")
                                                    : (StatusContent) savedInstanceState.getSerializable("bean");
    }

    @Override
    protected int inflateContentView() {
        return R.layout.as_ui_timeline_cmt;
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        BaseActivity baseActivity = (BaseActivity) getActivity();
        baseActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        baseActivity.setTitle(R.string.cmts_title);

        setHasOptionsMenu(true);

        FabBtnUtils.setFabBtn(getActivity(), fab, R.drawable.ic_reply, getRefreshView());
    }

    @Override
    protected String loadDisabledLabel() {
        return getString(R.string.disable_comments);
    }

    @Override
    protected String loadingLabel() {
        return String.format(getString(R.string.loading_cmts), AppSettings.getCommentCount());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("bean", mStatusContent);
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

        mStatusContent = savedInstanceState == null ? (StatusContent) getArguments().getSerializable("bean")
                                                    : (StatusContent) savedInstanceState.getSerializable("bean");

        if (mStatusContent == null) {
            getActivity().finish();
            return;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        super.onItemClick(parent, view, position, id);

        ListView listView = (ListView) getRefreshView();
        position -= listView.getHeaderViewsCount();
        if (position >= 0 && position < getAdapter().getCount()) {
            final StatusComment comment = getAdapterItems().get(position);

            final String[] commentMenuArr = GlobalContext.getInstance().getResources().getStringArray(R.array.cmt_menus);

            if (mStatusContent != null)
                comment.setStatus(mStatusContent);

            final List<String> menuList = new ArrayList<String>();
            // 回复
            if (comment.getUser() != null && !comment.getUser().getId().equals(AppContext.getUser().getId()))
                menuList.add(commentMenuArr[3]);
            // 转发
//			if (fragment instanceof TimelineCommentsFragment)
            if (comment.getStatus() != null &&
                    (comment.getUser() != null && !comment.getUser().getIdstr().equals(AppContext.getUser().getIdstr())))
                menuList.add(commentMenuArr[1]);
            // 复制
            menuList.add(commentMenuArr[0]);
            // 删除
            if (comment.getUser() != null && AppContext.getUser().getIdstr().equals(comment.getUser().getIdstr()))
                menuList.add(commentMenuArr[2]);

            new AlertDialogWrapper.Builder(getActivity())
                    .setTitle(comment.getUser().getScreen_name())
                    .setItems(menuList.toArray(new String[0]), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AisenUtils.commentMenuSelected(TimelineCommentFragment.this, menuList.toArray(new String[0])[which], comment);
                        }

                    })
                    .show();
        }
    }

    @Override
    protected IPaging<StatusComment, StatusComments> configPaging() {
        return new CommentsPagingProcessor();
    }

    @Override
    protected ABaseAdapter.AbstractItemView<StatusComment> newItemView() {
        return new TimelineCommentItemView(this);
    }

    @Override
    protected void requestData(RefreshMode mode) {
        new TimelineCommentTask(mode).execute();
    }

    class TimelineCommentTask extends PagingTask<Void, Void, StatusComments> {

        public TimelineCommentTask(RefreshMode mode) {
            super("TimelineCommentTask", mode);
        }

        @Override
        protected StatusComments workInBackground(RefreshMode mode, String previousPage, String nextPage, Void... p) throws TaskException {
            Params params = new Params();

            if ((mode == RefreshMode.refresh || mode == RefreshMode.reset) && !TextUtils.isEmpty(previousPage))
                params.addParameter("since_id", previousPage);

            if (mode == RefreshMode.update && !TextUtils.isEmpty(nextPage))
                params.addParameter("max_id", nextPage);

            params.addParameter("id", mStatusContent.getId() + "");

            params.addParameter("count", String.valueOf(AppSettings.getCommentCount()));

            return SinaSDK.getInstance(AppContext.getToken()).commentsShow(params);
        }

        @Override
        protected List<StatusComment> parseResult(StatusComments result) {
            return result.getComments();
        }

        @Override
        protected boolean handleResult(RefreshMode mode, List<StatusComment> datas) {
            // 如果是重置或者刷新数据，加载数据大于分页大小，则清空之前的数据
            if (mode == RefreshMode.reset || mode == RefreshMode.refresh)
                // 目前微博加载分页大小是默认大小
                if (datas.size() >= AppSettings.getCommentCount()) {
                    setAdapterItems(new ArrayList<StatusComment>());
                    return true;
                }

            return super.handleResult(mode, datas);
        }

        @Override
        protected void onFinished() {
            super.onFinished();

        }

        @Override
        protected void onFailure(TaskException exception) {
            super.onFailure(exception);

            showMessage(exception.getMessage());
        }

    }

    @Override
    public boolean onToolbarDoubleClick() {
        ((ListView) getRefreshView()).setSelectionFromTop(0, 0);
        requestDataDelay(200);

        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_cmts, menu);
        menu.removeItem(R.id.comment);
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
        AisenUtils.onMenuClicked(this, R.id.comment, mStatusContent);
    }

    @Override
    public void onResume() {
        super.onResume();

        CommentsHeaderView timelineItem = (CommentsHeaderView) headerView.getTag();
        if (timelineItem != null)
            timelineItem.bindingData(headerView, mStatusContent);

        mHandler.removeCallbacks(releaseRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();

        mHandler.postDelayed(releaseRunnable, 3 * 1000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mHandler.removeCallbacks(releaseRunnable);
    }

    Handler mHandler = new Handler();

    Runnable releaseRunnable = new Runnable() {

        @Override
        public void run() {
            CommentsHeaderView timelineItem = (CommentsHeaderView) headerView.getTag();
            if (timelineItem != null)
                timelineItem.layPicturs.release();
        }

    };

}
