package org.aisen.weibo.sina.ui.fragment.search;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.bean.SearchHisotyBean;
import org.aisen.weibo.sina.support.db.SinaDB;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.m.support.Inject.ViewInject;
import com.m.support.adapter.ABaseAdapter.AbstractItemView;
import com.m.support.sqlite.util.FieldUtils;
import com.m.support.task.TaskException;
import com.m.support.task.WorkTask;
import com.m.ui.fragment.AListFragment;

/**
 * 搜索历史记录
 * 
 * @author wangdan
 *
 */
public class SearchHistoryFragment extends AListFragment<SearchHisotyBean, ArrayList<SearchHisotyBean>>
										implements OnItemClickListener {

	public static SearchHistoryFragment newInstance(Type type) {
		SearchHistoryFragment fragment = new SearchHistoryFragment();
		
		Bundle args = new Bundle();
		args.putString("type", type.toString());
		fragment.setArguments(args);
		
		return fragment;
	}
	
	public static void addQuery(Type type, String query) {
		String selection = " type = ? and query = ? ";
		String[] selectionArgs = new String[]{ type.toString(), query };
		List<SearchHisotyBean> beans = SinaDB.getSqlite().selectAll(SearchHisotyBean.class, selection, selectionArgs);
		
		// 已存在就更新
		if (beans.size() > 0) {
			SearchHisotyBean bean = beans.get(0);
			
			SinaDB.getSqlite().update(null, bean);
		}
		else {
			SearchHisotyBean bean = new SearchHisotyBean();
			bean.setBeanId(UUID.randomUUID().toString());
			bean.setType(type.toString());
			bean.setQuery(query);
			
			SinaDB.getSqlite().insert(null, bean);
		}
		
	}

	public enum Type {
		user, status
	}
	
	private SearchTask mTask;
	private Type mType;
	
	private OnSearchItemClicked onseaItemClicked;
	
	@Override
	protected int inflateContentView() {
		return R.layout.ui_search_history;
	}
	
	@Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		super.layoutInit(inflater, savedInstanceSate);
		
		String type = savedInstanceSate == null ? getArguments().getString("type")
												: savedInstanceSate.getString("type");
		mType = Type.valueOf(type);
		
		getRefreshView().setOnItemClickListener(this);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putString("type", mType.toString());
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (position == getAdapter().getDatas().size() - 1) {
			new AlertDialog.Builder(getActivity()).setTitle(R.string.remind)
											.setIconAttribute(android.R.attr.alertDialogIcon)
											.setMessage(R.string.search_clear_search_remind)
											.setNegativeButton(R.string.cancel, null)
											.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

												@Override
												public void onClick(DialogInterface dialog, int which) {
													setItems(new ArrayList<SearchHisotyBean>());
													
													new WorkTask<Void, Void, Void>() {

														@Override
														public Void workInBackground(Void... params) throws TaskException {
															String whereClause = String.format(" %s = ? ", "type");
															String[] whereArgs = new String[]{ mType.toString() };
															
															SinaDB.getSqlite().delete(SearchHisotyBean.class, whereClause, whereArgs);
															return null;
														}
														
													}.execute();
												}
												
											})
											.show();
		}
		else {
			onseaItemClicked.onItemClicked(getAdapter().getDatas().get(position).getQuery());
		}
	}

	@Override
	protected AbstractItemView<SearchHisotyBean> newItemView() {
		return new SearchUsersItemView();
	}
	
	@Override
	public boolean onAcUnusedDoubleClicked() {
		return false;
	}
	
	public boolean _onAcUnusedDoubleClicked() {
		return super.onAcUnusedDoubleClicked();
	}
	
	@Override
	protected void requestData(RefreshMode mode) {
		new SearchTask().execute();
	}
	
	public void query() {
		requestData(RefreshMode.reset);
	}
	
	class SearchUsersItemView extends AbstractItemView<SearchHisotyBean> {

		@ViewInject(id = R.id.txtQuery)
		TextView txtQuery;
		@ViewInject(id = R.id.layItem)
		View layItem;
		@ViewInject(id = R.id.btnClearHistory)
		TextView btnClearHistory;
		
		@Override
		public int inflateViewId() {
			return R.layout.item_search_history;
		}

		@Override
		public void bindingData(View convertView, SearchHisotyBean data) {
			if (getPosition() == getAdapter().getCount() - 1) {
				layItem.setVisibility(View.GONE);
				btnClearHistory.setVisibility(View.VISIBLE);
				
				btnClearHistory.setText(data.getQuery());
			}
			else {
				layItem.setVisibility(View.VISIBLE);
				btnClearHistory.setVisibility(View.GONE);
				
				txtQuery.setText(data.getQuery());
			}
		}
		
	}
	
	class SearchTask extends WorkTask<Void, Void, ArrayList<SearchHisotyBean>> {

		public SearchTask() {
			if (mTask != null)
				mTask.cancel(true);
			
			mTask = this;
		}
		
		@Override
		public ArrayList<SearchHisotyBean> workInBackground(Void... params) throws TaskException {
			String selection = String.format(" %s = ? ", "type");
			String[] selectionArgs = new String[]{  mType.toString() };
			
			return (ArrayList<SearchHisotyBean>) SinaDB.getSqlite().selectAll(SearchHisotyBean.class, selection, 
														selectionArgs, String.format(" %s desc ", FieldUtils.CREATEAT), " 10 ");
		}
		
		@Override
		protected void onSuccess(ArrayList<SearchHisotyBean> result) {
			super.onSuccess(result);
			if (getActivity() == null)
				return;
			
			if (result.size() > 0) {
				SearchHisotyBean clearBean  = new SearchHisotyBean();
				clearBean.setQuery(getString(R.string.hint_clear_history));
				result.add(result.size(), clearBean);
				
				setItems(result);
			}
			else {
				setItems(new ArrayList<SearchHisotyBean>());
			}
		}
		
		@Override
		protected void onFinished() {
			super.onFinished();
			
			mTask = null;
		}
	
	}
	
	public void setOnseaItemClicked(OnSearchItemClicked onseaItemClicked) {
		this.onseaItemClicked = onseaItemClicked;
	}

	public interface OnSearchItemClicked {
		
		public void onItemClicked(String query);
		
	}

}
