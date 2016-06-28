package org.aisen.weibo.sina.ui.fragment.search;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import org.aisen.android.ui.fragment.itemview.IItemViewCreator;
import org.aisen.android.ui.widget.MDButton;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.sinasdk.bean.SearchsResultUser;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;
import org.aisen.weibo.sina.support.utils.ImageConfigUtils;
import org.aisen.weibo.sina.ui.activity.profile.UserProfileActivity;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;

import java.util.ArrayList;

/**
 * Created by wangdan on 16/2/24.
 */
public class SearchHeaderView extends ARecycleViewItemView<StatusContent> implements View.OnClickListener {

    @ViewInject(id = R.id.layUsers)
    LinearLayout layUsers;
    @ViewInject(id = R.id.txtEmpty)
    TextView txtEmpty;
    @ViewInject(id = R.id.recycleview)
    RecyclerView mRecycleView;

    private APagingFragment fragment;
    private BasicRecycleViewAdapter<SearchsResultUser> basicRecycleViewAdapter;

    public SearchHeaderView(APagingFragment fragment, View itemView) {
        super(fragment.getActivity(), itemView);

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

    @Override
    public void onClick(View v) {
        final SearchsResultUser data = (SearchsResultUser) v.getTag();
        WeiBoUser user = new WeiBoUser();
        user.setId(data.getId());
        user.setIdstr(data.getId());

        if (data.isFollowing()) {
            BizFragment.createBizFragment(fragment).destoryFriendship(user, new BizFragment.OnDestoryFriendshipCallback() {

                @Override
                public void onFriendshipDestoryed(WeiBoUser targetUser) {
                    data.setFollowing(false);

                    basicRecycleViewAdapter.notifyDataSetChanged();
                }

            });
        }
        else {
            BizFragment.createBizFragment(fragment).createFriendship(user, new BizFragment.OnCreateFriendshipCallback() {

                @Override
                public void onFriendshipCreated(WeiBoUser targetUser) {
                    data.setFollowing(true);

                    basicRecycleViewAdapter.notifyDataSetChanged();
                }

            });
        }
    }

    class HeaderItemCreator implements IItemViewCreator<SearchsResultUser> {

        @Override
        public View newContentView(LayoutInflater inflater, ViewGroup parent, int viewType) {
            return inflater.inflate(R.layout.item_search_headerview, parent, false);
        }

        @Override
        public IITemView<SearchsResultUser> newItemView(View convertView, int viewType) {
            return new ARecycleViewItemView<SearchsResultUser>(fragment.getActivity(), convertView) {

                @ViewInject(id = R.id.imgPhoto)
                ImageView imgPhoto;
                @ViewInject(id = R.id.txtName)
                TextView txtName;
                @ViewInject(id = R.id.txtRemark)
                TextView txtRemark;
                @ViewInject(id = R.id.btn)
                MDButton btn;

                @Override
                public void onBindData(View convertView, SearchsResultUser data, int position) {
                    RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) convertView.getLayoutParams();
//                    params.leftMargin = position == 0 || position == 1 ? 0 : Utils.dip2px(8);
//                    params.topMargin = position % 2 == 0 ? 0 : Utils.dip2px(8);
                    params.width = SystemUtils.getScreenWidth(getContext()) * 5 / 6;
                    params.height = Utils.dip2px(getContext(), 110);

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
                    if (data.isFollow_me() && data.isFollowing()) {
                        btn.setText(R.string.profile_friendship_each);
                    }
                    else if (data.isFollowing()) {
                        btn.setText(R.string.profile_friendship_destory);
                    }
                    else {
                        btn.setText(R.string.profile_friendship_create);
                    }
                    btn.setTag(data);
                    btn.setOnClickListener(SearchHeaderView.this);
                }

            };
        }

    }

}
