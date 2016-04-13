package org.aisen.weibo.sina.ui.fragment.publish;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.DateUtils;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.network.http.Params;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.sinasdk.bean.Group;
import org.aisen.weibo.sina.sinasdk.bean.Groups;
import org.aisen.weibo.sina.support.bean.PublishBean;
import org.aisen.weibo.sina.support.bean.PublishBean.PublishStatus;
import org.aisen.weibo.sina.support.bean.PublishType;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.support.utils.UMengUtil;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 新微博
 * 
 * @author wangdan
 *
 */
public class PublishStatusFragment extends APublishFragment {

	public static ABaseFragment newInstance(PublishBean bean) {
		PublishStatusFragment fragment = new PublishStatusFragment();
		
		if (bean != null) {
			Bundle args = new Bundle();
			args.putSerializable("bean", bean);
			fragment.setArguments(args);
		}
		
		return fragment;
	}
	
	@ViewInject(id = R.id.txtGroupHint)
	TextView txtGroupHint;
	@ViewInject(id = R.id.txtTiming)
	TextView txtTiming;
	
	@Override
	public int inflateContentView() {
		return R.layout.ui_publish_status;
	}
	
	@Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		super.layoutInit(inflater, savedInstanceSate);

        ((BaseActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		if (getPublishBean().getExtras() != null && getPublishBean().getExtras().containsKey("feedback")) {
            ((BaseActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_feedback);
		}
		else if (getPublishBean().getExtras() != null && getPublishBean().getExtras().containsKey("recommend")) {
            ((BaseActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_recommend);
		}
		else if (getPublishBean().getExtras() != null && getPublishBean().getExtras().containsKey("share")) {
            ((BaseActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_share);
		}
		else {
            ((BaseActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_new_status);
		}
		
		editContent.setHint(R.string.title_status_def);
		
		setVisiableHint();
		
		setTimingHint();
		
		// 如果是推荐，或者是分享图片，则不现实拍照按钮
		if (getPublishBean().getExtras() != null) {
			if (getPublishBean().getExtras().containsKey("share") || getPublishBean().getExtras().containsKey("recommend"))
				btnCamera.setVisibility(View.GONE);
		}
		
		// 2014-09-15 解决当文字超过限制后，因为定时设置，清除掉了错误提示，这里再次刷新一下界面
		refreshUI();

        setHasOptionsMenu(true);
	}
	
	private void setTimingHint() {
		if (getPublishBean().getTiming() > 0) {
			txtTiming.setVisibility(View.VISIBLE);
			
			txtTiming.setText(String.format(
									getString(R.string.draft_timing_hint),
									DateUtils.formatDate(getPublishBean().getTiming(), getString(R.string.draft_date_format))));

			if (getPublishBean().getTiming() < System.currentTimeMillis()) {
				txtErrorHint.setVisibility(View.VISIBLE);
				
				txtErrorHint.setText(R.string.draft_timing_expired);
			}
			else {
				txtErrorHint.setVisibility(View.GONE);
			}
		}
		else {
			txtErrorHint.setVisibility(View.GONE);
			
			txtTiming.setVisibility(View.GONE);
		}
		
	}
	
	private void setVisiableHint() {
		// 所有人可见
		if ("0".equals(getPublishBean().getParams().getParameter("visible"))) {
			txtGroupHint.setText(R.string.publish_all_visiable);
		}
		// 密友可见
		else if ("2".equals(getPublishBean().getParams().getParameter("visible"))) {
			txtGroupHint.setText(R.string.publish_miyou_visiable);
		}
		// 分组可见
		else if ("3".equals(getPublishBean().getParams().getParameter("visible"))) {
			Groups groups = AppContext.getAccount().getGroups();
			for (Group group : groups.getLists()) {
				if (group.getIdstr().equals(getPublishBean().getParams().getParameter("list_id"))) {
					txtGroupHint.setText(String.format(getString(R.string.publish_group_visiable), group.getName()));
					break;
				}
			}
		}
	}

	@Override
	PublishBean newPublishBean() {
		PublishBean bean = new PublishBean();
		bean.setStatus(PublishStatus.create);
		bean.setType(PublishType.status);
		
		Params params = new Params();
		
		// 默认所有人可见
		params.addParameter("visible", "0");
		
		bean.setParams(params);
		
		return bean;
	}
	
	public static PublishBean generateBean() {
		PublishBean bean = new PublishBean();
		bean.setStatus(PublishStatus.create);
		bean.setType(PublishType.status);
		
		Params params = new Params();
		
		// 默认所有人可见
		params.addParameter("visible", "0");
		
		bean.setParams(params);
		
		return bean;
	}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_publish, menu);

        // 添加分组
        if (AppContext.getAccount().getGroups() == null || AppContext.getAccount().getGroups().getLists().size() == 0) {
        }
        else {
            SubMenu subMenu = menu.addSubMenu(R.id.publish, 333, 2,
										GlobalContext.getInstance().getResources().getString(R.string.group_selected_visiable));
            for (int i = 0; i < AppContext.getAccount().getGroups().getLists().size(); i++) {
                Group group = AppContext.getAccount().getGroups().getLists().get(i);
                subMenu.add(100, i, i, group.getName());
            }
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // 取消定时
        menu.findItem(R.id.cancel_timing).setVisible(getPublishBean().getTiming() > 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 所有人可见
        if (item.getItemId() == R.id.group_all_visiable) {
            getPublishBean().getParams().addParameter("visible", "0");
        }
        // 设置定时
        else if (item.getItemId() == R.id.publish_timing) {
            setTiming();// 定时发布
        }
        // 取消定时
        else if (item.getItemId() == R.id.cancel_timing) {
            getPublishBean().setTiming(0);

            setTimingHint();
        }
        // 分组
        else {
            int groupIndex = item.getItemId();
            if (groupIndex < AppContext.getAccount().getGroups().getLists().size()) {
                Group group = AppContext.getAccount().getGroups().getLists().get(groupIndex);
                if (group.getName().equalsIgnoreCase(item.getTitle().toString())) {
                    getPublishBean().getParams().addParameter("visible", "3");

                    getPublishBean().getParams().addParameter("list_id", group.getIdstr());

                    setVisiableHint();
                }
            }
        }

        setVisiableHint();

        return super.onOptionsItemSelected(item);
    }

    private String[] overflowMenuItems;
    void popOverflowMenu(final View v) {
		List<String> itemList = new ArrayList<String>();
		itemList.add(getString(R.string.publish_all_visiable));
		if (AppContext.getAccount().getGroups() == null || AppContext.getAccount().getGroups().getLists().size() == 0)
			;
		else
			itemList.add(getString(R.string.publish_group_visiable_menu));
//		itemList.add("密友可见");
		itemList.add(getString(R.string.publish_set_timing));
		if (getPublishBean().getTiming() > 0)
			itemList.add(getString(R.string.publish_cancel_timing));
		
		overflowMenuItems = new String[itemList.size()];
		for (int i = 0; i < itemList.size(); i++)
			overflowMenuItems[i] = itemList.get(i);
		
		AisenUtils.showMenuDialog(this, v, overflowMenuItems, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (getString(R.string.publish_all_visiable).equals(overflowMenuItems[which])) {
					getPublishBean().getParams().addParameter("visible", "0");// 所有人可见
				} else if (getString(R.string.publish_miyou_visiable).equals(overflowMenuItems[which])) {
					getPublishBean().getParams().addParameter("visible", "2");// 密友可见
				} else if (getString(R.string.publish_group_visiable_menu).equals(overflowMenuItems[which])) {
					setGroupVisiable(v);// 分组可见
				} else if (getString(R.string.publish_set_timing).equals(overflowMenuItems[which])) {
					setTiming();// 定时发布
				} else if (getString(R.string.publish_cancel_timing).equals(overflowMenuItems[which])) {
					getPublishBean().setTiming(0);

					setTimingHint();
				}

				setVisiableHint();
			}
		});
	}

	// 设置分组可见
	private void setGroupVisiable(View v) {
		String[] groupArr = new String[AppContext.getAccount().getGroups().getLists().size()];
		for (int i = 0; i < AppContext.getAccount().getGroups().getLists().size(); i++)
			groupArr[i] = AppContext.getAccount().getGroups().getLists().get(i).getName();
		
		AisenUtils.showMenuDialog(this, v, groupArr, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				getPublishBean().getParams().addParameter("visible", "3");

				getPublishBean().getParams().addParameter("list_id", AppContext.getAccount().getGroups().getLists().get(which).getIdstr());

				setVisiableHint();
			}
		});
	}
	
	// 设置定时发布
	private void setTiming() {
		final Calendar calendar = Calendar.getInstance();
		if (getPublishBean().getTiming() > 0)
			calendar.setTimeInMillis(getPublishBean().getTiming());

		View contentView = View.inflate(getActivity(), R.layout.lay_publish_timing, null);
		final TextView btnDate = (TextView) contentView.findViewById(R.id.txtDate);
		final TextView btnTime = (TextView) contentView.findViewById(R.id.txtTime);
		btnTime.setText(DateUtils.formatDate(calendar.getTimeInMillis(), getString(R.string.publish_date_format_hm)));
		
		DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
			
			@Override
			public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
				Logger.v(TAG, String.format("onDateSet:yeat=%d, month = %d, day = %d", year, month, day));
				
				if (checkTiming(year, month, day, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))) {
					calendar.set(Calendar.YEAR, year);
					calendar.set(Calendar.MONTH, month);
					calendar.set(Calendar.DAY_OF_MONTH, day);
					
					Calendar c = Calendar.getInstance();
					if (month == c.get(Calendar.MONTH) && day == c.get(Calendar.DAY_OF_MONTH)) {
						btnDate.setText(R.string.publish_today);
					}
					else {
						btnDate.setText(DateUtils.formatDate(calendar.getTimeInMillis(), getString(R.string.publish_date_format_md)));
					}
					
					btnTime.performClick();
				}
				
			}
		};
		TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
			
			@Override
			public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
				Logger.v(TAG, String.format("onTimeSet:hourOfDay = %d, minute = %d", hourOfDay, minute));
				
				if (checkTiming(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute)) {
					calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
					calendar.set(Calendar.MINUTE, minute);
					
					btnTime.setText(DateUtils.formatDate(calendar.getTimeInMillis(), getString(R.string.publish_date_format_hm)));
				}
			}
		};
		
		final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(onDateSetListener,
				calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), true);
		final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(onTimeSetListener,
				calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false, false);
		
		OnClickListener timingOnClickListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 设置日期
				if (v.getId() == R.id.txtDate) {
					datePickerDialog.setVibrate(false);
					datePickerDialog.setYearRange(calendar.get(Calendar.YEAR), calendar.get(Calendar.YEAR));
					datePickerDialog.setCloseOnSingleTapDay(false);
					datePickerDialog.show(getFragmentManager(), "DatePickerDialog");
				}
				// 设置时间
				else if (v.getId() == R.id.txtTime) {
					timePickerDialog.setVibrate(false);
					timePickerDialog.setStartTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
	                timePickerDialog.setCloseOnSingleTapMinute(false);
	                timePickerDialog.show(getFragmentManager(), "TimePickerDialog");
				}
			}
		};
		btnDate.setOnClickListener(timingOnClickListener);
		btnTime.setOnClickListener(timingOnClickListener);
		
		new AlertDialogWrapper.Builder(getActivity()).setTitle(R.string.publish_timing_set_title)
							.setView(contentView)
							.setCancelable(false)
							.setNegativeButton(R.string.cancel, null)
							.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// FIXME
