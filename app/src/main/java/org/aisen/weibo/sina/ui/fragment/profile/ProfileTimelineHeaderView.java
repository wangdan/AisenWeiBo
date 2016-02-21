package org.aisen.weibo.sina.ui.fragment.profile;

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
 * Created by wangdan on 16/2/20.
 */
public class ProfileTimelineHeaderView extends ARecycleViewItemView<StatusContent> implements View.OnClickListener {

    public static final int LAYOUT_RES = R.layout.headerview_profile_timeline;

    private ProfileTimelineFragment fragment;

    @ViewInject(id = R.id.txtName)
    TextView txtTitle;

    public ProfileTimelineHeaderView(ProfileTimelineFragment fragment, View itemView) {
        super(itemView);

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

        String[] titles = fragment.getResources().getStringArray(R.array.user_headers);

        new AlertDialogWrapper.Builder(fragment.getActivity())
                .setTitle(R.string.profile_feature_dialog)
                .setNegativeButton(R.string.cancel, null)
                .setSingleChoiceItems(titles, Integer.parseInt(fragment.getFeature()), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Integer.parseInt(fragment.getFeature()) == which) {
                            dialog.dismiss();

                            return;
                        }

                        fragment.setFeature(String.valueOf(which));

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
        String[] titles = fragment.getResources().getStringArray(R.array.user_headers);
        txtTitle.setText(titles[Integer.parseInt(fragment.getFeature())]);
    }

}
