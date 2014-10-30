package org.aisen.weibo.sina.ui.fragment.menu;

import java.util.ArrayList;
import java.util.List;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.bean.AccountBean;
import org.aisen.weibo.sina.support.bean.MenuBean;
import org.aisen.weibo.sina.support.db.AccountDB;
import org.aisen.weibo.sina.support.publish.PublishDB;
import org.aisen.weibo.sina.support.publish.PublishManager;
import org.aisen.weibo.sina.support.utils.AisenUtil;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.AppSettings;
import org.aisen.weibo.sina.support.utils.ImageConfigUtils;
import org.aisen.weibo.sina.sys.service.UnreadService;
import org.aisen.weibo.sina.ui.activity.main.MainActivity;
import org.aisen.weibo.sina.ui.activity.search.SearchActivity;
import org.aisen.weibo.sina.ui.fragment.account.AccountFragment;
import org.android.loader.BitmapLoader;
import org.sina.android.bean.WeiBoUser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.m.common.utils.ActivityHelper;
import com.m.common.utils.Logger;
import com.m.support.Inject.ViewInject;
import com.m.support.adapter.ABaseAdapter.AbstractItemView;
import com.m.support.task.TaskException;
import com.m.support.task.WorkTask;
import com.m.ui.activity.BaseActivity;
import com.m.ui.fragment.AListFragment;
import com.m.ui.utils.ViewUtils;

/**
 * 抽屉菜单
 * 
 * @author wangdan
 *
 */
public class MenuFragment_v2 extends AListFragment<MenuBean, ArrayList<MenuBean>> implements OnItemClickListener {

	public static MenuFragment_v2 newInstance(String type) {
		MenuFragment_v2 fragment = new MenuFragment_v2();
		
		if (!TextUtils.isEmpty(type)) {
			Bundle args = new Bundle();
			args.putString("type", type);
			fragment.setArguments(args);
		}
		
		return fragment;
	}
	
	private MenuCallback menuCallback;
	
	private MenuBean lastSelectedMenu;
	
	private int draftSize;
	
	private View menusHeader;
	
	private View profileHeader;
	
	TextView txtMainTitle;
	TextView txtMainCounter;
	TextView txtMentionTitle;
	TextView txtMentionCounter;
	TextView txtCmtTitle;
	TextView txtCmtCounter;
	TextView txtFriendsTitle;
	TextView txtFriendsCounter;
	View btnAccounts;
	
	@Override
	protected int inflateContentView() {
		return R.layout.ui_menu;
	}
	
	@Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		super.layoutInit(inflater, savedInstanceSate);
		
		getListView().setOnItemClickListener(this);
		
		setItems(generateMenus());
		
		if (savedInstanceSate == null) {
			int index = getListView().getHeaderViewsCount();
			
			lastSelectedMenu = newMenu("1");
			if (getArguments() != null) {
				String type = getArguments().getString("type");
				
				lastSelectedMenu = newMenu(type);
				for (int i = 0; i < getAdapterItems().size(); i++) {
					MenuBean bean = getAdapterItems().get(i);
					if (bean.getType().equals(type)) {
						lastSelectedMenu = bean;
						index = i + getListView().getHeaderViewsCount();
						break;
					}
				}
				
			}
			
			if (index <= getListView().getHeaderViewsCount()) {
				onMenuClicked(lastSelectedMenu);
			}
			else {
				onItemClick(getListView(), null, index, index);
			}
		}
		else {
			lastSelectedMenu = (MenuBean) savedInstanceSate.getSerializable("lastSelectedMenu");

			setItemTextColor(lastSelectedMenu);
		}
		
