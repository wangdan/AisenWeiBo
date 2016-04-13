package org.aisen.weibo.sina.ui.fragment.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.ui.fragment.itemview.AHeaderItemViewCreator;
import org.aisen.android.ui.fragment.itemview.IITemView;
import org.aisen.android.ui.fragment.itemview.IItemViewCreator;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;
import org.aisen.weibo.sina.sinasdk.bean.Token;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.ui.fragment.timeline.ATimelineFragment;
import org.aisen.weibo.sina.ui.fragment.timeline.ATimelineHeaderView;
import org.aisen.weibo.sina.ui.fragment.timeline.TimelineItemView;

/**
 * 用户的微博
 *
 * Created by wangdan on 16/1/12.
 */
public class ProfileTimelineFragment extends ATimelineFragment {

    public static ProfileTimelineFragment newInstance(WeiBoUser user) {
        Bundle args = new Bundle();
        args.putSerializable("mUser", user);

        ProfileTimelineFragment fragment = new ProfileTimelineFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private WeiBoUser mUser;

    @Override
    public int inflateContentView() {
        return R.layout.ui_timeline_profile;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUser = savedInstanceState == null ? (WeiBoUser) getArguments().getSerializable("mUser")
                                          : (WeiBoUser) savedInstanceState.getSerializable("mUser");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("mUser", mUser);
    }

    @Override
    public IItemViewCreator<StatusContent> configItemViewCreator() {
        return new IItemViewCreator<StatusContent>() {

            @Override
            public View newContentView(LayoutInflater inflater, ViewGroup parent, int viewType) {
                return inflater.inflate(R.layout.item_profile_timeline, parent, false);
            }

            @Override
            public IITemView<StatusContent> newItemView(View convertView, int viewType) {
                return new TimelineItemView(convertView, ProfileTimelineFragment.this);
            }

        };
    }

    @Override
    protected AHeaderItemViewCreator<StatusContent> configHeaderViewCreator() {
        return new AHeaderItemViewCreator<StatusContent>() {

            @Override
            public int[][] setHeaders() {
                return new int[][]{ { ATimelineHeaderView.LAYOUT_RES, 100 } };
            }

            @Override
            public IITemView<StatusContent> newItemView(View convertView, int viewType) {
                return new ATimelineHeaderView(ProfileTimelineFragment.this, convertView) {

                    @Override
                    protected int getTitleArrRes() {
                        return R.array.user_headers;
                    }

                    @Override
                    protected String[] getTitleFeature() {
                        return ATimelineHeaderView.profileFeatureArr;
                    }

                };
            }

        };
    }

    @Override
    public void requestData(RefreshMode mode) {
        new ProfileTimelineTask(mode).execute();
    }

    class ProfileTimelineTask extends ATimelineTask {

        public ProfileTimelineTask(RefreshMode mode) {
            super(mode);
        }

        @Override
        public StatusContents getStatusContents(Params params) throws TaskException {
            // 不管user_id字段传值什么，都返回登录用户的微博
            if (AppContext.getAccount().getUser().getIdstr().equals(mUser.getIdstr())) {
                params.addParameter("user_id", mUser.getIdstr());
            }
            else {
                params.addParameter("screen_name", mUser.getScreen_name());
            }

            params.addParameter("count", String.valueOf(AppSettings.getTimelineCount()));

            Token token = AppContext.getAccount().getAdvancedToken();
            if (token == null)
                token = AppContext.getAccount().getAccessToken();

            StatusContents statusContents = SinaSDK.getInstance(token, getTaskCacheMode(this)).statusesUserTimeLine(params);

            if (statusContents != null && statusContents.getStatuses() != null && statusContents.getStatuses().size() > 0) {
                for (StatusContent status : statusContents.getStatuses())
                    status.setUser(mUser);
            }

            return statusContents;
        }

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
