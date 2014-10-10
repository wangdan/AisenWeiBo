package org.aisen.weibo.sina.ui.fragment.timeline;

import java.util.ArrayList;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.bean.TimelineGroupBean;
import org.aisen.weibo.sina.support.cache.FavoritesCacheUtility;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.AppSettings;
import org.aisen.weibo.sina.support.utils.BaiduAnalyzeUtils;
import org.sina.android.SinaSDK;
import org.sina.android.bean.Favorities;
import org.sina.android.bean.Favority;
import org.sina.android.bean.StatusContent;
import org.sina.android.bean.StatusContents;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.m.common.context.GlobalContext;
import com.m.support.paging.IPaging;
import com.m.support.paging.PageIndexPaging;
import com.m.support.task.TaskException;
import com.m.ui.fragment.ABaseFragment;

/**
 * 用户收藏微博
 * 
 * @author Jeff.Wang
 *
 * @date 2014年9月18日
 */
public class FavoritesFragment extends ATimelineFragment {

    public static ABaseFragment newInstance() {
        FavoritesFragment fragment = new FavoritesFragment();

        TimelineGroupBean bean = new TimelineGroupBean();
        bean.setGroup("7");
        bean.setTitle(GlobalContext.getInstance().getResources().getString(R.string.draw_fav_title));
        bean.setType("7");

        Bundle args = new Bundle();
        args.putSerializable("bean", bean);
        fragment.setArguments(args);

        return fragment;
    }
    
    @Override
    protected void config(RefreshConfig config) {
    	super.config(config);
    	
    	config.savePosition = false;
    }

    @Override
    protected void requestData(RefreshMode mode) {
        new FavoritesTask(mode == RefreshMode.refresh ? RefreshMode.reset : mode).execute();
    }

    @Override
    protected IPaging<StatusContent, StatusContents> configPaging() {
        return new PageIndexPaging<StatusContent, StatusContents>("total_number");
    }

    class FavoritesTask extends TimelineTask {

        FavoritesTask(RefreshMode mode) {
            super(mode);
        }

        @Override
        protected StatusContents workInBackground(RefreshMode mode, String previousPage, String nextPage, Void... params) throws TaskException {
            Favorities favorities = SinaSDK.getInstance(AppContext.getToken(), getTaskCacheMode(this)).favorites(nextPage, String.valueOf(AppSettings.getTimelineCount()));

            StatusContents statusContents = new StatusContents(new ArrayList<StatusContent>());
            for (Favority favority : favorities.getFavorites()) {
                statusContents.getStatuses().add(favority.getStatus());
            }
            statusContents.setCache(favorities.isCache());
            statusContents.setExpired(favorities.expired());
            
            // 如果是重置数据，就判断新加载的数据是否是全部加载完了
            int total = mode == RefreshMode.reset ? 0 : getAdapter().getCount();
            total += favorities.getFavorites().size();
            if (total >= favorities.getTotal_number())
            	statusContents.setNoMore(true);

            return statusContents;
        }

    }

    @Override
    public void onResume() {
    	super.onResume();
    	
    	IntentFilter filter = new IntentFilter();
    	filter.addAction("org.aisen.weibo.sina.FAV_DESTORY");
    	getActivity().registerReceiver(receiver, filter);
    	
    	BaiduAnalyzeUtils.onPageStart("收藏的微博");
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	
    	getActivity().unregisterReceiver(receiver);
    	
    	BaiduAnalyzeUtils.onPageEnd("收藏的微博");
    }
    
    // 接收来自BizFragment的删除(溢出菜单)
    private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null && "org.aisen.weibo.sina.FAV_DESTORY".equals(intent.getAction())) {
				String statusId = intent.getStringExtra("statusId");
				
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
    	for (StatusContent status : getAdapter().getDatas()) {
    		if (status.getId().equals(statusId)) {
    			// 更新ListView
    			getAdapter().removeItemAndRefresh(status);
    			
    			FavoritesCacheUtility.destory(statusId);
    			break;
    		}
    	}
    }
    
}
