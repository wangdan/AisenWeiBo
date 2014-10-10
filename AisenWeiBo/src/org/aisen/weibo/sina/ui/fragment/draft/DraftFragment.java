package org.aisen.weibo.sina.ui.fragment.draft;

import java.util.ArrayList;
import java.util.List;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.MyApplication;
import org.aisen.weibo.sina.support.bean.PublishBean;
import org.aisen.weibo.sina.support.bean.PublishType;
import org.aisen.weibo.sina.support.publish.PublishDB;
import org.aisen.weibo.sina.support.publish.PublishManager;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.BaiduAnalyzeUtils;
import org.aisen.weibo.sina.sys.service.PublishService;
import org.aisen.weibo.sina.ui.activity.publish.PublishActivity;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;
import org.aisen.weibo.sina.ui.widget.AisenTextView;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.ListView;
import android.widget.TextView;

import com.m.common.context.GlobalContext;
import com.m.common.utils.ActivityHelper;
import com.m.common.utils.DateUtils;
import com.m.support.Inject.ViewInject;
import com.m.support.adapter.ABaseAdapter.AbstractItemView;
import com.m.support.task.TaskException;
import com.m.support.task.WorkTask;
import com.m.ui.fragment.ABaseFragment;
import com.m.ui.fragment.AListFragment;
import com.m.ui.utils.ViewUtils;

/**
 * 草稿箱
 * 
 * @author wangdan
 *
 */
