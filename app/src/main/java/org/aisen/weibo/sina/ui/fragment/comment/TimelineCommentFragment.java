package org.aisen.weibo.sina.ui.fragment.comment;

import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.paging.IPaging;
import org.aisen.android.ui.fragment.AListFragment;
import org.aisen.android.ui.fragment.itemview.BasicFooterView;
import org.aisen.android.ui.fragment.itemview.IITemView;
import org.aisen.android.ui.fragment.itemview.IItemViewCreator;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.StatusComment;
import org.aisen.weibo.sina.sinasdk.bean.StatusComments;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.support.paging.CommentPaging;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.ui.activity.base.SinaCommonActivity;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * 微博评论
 *
 * Created by wangdan on 16/1/7.
 */
public class TimelineCommentFragment extends AListFragment<StatusComment, StatusComments> {

    public static TimelineCommentFragment newInstance(StatusContent status) {
        Bundle arts = new Bundle();
        arts.putSerializable("status", status);

        TimelineCommentFragment fragment = new TimelineCommentFragment();
        fragment.setArguments(arts);
        return fragment;
    }

    private StatusContent mStatusContent;

    @Override
    public int inflateContentView() {
        return R.layout.ui_timeline_comment;
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        getContentView().setBackgroundColor(Color.WHITE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mStatusContent = savedInstanceState != null ? (StatusContent) savedInstanceState.getSerializable("status")
                                                    : (StatusContent) getArguments().getSerializable("status");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        BizFragment.createBizFragment(getActivity()).getFabAnimator().attachToListView(getRefreshView(), null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("status", mStatusContent);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        super.onItemClick(parent, view, position, id);

        if (getAdapterItems().size() == 0) {
            return;
        }

        final StatusComment comment = getAdapterItems().get(position);

        final String[] commentMenuArr = GlobalContext.getInstance().getResources().getStringArray(R.array.cmt_menus);

        if (mStatusContent != null)
            comment.setStatus(mStatusContent);

        final List<String> menuList = new ArrayList<String>();
        // 回复
        if (comment.getUser() != null && !comment.getUser().getId().equals(AppContext.getAccount().getUser().getId()))
            menuList.add(commentMenuArr[3]);
        // 转发
        if (comment.getStatus() != null &&
                (comment.getUser() != null && !comment.getUser().getIdstr().equals(AppContext.getAccount().getUser().getIdstr())))
            menuList.add(commentMenuArr[1]);
        // 复制
        menuList.add(commentMenuArr[0]);
        // 删除
        if (comment.getUser() != null && AppContext.getAccount().getUser().getIdstr().equals(comment.getUser().getIdstr()))
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

    @Override
    protected IItemViewCreator<StatusComment> configFooterViewCreator() {
        return new IItemViewCreator<StatusComment>() {

            @Override
            public View newContentView(LayoutInflater inflater, ViewGroup parent, int viewType) {
                return inflater.inflate(BasicFooterView.LAYOUT_RES, parent, false);
            }

            @Override
            public IITemView<StatusComment> newItemView(View convertView, int viewType) {
                return new BasicFooterView<StatusComment>(getActivity(), convertView, TimelineCommentFragment.this) {

                    @Override
                    protected String endpagingText() {
                        return getString(R.string.disable_comments);
                    }

                    @Override
                    protected String loadingText() {
                        return String.format(getString(R.string.loading_cmts), AppSettings.getCommentCount());
                    }

                };
            }

        };
    }

    @Override
    public IItemViewCreator<StatusComment> configItemViewCreator() {
        return new IItemViewCreator<StatusComment>() {

            @Override
            public View newContentView(LayoutInflater inflater, ViewGroup parent, int viewType) {
                return inflater.inflate(TimelineCommentItemView.LAYOUT_RES, parent, false);
            }

            @Override
            public IITemView<StatusComment> newItemView(View convertView, int viewType) {
                return new TimelineCommentItemView(TimelineCommentFragment.this, convertView);
            }

        };
    }

    @Override
    protected IPaging<StatusComment, StatusComments> newPaging() {
        return new CommentPaging();
    }

    @Override
    public void requestData(RefreshMode mode) {
        new CommentTask(mode).execute();
    }

    class CommentTask extends APagingTask<Void, Void, StatusComments> {

        public CommentTask(RefreshMode mode) {
            super(mode);
        }

        @Override
        protected List<StatusComment> parseResult(StatusComments statusComments) {
            return statusComments.getComments();
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
        protected StatusComments workInBackground(RefreshMode mode, String previousPage, String nextPage, Void... p) throws TaskException {
            Params params = new Params();

            if ((mode == RefreshMode.refresh || mode == RefreshMode.reset) && !TextUtils.isEmpty(previousPage))
                params.addParameter("since_id", previousPage);

            if (mode == RefreshMode.update && !TextUtils.isEmpty(nextPage))
                params.addParameter("max_id", nextPage);

            params.addParameter("id", mStatusContent.getId() + "");

            params.addParameter("count", String.valueOf(AppSettings.getCommentCount()));

            StatusComments statusComments = SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).commentsShow(params);
            statusComments.setEndPaging(statusComments.getComments().size() <= 10);

            return statusComments;
        }

        @Override
        protected void onFailure(TaskException exception) {
            super.onFailure(exception);

            showMessage(exception.getMessage());
        }

        @Override
        protected void onFinished() {
            super.onFinished();

            if (getActivity() != null) {
                Fragment fragment = getActivity().getFragmentManager().findFragmentByTag(SinaCommonActivity.FRAGMENT_TAG);
                if (fragment != null && fragment instanceof TimelineDetailPagerFragment) {
                    ((TimelineDetailPagerFragment) fragment).refreshEnd();
                }
            }
        }

    }

    @Override
    public boolean onToolbarDoubleClick() {
        if (AisenUtils.checkTabsFragmentCanRequestData(this)) {
            Fragment fragment = getActivity().getFragmentManager().findFragmentByTag(SinaCommonActivity.FRAGMENT_TAG);
            if (fragment != null && fragment instanceof TimelineDetailPagerFragment) {
                ((TimelineDetailPagerFragment) fragment).swipeRefreshLayout.setRefreshing(true);
            }

            requestDataDelaySetRefreshing(AppSettings.REQUEST_DATA_DELAY);
            getRefreshView().setSelectionFromTop(0, 0);

            return true;
        }

        return false;
    }

}
