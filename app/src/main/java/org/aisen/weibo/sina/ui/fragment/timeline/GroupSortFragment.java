package org.aisen.weibo.sina.ui.fragment.timeline;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import org.aisen.android.common.utils.ViewUtils;
import org.aisen.android.component.container.FragmentContainerActivity;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.adapter.ABaseAdapter.AbstractItemView;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.ADragSortFragment;
import org.aisen.android.ui.fragment.ARefreshFragment;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.Group;
import org.aisen.weibo.sina.sinasdk.bean.GroupSortResult;
import org.aisen.weibo.sina.sinasdk.bean.Groups;
import org.aisen.weibo.sina.support.bean.AccountBean;
import org.aisen.weibo.sina.support.db.AccountDB;
import org.aisen.weibo.sina.support.utils.ThemeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 分组排序
 * 2014-09-11 修改为分组编辑页面
 *
 * @author wangdan
 *
 */
public class GroupSortFragment extends ADragSortFragment<Group, Groups> {

	public static void lanuch(Activity from) {
		FragmentContainerActivity.launch(from, GroupSortFragment.class, null);
	}


	@ViewInject(id = R.id.dragsortListview)
	DragSortListView mDragSortListView;

	@Override
	protected int inflateContentView() {
		return R.layout.as_ui_drag_sort;
	}

	@Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		super.layoutInit(inflater, savedInstanceSate);

		BaseActivity activity = (BaseActivity) getActivity();
		activity.getSupportActionBar().setTitle(R.string.title_groups);
		activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		getRefreshView().setOnItemClickListener(this);
        getListView().setDragEnabled(true);

