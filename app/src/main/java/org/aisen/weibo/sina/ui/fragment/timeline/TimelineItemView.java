package org.aisen.weibo.sina.ui.fragment.timeline;

import android.view.View;
import android.widget.TextView;

import org.aisen.android.support.adapter.ABaseAdapter;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;

/**
 * Created by wangdan on 16/1/4.
 */
public class TimelineItemView extends ABaseAdapter.AItemView<StatusContent> {

    @ViewInject(id = R.id.txtContent)
    TextView txtContent;

    @Override
    public int inflateViewId() {
        return R.layout.item_timeline;
    }

    @Override
    public void bindingData(View convertView, StatusContent data, int position) {
        txtContent.setText(data.getText());
    }

}
