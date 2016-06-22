package org.aisen.weibo.sina.ui.fragment.menu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.ActivityHelper;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.common.utils.Utils;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.AListFragment;
import org.aisen.android.ui.fragment.adapter.ABasicItemView;
import org.aisen.android.ui.fragment.itemview.IITemView;
import org.aisen.android.ui.fragment.itemview.IItemViewCreator;
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

        selectedPosition = savedInstanceState == null ? ActivityHelper.getIntShareData(GlobalContext.getInstance(), getPositionKey(), 0)
                                                      : savedInstanceState.getInt(getPositionKey(), 0);
    }

    public void resetSelectedPosition() {
        selectedPosition = ActivityHelper.getIntShareData(GlobalContext.getInstance(), getPositionKey(), 0);
    }

    @Override
    public int inflateContentView() {
        return R.layout.ui_fab_groups;
    }

    @Override
    protected void setupRefreshConfig(RefreshConfig config) {
        super.setupRefreshConfig(config);

        config.footerMoreEnable = false;
    }

    @Override
    public IItemViewCreator<Group> configItemViewCreator() {
        return new IItemViewCreator<Group>() {

            @Override
            public View newContentView(LayoutInflater inflater, ViewGroup parent, int viewType) {
                return inflater.inflate(R.layout.item_main_group, parent, false);
            }

            @Override
            public IITemView<Group> newItemView(View convertView, int viewType) {
                return new FabGroupsItemView(convertView);
            }

        };
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        super.onItemClick(parent, view, position, id);

        selectedPosition = position;
        getAdapter().notifyDataSetChanged();

        ActivityHelper.putIntShareData(GlobalContext.getInstance(), getPositionKey(), position);

        if (getActivity() instanceof OnFabGroupSelectedCallback) {
            ((OnFabGroupSelectedCallback) getActivity()).onGroupSelected(position, getAdapterItems().get(position));
        }
    }

    @Override
    public void requestData(RefreshMode mode) {

    }

    @Override
    public void setItems(ArrayList<Group> items) {
        super.setItems(items);

        // 设置列表的尺寸
        int width = Utils.dip2px(getActivity(), 175);
        if (width > SystemUtils.getScreenWidth(getActivity()) / 2) {
            width = SystemUtils.getScreenWidth(getActivity()) / 2;
        }
        if (items.size() > 7) {
            getContentView().setLayoutParams(new FrameLayout.LayoutParams(width, Utils.dip2px(getActivity(), 340)));
        }
        else {
            getContentView().setLayoutParams(new FrameLayout.LayoutParams(width, FrameLayout.LayoutParams.WRAP_CONTENT));
        }
    }

    public void show() {
        int top = 0;
        if (getRefreshView().getChildCount() > 0 && getRefreshView().getChildAt(0).getHeight() > 0) {
            top = getRefreshView().getChildAt(0).getHeight() * 3;
        }
        else {
            top = Utils.dip2px(getActivity(), 175);
        }
        getRefreshView().setSelectionFromTop(selectedPosition, top);
    }

    public void triggerLastPosition() {
        if (selectedPosition >= getAdapterItems().size()) {
            selectedPosition = 0;
        }

        onItemClick(getRefreshView(), null, selectedPosition, 0);
    }

    class FabGroupsItemView extends ABasicItemView<Group> {

        @ViewInject(id = R.id.txtTitle)
        TextView txtTitle;

        public FabGroupsItemView(View convertView) {
            super(getActivity(), convertView);
        }


        @Override
        public void onBindData(View convertView, Group data, int position) {
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
