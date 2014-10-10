package org.aisen.weibo.sina.ui.fragment.account;

import java.util.ArrayList;
import java.util.List;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.MyApplication;
import org.aisen.weibo.sina.support.bean.AccountBean;
import org.aisen.weibo.sina.support.db.AccountDB;
import org.aisen.weibo.sina.support.publish.PublishNotifier;
import org.aisen.weibo.sina.support.publish.UnreadCountNotifier;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.ImageConfigUtils;
import org.aisen.weibo.sina.sys.service.PublishService;
import org.aisen.weibo.sina.sys.service.UnreadService;
import org.aisen.weibo.sina.ui.activity.common.FragmentContainerActivity;
import org.aisen.weibo.sina.ui.activity.main.MainActivity;
import org.android.loader.BitmapLoader;
import org.sina.android.bean.UnreadCount;
import org.sina.android.bean.WeiBoUser;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.m.common.utils.Logger;
import com.m.support.Inject.ViewInject;
import com.m.support.adapter.ABaseAdapter.AbstractItemView;
import com.m.support.task.TaskException;
import com.m.support.task.WorkTask;
import com.m.ui.fragment.AListFragment;
import com.m.ui.utils.ViewUtils;

/**
 * 账号界面，管理授权账号
 * 
 * @author wangdan
 *
 */
