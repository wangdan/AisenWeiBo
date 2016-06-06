package org.aisen.weibo.sina.ui.fragment.search;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.ARecycleViewFragment;
import org.aisen.android.ui.fragment.adapter.ARecycleViewItemView;
import org.aisen.android.ui.fragment.itemview.IItemViewCreator;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.sinasdk.bean.SuggestionsUser;

/**
 * 搜索用户
 *
 * Created by wangdan on 16/2/24.
 */
public class SearchUserFragment extends ARecycleViewFragment<SuggestionsUser, SuggestionsUser[]> {

    @Override
    protected void setupRefreshConfig(RefreshConfig config) {
        super.setupRefreshConfig(config);

        config.footerMoreEnable = false;
    }

    @Override
    public IItemViewCreator<SuggestionsUser> configItemViewCreator() {
        return null;
    }

    @Override
    public void requestData(RefreshMode mode) {
//        SinaSDK.ge
    }

    class SearchUserItemView extends ARecycleViewItemView<SuggestionsUser> {

        @ViewInject(id = R.id.imgPhoto)
        ImageView imgPhoto;
        @ViewInject(id = R.id.txtName)
        TextView txtName;
        @ViewInject(id = R.id.txtRemark)
        TextView txtRemark;

        public SearchUserItemView(View itemView) {
            super(getActivity(), itemView);
        }

        @Override
        public void onBindData(View convertView, SuggestionsUser data, int position) {
//            BitmapLoader.getInstance().display(SearchUserFragment.this,
//                    AisenUtils.getUserPhoto(data), imgPhoto, ImageConfigUtils.getLargePhotoConfig());
//            String name = data.getScreen_name();
//            if (!TextUtils.isEmpty(data.getRemark()))
//                name = String.format("%s(%s)", name, data.getRemark());
//            txtName.setText(name);
//            txtRemark.setVisibility(View.VISIBLE);
//            if (data.getStatus() != null)
//                txtRemark.setText(data.getStatus().getText());
//            else if (!TextUtils.isEmpty(data.getDescription()))
//                txtRemark.setText(data.getDescription());
//            else {
//                txtRemark.setVisibility(View.GONE);
//                txtRemark.setText("");
//            }
        }

    }

}
