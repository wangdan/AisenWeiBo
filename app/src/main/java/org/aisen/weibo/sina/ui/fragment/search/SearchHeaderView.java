package org.aisen.weibo.sina.ui.fragment.search;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.adapter.ARecycleViewItemView;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.sinasdk.bean.SearchsResultUser;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.support.utils.ImageConfigUtils;
import org.aisen.weibo.sina.ui.widget.swipecardview.SwipeFlingAdapterView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangdan on 16/2/24.
 */
public class SearchHeaderView extends ARecycleViewItemView<StatusContent> implements SwipeFlingAdapterView.onFlingListener, SwipeFlingAdapterView.OnItemClickListener {

    @ViewInject(id = R.id.layUsers)
    LinearLayout layUsers;
    @ViewInject(id = R.id.txtEmpty)
    TextView txtEmpty;
    @ViewInject(id = R.id.swipeView)
    SwipeFlingAdapterView swipeView;

    private InnerAdapter adapter;
    private List<SearchsResultUser> users;
    private Context context;

    public SearchHeaderView(View itemView, Context context) {
        super(itemView);

        this.context = context;
    }

    @Override
    public void onBindView(View convertView) {
        super.onBindView(convertView);

        //swipeView.setIsNeedSwipe(true);
        swipeView.setFlingListener(this);
        swipeView.setOnItemClickListener(this);

        users = new ArrayList<>();
        adapter = new InnerAdapter();
        swipeView.setAdapter(adapter);
    }

    @Override
    public void onBindData(View convertView, StatusContent data, int position) {

    }

    public void setUsers(List<SearchsResultUser> users) {
        this.users = users;

        layUsers.setVisibility(users.size() == 0 ? View.GONE : View.VISIBLE);
        txtEmpty.setVisibility(users.size() == 0 ? View.VISIBLE : View.GONE);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClicked(MotionEvent event, View v, Object dataObject) {

    }

    @Override
    public void removeFirstObjectInAdapter() {

    }

    @Override
    public void onLeftCardExit(Object dataObject) {

    }

    @Override
    public void onRightCardExit(Object dataObject) {

    }

    @Override
    public void onAdapterAboutToEmpty(int itemsInAdapter) {

    }

    @Override
    public void onScroll(float progress, float scrollXProgress) {

    }

    private class InnerAdapter extends BaseAdapter {

        public boolean isEmpty() {
            return users.isEmpty();
        }

        public void remove(int index) {
            if (index > -1 && index < users.size()) {
                users.remove(index);
                notifyDataSetChanged();
            }
        }

        @Override
        public int getCount() {
            return users.size();
        }

        @Override
        public SearchsResultUser getItem(int position) {
            if(users == null || users.size()==0) return null;
            return users.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_search_headerview, parent, false);
            }
            convertView.getLayoutParams().width = SystemUtils.getScreenWidth() - 300;
            convertView.getLayoutParams().height = 200;
            SearchsResultUser user = users.get(position);

            ImageView imgPhoto = (ImageView) convertView.findViewById(R.id.imgPhoto);
            BitmapLoader.getInstance().display(null,
                    user.getProfile_image_url(), imgPhoto, ImageConfigUtils.getLargePhotoConfig());
            String name = user.getScreen_name();
            if (!TextUtils.isEmpty(user.getRemark()))
                name = String.format("%s(%s)", name, user.getRemark());
            TextView txtName = (TextView) convertView.findViewById(R.id.txtName);
            txtName.setText(name);
            TextView txtRemark = (TextView) convertView.findViewById(R.id.txtRemark);
            txtRemark.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(user.getDesc1()))
                txtRemark.setText(user.getDesc1());
            else if (!TextUtils.isEmpty(user.getDescription()))
                txtRemark.setText(user.getDescription());
            else {
                txtRemark.setVisibility(View.GONE);
                txtRemark.setText("");
            }

            return convertView;
        }

    }

}
