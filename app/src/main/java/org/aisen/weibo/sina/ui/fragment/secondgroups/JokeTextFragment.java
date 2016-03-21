package org.aisen.weibo.sina.ui.fragment.secondgroups;

import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.adapter.ARecycleViewItemView;
import org.aisen.android.ui.fragment.itemview.IITemView;
import org.aisen.android.ui.fragment.itemview.IItemViewCreator;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.bean.JokeBean;
import org.aisen.weibo.sina.support.utils.ThemeUtils;

import java.util.Random;

/**
 * Created by wangdan on 16/3/14.
 */
public class JokeTextFragment extends JokeBaseFragment {

    public static JokeTextFragment newInstance() {
        return new JokeTextFragment();
    }

    @Override
    protected int setType() {

        return 0;
    }

    @Override
    public IItemViewCreator<JokeBean> configItemViewCreator() {
        return new IItemViewCreator<JokeBean>() {

            @Override
            public View newContentView(LayoutInflater inflater, ViewGroup parent, int viewType) {
                return inflater.inflate(R.layout.item_joke_text, parent, false);
            }

            @Override
            public IITemView<JokeBean> newItemView(View convertView, int viewType) {
                return new JokeTextItemView(convertView);
            }

        };
    }

    class JokeTextItemView extends ARecycleViewItemView<JokeBean> {

        @ViewInject(id = R.id.txtJoke)
        TextView txtJoke;

        public JokeTextItemView(View itemView) {
            super(itemView);
        }

        @Override
        public void onBindData(View convertView, JokeBean data, int position) {
            txtJoke.setText(data.getExcerpt());

            if (convertView instanceof CardView) {
                ((CardView) convertView).setCardBackgroundColor(getResources().getColor(ThemeUtils.themeColorArr[new Random().nextInt(ThemeUtils.themeColorArr.length)][0]));
            }
        }

    }

}
