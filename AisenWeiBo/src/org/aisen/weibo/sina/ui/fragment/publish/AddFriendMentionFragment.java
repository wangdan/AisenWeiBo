package org.aisen.weibo.sina.ui.fragment.publish;

import com.m.component.container.FragmentContainerActivity;
import com.m.support.adapter.ABaseAdapter;
import com.m.ui.fragment.ABaseFragment;
import com.m.ui.fragment.AListFragment;
import com.m.ui.fragment.ARefreshFragment;

import org.sina.android.bean.Friendship;
import org.sina.android.bean.WeiBoUser;

/**
 * Created by wangdan on 15/4/25.
 */
public class AddFriendMentionFragment extends AListFragment<WeiBoUser, Friendship> {

    public static void launch(ABaseFragment from, int requestCode) {
        FragmentContainerActivity.launchForResult(from, AddFriendMentionFragment.class, null, requestCode);
    }

    @Override
    protected ABaseAdapter.AbstractItemView<WeiBoUser> newItemView() {
        return null;
    }

    @Override
    protected void requestData(RefreshMode mode) {

    }

}
