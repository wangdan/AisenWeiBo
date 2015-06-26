package org.aisen.weibo.sina.ui.fragment.friendship;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;

import org.aisen.android.component.container.FragmentArgs;
import org.aisen.android.component.container.FragmentContainerActivity;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.AAutoReleaseStripTabsFragment;
import org.aisen.android.ui.fragment.ABaseFragment;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.ui.activity.basic.MainActivity;
import org.aisen.weibo.sina.ui.fragment.basic.AMainStripTabsFragment;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;

import java.util.ArrayList;

/**
 * 首页朋友关系Pager
 * 
 * @author wangdan
 *
 */
public class FriendshipTabsFragment extends AAutoReleaseStripTabsFragment {

	/**
	 * 用户关系 
	 * 
	 * @param from
	 * @param user
	 * @param type 1:粉丝 0:关注 2:互粉
	 */
	public static void launch(Activity from, WeiBoUser user, int type) {
		FragmentArgs args = new FragmentArgs();
		args.add("user", user);
		args.add(SET_INDEX, type);
		
		FragmentContainerActivity.launch(from, FriendshipTabsFragment.class, args);
	}
	
	public static ABaseFragment newInstance() {
		FriendshipTabsFragment fragment = new FriendshipTabsFragment();
		
		Bundle args = new Bundle();
		args.putSerializable("user", AppContext.getUser());
		// 如果有新粉丝，默认选择粉丝页面
		int type =  AppContext.getUnreadCount().getFollower() > 0 ? 1 : 0;
		args.putInt(SET_INDEX, type);
		fragment.setArguments(args);
		
		return fragment;
	}
	
	private WeiBoUser mUser;
	private int type;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mUser = savedInstanceState == null ? (WeiBoUser) getArguments().getSerializable("user")
                                           : (WeiBoUser) savedInstanceState.getSerializable("user");
        type = savedInstanceState == null ? getArguments().getInt("index", type)
                                          : savedInstanceState.getInt("index");

        super.onCreate(savedInstanceState);
    }

    @Override
    protected int inflateContentView() {
        if (getActivity() instanceof MainActivity)
            return R.layout.as_ui_main_tabs;

        return super.inflateContentView();
    }

    @Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		super.layoutInit(inflater, savedInstanceSate);

		if (getActivity() instanceof MainActivity) {
		}
		else {
			BaseActivity activity = (BaseActivity) getActivity();
			activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			activity.getSupportActionBar().setTitle(AisenUtils.getUserScreenName(mUser));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putSerializable("user", mUser);
		outState.putInt("index", type);
	}

    @Override
    protected ArrayList<StripTabItem> generateTabs() {
        ArrayList<StripTabItem> beans = new ArrayList<StripTabItem>();

        beans.add(new StripTabItem("300", getString(R.string.friends)));
        beans.add(new StripTabItem("301", getString(R.string.followers)));
        // 是当前授权用户时，显示互粉
        if (mUser.getIdstr().equals(AppContext.getUser().getIdstr()))
            beans.add(new StripTabItem("302", getString(R.string.bilateral)));

        return beans;
    }

	@Override
	protected ABaseFragment newFragment(StripTabItem bean) {
		switch (Integer.parseInt(bean.getType())) {
		case 300:
			return FriendsFragment.newInstance(mUser);
		case 301:
			return FollowersFragment.newInstance(mUser);
		case 302:
			return BilateralFragment.newInstance(mUser);
		}
		
		return null;
	}

    @Override
    public void onPageSelected(int position) {
        super.onPageSelected(position);

        if (getActivity() instanceof MainActivity)
            ((MainActivity) getActivity()).toggleToolbarShown(true);
    }

}
