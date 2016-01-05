package org.aisen.weibo.sina.ui.fragment.timeline;

import android.view.View;
import android.widget.TextView;

import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.adapter.ABasicItemView;
import org.aisen.android.ui.fragment.adapter.ARecycleViewItemView;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;

/**
 * Created by wangdan on 16/1/4.
 */
public class TimelineItemView extends ARecycleViewItemView<StatusContent> {

    public static final int LAYOUT_RES = R.layout.item_timeline;

    @ViewInject(id = R.id.txtContent)
    TextView txtContent;

    public TimelineItemView(View convertView) {
        super(convertView);
    }

    @Override
    public void onBindData(View convertView, StatusContent data, int position) {
        txtContent.setText(data.getText());
    }

}
