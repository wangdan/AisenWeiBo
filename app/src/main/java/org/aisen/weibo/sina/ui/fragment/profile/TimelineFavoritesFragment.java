package org.aisen.weibo.sina.ui.fragment.profile;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.bean.TabItem;
import org.aisen.android.support.paging.IPaging;
import org.aisen.android.support.paging.PageIndexPaging;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.activity.container.FragmentArgs;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.android.ui.fragment.ATabsFragment;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.Favorities;
import org.aisen.weibo.sina.sinasdk.bean.Favority;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;
import org.aisen.weibo.sina.support.cache.FavoritesCacheUtility;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.ui.activity.base.SinaCommonActivity;
import org.aisen.weibo.sina.ui.fragment.timeline.ATimelineFragment;

import java.util.ArrayList;

/**
 * 用户收藏微博
 * 
 * @author Jeff.Wang
 *
 * @date 2014年9月18日
 */
public class TimelineFavoritesFragment extends ATimelineFragment
                                        implements ATabsFragment.ITabInitData, ProfilePagerFragment.IUserProfileRefresh {

    public static ABaseFragment newInstance() {
        TimelineFavoritesFragment fragment = new TimelineFavoritesFragment();

        TabItem bean = new TabItem();
        bean.setTag("7");
        bean.setTitle(GlobalContext.getInstance().getResources().getString(R.string.draw_fav_title));
        bean.setType("7");

        Bundle args = new Bundle();
        args.putSerializable("bean", bean);
        fragment.setArguments(args);

        return fragment;
    }
    
    public static void launch(Activity from) {
        TabItem bean = new TabItem();
        bean.setTag("7");
        bean.setTitle(GlobalContext.getInstance().getResources().getString(R.string.draw_fav_title));
        bean.setType("7");

        FragmentArgs args = new FragmentArgs();
        args.add("bean", bean);
        args.add("launch", true);
        
        SinaCommonActivity.launch(from, TimelineFavoritesFragment.class, args);
    }

    private boolean launch = false;

    @Override
    public void onCreate(Bundle savedInstanceSate) {
        super.onCreate(savedInstanceSate);

        launch = savedInstanceSate == null ? getArguments().getBoolean("launch", false)
                                           : savedInstanceSate.getBoolean("launch");
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
    	super.layoutInit(inflater, savedInstanceSate);
        if (launch) {
            BaseActivity activity = (BaseActivity) getActivity();
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setTitle(R.string.myfav_title);
        }

        setViewVisiable(getLoadingLayout(), View.VISIBLE);
        setViewVisiable(getEmptyLayout(), View.GONE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("launch", launch);
    }

    @Override
    public void onTabRequestData() {
        // 如果还没有加载过数据，就开始加载
        if (getTaskCount(PAGING_TASK_ID) == 0) {
            requestData(RefreshMode.reset);
        }
    }

    @Override
    public void requestData(RefreshMode mode) {
        boolean load = true;

        // 如果还没有加载过数据，切且显示的是当前的页面
        if (getTaskCount(PAGING_TASK_ID) == 0) {
            load = AisenUtils.checkTabsFragmentCanRequestData(this);
        }

        if (load)
            new FavoritesTask(mode == RefreshMode.refresh ? RefreshMode.reset : mode).execute();
    }

    @Override
    protected IPaging<StatusContent, StatusContents> newPaging() {
        return new PageIndexPaging<StatusContent, StatusContents>("total_number");
    }

    class FavoritesTask extends ATimelineTask {

        FavoritesTask(RefreshMode mode) {
            super(mode);
        }

        @Override
        protected StatusContents workInBackground(RefreshMode mode, String previousPage, String nextPage, Void... p) throws TaskException {
            Favorities favorities = SinaSDK.getInstance(AppContext.getAccount().getAdvancedToken(), getTaskCacheMode(this)).favorites(nextPage, String.valueOf(AppSettings.getTimelineCount()));

            StatusContents statusContents = new StatusContents(new ArrayList<StatusContent>());
            for (Favority favority : favorities.getFavorites()) {
                statusContents.getStatuses().add(favority.getStatus());
            }
            statusContents.setFromCache(favorities.isFromCache());
            statusContents.setOutofdate(favorities.isOutofdate());

            // 如果是重置数据，就判断新加载的数据是否是全部加载完了
            int total = mode == RefreshMode.reset ? 0 : getAdapter().getDatas().size();
            total += favorities.getFavorites().size();
            if (total >= favorities.getTotal_number())
                statusContents.setEndPaging(true);

            return statusContents;
        }

        @Override
        public StatusContents getStatusContents(Params params) throws TaskException {
            return null;
        }

    }

    @Override
    public void onResume() {
    	super.onResume();
    	
    	IntentFilter filter = new IntentFilter();
    	filter.addAction("org.aisen.weibo.sina.FAV_DESTORY");
    	getActivity().registerReceiver(receiver, filter);
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	
    	getActivity().unregisterReceiver(receiver);
    }
    
    // 接收来自BizFragment的删除(溢出菜单)
    private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null && "org.aisen.weibo.sina.FAV_DESTORY".equalsIgnoreCase(intent.getAction())
                    && intent.getExtras() != null) {
				String statusId = intent.getExtras().getString("statusId");
				
				destoryFav(statusId);
			}
		}
    	
    };
    
    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		// 详情页面的取消收藏
		if (resultCode == Activity.RESULT_OK && requestCode == 1000) {
			String statusId = data.getStringExtra("statusId");
			destoryFav(statusId);
		}
	}
    
    private void destoryFav(String statusId) {
    	for (StatusContent status : getAdapterItems()) {
    		if (String.valueOf(status.getId()).equals(String.valueOf(statusId))) {
    			// 更新ListView
    			getAdapterItems().remove(status);
    			getAdapter().notifyDataSetChanged();
    			
    			FavoritesCacheUtility.destory(statusId);
    			break;
    		}
    	}
    }

    @Override
    public void refreshProfile() {
        requestDataDelay(100);
    }

    @Override
    public boolean onToolbarDoubleClick() {
        if (AisenUtils.checkTabsFragmentCanRequestData(this)) {
            requestDataDelaySetRefreshing(AppSettings.REQUEST_DATA_DELAY);
            getRefreshView().scrollToPosition(0);

            return true;
        }

        return false;
    }
    
}
