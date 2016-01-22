package org.aisen.weibo.sina.ui.fragment.timeline;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.aisen.android.common.utils.Utils;
import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.android.ui.fragment.itemview.DefDividerItemView;
import org.aisen.android.ui.fragment.itemview.IITemView;
import org.aisen.android.ui.fragment.itemview.IItemViewCreator;
import org.aisen.android.ui.fragment.itemview.NormalItemViewCreator;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;
import org.aisen.weibo.sina.sinasdk.bean.StatusRepost;

/**
 * 某条原创微博的转发微博
 *
 * Created by wangdan on 16/1/22.
 */
public class TimelineRepostFragment extends ATimelineFragment {

    public static TimelineRepostFragment newInstance(StatusContent statusContent) {
        Bundle args = new Bundle();
        args.putSerializable("status", statusContent);

        TimelineRepostFragment fragment = new TimelineRepostFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private StatusContent statusContent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        statusContent = savedInstanceState == null ? (StatusContent) getArguments().getSerializable("status")
                                                   : (StatusContent) savedInstanceState.getSerializable("status");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("status", statusContent);
    }

    @Override
    protected void setupRefreshView(Bundle savedInstanceSate) {
        super.setupRefreshView(savedInstanceSate);

        int color = getResources().getColor(R.color.divider_timeline_item);
        getRefreshView().addItemDecoration(new DefDividerItemView(color));
        getRefreshView().setBackgroundColor(Color.WHITE);
    }

    @Override
    public IItemViewCreator<StatusContent> configItemViewCreator() {
        return new NormalItemViewCreator<StatusContent>(TimelineItemView.LAYOUT_RES) {

            @Override
            public IITemView<StatusContent> newItemView(View convertView, int viewType) {
                return new RepostItemView(convertView, TimelineRepostFragment.this);
            }

        };
    }

    @Override
    protected void requestData(RefreshMode mode) {
        new RepostTask(mode).execute();
    }

    class RepostTask extends ATimelineTask {

        public RepostTask(RefreshMode mode) {
            super(mode);
        }

        @Override
        public StatusContents getStatusContents(Params params) throws TaskException {
            String statusId = statusContent.getId() + "";
            params.addParameter("id", statusId);

            StatusRepost statusRepost = SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).statusRepostTimeline(params);
            if (statusRepost != null) {
                for (StatusContent status : statusRepost.getReposts()) {
                    status.setRetweeted_status(null);
                }

                return new StatusContents(statusRepost.getReposts());
            }

            throw new TaskException(TaskException.TaskError.resultIllegal.toString());
        }

    }

    class RepostItemView extends TimelineItemView {

        int paddingH;
        int paddingV;
        int menuPadding;

        public RepostItemView(View convertView, ABaseFragment fragment) {
            super(convertView, fragment);

            paddingH = Utils.dip2px(18);
            paddingV = Utils.dip2px(16);
            menuPadding = Utils.dip2px(10);
        }

        @Override
        public void onBindData(View convertView, StatusContent data, int position) {
            super.onBindData(convertView, data, position);

            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) cardView.getLayoutParams();
            lp.setMargins(0, 0, 0, 0);
            cardView.setLayoutParams(lp);
//            cardView.getChildAt(0).setPadding(paddingH, paddingV, paddingH, 0);
            cardView.setRadius(0);
            cardView.setCardElevation(0);
        }
    }

}
