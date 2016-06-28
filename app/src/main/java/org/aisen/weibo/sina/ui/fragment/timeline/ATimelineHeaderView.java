package org.aisen.weibo.sina.ui.fragment.timeline;

import android.content.DialogInterface;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.APagingFragment;
import org.aisen.android.ui.fragment.adapter.ARecycleViewItemView;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;

/**
 * Created by wangdan on 16/4/3.
 */
public abstract class ATimelineHeaderView extends ARecycleViewItemView<StatusContent> implements View.OnClickListener {

    public static final int LAYOUT_RES = R.layout.headerview_profile_timeline;

    private ATimelineFragment fragment;

    public static final String[] timelineFeatureArr = { "0", "1", "2", "5" };

    public static final String[] profileFeatureArr = { "0", "1", "2" };

    @ViewInject(id = R.id.txtName)
    TextView txtTitle;

    public ATimelineHeaderView(ATimelineFragment fragment, View itemView) {
        super(fragment.getActivity(), itemView);

        this.fragment = fragment;

        itemView.setOnClickListener(this);
    }

    @Override
    public void onBindView(View convertView) {
        super.onBindView(convertView);

        setHeaderView();
    }

    @Override
    public void onBindData(View convertView, StatusContent data, int position) {

    }

    @Override
    public void onClick(View v) {
        if (fragment.isRefreshing())
            return;

        String[] titles = fragment.getResources().getStringArray(getTitleArrRes());

        new AlertDialogWrapper.Builder(fragment.getActivity())
                .setTitle(R.string.profile_feature_dialog)
                .setNegativeButton(R.string.cancel, null)
                .setSingleChoiceItems(titles, getFeaturePosition(), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (getFeaturePosition() == which) {
                            dialog.dismiss();

                            return;
                        }

                        fragment.setFeature(getTitleFeature()[which]);

                        // 清理线程状态，可以加载缓存
                        fragment.cleatTaskCount(APagingFragment.PAGING_TASK_ID);

                        setHeaderView();

                        fragment.requestDataDelaySetRefreshing(200);

                        dialog.dismiss();
                    }

                })
                .show();
    }

    private void setHeaderView() {
        String[] titles = fragment.getResources().getStringArray(getTitleArrRes());
        txtTitle.setText(titles[getFeaturePosition()]);
    }

    private int getFeaturePosition() {
        for (int i = 0; i < getTitleFeature().length; i++) {
            if (getTitleFeature()[i].equals(fragment.getFeature()))
                return i;
        }

        return 0;
    }

    abstract protected int getTitleArrRes();

    abstract protected String[] getTitleFeature();

}
