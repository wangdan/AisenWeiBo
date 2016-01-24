package org.aisen.weibo.sina.ui.fragment.timeline;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.aisen.android.common.utils.Utils;
import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.ATabsFragment;
import org.aisen.android.ui.fragment.adapter.ARecycleViewItemView;
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
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.support.utils.ImageConfigUtils;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;
import org.aisen.weibo.sina.ui.widget.AisenTextView;

/**
 * 某条原创微博的转发微博
 *
 * Created by wangdan on 16/1/22.
 */
public class TimelineRepostFragment extends ATimelineFragment implements ATabsFragment.ITabInitData {

    public static TimelineRepostFragment newInstance(StatusContent statusContent) {
        Bundle args = new Bundle();
        args.putSerializable("status", statusContent);

        TimelineRepostFragment fragment = new TimelineRepostFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private StatusContent statusContent;

    private BizFragment bizFragment;

    @Override
    public int inflateContentView() {
        return R.layout.ui_timeline_comment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        statusContent = savedInstanceState == null ? (StatusContent) getArguments().getSerializable("status")
                                                   : (StatusContent) savedInstanceState.getSerializable("status");

        bizFragment = BizFragment.createBizFragment(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("status", statusContent);
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        bindAdapter(getAdapter());
        getContentView().setBackgroundColor(Color.WHITE);
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
        return new NormalItemViewCreator<StatusContent>(R.layout.item_timeline_comment) {

            @Override
            public IITemView<StatusContent> newItemView(View convertView, int viewType) {
                return new RepostItemView(convertView);
            }

        };
    }

    @Override
    protected void requestData(RefreshMode mode) {
        boolean load = true;

        // 如果还没有加载过数据，切且显示的是当前的页面
        if (getTaskCount(PAGING_TASK_ID) == 0) {
            load = AisenUtils.checkTabsFragmentCanRequestData(this);
        }

        if (load)
            new RepostTask(mode).execute();
    }

    @Override
    public void onTabRequestData() {
        requestDataDelay(100);
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

    class RepostItemView extends ARecycleViewItemView<StatusContent> {

        @ViewInject(id = R.id.imgPhoto)
        ImageView imgPhoto;
        @ViewInject(id = R.id.txtName)
        TextView txtName;
        @ViewInject(id = R.id.txtDesc)
        TextView txtDesc;
        @ViewInject(id = R.id.txtContent)
        AisenTextView txtContent;

        int firstTop;
        int normalTop;

        public RepostItemView(View convertView) {
            super(convertView);

            firstTop = Utils.dip2px(16);
            normalTop = Utils.dip2px(8);
        }


        @Override
        public void onBindData(View convertView, StatusContent data, int position) {
            WeiBoUser user = data.getUser();
            if (user != null) {
                BitmapLoader.getInstance().display(TimelineRepostFragment.this,
                        AisenUtils.getUserPhoto(user),
                        imgPhoto, ImageConfigUtils.getLargePhotoConfig());
                bizFragment.userShow(imgPhoto, user);
                txtName.setText(AisenUtils.getUserScreenName(user));
            }
            else {
                bizFragment.userShow(imgPhoto, null);
                txtName.setText(R.string.error_cmts);
                imgPhoto.setImageResource(R.drawable.user_placeholder);
            }

            txtContent.setContent(data.getText());
            AisenUtils.setTextSize(txtContent);

            String createAt = AisenUtils.convDate(data.getCreated_at());
            String from = String.format("%s", Html.fromHtml(data.getSource()));
            String desc = String.format("%s %s", createAt, from);
            txtDesc.setText(desc);

            int top = position == 0 ? firstTop : normalTop;
            convertView.setPadding(convertView.getPaddingLeft(), top, convertView.getPaddingRight(), convertView.getPaddingBottom());
        }

    }

}
