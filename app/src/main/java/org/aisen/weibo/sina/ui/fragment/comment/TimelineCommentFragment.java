package org.aisen.weibo.sina.ui.fragment.comment;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.paging.IPaging;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.activity.container.FragmentArgs;
import org.aisen.android.ui.fragment.ARecycleViewSwipeRefreshFragment;
import org.aisen.android.ui.fragment.itemview.AHeaderItemViewCreator;
import org.aisen.android.ui.fragment.itemview.BasicFooterView;
import org.aisen.android.ui.fragment.itemview.DefDividerItemView;
import org.aisen.android.ui.fragment.itemview.IITemView;
import org.aisen.android.ui.fragment.itemview.IItemViewCreator;
import org.aisen.android.ui.fragment.itemview.NormalItemViewCreator;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.StatusComment;
import org.aisen.weibo.sina.sinasdk.bean.StatusComments;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.support.paging.CommentPaging;
import org.aisen.weibo.sina.ui.activity.base.SinaCommonActivity;

import java.util.List;

/**
 * 微博评论
 *
 * Created by wangdan on 16/1/7.
 */
public class TimelineCommentFragment extends ARecycleViewSwipeRefreshFragment<StatusComment, StatusComments> {

    public static void launch(Activity from, StatusContent status) {
        FragmentArgs args = new FragmentArgs();
        args.add("status", status);

        SinaCommonActivity.launch(from, TimelineCommentFragment.class, args);
    }

    private StatusContent mStatusContent;

    @Override
    protected int inflateContentView() {
        return R.layout.ui_timeline_comment;
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        bindAdapter(getAdapter());
        getContentView().setBackgroundColor(Color.WHITE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mStatusContent = savedInstanceState != null ? (StatusContent) savedInstanceState.getSerializable("status")
                                                    : (StatusContent) getArguments().getSerializable("status");

        BaseActivity activity = (BaseActivity) getActivity();
        activity.getSupportActionBar().setTitle(R.string.cmts_title);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("status", mStatusContent);
    }

    @Override
    protected void setupRefreshView(Bundle savedInstanceSate) {
        super.setupRefreshView(savedInstanceSate);

        int color = getResources().getColor(R.color.divider_timeline_item);
        getRefreshView().addItemDecoration(new DefDividerItemView(color));
    }

    @Override
    protected IItemViewCreator<StatusComment> configFooterViewCreator() {
        return new NormalItemViewCreator<StatusComment>(BasicFooterView.LAYOUT_RES) {

            @Override
            public IITemView<StatusComment> newItemView(View convertView, int viewType) {
                return new BasicFooterView<StatusComment>(convertView, TimelineCommentFragment.this) {

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
        return new NormalItemViewCreator<StatusComment>(TimelineCommentItemView.LAYOUT_RES) {

            @Override
            public IITemView<StatusComment> newItemView(View convertView, int viewType) {
                return new TimelineCommentItemView(TimelineCommentFragment.this, convertView);
            }

        };
    }

    @Override
    protected AHeaderItemViewCreator<StatusComment> configHeaderViewCreator() {
        return new AHeaderItemViewCreator<StatusComment>() {

            @Override
            public int[][] setHeaderLayoutRes() {
                return new int[][]{ { CommentHeaderItemView.COMMENT_HEADER_01_RES, CommentHeaderItemView.COMMENT_HEADER_01 } };
            }

            @Override
            public IITemView<StatusComment> newItemView(View convertView, int viewType) {
                if (viewType == CommentHeaderItemView.COMMENT_HEADER_01) {
                    return new CommentHeaderItemView(TimelineCommentFragment.this, convertView, mStatusContent);
                }

                return null;
            }

        };
    }

    @Override
    protected IPaging<StatusComment, StatusComments> newPaging() {
        return new CommentPaging();
    }

    @Override
    protected void requestData(RefreshMode mode) {
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

    }

}