//									getPublishBean().setTiming(System.currentTimeMillis() + 15 * 1000);
									calendar.set(Calendar.SECOND, 0);
									getPublishBean().setTiming(calendar.getTimeInMillis() / 1000 * 1000);

                                    getActivity().invalidateOptionsMenu();

									setTimingHint();
								}
							})
							.show();
	}
	
	private boolean checkTiming(int year, int month, int dayOfMonth, int hourOfDay, int minute) {
		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
		calendar.set(Calendar.MINUTE, minute);
		
		if (calendar.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
			showMessage(R.string.error_timing);
			
			return false;
		}
		
		return true;
	}
	
	@Override
	boolean checkValid(PublishBean bean) {
		String content = editContent.getText().toString();

		if (bean.getExtras() != null && bean.getPics() != null && TextUtils.isEmpty(content)) {
//			content = getString(R.string.publish_share_pic) + " ";
		}
		
		if (TextUtils.isEmpty(content)) {
			showMessage(R.string.error_none_status);
			return false;
		}
		
		// status
		bean.getParams().addParameter("status", content);
		// visible
		// params.addParameter("visible", content);
		// list_id
		// params.addParameter("list_id", content);
		// lat
		// params.addParameter("lat", content);
		// long
		// params.addParameter("long", content);
		
		return true;
	}

	@Override
	protected void send() {
		// 单个图片或者没有图片，用aisen发布即可
		if (getPublishBean().getPics() == null || getPublishBean().getPics().length == 1) {
			super.send();
		}
		// 发送多个图片时，需要高级权限
		else {
			BizFragment.createBizFragment(this).checkProfile(new BizFragment.CheckProfileCallback() {

				@Override
				public void onCheckProfileSuccess() {
					PublishStatusFragment.super.send();
				}

				@Override
				public void onCheckProfileFaild() {
					showMessage(R.string.publish_request_ad_auth_faild);
				}

			});
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		UMengUtil.onPageStart(getActivity(), "发布新微博页");
	}

	@Override
	public void onPause() {
		super.onPause();

		UMengUtil.onPageEnd(getActivity(), "发布新微博页");
	}

}
