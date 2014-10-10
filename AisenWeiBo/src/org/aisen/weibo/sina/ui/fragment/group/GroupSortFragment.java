package org.aisen.weibo.sina.ui.fragment.group;

import java.util.ArrayList;
import java.util.List;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.bean.AccountBean;
import org.aisen.weibo.sina.support.db.AccountDB;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.BaiduAnalyzeUtils;
import org.aisen.weibo.sina.ui.activity.common.FragmentContainerActivity;
import org.sina.android.SinaSDK;
import org.sina.android.bean.Group;
import org.sina.android.bean.GroupSortResult;
import org.sina.android.bean.Groups;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.m.common.utils.ActivityHelper;
import com.m.support.Inject.ViewInject;
import com.m.support.adapter.ABaseAdapter.AbstractItemView;
import com.m.support.task.TaskException;
import com.m.support.task.WorkTask;
import com.m.ui.fragment.ADragSortFragment;
import com.m.ui.utils.ViewUtils;

/**
 * 分组排序
 * 2014-09-11 修改为分组编辑页面
 * 
 * @author wangdan
 *
 */
public class GroupSortFragment extends ADragSortFragment<Group, Groups> 
								implements OnItemClickListener, MultiChoiceModeListener {

	public static void lanuch(Activity from) {
		FragmentContainerActivity.launch(from, GroupSortFragment.class, null);
	}

	private boolean sortEnable;
	private boolean isSelectedModeActivated = false;
	private SparseBooleanArray checkedArray;
	
	@Override
	protected int inflateContentView() {
		return R.layout.ui_drag_sort;
	}
	
	@Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		super.layoutInit(inflater, savedInstanceSate);
		
		sortEnable = savedInstanceSate == null ? false : savedInstanceSate.getBoolean("sortEnable");
		
		getActivity().getActionBar().setTitle(R.string.title_groups);
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		
		getRefreshView().setOnItemClickListener(this);
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		getListView().setMultiChoiceModeListener(this);
		
		checkedArray = new SparseBooleanArray();
		
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putBoolean("sortEnable", sortEnable);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (isSelectedModeActivated) {
			getListView().setItemChecked(position, !getListView().isItemChecked(position));
		}
	}

	@Override
	protected AbstractItemView<Group> newItemView() {
		return new GroupSortItemView();
	}

	@Override
	protected void requestData(RefreshMode mode) {
		copyAndSet();
	}
	
	private void copyAndSet() {
		ArrayList<Group> groupList = new ArrayList<Group>();
		for (Group group : AppContext.getGroups().getLists())
			groupList.add(group);
		setItems(groupList);
	}
	
	@Override
	protected void onItemDroped(int from, int to) {
		super.onItemDroped(from, to);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		
		inflater.inflate(R.menu.group_sort, menu);
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		menu.findItem(R.id.sort).setVisible(!sortEnable);
		menu.findItem(R.id.add).setVisible(!sortEnable);
		menu.findItem(R.id.submit).setVisible(sortEnable);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.sort) {
			sortEnable = true;
			getListView().setDragEnabled(sortEnable);
			
			getListView().setChoiceMode(ListView.CHOICE_MODE_NONE);
		}
		else if (item.getItemId() == R.id.add) {
			createGroup(null);
		}
		else if (item.getItemId() == R.id.submit) {
			new SortGroupTask().executeOnSerialExecutor();
		}
		
		getActivity().invalidateOptionsMenu();
		getAdapter().notifyDataSetChanged();
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onHomeClick() {
		return super.onBackClick();
	}
	
	@Override
	public boolean onBackClick() {
		if (sortEnable) {
			new AlertDialog.Builder(getActivity()).setMessage(R.string.group_sort_cancel_remind)
								.setNegativeButton(R.string.bu, null)
								.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										sortEnable = false;
										getListView().setDragEnabled(false);
										
										getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
										
										getActivity().invalidateOptionsMenu();
										
										copyAndSet();
									}
									
								})
								.show();
			
			return true;
		}
		
		return super.onBackClick();
	}
	
	/**
	 * 创建分组
	 * 
	 * @param group
	 */
	private void createGroup(String group) {
		View entryView = View.inflate(getActivity(), R.layout.lay_dialog_remark_entry, null);
		final EditText editRemark = (EditText) entryView.findViewById(R.id.editRemark);
		editRemark.setHint(R.string.group_group_name_hint);
		if (!TextUtils.isEmpty(group))
			editRemark.setText(group);
		editRemark.setSelection(editRemark.getText().toString().length());
		new AlertDialog.Builder(getActivity()).setTitle(R.string.group_create_group)
							.setView(entryView)
							.setNegativeButton(R.string.cancel, null)
							.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									if (TextUtils.isEmpty(editRemark.getText().toString())) {
										showMessage(R.string.group_create_group_hint);
										
										return;
									}
									
									new CreateGroupTask().execute(editRemark.getText().toString());
								}
										
							})
							.show();
	}
	
	/**
	 * 更新分组名称
	 * 
	 * @param group
	 * @param rename
	 */
	private void modifyGroup(final Group group, String rename) {
		View entryView = View.inflate(getActivity(), R.layout.lay_dialog_remark_entry, null);
		final EditText editRemark = (EditText) entryView.findViewById(R.id.editRemark);
		editRemark.setHint(R.string.group_group_name_hint);
		if (!TextUtils.isEmpty(rename))
			editRemark.setText(rename);
		editRemark.setSelection(editRemark.getText().toString().length());
		new AlertDialog.Builder(getActivity()).setTitle(R.string.group_update_group_name)
							.setView(entryView)
							.setNegativeButton(R.string.cancel, null)
							.setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									if (TextUtils.isEmpty(editRemark.getText().toString())) {
										showMessage(R.string.group_create_group_hint);
										
										return;
									}
									
									new ModifyGroupTask(group).execute(editRemark.getText().toString());
								}
										
							})
							.show();
	}
	
	/**
	 * 删除分组
	 * 
	 * @param group
	 * @param rename
	 */
	private void destoryGroup(final List<Group> groupList, final ActionMode mode) {
		new AlertDialog.Builder(getActivity()).setTitle(R.string.group_del_group_title)
							.setMessage(R.string.group_del_group_confirm)
							.setNegativeButton(R.string.cancel, null)
							.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									mode.finish();
									
									new DestoryGroupTask(groupList).execute();
								}
										
							})
							.show();
	}
	
	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.group_edit, menu);
        mode.setTitle(R.string.title_edit_group);
        setSubtitle(mode);
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode arg0) {
		isSelectedModeActivated = false;
		checkedArray.clear();
        getListView().clearChoices();
		
		getAdapter().notifyDataSetChanged();
	}

	@Override
	public boolean onPrepareActionMode(ActionMode arg0, Menu menu) {
		final int checkedCount = getListView().getCheckedItemCount();
        menu.findItem(R.id.edit).setVisible(checkedCount <= 1);
		
		isSelectedModeActivated = true;
		
		getAdapter().notifyDataSetChanged();
		return true;
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
    	mode.setSubtitle(String.format(getString(R.string.opts_select_remind), checkedCount));

    	mode.invalidate();
    }

	@Override
	public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
		if (item.getItemId() == R.id.delete) {
			if (getListView().getCheckedItemCount() == 0) {
     			showMessage(R.string.none_opts_selected);
     			return true;
     		}
			
			List<Group> groupList = new ArrayList<Group>();
			SparseBooleanArray selectedItems = checkedArray;
            for (int i = 0; i < getAdapter().getDatas().size(); i++) {
            	if (selectedItems.get(i)) {
            		Group group = getAdapter().getDatas().get(i);
            		
            		groupList.add(group);
            	}
            }
            
			destoryGroup(groupList, mode);
		}
		else if (item.getItemId() == R.id.edit) {
			SparseBooleanArray selectedItems = checkedArray;
            for (int i = 0; i < getAdapter().getDatas().size(); i++) {
            	if (selectedItems.get(i)) {
            		Group group = getAdapter().getDatas().get(i);
            		
            		modifyGroup(group, group.getName());
            	}
            }
            mode.finish();
		}
		return true;
	}
	
	/**
	 * 更新分组
	 * 
	 * @author Jeff.Wang
	 *
	 * @date 2014年9月13日
	 */
	class ModifyGroupTask extends WorkTask<String, Void, Group> {
		
		Group group;
		
		ModifyGroupTask(Group group) {
			this.group = group;
		}
		
		@Override
		protected void onPrepare() {
			super.onPrepare();
			
			ViewUtils.createNormalProgressDialog(getActivity(), getString(R.string.group_update_group_loading)).show();
		};

		@Override
		public Group workInBackground(String... params) throws TaskException {
			Group result = SinaSDK.getInstance(AppContext.getToken()).friendshipGroupsUpdate(group.getIdstr(), params[0], null, null);
			
			group.setName(getParams()[0]);
			
			AccountBean accountBean = AccountDB.getLogedinAccount();
			AppContext.getGroups().setLists(getAdapter().getDatas());
			accountBean.setGroups(AppContext.getGroups());
			
			AccountDB.newAccount(accountBean);
			AccountDB.setLogedinAccount(accountBean);
			
			return result; 
		}

		@Override
		protected void onFailure(TaskException exception) {
			super.onFailure(exception);
			
			showMessage(exception.getMessage());
			
			modifyGroup(group, getParams()[0]);
		};

		@Override
		protected void onSuccess(Group result) {
			super.onSuccess(result);
			
			// 更新首页排序
			ActivityHelper.getInstance().putBooleanShareData("ChanneSortHasChanged", true);
			
			copyAndSet();
		};
		
		@Override
		protected void onFinished() {
			super.onFinished();
			
			ViewUtils.dismissNormalProgressDialog();
		};
	}
	
	/**
	 * 新建分组
	 * 
	 * @author Jeff.Wang
	 *
	 * @date 2014年9月13日
	 */
	class CreateGroupTask extends WorkTask<String, Void, Group> {
		
		@Override
		protected void onPrepare() {
			super.onPrepare();
			
			ViewUtils.createNormalProgressDialog(getActivity(), getString(R.string.group_create_group_loading)).show();
		};

		@Override
		public Group workInBackground(String... params) throws TaskException {
			return SinaSDK.getInstance(AppContext.getToken()).friendshipsGroupsCreate(params[0], null, null);
		}

		@Override
		protected void onFailure(TaskException exception) {
			super.onFailure(exception);
			
			showMessage(exception.getMessage());
			
			createGroup(getParams()[0]);
		};

		@Override
		protected void onSuccess(Group result) {
			super.onSuccess(result);
			
			AppContext.getGroups().getLists().add(result);
			
			// 更新首页排序
			ActivityHelper.getInstance().putBooleanShareData("ChanneSortHasChanged", true);
			
			copyAndSet();
		};
		
		@Override
		protected void onFinished() {
			super.onFinished();
			
			ViewUtils.dismissNormalProgressDialog();
		};
	}
	
	/**
	 * 删除分组
	 * 
	 * @author Jeff.Wang
	 *
	 * @date 2014年9月13日
	 */
	class DestoryGroupTask extends WorkTask<String, String, Boolean> {
		
		List<Group> groupList;
		
		DestoryGroupTask(List<Group> groupList) {
			this.groupList = groupList;
		}
		
		@Override
		protected void onPrepare() {
			super.onPrepare();
			
			ViewUtils.createNormalProgressDialog(getActivity(), getString(R.string.group_del_group_loading)).show();
		};
		
		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);

			if (getActivity() == null)
				return;
			if (values != null) {
				if ("1".equals(values[0]))
					ViewUtils.updateNormalProgressDialog(String.format(getString(R.string.group_del_group_update), values[1]));
				else
					showMessage(String.format(getString(R.string.group_del_group_faild), values[1]));
			}
		}

		@Override
		public Boolean workInBackground(String... params) throws TaskException {
			for (int i = 0; i < groupList.size(); i++) {
				Group group = groupList.get(i);
				try {
					// 删除分组
					publishProgress("1", group.getName());
					SinaSDK.getInstance(AppContext.getToken()).friendshipGroupsDestory(group.getIdstr());
					
					// 删除本地分组
					getAdapter().removeItem(group);
					
					AccountBean accountBean = AccountDB.getLogedinAccount();
					AppContext.getGroups().setLists(getAdapter().getDatas());
					accountBean.setGroups(AppContext.getGroups());
					
					AccountDB.newAccount(accountBean);
					AccountDB.setLogedinAccount(accountBean);	
					
					// 更新首页排序
					ActivityHelper.getInstance().putBooleanShareData("ChanneSortHasChanged", true);
				} catch (Exception e) {
					publishProgress("0", group.getName());
				}
			}
			
			return true;
		}

		@Override
		protected void onSuccess(Boolean result) {
			super.onSuccess(result);
			
			copyAndSet();
		};
		
		@Override
		protected void onFinished() {
			super.onFinished();
			
			ViewUtils.dismissNormalProgressDialog();
		};
	}
	
	/**
	 * 分组排序
	 * 
	 * @author Jeff.Wang
	 *
	 * @date 2014年9月13日
	 */
	class SortGroupTask extends WorkTask<Void, Void, GroupSortResult> {

		@Override
		protected void onPrepare() {
			super.onPrepare();
			
			ViewUtils.createNormalProgressDialog(getActivity(), getString(R.string.group_sort_group_loading)).show();
		}
		
		@Override
		public GroupSortResult workInBackground(Void... params) throws TaskException {
			GroupSortResult result = SinaSDK.getInstance(AppContext.getToken()).friendshipGroupsOrder(getAdapter().getDatas());
			
			AccountBean accountBean = AccountDB.getLogedinAccount();
			AppContext.getGroups().setLists(getAdapter().getDatas());
			accountBean.setGroups(AppContext.getGroups());
			
			AccountDB.newAccount(accountBean);
			AccountDB.setLogedinAccount(accountBean);
			
			return result;
		}
		
		@Override
		protected void onFailure(TaskException exception) {
			super.onFailure(exception);
			
			showMessage(exception.getErrorMsg());
		}
		
		@Override
		protected void onSuccess(GroupSortResult result) {
			super.onSuccess(result);
			
			if (getActivity() == null)
				return;
			
			sortEnable = false;
			getListView().setDragEnabled(false);
			getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
			
			getActivity().invalidateOptionsMenu();
			getAdapter().notifyDataSetChanged();
			
			// 更新首页排序
			ActivityHelper.getInstance().putBooleanShareData("ChanneSortHasChanged", true);
			
			BaiduAnalyzeUtils.onEvent("sort_group", "分组排序");
			
			if ("true".equals(result.getResult()))
				showMessage(R.string.update_success);
			else
				showMessage(R.string.update_faild);
		};
		
		@Override
		protected void onFinished() {
			super.onFinished();
			
			ViewUtils.dismissNormalProgressDialog();
		}
		
	}
	
	class GroupSortItemView extends AbstractItemView<Group> {

		@ViewInject(id = R.id.drag_handle)
		View dragHandle;
		@ViewInject(id = R.id.txtName)
		TextView txtName;
		@ViewInject(id = R.id.container)
		View container;
		
		@Override
		public int inflateViewId() {
			return R.layout.item_group_sort;
		}

		@Override
		public void bindingData(View convertView, Group data) {
			txtName.setText(data.getName());
			
			dragHandle.setVisibility(sortEnable ? View.VISIBLE : View.GONE);
		}
		
		@Override
		public void updateConvertView(Group data, View convertView, int selectedPosition) {
			super.updateConvertView(data, convertView, selectedPosition);
			
			container.setSelected(checkedArray.get(getPosition()));
			convertView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT));
		}
		
	}
	
}