		BaseActivity baseActivity = (BaseActivity) getActivity();
		org.aisen.weibo.sina.ui.fragment.base.ActivityHelper activityHelper = (org.aisen.weibo.sina.ui.fragment.base.ActivityHelper) baseActivity.getActivityHelper();
		getRefreshView().setPadding(getRefreshView().getPaddingLeft(), 
										getRefreshView().getPaddingTop(), 
										getRefreshView().getPaddingRight(), 
										activityHelper.wallpaper.systemBarConfig.getPixelInsetBottom());
	}
	
	@Override
	public void addHeadView(ListView listView) {
		super.addHeadView(listView);
		
		profileHeader = View.inflate(getActivity(), R.layout.lay_menu_profile, null);
		
		btnAccounts = profileHeader.findViewById(R.id.btnAccounts);
		btnAccounts.setOnClickListener(accountSwitchOnClickListener);
		setAccountItem();
		
		listView.addHeaderView(profileHeader);
		
		menusHeader = View.inflate(getActivity(), R.layout.lay_menu_header, null);
		
		MenuBean menu = newMenu("1");
		View layMain = menusHeader.findViewById(R.id.layMain);
		layMain.setOnClickListener(onMenuClickListener);
		layMain.setTag(menu);
		txtMainTitle = (TextView) layMain.findViewById(R.id.txtTitle);
		txtMainTitle.setText(getString(menu.getTitleRes()));
		txtMainCounter = (TextView) layMain.findViewById(R.id.txtCounter);
		
		menu = newMenu("2");
		View layMenthion = menusHeader.findViewById(R.id.layMenthion);
		layMenthion.setOnClickListener(onMenuClickListener);
		layMenthion.setTag(menu);
		txtMentionTitle = (TextView) layMenthion.findViewById(R.id.txtTitle);
		txtMentionTitle.setText(getString(menu.getMenuTitleRes()));
		txtMentionCounter = (TextView) layMenthion.findViewById(R.id.txtCounter);
		
		menu = newMenu("3");
		View layCmt = menusHeader.findViewById(R.id.layCmt);
		layCmt.setOnClickListener(onMenuClickListener);
		layCmt.setTag(menu);
		txtCmtTitle = (TextView) layCmt.findViewById(R.id.txtTitle);
		txtCmtTitle.setText(getString(menu.getTitleRes()));
		txtCmtCounter = (TextView) layCmt.findViewById(R.id.txtCounter);
		
		menu = newMenu("4");
		View layFriend = menusHeader.findViewById(R.id.layFriend);
		layFriend.setOnClickListener(onMenuClickListener);
		layFriend.setTag(menu);
		txtFriendsTitle = (TextView) layFriend.findViewById(R.id.txtTitle);
		txtFriendsTitle.setText(getString(menu.getTitleRes()));
		txtFriendsCounter = (TextView) layFriend.findViewById(R.id.txtCounter);
		
		listView.addHeaderView(menusHeader);
	}
	
	private OnClickListener onMenuClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			MenuBean menu = (MenuBean) v.getTag();
			
			onMenuClicked(menu);
			
			((MainActivity) getActivity()).closeDrawer();
		}
	};
	
	public View addHeadView() {
		View header = View.inflate(getActivity(), R.layout.lay_menu_search, null);
		header.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SearchActivity.launch(getActivity());
			}
		});
		return header;
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		MenuBean entity = null; 
				
		if (position == 0) {
			entity = newMenu("0");
		}
		else {
			entity = getAdapterItems().get(position - getListView().getHeaderViewsCount());
		}
		
		
		if ("0".equals(entity.getType()))
			Logger.d("查看用户信息");
		else 
			Logger.d(getString(entity.getTitleRes()));
		
		if (onMenuClicked(entity))
			return;

		((MainActivity) getActivity()).closeDrawer();		
	}
	
	private boolean onMenuClicked(MenuBean menu) {
		if (menuCallback != null && menuCallback.onMenuSelected(menu, false)) {
			return true;
		}
		
		setItemTextColor(menu);
		
		return false;
	}
	
	public void setAccountItem() {
		WeiBoUser user = AppContext.getUser();
		
		ViewUtils.setTextViewValue(profileHeader, R.id.txtName, user.getScreen_name());
		ImageView imgPhoto = (ImageView) profileHeader.findViewById(R.id.imgPhoto);
		BitmapLoader.getInstance().display(MenuFragment_v2.this, 
												user.getAvatar_large(), imgPhoto, ImageConfigUtils.getLargePhotoConfig());
		ImageView imgCover = (ImageView) profileHeader.findViewById(R.id.imgCover);
		BitmapLoader.getInstance().display(MenuFragment_v2.this, 
												user.getCover_image(), imgCover, ImageConfigUtils.getPhotoCoverConfig());
	}
	
	public void setItemTextColor(MenuBean menu) {
		lastSelectedMenu = menu;
		
		if (menu.getType().equals("1")) {
			txtMainTitle.setTextColor(Color.parseColor(AppSettings.getThemeColor()));
			txtMainTitle.getPaint().setFakeBoldText(true);
		}
		else {
			txtMainTitle.setTextColor(Color.parseColor("#ff333333"));
			txtMainTitle.getPaint().setFakeBoldText(false);
		}
		
		if (menu.getType().equals("2")) {
			txtMentionTitle.setTextColor(Color.parseColor(AppSettings.getThemeColor()));
			txtMentionTitle.getPaint().setFakeBoldText(true);
		}
		else {
			txtMentionTitle.setTextColor(Color.parseColor("#ff333333"));
			txtMentionTitle.getPaint().setFakeBoldText(false);
		}
		
		if (menu.getType().equals("3")) {
			txtCmtTitle.setTextColor(Color.parseColor(AppSettings.getThemeColor()));
			txtCmtTitle.getPaint().setFakeBoldText(true);
		}
		else {
			txtCmtTitle.setTextColor(Color.parseColor("#ff333333"));
			txtCmtTitle.getPaint().setFakeBoldText(false);
		}
		
		if (menu.getType().equals("4")) {
			txtFriendsTitle.setTextColor(Color.parseColor(AppSettings.getThemeColor()));
			txtFriendsTitle.getPaint().setFakeBoldText(true);
		}
		else {
			txtFriendsTitle.setTextColor(Color.parseColor("#ff333333"));
			txtFriendsTitle.getPaint().setFakeBoldText(false);
		}
		
		getAdapter().notifyDataSetChanged();
		updateHeaderCounter();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putSerializable("lastSelectedMenu", lastSelectedMenu);
	}

	@Override
	protected AbstractItemView<MenuBean> newItemView() {
		return new MenuItemView();
	}

	@Override
	protected void requestData(RefreshMode mode) {
		
	}
	
	private void updateHeaderCounter() {
		int count = 0;
		
		// 朋友圈
		if (AppContext.getUnreadCount().getFollower() > 0) {
			txtFriendsCounter.setText(String.valueOf(AppContext.getUnreadCount().getFollower()));
			txtFriendsCounter.setVisibility(View.VISIBLE);
		}
		else {
			txtFriendsCounter.setVisibility(View.GONE);
		}
		
		// 提及
		if (AppSettings.isNotifyStatusMention()) 
			count += AppContext.getUnreadCount().getMention_status();
		if (AppSettings.isNotifyCommentMention()) 
			count += AppContext.getUnreadCount().getMention_cmt();
		if (count > 0) {
			txtMentionCounter.setText(String.valueOf(count));
			txtMentionCounter.setVisibility(View.VISIBLE);
		}
		else {
			txtMentionCounter.setVisibility(View.GONE);
		}
		
		// 评论
		if (AppContext.getUnreadCount().getCmt() > 0) {
			txtCmtCounter.setText(String.valueOf(AppContext.getUnreadCount().getCmt()));
			txtCmtCounter.setVisibility(View.VISIBLE);
		}
		else {
			txtCmtCounter.setVisibility(View.GONE);
		}
	}
	
	private void updateCounter(TextView view, MenuBean menu) {
		
		view.setVisibility(View.GONE);

		if (AppContext.getUnreadCount() == null) {
			Logger.w("UnradCount is null");
			return;
		}
		
		int count = 0;
		switch (Integer.parseInt(menu.getType())) {
		// 草稿
		case 6:
			if (draftSize > 0)
				count = draftSize;
			break;
		// 设置
		case 5:
			count = ActivityHelper.getInstance().getBooleanShareData("newVersion", false) ? 1 : 0;
			break;
		default:
			break;
		}
		
		if (count > 0) {
			if (count > 100)
				view.setText("100+");
			else 
				view.setText(String.valueOf(count));
			
			view.setVisibility(View.VISIBLE);
		}
	}
	
	private ArrayList<MenuBean> generateMenus() {
		ArrayList<MenuBean> menuList = new ArrayList<MenuBean>();
		
		// 收藏
		menuList.add(newMenu("7"));
		// 草稿
		menuList.add(newMenu("6"));
		// 搜索
		menuList.add(newMenu("8"));
		// 设置
		menuList.add(newMenu("5"));
		
		return menuList;
	}
	
	public static MenuBean newMenu(String type) {
		MenuBean menuBean = null;

		switch (Integer.parseInt(type)) {
		// 个人信息
		case 0:
			menuBean = new MenuBean(-1, R.string.draw_profile, R.string.draw_profile, "0");
			break;
		// 微博首页
		case 1:
			menuBean = new MenuBean(R.drawable.ic_left_home, R.string.draw_timeline, R.string.draw_timeline, "1");
			break;
		// 消息
		case 2:
			menuBean = new MenuBean(R.drawable.ic_left_messages, R.string.draw_message, R.string.mention_title, "2");
			break;
		// 评论
		case 3:
			menuBean = new MenuBean(R.drawable.ic_left_news, R.string.draw_comment, R.string.draw_comment, "3");
			break;
		// 朋友关系
		case 4:
			menuBean = new MenuBean(R.drawable.ic_left_groups, R.string.draw_friendship, R.string.draw_friendship, "4");
			break;
		// 设置
		case 5:
			menuBean = new MenuBean(R.drawable.ic_left_settings, R.string.draw_settings, R.string.draw_settings, "5");
			break;
		// 草稿
		case 6:
			menuBean = new MenuBean(R.drawable.ic_left_draft, R.string.draw_draft, R.string.draw_draft, "6");
			break;
		// 收藏
		case 7:
			menuBean = new MenuBean(R.drawable.ic_left_fave, R.string.draw_fav, R.string.draw_fav_title, "7");
			break;
		// 搜索
		case 8:
			menuBean = new MenuBean(R.drawable.ic_left_fave, R.string.draw_search_v2, R.string.draw_search_title, "8");
			break;
		}
		
		return menuBean;
	}
	
	OnClickListener accountSwitchOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			final List<AccountBean> accountList = AccountDB.query();
			for (AccountBean bean : accountList) {
				if (bean.getUser().getIdstr().equals(AppContext.getUser().getIdstr())) {
					accountList.remove(bean);
					break;
				}
			}
			
			final String[] items = new String[accountList.size() + 1];
			for (int i = 0; i < accountList.size(); i++)
				items[i] = accountList.get(i).getUser().getScreen_name();
			items[items.length - 1] = getString(R.string.draw_accounts);
			
			AisenUtil.showMenuDialog(MenuFragment_v2.this, v, items, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (which == items.length - 1) {
						// 账号管理
						AccountFragment.launch(getActivity());
					}
					else {
						// 切换用户
						AccountFragment.login(accountList.get(which), true);
					}
				}
			});
			
		}
	};
	
	@Override
	public void onResume() {
		super.onResume();
		
		updateHeaderCounter();
		getAdapter().notifyDataSetChanged();
		
		// 将菜单置顶
		final ListView listView = ((ListView) getRefreshView()); 
		listView.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				listView.setSelectionFromTop(0, 0);
			}
		
		}, 100);
		
		if (AppContext.isLogedin())
			new RefreshDraftTask().execute();
		
		if (lastSelectedMenu != null)
			setItemTextColor(lastSelectedMenu);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(UnreadService.ACTION_UNREAD_CHANGED);
		filter.addAction(PublishManager.ACTION_PUBLISH_CHANNGED);
		getActivity().registerReceiver(receiver, filter);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		getActivity().unregisterReceiver(receiver);
	}
	
	BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null && !TextUtils.isEmpty(intent.getAction())) {
				if (UnreadService.ACTION_UNREAD_CHANGED.equals(intent.getAction())) {
					getAdapter().notifyDataSetChanged();
					updateHeaderCounter();
				}
				else if (PublishManager.ACTION_PUBLISH_CHANNGED.equals(intent.getAction())) {
					new RefreshDraftTask().execute();
				}
			}
		}
		
	};
	
	class MenuItemView extends AbstractItemView<MenuBean> {

		@ViewInject(id = R.id.txtTitle)
		TextView txtTitle;
		@ViewInject(id = R.id.txtCounter)
		TextView txtCounter;
		
		@Override
		public int inflateViewId() {
			return R.layout.item_menu_v2;
		}

		@Override
		public void bindingData(View convertView, MenuBean data) {
			// 2014-09-01 解决注销账号时崩溃的BUG
			if (!AppContext.isLogedin())
				return;
			
			txtTitle.setText(data.getMenuTitleRes());
		}
		
		@Override
		public void updateConvertView(MenuBean data, View convertView, int selectedPosition) {
			super.updateConvertView(data, convertView, selectedPosition);
			
			updateCounter(txtCounter, data);
			
			if (lastSelectedMenu.getType().equals(data.getType())) {
				txtTitle.setTextColor(Color.parseColor(AppSettings.getThemeColor()));
				txtTitle.getPaint().setFakeBoldText(true);
			}
			else {
				txtTitle.setTextColor(Color.parseColor("#ff676767"));
				txtTitle.getPaint().setFakeBoldText(false);
			}
		}
		
	}
	
	class RefreshDraftTask extends WorkTask<Void, Void, Boolean> {

		@Override
		public Boolean workInBackground(Void... params) throws TaskException {
			draftSize = PublishDB.getPublishList(AppContext.getUser()).size();
			return true;
		}
		
		@Override
		protected void onSuccess(Boolean result) {
			super.onSuccess(result);
			
			getAdapter().notifyDataSetChanged();
		}
		
	}
	
	@Override
	public boolean onAcUnusedDoubleClicked() {
		return false;
	}
	
	public void setMenuCallback(MenuCallback menuCallback) {
		this.menuCallback = menuCallback;
	}
	
	public interface MenuCallback {
		
		public boolean onMenuSelected(MenuBean menu, boolean replace);
		
	}

}
