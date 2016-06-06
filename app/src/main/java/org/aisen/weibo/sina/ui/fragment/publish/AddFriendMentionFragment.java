package org.aisen.weibo.sina.ui.fragment.publish;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.support.paging.IPaging;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.android.ui.fragment.AListSwipeRefreshFragment;
import org.aisen.android.ui.fragment.adapter.ARecycleViewItemView;
import org.aisen.android.ui.fragment.itemview.IITemView;
import org.aisen.android.ui.fragment.itemview.IItemViewCreator;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.Friendship;
import org.aisen.weibo.sina.sinasdk.bean.Token;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;
import org.aisen.weibo.sina.support.paging.FriendshipPaging;
import org.aisen.weibo.sina.support.sqlit.FriendMentionDB;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.support.utils.ImageConfigUtils;
import org.aisen.weibo.sina.support.utils.UMengUtil;
import org.aisen.weibo.sina.ui.activity.base.SinaCommonActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangdan on 15/4/25.
 */
public class AddFriendMentionFragment extends AListSwipeRefreshFragment<WeiBoUser, Friendship>
                                            implements AdapterView.OnItemClickListener, SearchView.OnQueryTextListener, MenuItemCompat.OnActionExpandListener{

    public static void launch(ABaseFragment from, int requestCode) {
        SinaCommonActivity.launchForResult(from, AddFriendMentionFragment.class, null, requestCode);
    }

    @ViewInject(id = R.id.laySearchSuggest)
    View laySearchSuggest;
    @ViewInject(id = R.id.layoutContent)
    View layContent;

    private int recentSize;

    private MentionSuggestionFragment suggestionFragment;

    @Override
    public int inflateContentView() {
        return R.layout.ui_add_friend_mention;
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        BaseActivity activity = (BaseActivity) getActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setTitle(R.string.publish_mention);

        getRefreshView().setOnItemClickListener(this);

        setHasOptionsMenu(true);

        if (savedInstanceSate == null) {
            suggestionFragment = MentionSuggestionFragment.newInstance();
            getFragmentManager().beginTransaction().add(R.id.laySearchSuggest, suggestionFragment, "MentionSuggestionFragment").commit();
        }
        else {
            suggestionFragment = (MentionSuggestionFragment) getFragmentManager().findFragmentByTag("MentionSuggestionFragment");
        }
    }

    @Override
    protected void setupRefreshConfig(RefreshConfig config) {
        super.setupRefreshConfig(config);

        config.emptyHint = getString(R.string.empty_friends);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        position = position - ((ListView) getRefreshView()).getHeaderViewsCount();

        FriendMentionDB.addFriend(getAdapterItems().get(position));

        Intent data = new Intent();
        data.putExtra("bean", getAdapterItems().get(position));
        getActivity().setResult(Activity.RESULT_OK, data);
        getActivity().finish();
    }

    @Override
    public IItemViewCreator<WeiBoUser> configItemViewCreator() {
        return new IItemViewCreator<WeiBoUser>() {

            @Override
            public View newContentView(LayoutInflater inflater, ViewGroup parent, int viewType) {
                return inflater.inflate(R.layout.item_friend, parent, false);
            }

            @Override
            public IITemView<WeiBoUser> newItemView(View convertView, int viewType) {
                return new FriendItemView(convertView);
            }

        };
    }

    @Override
    protected IPaging<WeiBoUser, Friendship> newPaging() {
        return new FriendshipPaging();
    }

    @Override
    public void requestData(RefreshMode mode) {
        new FriendTask(mode == RefreshMode.refresh ? RefreshMode.reset : mode).execute();
    }

    class FriendItemView extends ARecycleViewItemView<WeiBoUser> {

        @ViewInject(id = R.id.imgPhoto)
        ImageView imgPhoto;
        @ViewInject(id = R.id.txtName)
        TextView txtName;
        @ViewInject(id = R.id.txtRemark)
        TextView txtRemark;
        @ViewInject(id = R.id.txtDivider)
        TextView txtDivider;
        @ViewInject(id = R.id.layDivider)
        View layDivider;

        public FriendItemView(View itemView) {
            super(getActivity(), itemView);
        }

        @Override
        public void onBindData(View convertView, WeiBoUser data, int position) {
            BitmapLoader.getInstance().display(AddFriendMentionFragment.this,
                    AisenUtils.getUserPhoto(data),
                    imgPhoto, ImageConfigUtils.getLargePhotoConfig());

            txtName.setText(data.getScreen_name());
            if (!TextUtils.isEmpty(data.getRemark()))
                txtRemark.setText(data.getRemark());
            else
                txtRemark.setText("");

            if (recentSize > 0) {
                layDivider.setVisibility(getPosition() == 0 || getPosition() == recentSize ? View.VISIBLE : View.GONE);
                if (getPosition() == 0)
                    txtDivider.setText(R.string.publish_recent);
                else if (getPosition() == recentSize)
                    txtDivider.setText(R.string.publish_all);
            }
            else {
                layDivider.setVisibility(View.GONE);
            }
        }

    }

    class FriendTask extends APagingTask<Void, Void, Friendship> {

        public FriendTask(RefreshMode mode) {
            super(mode);
        }

        @Override
        protected List<WeiBoUser> parseResult(Friendship result) {
            return result.getUsers();
        }

        @Override
        protected Friendship workInBackground(RefreshMode mode, String previousPage, String nextPage,
                                              Void... params) throws TaskException {
            Token token = AppContext.getAccount().getAdvancedToken();
            if (token == null) {
                token = AppContext.getAccount().getAccessToken();
            }

            if (mode != RefreshMode.update)
                nextPage = "0";

            Friendship friendship = SinaSDK.getInstance(token, getTaskCacheMode(this))
                                                    .friendshipsFriends(AppContext.getAccount().getUser().getIdstr(), null, nextPage, 50);

            if ("0".equalsIgnoreCase(nextPage)) {
                List<WeiBoUser> recentUsers = FriendMentionDB.getRecentMention("5");
                recentSize = recentUsers.size();

                ArrayList<WeiBoUser> fuck = new ArrayList<>();
                fuck.addAll(recentUsers);
                if (recentUsers.size() > 0 && friendship != null && friendship.getUsers() != null) {
                    for (WeiBoUser lineUser : friendship.getUsers()) {
                        boolean find = false;
                        for (WeiBoUser user : recentUsers) {
                            if (user.getIdstr().equalsIgnoreCase(lineUser.getIdstr())) {
                                find = true;
                                break;
                            }
                        }
                        if (!find)
                            fuck.add(lineUser);
                    }
                    friendship.setUsers(fuck);
                }
            }
            if (friendship.getNext_cursor() <= 0)
                friendship.setEndPaging(true);

            return friendship;
        }

        @Override
        protected void onFailure(TaskException exception) {
            super.onFailure(exception);

            showMessage(exception.getMessage());
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_mention_friend, menu);

        MenuItem switchItem = menu.findItem(R.id.menuSwitch);
        View viewTitle = switchItem.getActionView();
        final EditText editQuary = (EditText) viewTitle.findViewById(R.id.editQuery);
        editQuary.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER))  {
                    onQueryTextSubmit(v.getText().toString());

                    onMenuItemActionExpand(null);
                    return true;
                }

                return false;
            }
        });
        editQuary.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                onQueryTextChange(s.toString());

                if (TextUtils.isEmpty(s.toString()))
                    onMenuItemActionCollapse(null);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }

        });
        editQuary.setHint(R.string.metion_search_hint);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Logger.v("suggestion query ---> " + newText);

        suggestionFragment.query(newText);

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Logger.v("suggestion query ---> " + query);

        suggestionFragment.query(query);

        return true;
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        layContent.setVisibility(View.GONE);
        laySearchSuggest.setVisibility(View.VISIBLE);

        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        layContent.setVisibility(View.VISIBLE);
        laySearchSuggest.setVisibility(View.GONE);
        BaseActivity activity = (BaseActivity) getActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        UMengUtil.onPageStart(getActivity(), "搜索提及好友页");
    }

    @Override
    public void onPause() {
        super.onPause();

        UMengUtil.onPageEnd(getActivity(), "搜索提及好友页");
    }

}
