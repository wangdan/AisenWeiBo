package org.aisen.weibo.sina.ui.fragment.menu;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.aisen.android.common.utils.ActivityHelper;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.common.utils.Utils;
import org.aisen.android.support.adapter.ABaseAdapter;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.AListFragment;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.sinasdk.bean.Group;
import org.aisen.weibo.sina.sinasdk.bean.Groups;

import java.util.ArrayList;

/**
 * 维护FabSheet的界面
 *
 * Created by wangdan on 16/1/2.
 */
public class FabGroupsFragment extends AListFragment<Group, Groups> {

    private int selectedPosition = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        selectedPosition = savedInstanceState == null ? ActivityHelper.getIntShareData(getPositionKey(), 0)
                                                      : savedInstanceState.getInt(getPositionKey(), 0);
    }

    @Override
    protected int inflateContentView() {
        return R.layout.ui_fab_groups;
    }

    @Override
    protected void configRefresh(RefreshConfig config) {
        super.configRefresh(config);

        config.footerMoreEnable = false;
    }

    @Override
    protected ABaseAdapter.AItemView<Group> newItemView() {
        return new FabGroupsItemView();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        super.onItemClick(parent, view, position, id);

        selectedPosition = position;
        getAdapter().notifyDataSetChanged();

        ActivityHelper.putIntShareData(getPositionKey(), position);

        if (getActivity() instanceof OnFabGroupSelectedCallback) {
            ((OnFabGroupSelectedCallback) getActivity()).onGroupSelected(position, getAdapterItems().get(position));
        }
    }

    @Override
    protected void requestData(RefreshMode mode) {

    }

    @Override
    public void setItems(ArrayList<Group> items) {
        super.setItems(items);

        // 设置列表的尺寸
        int width = Utils.dip2px(175);
        if (width > SystemUtils.getScreenWidth() / 2) {
            width = SystemUtils.getScreenWidth() / 2;
        }
        if (items.size() > 5) {
            getContentView().setLayoutParams(new FrameLayout.LayoutParams(width, Utils.dip2px(250)));
        }
        else {
            getContentView().setLayoutParams(new FrameLayout.LayoutParams(width, FrameLayout.LayoutParams.WRAP_CONTENT));
        }
    }

    public void show() {
        getRefreshView().setSelectionFromTop(selectedPosition, Utils.dip2px(175));
    }

    public void triggerLastPosition() {
        onItemClick(getRefreshView(), null, selectedPosition, 0);
    }

    class FabGroupsItemView extends ABaseAdapter.AItemView<Group> {

        @ViewInject(id = R.id.txtTitle)
        TextView txtTitle;

        @Override
        public int inflateViewId() {
            return R.layout.item_main_group;
        }

        @Override
        public void bindingData(View convertView, Group data, int position) {
            txtTitle.setText(data.getName());

            if (selectedPosition == position) {
                getConvertView().setBackgroundResource(R.drawable.abc_list_pressed_holo_light);
            }
            else {
                getConvertView().setBackgroundColor(getResources().getColor(R.color.comm_transparent));
            }
        }

    }

    private String getPositionKey() {
        return "FabGroupsPosition-" + AppContext.getAccount().getUid();
    }

    public interface OnFabGroupSelectedCallback {

        void onGroupSelected(int position, Group group);

    }

}
