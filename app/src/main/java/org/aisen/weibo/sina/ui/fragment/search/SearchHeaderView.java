package org.aisen.weibo.sina.ui.fragment.search;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.common.utils.Utils;
import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.APagingFragment;
import org.aisen.android.ui.fragment.adapter.ARecycleViewItemView;
import org.aisen.android.ui.fragment.adapter.BasicRecycleViewAdapter;
import org.aisen.android.ui.fragment.itemview.IITemView;
import org.aisen.android.ui.fragment.itemview.NormalItemViewCreator;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.sinasdk.bean.SearchsResultUser;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.support.utils.ImageConfigUtils;
import org.aisen.weibo.sina.ui.activity.profile.UserProfileActivity;

import java.util.ArrayList;

/**
 * Created by wangdan on 16/2/24.
 */
public class SearchHeaderView extends ARecycleViewItemView<StatusContent> {

    @ViewInject(id = R.id.layUsers)
    LinearLayout layUsers;
    @ViewInject(id = R.id.txtEmpty)
    TextView txtEmpty;
    @ViewInject(id = R.id.recycleview)
    RecyclerView mRecycleView;

    private APagingFragment fragment;
    private BasicRecycleViewAdapter<SearchsResultUser> basicRecycleViewAdapter;

    public SearchHeaderView(APagingFragment fragment, View itemView) {
        super(itemView);

        this.fragment = fragment;
    }

    @Override
    public void onBindView(View convertView) {
        super.onBindView(convertView);

        GridLayoutManager linearLayoutManager = new GridLayoutManager(fragment.getActivity(), 2, LinearLayoutManager.HORIZONTAL, false);
        mRecycleView.setLayoutManager(linearLayoutManager);
        basicRecycleViewAdapter = new BasicRecycleViewAdapter(fragment, new HeaderItemCreator(), new ArrayList<>());
        basicRecycleViewAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UserProfileActivity.launch(fragment.getActivity(), basicRecycleViewAdapter.getDatas().get(position).getScreen_name());
            }

        });
        mRecycleView.setAdapter(basicRecycleViewAdapter);
    }

    @Override
    public void onBindData(View convertView, StatusContent data, int position) {

    }

    public void setUsers(ArrayList<SearchsResultUser> users) {
        layUsers.setVisibility(users.size() == 0 ? View.GONE : View.VISIBLE);
        txtEmpty.setVisibility(users.size() == 0 ? View.VISIBLE : View.GONE);
        basicRecycleViewAdapter.getDatas().clear();
        basicRecycleViewAdapter.getDatas().addAll(users);
        basicRecycleViewAdapter.notifyDataSetChanged();
    }

    class HeaderItemCreator extends NormalItemViewCreator<SearchsResultUser> {

        public HeaderItemCreator() {
            super(R.layout.item_search_headerview);
        }

        @Override
        public IITemView<SearchsResultUser> newItemView(View convertView, int viewType) {
            return new ARecycleViewItemView<SearchsResultUser>(convertView) {

                @ViewInject(id = R.id.imgPhoto)
                ImageView imgPhoto;
                @ViewInject(id = R.id.txtName)
                TextView txtName;
                @ViewInject(id = R.id.txtRemark)
                TextView txtRemark;

                @Override
                public void onBindView(View convertView) {
                    super.onBindView(convertView);

                    convertView.getLayoutParams().width = SystemUtils.getScreenWidth() * 5 / 6;
                    convertView.getLayoutParams().height = Utils.dip2px(110);
                }

                @Override
                public void onBindData(View convertView, SearchsResultUser data, int position) {
                    RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) convertView.getLayoutParams();
                    params.leftMargin = position == 0 || position == 1 ? 0 : Utils.dip2px(8);
                    params.topMargin = position % 2 == 0 ? 0 : Utils.dip2px(8);

                    BitmapLoader.getInstance().display(fragment, data.getProfile_image_url(), imgPhoto, ImageConfigUtils.getLargePhotoConfig());
                    String name = data.getScreen_name();
                    if (!TextUtils.isEmpty(data.getRemark()))
                        name = String.format("%s(%s)", name, data.getRemark());
                    txtName.setText(name);
                    txtRemark.setVisibility(View.VISIBLE);
                    if (!TextUtils.isEmpty(data.getDesc1()))
                        txtRemark.setText(data.getDesc1());
                    else if (!TextUtils.isEmpty(data.getDesc2()))
                        txtRemark.setText(data.getDesc2());
                    else if (!TextUtils.isEmpty(data.getDescription()))
                        txtRemark.setText(data.getDescription());
                    else {
                        txtRemark.setVisibility(View.GONE);
                        txtRemark.setText("");
                    }
                }

            };
        }

    }

}