public class DraftFragment extends AListFragment<PublishBean, ArrayList<PublishBean>> 
								implements OnItemClickListener, MultiChoiceModeListener {

	public static ABaseFragment newInstance() {
		return new DraftFragment();
	}
	
	private BizFragment bizFragment;
	private boolean isSelectedModeActivated = false;
	private SparseBooleanArray checkedArray;
	
	@Override
	protected int inflateContentView() {
		return R.layout.ui_draft;
	}
	
	@Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		super.layoutInit(inflater, savedInstanceSate);
		
		getRefreshView().setOnItemClickListener(this);
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		getListView().setMultiChoiceModeListener(this);
		
		checkedArray = new SparseBooleanArray();
		
		try {
			bizFragment = BizFragment.getBizFragment(this);
		} catch (Exception e) {
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (isSelectedModeActivated) {
			getListView().setItemChecked(position, !getListView().isItemChecked(position));
		}
		else {
			PublishBean bean = getAdapter().getDatas().get(position);
			
			switch (bean.getType()) {
			case status:
				PublishActivity.publishStatus(getActivity(), bean);
				break;
			case statusRepost:
				PublishActivity.publishStatusRepost(getActivity(), bean, bean.getStatusContent());
				break;
			case commentReply:
				PublishActivity.publishCommentReply(getActivity(), bean, bean.getStatusComment(), false);
				break;
			case commentCreate:
				PublishActivity.publishStatusComment(getActivity(), bean, bean.getStatusContent());
				break;
			default:
				break;
			}
		}
	}

	@Override
	protected AbstractItemView<PublishBean> newItemView() {
		return new DraftboxItemView();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(PublishManager.ACTION_PUBLISH_CHANNGED);
		getActivity().registerReceiver(receiver, filter);
		
		new DraftTask(RefreshMode.reset).execute();
		
		BaiduAnalyzeUtils.onPageStart("草稿箱");
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		getActivity().unregisterReceiver(receiver);
		
		BaiduAnalyzeUtils.onPageEnd("草稿箱");
	}
	
	BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null && PublishManager.ACTION_PUBLISH_CHANNGED.equals(intent.getAction()))
				new DraftTask(RefreshMode.reset).execute();
		}
		
	};

	@Override
	protected void requestData(RefreshMode mode) {
	}
	
	class DraftTask extends PagingTask<Void, Void, ArrayList<PublishBean>> {

		public DraftTask(RefreshMode mode) {
			super("DraftTask", mode);
		}

		@Override
		protected List<PublishBean> parseResult(ArrayList<PublishBean> result) {
			return result;
		}

		@Override
		protected ArrayList<PublishBean> workInBackground(RefreshMode mode, String previousPage,
				String nextPage, Void... params) throws TaskException {
			return PublishDB.getPublishList(AppContext.getUser());
		}
		
	}
	
	class DraftboxItemView extends AbstractItemView<PublishBean> {

		@ViewInject(id = R.id.txtType)
		TextView txtType;
		@ViewInject(id = R.id.txtTiming)
		TextView txtTiming;
		@ViewInject(id = R.id.txtContent)
		AisenTextView txtContent;
		@ViewInject(id = R.id.txtError)
		TextView txtError;
		@ViewInject(id = R.id.container)
		View container;

		@Override
		public int inflateViewId() {
			return R.layout.item_draft;
		}

		@Override
		public void bindingData(View convertView, PublishBean data) {
			txtTiming.setVisibility(View.GONE);
			
			PublishType type = data.getType();
			if (type == PublishType.status) {
				txtType.setText(R.string.draft_type_status);
				if (data.getTiming() > 0) {
					txtTiming.setText(String.format(
											getString(R.string.draft_timing_hint), 
											DateUtils.formatDate(data.getTiming(), getString(R.string.draft_date_format))));
					txtTiming.setVisibility(View.VISIBLE);
				}
			}
			else if (type == PublishType.commentCreate)
				txtType.setText(R.string.draft_type_create_cmt);
			else if (type == PublishType.commentReply)
				txtType.setText(R.string.draft_type_reply_cmt);
			else if (type == PublishType.statusRepost)
				txtType.setText(R.string.draft_type_repost_status);

			txtContent.setContent(data.getText());
			
			if (data.getTiming() > 0 && data.getTiming() < System.currentTimeMillis()) {
				txtError.setVisibility(View.VISIBLE);
					
				txtError.setText(R.string.draft_timing_expired);
			}
			else {
				if (!TextUtils.isEmpty(data.getErrorMsg()))
					txtError.setText(data.getErrorMsg());
				txtError.setVisibility(TextUtils.isEmpty(data.getErrorMsg()) ? View.GONE : View.VISIBLE);
			}
			
			if (bizFragment != null)
				bizFragment.bindOnTouchListener(txtContent);
		}

		@Override
		public void updateConvertView(PublishBean data, View convertView, int selectedPosition) {
			super.updateConvertView(data, convertView, selectedPosition);

			container.setSelected(checkedArray.get(getPosition()));
			convertView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT));
		}
		
	}
	
	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.draft, menu);
        menu.findItem(R.id.clear).setVisible(false);
        mode.setTitle(R.string.title_draft);
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
	public boolean onPrepareActionMode(ActionMode arg0, Menu arg1) {
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
        switch (checkedCount) {
            case 0:
                mode.setSubtitle(R.string.draft_del_draft_remind);
                break;
            default:
            	mode.setSubtitle(String.format(getString(R.string.opts_select_remind), checkedCount));
                break;
        }
    }

	@Override
	public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
		if (item.getItemId() == R.id.delete) {
			if (getListView().getCheckedItemCount() == 0) {
     			showMessage(R.string.none_opts_selected);
     			return true;
     		}
			
			deleteDraft(mode);
		}
		else if (item.getItemId() == R.id.reSend) {
			SparseBooleanArray selectedItems = checkedArray;
            for (int i = 0; i < getAdapter().getDatas().size(); i++) {
            	if (selectedItems.get(i)) {
            		PublishService.publish(getActivity(), getAdapter().getDatas().get(i));
            	}
            }
            mode.finish();
		}
		else if (item.getItemId() == R.id.clear) {
			
		}
		return true;
	}
	
	private void deleteDraft(final ActionMode mode) {
		new AlertDialog.Builder(getActivity()).setTitle(R.string.remind)
										.setMessage(R.string.draft_del_confirm)
										.setNegativeButton(R.string.cancel, null)
										.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

											@Override
											public void onClick(DialogInterface dialog, int which) {
												if (getListView().getCheckedItemCount() > 0) {
									                final int size = getListView().getCheckedItemCount();
									       		 
										       		 new WorkTask<Void, Void, ArrayList<PublishBean>>() {
	
											       			@Override
											               	protected void onPrepare() {
											       				ViewUtils.createNormalProgressDialog(getActivity(), getString(R.string.draft_del_draft_loading)).show();
											               	};
											               	
											               	@Override
											               	protected void onSuccess(java.util.ArrayList<PublishBean> result) {
											               		getAdapter().setDatasAndRefresh(result);
											               		
											               		showMessage(String.format(getString(R.string.draft_del_draft_hint), size));
											               		
											               		Intent intent = new Intent();
											            		intent.setAction(PublishManager.ACTION_PUBLISH_CHANNGED);
											            		GlobalContext.getInstance().sendBroadcast(intent);
											               	};
											               	
											               	@Override
											               	protected void onFinished() {
											               		ViewUtils.dismissNormalProgressDialog();
											               		
											               		mode.finish();
											               		
											               		new DraftTask(RefreshMode.reset).execute();
											               	};
										               	 
															@Override
															public ArrayList<PublishBean> workInBackground(Void... params) throws TaskException {
																try {
																	Thread.sleep(500);
																} catch (Exception e) {
																}
																
																ActivityHelper.getInstance().putBooleanShareData("ChanneSortHasChanged", true);
	
												        		SparseBooleanArray selectedItems = checkedArray;
												                ArrayList<PublishBean> unselectedBeans = new ArrayList<PublishBean>();
												                for (int i = 0; i < getAdapter().getDatas().size(); i++) {
												                	if (selectedItems.get(i)) {
												                		if (getAdapter().getDatas().get(i).getTiming() > 0)
												                			MyApplication.removePublishAlarm(getAdapter().getDatas().get(i));
												                		 
												                		PublishDB.deletePublish(getAdapter().getDatas().get(i), AppContext.getUser());
												                	}
												                }
																
																return unselectedBeans;
															}
															
										                }.execute();
													}												
												}
											
										})
										.show();
	}

}