public class AccountFragment extends AListFragment<AccountBean, ArrayList<AccountBean>> 
								implements OnItemClickListener, MultiChoiceModeListener {

	public static void launch(Activity from) {
		FragmentContainerActivity.launch(from, AccountFragment.class, null);
	}
	
	// 登录账号
	public static void login(AccountBean accountBean, boolean toMain) {
		if (AppContext.isLogedin()) {
			// 1、清理定时发布
			MyApplication.removeAllPublishAlarm();
			// 2、清理正在发布的数据
			PublishService.stopPublish();
			// 3、重新开始读取未读消息
			UnreadService.stopService();
			// 4、清理未读消息
			UnreadCountNotifier.mCount = new UnreadCount();
			// 5、清理通知栏
			PublishNotifier.cancelAll();
		}
		
		// 登录该账号
		AppContext.login(accountBean.getUser(), accountBean.getGroups(), accountBean.getToken());
		AccountDB.setLogedinAccount(accountBean);

		// 进入首页
		if (toMain)
			MainActivity.login();
	}
	
	public static final String TAG = "Account";
	
	@ViewInject(id = R.id.btnAdd, click = "addAccountListener")
	View btnAdd;
	
	private boolean isSelectedModeActivated = false;
	private SparseBooleanArray checkedArray;
	
	@Override
	protected int inflateContentView() {
		return R.layout.ui_account;
	}
	
	@Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		super.layoutInit(inflater, savedInstanceSate);
		
		ActionBar actionBar = getActivity().getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(R.string.title_acount);
		
		setHasOptionsMenu(true);
		getListView().setOnItemClickListener(this);
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		getListView().setMultiChoiceModeListener(this);
		checkedArray = new SparseBooleanArray();
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		AccountBean account = getAdapter().getDatas().get(position);
		if (AccountBean.isExpired(account)) {
			new AlertDialog.Builder(getActivity()).setTitle(R.string.remind)
								.setMessage(R.string.account_expired)
								.setNegativeButton(R.string.no, null)
								.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										LoginFragment.launch(AccountFragment.this, 1000);
									}
									
								})
								.show();
			
			return;
		}
		
		if (isSelectedModeActivated) {
			getListView().setItemChecked(position, !getListView().isItemChecked(position));
		}
		else {
			login(account, true);
			
			getActivity().finish();
		}
	}

	@Override
	protected AbstractItemView<AccountBean> newItemView() {
		return new AccountItemView();
	}

	@Override
	protected void requestData(RefreshMode mode) {
		new AccountTask(RefreshMode.reset).execute();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		
		inflater.inflate(R.menu.account, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// 新增授权
		if (item.getItemId() == R.id.add) {
			LoginFragment.launch(this, 1000);
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	void addAccountListener(View v) {
		LoginFragment.launch(this, 1000);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == 1000 && resultCode == Activity.RESULT_OK) {
			new AlertDialog.Builder(getActivity()).setTitle(R.string.remind)
								.setMessage(R.string.account_newaccount_remind)
								.setPositiveButton(R.string.i_know, null)
								.show();
			
			requestData(RefreshMode.reset);
		}
	}
	
	class AccountItemView extends AbstractItemView<AccountBean> {

		@ViewInject(id = R.id.txtName)
		TextView txtName;
		@ViewInject(id = R.id.txtDesc)
		TextView txtDesc;
		@ViewInject(id = R.id.imgPhoto)
		ImageView imgPhoto;
		@ViewInject(id = R.id.viewCover)
		View viewCover;
		@ViewInject(id = R.id.container)
		View container;
		@ViewInject(id = R.id.txtTokenInfo)
		TextView txtTokenInfo;
		
		@Override
		public int inflateViewId() {
			return R.layout.item_account;
		}

		@Override
		public void bindingData(View convertView, AccountBean data) {
			WeiBoUser user = data.getUser();
			
			BitmapLoader.getInstance().display(AccountFragment.this, 
							data.getUser().getAvatar_large(), imgPhoto, ImageConfigUtils.getPhotoConfig());
			
			txtName.setText(user.getScreen_name());
			txtDesc.setText(user.getDescription() + "");
			txtTokenInfo.setText(R.string.account_relogin_remind);
			if (AccountBean.isExpired(data)) {
				txtTokenInfo.setVisibility(View.VISIBLE);
			}
			else {
				txtTokenInfo.setVisibility(View.GONE);
			}
			
			if (AppContext.isLogedin()) 
				viewCover.setVisibility(data.getUser().getIdstr().equals(AppContext.getUser().getIdstr()) ? View.GONE : View.VISIBLE);
		}
		
		@Override
		public void updateConvertView(AccountBean data, View convertView, int selectedPosition) {
			super.updateConvertView(data, convertView, selectedPosition);

			container.setSelected(checkedArray.get(getPosition()));
		}
		
	}
	
	class AccountTask extends PagingTask<Void, Void, ArrayList<AccountBean>> {

		public AccountTask(RefreshMode mode) {
			super("AccountTask", mode);
		}

		@Override
		protected List<AccountBean> parseResult(ArrayList<AccountBean> result) {
			return result;
		}

		@Override
		protected ArrayList<AccountBean> workInBackground(RefreshMode mode, String previousPage,
				String nextPage, Void... params) throws TaskException {
			return (ArrayList<AccountBean>) AccountDB.query();
		}
		
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.account_choice, menu);
        mode.setTitle(R.string.title_acount);
        setSubtitle(mode);
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		isSelectedModeActivated = true;
		
		getAdapter().notifyDataSetChanged();
		return true;
	}

	@Override
	public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
		if (item.getItemId() == R.id.remove) {
			if (getListView().getCheckedItemCount() == 0) {
     			showMessage(getString(R.string.none_opts_selected));
     			return true;
     		}
			
			new AlertDialog.Builder(getActivity())
							.setTitle(R.string.remind)
							.setIconAttribute(android.R.attr.alertDialogIcon)
							.setMessage(R.string.account_destory_account_remind)
							.setNegativeButton(R.string.cancel, null)
							.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									final int size = getListView().getCheckedItemCount();
									
									new WorkTask<Void, Void, Boolean>() {

										@Override
										protected void onPrepare() {
											super.onPrepare();
											
											ViewUtils.createNormalProgressDialog(getActivity(), getString(R.string.account_delete_account_loading)).show();
										};
										
										@Override
										public Boolean workInBackground(Void... params) throws TaskException {
											SparseBooleanArray selectedItems = checkedArray;
							                for (int i = 0; i < getAdapter().getDatas().size(); i++) {
							                	if (selectedItems.get(i)) {
							                		AccountBean account = getAdapter().getDatas().get(i);
							                		
							                		Logger.w(TAG, "删除账号 uid = " + account.getUserId());
							                		AccountDB.remove(account.getUserId());
							     
							                		// 如果是登录账号，退出登录
							                		if (AppContext.isLogedin() && account.getUserId().equals(AppContext.getUser().getIdstr()))
							                			AppContext.logout();
							                	}
							                }
											return true;
										}
										
										@Override
										protected void onSuccess(Boolean result) {
											super.onSuccess(result);
											
											mode.finish();
											
											ViewUtils.dismissNormalProgressDialog();
											
											ViewUtils.showMessage(String.format(getString(R.string.account_account_deleted), size));
											
											requestData(RefreshMode.reset);
										};
										
									}.execute();
								}
							})
							.show();
		}
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		isSelectedModeActivated = false;
		checkedArray.clear();
        getListView().clearChoices();
		
		getAdapter().notifyDataSetChanged();
	}

	@Override
	public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
		final int checkedCount = getListView().getCheckedItemCount();
		if (checkedCount > 0)
			setSubtitle(mode);
		
		checkedArray.put(position, checked);
		getAdapter().notifyDataSetChanged();
	}
	
	private void setSubtitle(ActionMode mode) {
        final int checkedCount = getListView().getCheckedItemCount();
        switch (checkedCount) {
            case 0:
                mode.setSubtitle(R.string.account_account_select_remind);
                break;
            default:
            	mode.setSubtitle(String.format(getString(R.string.opts_select_remind), checkedCount));
                break;
        }
    }
	
	@Override
	public boolean onBackClick() {
		if (!AppContext.isLogedin()) {
			new AlertDialog.Builder(getActivity()).setTitle(R.string.remind)
								.setMessage(R.string.account_account_exit_remind)
								.setNegativeButton(R.string.cancel, null)
								.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										getActivity().finish();
									}
								})
								.show();
			return true;
		}
		
		return super.onBackClick();
	}
	
}
