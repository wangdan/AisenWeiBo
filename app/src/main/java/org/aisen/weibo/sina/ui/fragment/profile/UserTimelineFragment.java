package org.aisen.weibo.sina.ui.fragment.profile;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import org.aisen.android.common.utils.ActivityHelper;
import org.aisen.android.component.container.FragmentArgs;
import org.aisen.android.component.container.FragmentContainerActivity;
import org.aisen.android.network.http.Params;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.adapter.ABaseAdapter;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.AStripTabsFragment;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.ui.fragment.settings.AboutWebFragment;
import org.aisen.weibo.sina.ui.fragment.timeline.ATimelineFragment;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.AccessToken;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.StatusContents;
import org.aisen.weibo.sina.sinasdk.bean.Token;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;

/**
 * 用户的微博
 *
 * Created by wangdan on 15-3-1.
 */
public class UserTimelineFragment extends ATimelineFragment
                                       implements UserProfilePagerFragment.IUserProfileRefresh,
                                                  AStripTabsFragment.IStripTabInitData {

    public static UserTimelineFragment newInstance(WeiBoUser user, String feature) {
        Bundle args = new Bundle();
        args.putSerializable("user", user);
        args.putSerializable("launch", false);
        if (!TextUtils.isEmpty(feature))
            args.putSerializable("feature", feature);

        UserTimelineFragment fragment = new UserTimelineFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static void launch(Activity from, WeiBoUser user) {
        FragmentArgs args = new FragmentArgs();
        args.add("user", user);
        args.add("launch", true);

        FragmentContainerActivity.launch(from, UserTimelineFragment.class, args);
    }

    private View headerView;

    private boolean launch;
    private WeiBoUser mUser;
    private String feature;

    @Override
    public void onCreate(Bundle savedInstanceSate) {
        super.onCreate(savedInstanceSate);

        mUser = savedInstanceSate == null ? (WeiBoUser) getArguments().getSerializable("user")
                                          : (WeiBoUser) savedInstanceSate.getSerializable("user");
        launch = savedInstanceSate == null ? getArguments().getBoolean("launch", false)
                                           : savedInstanceSate.getBoolean("launch");
        feature = savedInstanceSate == null ? getArguments().getString("feature", null)
                                            : savedInstanceSate.getString("feature", null);
        if (feature == null)
            feature = "0";
    }

    @Override
    protected int inflateContentView() {
        return R.layout.as_ui_user_timeline;
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        if (launch) {
            BaseActivity activity = (BaseActivity) getActivity();
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setTitle(mUser.getScreen_name());
        }
    }

    @Override
    protected void setInitSwipeRefresh(ListView listView, SwipeRefreshLayout swipeRefreshLayout, Bundle savedInstanceState) {
        super.setInitSwipeRefresh(listView, swipeRefreshLayout, savedInstanceState);

        headerView = View.inflate(getActivity(), R.layout.as_header_user_timeline, null);
        headerView.setOnClickListener(showFeatureDialogListener);

        listView.addHeaderView(headerView);

        setHeaderView();
    }

    private void setHeaderView() {
        TextView txtView = (TextView) headerView.findViewById(R.id.txtName);
        String[] titles = getResources().getStringArray(R.array.user_headers);
        txtView.setText(titles[Integer.parseInt(feature)]);
    }

    View.OnClickListener showFeatureDialogListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (isRefreshing())
                return;

            String[] titles = getResources().getStringArray(R.array.user_headers);

            new AlertDialogWrapper.Builder(getActivity())
                                    .setTitle(R.string.profile_feature_dialog)
                                    .setNegativeButton(R.string.cancel, null)
                                    .setSingleChoiceItems(titles, Integer.parseInt(feature), new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Integer.parseInt(feature) == which) {
                                                dialog.dismiss();

                                                return;
                                            }

                                            feature = String.valueOf(which);

                                            // 清理线程状态，可以加载缓存
                                            cleatTaskCount("TimelineTask");

                                            setHeaderView();

                                            requestDataDelay(200);

                                            dialog.dismiss();
                                        }

                                    })
                                    .show();
        }

    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (!TextUtils.isEmpty(feature))
            outState.putString("feature", feature);
        outState.putBoolean("launch", launch);
        outState.putSerializable("user", mUser);
    }

    @Override
    protected ABaseAdapter.AbstractItemView<StatusContent> newItemView() {
        return new UserTimelineItemView(this, true);
    }

    @Override
    public void onStripTabRequestData() {
        // 如果还没有加载过数据，就开始加载
        if (getTaskCount("TimelineTask") == 0) {
            requestData(RefreshMode.reset);
        }
    }

    @Override
    protected void requestData(RefreshMode mode) {
        boolean load = true;

        // 如果还没有加载过数据，切且显示的是当前的页面
        if (getTaskCount("TimelineTask") == 0) {
            Fragment fragment = getPagerCurrentFragment();
            if (fragment != null && fragment != this)
                load = false;
        }

        if (load)
            new UserTimelineTask(mode == RefreshMode.refresh ? RefreshMode.reset : mode).execute();
    }

    @Override
    public void refreshProfile() {
        if (!isRefreshing())
            requestDataDelay(100);
    }

    // 用户微博列表
    class UserTimelineTask extends TimelineTask {

        public UserTimelineTask(RefreshMode mode) {
            super(mode);
        }

        @Override
        protected StatusContents workInBackground(RefreshMode mode, String previousPage, String nextPage,
                                                  Void... p) throws TaskException {
            Params params = new Params();

            if (mode == RefreshMode.refresh && !TextUtils.isEmpty(previousPage))
                params.addParameter("since_id", previousPage);

            if (mode == RefreshMode.update && !TextUtils.isEmpty(nextPage))
                params.addParameter("max_id", nextPage);

            // 是否是原创
            if (!TextUtils.isEmpty(feature))
			    params.addParameter("feature", feature);

            // 不管user_id字段传值什么，都返回登录用户的微博
            if (AppContext.getUser().getIdstr().equals(mUser.getIdstr())) {
                params.addParameter("user_id", mUser.getIdstr());
            }
            else {
                params.addParameter("screen_name", mUser.getScreen_name());
            }

            params.addParameter("count", String.valueOf(AppSettings.getTimelineCount()));

            Token token = null;
            // 是当前登录用户
            if (AisenUtils.isLoggedUser(mUser)) {
                if (AppContext.getAccount().getAdvancedToken() != null) {
                    token = AppContext.getAdvancedToken();
                    params.addParameter("source", AppContext.getAdvancedToken().getAppKey());
                }
            }
            else {
                if (AppContext.getAdvancedToken() != null) {
                    AccessToken accessToken = AppContext.getAdvancedToken();

                    token = new Token();
                    token.setToken(accessToken.getToken());
                    token.setSecret(accessToken.getSecret());

                    params.addParameter("source", accessToken.getAppKey());
                }
            }
            if (token == null)
                token = AppContext.getToken();

            StatusContents statusContents = SinaSDK.getInstance(token, getTaskCacheMode(this)).statusesUserTimeLine(params);

            if (statusContents != null && statusContents.getStatuses() != null && statusContents.getStatuses().size() > 0) {
                for (StatusContent status : statusContents.getStatuses())
                    status.setUser(mUser);
            }

            return statusContents;
        }

        @Override
        protected void onSuccess(StatusContents result) {
            super.onSuccess(result);

            if (result == null || getActivity() == null)
                return;

            getActivity().invalidateOptionsMenu();

            if (result.isCache())
                return;

            boolean remind = AppContext.getAdvancedToken() == null || AppContext.getAdvancedToken().isExpired();
            // 提示用户去高级授权
            if (AppContext.getAccount().getAdvancedToken() != null) {
                remind = false;
            }
            else {
                if (mUser.getIdstr().equalsIgnoreCase(AppContext.getUser().getIdstr())) {
                    remind = true;
                }
            }
            if (remind) {
                if (!ActivityHelper.getBooleanShareData("IgnoreWeicoRemind", false)) {
                    new AlertDialogWrapper.Builder(getActivity())
                            .setTitle(R.string.remind)
                            .setMessage(R.string.profile_help)
                            .setNegativeButton(R.string.cancel, null)
                            .setNeutralButton(R.string.donnot_remind, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityHelper.putBooleanShareData("IgnoreWeicoRemind", true);
                                }

                            })
                            .setPositiveButton(R.string.title_help, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    AboutWebFragment.launchHelp(getActivity());
                                }

                            })
                            .show();
                }
            }
        }

    }

}