		setHasOptionsMenu(true);
	}

	@Override
	protected DragSortController buildController(DragSortListView dslv) {
		DragSortController controller = super.buildController(dslv);
		controller.setBackgroundColor(Color.parseColor("#66000000"));
		return controller;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String[] items = new String[]{ getString(R.string.title_edit_group), getString(R.string.group_del_group_title) };

        final Group group = getAdapterItems().get(position);

        new AlertDialogWrapper.Builder(getActivity()).setTitle(group.getName())
                                            .setItems(items, new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    switch (which) {
                                                    // 编辑分组
                                                    case 0:
                                                        modifyGroup(group, group.getName());
                                                        break;
                                                    // 删除分组
                                                    case 1:
                                                        destoryGroup(group);
                                                        break;
                                                    }
                                                }

                                            })
                                            .setCancelable(true)
                                            .show();
	}

	@Override
	protected AbstractItemView<Group> newItemView() {
		return new GroupSortItemView();
	}

	@Override
	protected void requestData(ARefreshFragment.RefreshMode mode) {
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

        if (from != to)
            new SortGroupTask().executeOnSerialExecutor();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.menu_group_sort, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.add) {
			createGroup(null);
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * 创建分组
	 *
	 * @param group
	 */
	private void createGroup(String group) {
		View entryView = View.inflate(getActivity(), R.layout.as_lay_dialog_remark_entry, null);
		final EditText editRemark = (EditText) entryView.findViewById(R.id.editRemark);
		editRemark.setHint(R.string.group_group_name_hint);
		if (!TextUtils.isEmpty(group))
			editRemark.setText(group);
		editRemark.setSelection(editRemark.getText().toString().length());
		new AlertDialogWrapper.Builder(getActivity()).setTitle(R.string.group_create_group)
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
		View entryView = View.inflate(getActivity(), R.layout.as_lay_dialog_remark_entry, null);
		final EditText editRemark = (EditText) entryView.findViewById(R.id.editRemark);
		editRemark.setHint(R.string.group_group_name_hint);
		if (!TextUtils.isEmpty(rename))
			editRemark.setText(rename);
		editRemark.setSelection(editRemark.getText().toString().length());
		new AlertDialogWrapper.Builder(getActivity()).setTitle(R.string.group_update_group_name)
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
	 */
	private void destoryGroup(final Group group) {
        new AlertDialogWrapper.Builder(getActivity())
                                .setTitle(group.getName())
                                .setMessage(R.string.group_del_group_confirm)
                                .setNegativeButton(R.string.cancel, null)
                                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        new DestoryGroupTask(group).execute();
                                    }

                                })
                                .show();
	}

	/**
	 * 更新分组
	 */
	class ModifyGroupTask extends WorkTask<String, Void, Group> {

		Group group;

		ModifyGroupTask(Group group) {
			this.group = group;
		}

		@Override
		protected void onPrepare() {
			super.onPrepare();

			ViewUtils.createProgressDialog(getActivity(), getString(R.string.group_update_group_loading), ThemeUtils.getThemeColor()).show();
		};

		@Override
		public Group workInBackground(String... params) throws TaskException {
			Group result = SinaSDK.getInstance(AppContext.getToken()).friendshipGroupsUpdate(group.getIdstr(), params[0], null, null);

			group.setName(getParams()[0]);

			AccountBean accountBean = AccountDB.getLogedinAccount();
			AppContext.getGroups().setLists(getAdapterItems());
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

			onGroupChanged();

			copyAndSet();
		};

		@Override
		protected void onFinished() {
			super.onFinished();

			ViewUtils.dismissProgressDialog();
		};
	}

	public void onGroupChanged() {
		TimelineTabsFragment.setGroupChanged(true);
	}

	/**
	 * 新建分组
	 */
	class CreateGroupTask extends WorkTask<String, Void, Group> {

		@Override
		protected void onPrepare() {
			super.onPrepare();

			ViewUtils.createProgressDialog(getActivity(), getString(R.string.group_create_group_loading), ThemeUtils.getThemeColor()).show();
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

			onGroupChanged();

			copyAndSet();
		};

		@Override
		protected void onFinished() {
			super.onFinished();

			ViewUtils.dismissProgressDialog();
		};
	}

	/**
	 * 删除分组
	 */
	class DestoryGroupTask extends WorkTask<String, String, Boolean> {

		Group group;

		DestoryGroupTask(Group group) {
			this.group = group;
		}

		@Override
		protected void onPrepare() {
			super.onPrepare();

			ViewUtils.createProgressDialog(getActivity(), getString(R.string.group_del_group_loading), ThemeUtils.getThemeColor()).show();
		};

		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);

			if (getActivity() == null)
				return;
			if (values != null) {
				if ("1".equals(values[0]))
					ViewUtils.updateProgressDialog(String.format(getString(R.string.group_del_group_update), values[1]));
				else
					showMessage(String.format(getString(R.string.group_del_group_faild), values[1]));
			}
		}

		@Override
		public Boolean workInBackground(String... params) throws TaskException {
            try {
                // 删除分组
                publishProgress("1", group.getName());
                SinaSDK.getInstance(AppContext.getToken()).friendshipGroupsDestory(group.getIdstr());

                // 删除本地分组
                getAdapterItems().remove(group);

                AccountBean accountBean = AccountDB.getLogedinAccount();
                AppContext.getGroups().setLists(getAdapterItems());
                accountBean.setGroups(AppContext.getGroups());

                AccountDB.newAccount(accountBean);
                AccountDB.setLogedinAccount(accountBean);

                onGroupChanged();
            } catch (Exception e) {
                publishProgress("0", group.getName());
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

			ViewUtils.dismissProgressDialog();
		};
	}

	/**
	 * 分组排序
	 */
	class SortGroupTask extends WorkTask<Void, Void, GroupSortResult> {

		@Override
		protected void onPrepare() {
			super.onPrepare();

			ViewUtils.createProgressDialog(getActivity(), getString(R.string.group_sort_group_loading), ThemeUtils.getThemeColor()).show();
		}

		@Override
		public GroupSortResult workInBackground(Void... params) throws TaskException {
			List<Group> groupList = getAdapterItems();
			GroupSortResult result = SinaSDK.getInstance(AppContext.getToken()).friendshipGroupsOrder(groupList);

			AccountBean accountBean = AccountDB.getLogedinAccount();
			AppContext.getGroups().setLists(getAdapterItems());
			accountBean.setGroups(AppContext.getGroups());

			AccountDB.newAccount(accountBean);
			AccountDB.setLogedinAccount(accountBean);

			return result;
		}

		@Override
		protected void onFailure(TaskException exception) {
			super.onFailure(exception);

			showMessage(exception.getMessage());
		}

		@Override
		protected void onSuccess(GroupSortResult result) {
			super.onSuccess(result);

			if (getActivity() == null)
				return;

            TimelineTabsFragment.setGroupChanged(true);

			getActivity().invalidateOptionsMenu();
			getAdapter().notifyDataSetChanged();

			onGroupChanged();

			if ("true".equals(result.getResult()))
				showMessage(R.string.update_success);
			else
				showMessage(R.string.update_faild);
		};

		@Override
		protected void onFinished() {
			super.onFinished();

			ViewUtils.dismissProgressDialog();
		}

	}

	class GroupSortItemView extends AbstractItemView<Group> {

		@ViewInject(id = R.id.drag_handle)
		View dragHandle;
		@ViewInject(id = R.id.txtName)
		TextView txtName;
        @ViewInject(id = R.id.txtCounter)
        TextView txtCounter;
		@ViewInject(id = R.id.container)
		View container;

		@Override
		public int inflateViewId() {
			return R.layout.as_item_group_sort;
		}

		@Override
		public void bindingData(View convertView, Group data) {
			txtName.setText(data.getName());
            txtCounter.setText(String.format("%s人", data.getMember_count()));
		}

		@Override
		public void updateConvertView(Group data, View convertView, int selectedPosition) {
			super.updateConvertView(data, convertView, selectedPosition);

			convertView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT));
		}

	}


}
