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
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.m.common.utils.ActivityHelper;
import com.m.common.utils.Logger;
import com.m.support.Inject.ViewInject;
import com.m.support.adapter.ABaseAdapter.AbstractItemView;
import com.m.support.task.TaskException;
import com.m.support.task.WorkTask;
import com.m.ui.fragment.AListFragment;

/**
 * 抽屉菜单
 * 
 * @author wangdan
 *
 */
public class MenuFragment extends AListFragment<MenuBean, ArrayList<MenuBean>> implements OnItemClickListener {

	public static MenuFragment newInstance(String type) {
		MenuFragment fragment = new MenuFragment();
		
		if (!TextUtils.isEmpty(type)) {
			Bundle args = new Bundle();
			args.putString("type", type);
			fragment.setArguments(args);
		}
		
		return fragment;
	}
	
	private int selectedPosition;
	
	private MenuCallback menuCallback;
	
	private int draftSize;
	
	private int accountSize;
	
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
			int index = getListView().getHeaderViewsCount() + 1;
			if (getArguments() != null) {
				String type = getArguments().getString("type");
				
				for (int i = 0; i < getAdapter().getDatas().size(); i++) {
					MenuBean bean = getAdapter().getDatas().get(i);
					if (bean.getType().equals(type)) {
						index = i + getListView().getHeaderViewsCount();
						break;
					}
				}
			}
			
