package org.aisen.weibo.sina.ui.fragment.timeline;

import android.view.View;
import android.widget.TextView;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.android.ui.fragment.adapter.ARecycleViewItemView;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;
import org.aisen.weibo.sina.ui.widget.TimelinePicsView;

/**
 * Created by wangdan on 16/4/3.
 */
public class TimelinePhotosItemView extends ARecycleViewItemView<StatusContent> implements View.OnClickListener {

    public static final int LAYOUT_RES = R.layout.item_timeline_photos;

    @ViewInject(id = R.id.txtName)
    TextView txtName;
    @ViewInject(id = R.id.txtDesc)
    TextView txtDesc;
    @ViewInject(id = R.id.layPicturs)
    TimelinePicsView layPicturs;

    private ABaseFragment fragment;
    private BizFragment bizFragment;

    private StatusContent data;

    public TimelinePhotosItemView(View itemView, ABaseFragment fragment) {
        super(fragment.getActivity(), itemView);

        this.fragment = fragment;
        bizFragment = BizFragment.createBizFragment(fragment);
    }

    @Override
    public void onBindData(View convertView, StatusContent data, int position) {
        this.data = data;

        // username
        if (data.getUser() != null) {
            txtName.setText(AisenUtils.getUserScreenName(data.getUser()));
        }
        txtName.setOnClickListener(this);
        // 转自
        if (data.getRetweeted_status() != null) {
            txtDesc.setVisibility(View.VISIBLE);
            if (data.getRetweeted_status().getUser() != null) {
                txtDesc.setText(String.format(GlobalContext.getInstance().getResources().getString(R.string.timeline_feature_repost), AisenUtils.getUserScreenName(data.getRetweeted_status().getUser())));
            }
        }
        else {
            txtDesc.setVisibility(View.GONE);
        }
        // pictures
        StatusContent s = data.getRetweeted_status() != null ? data.getRetweeted_status() : data;
        layPicturs.setPics(s, fragment);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.txtName) {
            if (data.getUser() != null)
                bizFragment.launchProfile(data.getUser());
        }
    }

}