			onItemClick(getListView(), null, index, index);
		}
		else {
			selectedPosition = savedInstanceSate.getInt("selectedPosition", 0);
			
			getAdapter().setSelected(selectedPosition);
		}
	}
	
	public View addHeadView() {
		View header = View.inflate(getActivity(), R.layout.lay_menu_search, null);
		header.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				SearchUsersFragment.launch(getActivity());
				SearchActivity.launch(getActivity());
			}
		});
		return header;
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		MenuBean entity = getAdapter().getDatas().get(position - getListView().getHeaderViewsCount());
		
		if ("0".equals(entity.getType()))
			Logger.d("查看用户信息");
		else 
			Logger.d(getString(entity.getTitleRes()));
		
		if (menuCallback != null && menuCallback.onMenuSelected(entity, false)) {
			return;
		}
		
		selectedPosition = position;
		
		getAdapter().setSelected(position);
		((MainActivity) getActivity()).closeDrawer();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putInt("selectedPosition", selectedPosition);
	}

	@Override
	protected AbstractItemView<MenuBean> newItemView() {
		return new MenuItemView();
	}

	@Override
	protected void requestData(RefreshMode mode) {
		
	}
	
	private void updateCounter(TextView view, MenuBean menu) {
		view.setVisibility(View.GONE);

		if (AppContext.getUnreadCount() == null) {
			Logger.w("UnradCount is null");
			return;
		}
		
		int count = 0;
		
		switch (Integer.parseInt(menu.getType())) {
		// 朋友圈
		case 4:
			if (AppContext.getUnreadCount().getFollower() > 0)
				count = AppContext.getUnreadCount().getFollower();
			
			break;
		// 提及
		case 2:
			if (AppSettings.isNotifyStatusMention()) 
				count += AppContext.getUnreadCount().getMention_status();
			if (AppSettings.isNotifyCommentMention()) 
				count += AppContext.getUnreadCount().getMention_cmt();
			
			break;
		// 评论
		case 3:
			if (AppContext.getUnreadCount().getCmt() > 0) 
				count = AppContext.getUnreadCount().getCmt();
				
			break;
		// 草稿
		case 6:
			if (draftSize > 0)
				count = draftSize;
			break;
		// 设置
		case 5:
			count = ActivityHelper.getInstance().getBooleanShareData("newVersion", false) ? 1 : 0;
			break;
		// 微博
		case 1:
		// 个人信息
		case 0:
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
		
		// 个人信息
		menuList.add(newMenu(-1, R.string.draw_profile, R.string.draw_profile, "0"));
		// 微博首页
		menuList.add(newMenu(R.drawable.ic_left_home, R.string.draw_timeline, R.string.draw_timeline, "1"));
		// 消息
		menuList.add(newMenu(R.drawable.ic_left_messages, R.string.draw_message, R.string.mention_title, "2"));
		// 评论
		menuList.add(newMenu(R.drawable.ic_left_news, R.string.draw_comment, R.string.draw_comment, "3"));
		// 收藏
		menuList.add(newMenu(R.drawable.ic_left_fave, R.string.draw_fav, R.string.draw_fav_title, "7"));
		// 朋友关系
		menuList.add(newMenu(R.drawable.ic_left_groups, R.string.draw_friendship, R.string.draw_friendship, "4"));
		// 草稿
		menuList.add(newMenu(R.drawable.ic_left_draft, R.string.draw_draft, R.string.draw_draft, "6"));
		// 设置
		menuList.add(newMenu(R.drawable.ic_left_settings, R.string.draw_settings, R.string.draw_settings, "5"));
		
		return menuList;
	}
	
	private MenuBean newMenu(int iconRes, int menuRes, int titleRes, String type) {
		MenuBean menuBean = new MenuBean();
		
		menuBean.setIconRes(iconRes);
		menuBean.setMenuTitleRes(menuRes);
		menuBean.setTitleRes(titleRes);
		menuBean.setType(type);
		
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
			
			String[] items = new String[accountList.size()];
			for (int i = 0; i < accountList.size(); i++)
				items[i] = accountList.get(i).getUser().getScreen_name();
			
			AisenUtil.showMenuDialog(MenuFragment.this, v, items, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// 切换用户
					AccountFragment.login(accountList.get(which), true);
				}
			});
			
		}
	};
	
	@Override
	public void onResume() {
		super.onResume();
		
		getAdapter().notifyDataSetChanged();
		
		if (AppContext.isLogedin())
			new RefreshDraftTask().execute();
		
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
				}
				else if (PublishManager.ACTION_PUBLISH_CHANNGED.equals(intent.getAction())) {
					new RefreshDraftTask().execute();
				}
			}
		}
		
	};
	
	class MenuItemView extends AbstractItemView<MenuBean> {

		@ViewInject(id = R.id.imgIcon)
		ImageView imgIcon;
		@ViewInject(id = R.id.txtTitle)
		TextView txtTitle;
		@ViewInject(id = R.id.txtCounter)
		TextView txtCounter;
		@ViewInject(id = R.id.btnSwitchAccount)
		View btnSwitch;
		
		@Override
		public int inflateViewId() {
			return R.layout.item_menu;
		}

		@Override
		public void bindingData(View convertView, MenuBean data) {
			// 2014-09-01 解决注销账号时崩溃的BUG
			if (!AppContext.isLogedin())
				return;
			
			btnSwitch.setVisibility(View.GONE);

			if ("0".equals(data.getType())) {
				WeiBoUser logedinUser = AppContext.getUser();
				
				if (accountSize > 1) {
					btnSwitch.setVisibility(View.VISIBLE);
					btnSwitch.setOnClickListener(accountSwitchOnClickListener);
				}
				txtTitle.setText(logedinUser.getScreen_name());
				BitmapLoader.getInstance().display(MenuFragment.this, 
												logedinUser.getAvatar_large(), imgIcon, ImageConfigUtils.getLargePhotoConfig());
			}
			else {
				imgIcon.setImageResource(data.getIconRes());
				txtTitle.setText(data.getMenuTitleRes());
			}
		}
		
		@Override
		public void updateConvertView(MenuBean data, View convertView, int selectedPosition) {
			super.updateConvertView(data, convertView, selectedPosition);
			
			updateCounter(txtCounter, data);
		}
		
	}
	
	class RefreshDraftTask extends WorkTask<Void, Void, Boolean> {

		@Override
		public Boolean workInBackground(Void... params) throws TaskException {
			draftSize = PublishDB.getPublishList(AppContext.getUser()).size();
			accountSize = AccountDB.query().size();
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
