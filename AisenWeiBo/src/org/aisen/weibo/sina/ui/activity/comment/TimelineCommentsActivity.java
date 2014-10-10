package org.aisen.weibo.sina.ui.activity.comment;

import java.util.ArrayList;
import java.util.List;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.utils.AisenUtil;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.AppSettings;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;
import org.aisen.weibo.sina.ui.fragment.comment.TimelineCommentsFragment;
import org.aisen.weibo.sina.ui.fragment.timeline.RepostTimelineFragment;
import org.sina.android.bean.StatusContent;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.m.common.context.GlobalContext;
import com.m.support.Inject.ViewInject;
import com.m.ui.activity.AViewpagerActivity;
import com.m.ui.fragment.ABaseFragment;

public class TimelineCommentsActivity extends AViewpagerActivity implements OnClickListener {

	public static void launch(ABaseFragment from, StatusContent status) {
		Intent intent = new Intent(from.getActivity(), TimelineCommentsActivity.class);
		intent.putExtra("bean", status);
		from.startActivityForResult(intent, 1000);
	}
	
	@ViewInject(id = R.id.btnComment, click = "btnClicked")
	View btnComment;
	@ViewInject(id = R.id.btnRepost, click = "btnClicked")
	View btnRepost;
	@ViewInject(id = R.id.btnFavor, click = "btnClicked")
	View btnFavor;
	@ViewInject(id = R.id.btnOverflow, click = "btnClicked")
	View btnOverflow;
	
	private StatusContent mStatusContent;
	
	private View menuActionView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mStatusContent = savedInstanceState == null ? (StatusContent) getIntent().getSerializableExtra("bean")
													: (StatusContent) savedInstanceState.getSerializable("bean");
		
		super.onCreate(savedInstanceState);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setTitle(R.string.title_cmt);
		
		BizFragment.getBizFragment(this);
		
		// 微博不是普通微博
		if (mStatusContent.getVisible() == null || "0".equals(mStatusContent.getVisible().getType())) {
		}
		else {
			findViewById(R.id.dividerRepost).setVisibility(View.GONE);
			btnRepost.setVisibility(View.GONE);
		}
	}
	
	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(R.layout.ui_comment_pager);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putSerializable("bean", mStatusContent);
	}
	
	@Override
	protected int setViewPagerTitles() {
		return R.array.comments;
	}

	@Override
	protected void setViewPagerFragments(List<ABaseFragment> fragmentList) {

		fragmentList.add(TimelineCommentsFragment.newInstance(mStatusContent));
		
		if (TextUtils.isEmpty(mStatusContent.getReposts_count()) 
				|| Integer.parseInt(mStatusContent.getReposts_count()) == 0) {
			
		} else {
			fragmentList.add(RepostTimelineFragment.newInstance(mStatusContent));
		}
		
	}
	
	@Override
	protected boolean showIndicator() {
		return false;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		if (TextUtils.isEmpty(mStatusContent.getReposts_count()) 
				|| Integer.parseInt(mStatusContent.getReposts_count()) == 0) {
						
		}
		else {
			getMenuInflater().inflate(R.menu.timeline_comments, menu);
			
			MenuItem switchItem = menu.findItem(R.id.menuSwitch);
			View viewTitle = switchItem.getActionView();
			viewTitle.setOnClickListener(this);
			menuActionView = viewTitle;
			
			View txtComment = viewTitle.findViewById(R.id.txtComment);
			txtComment.setSelected(getViewPager().getCurrentItem() == 0);
			View txtRepost = viewTitle.findViewById(R.id.txtRepost);
			txtRepost.setSelected(getViewPager().getCurrentItem() == 1);
		}
		
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public void onPageSelected(int position) {
		super.onPageSelected(position);
		
		if (menuActionView != null) {
			View txtComment = menuActionView.findViewById(R.id.txtComment);
			txtComment.setSelected(getViewPager().getCurrentItem() == 0);
			View txtRepost = menuActionView.findViewById(R.id.txtRepost);
			txtRepost.setSelected(getViewPager().getCurrentItem() == 1);
		}
	}

	@Override
	public void onClick(View v) {
		getViewPager().setCurrentItem(getViewPager().getCurrentItem() == 0 ? 1 : 0);
		
		View txtComment = v.findViewById(R.id.txtComment);
		txtComment.setSelected(getViewPager().getCurrentItem() == 0);
		View txtRepost = v.findViewById(R.id.txtRepost);
		txtRepost.setSelected(getViewPager().getCurrentItem() == 1);
	}
	
	void btnClicked(View v) {
		try {
			// 评论
			if (v.getId() == R.id.btnComment) {
				BizFragment.getBizFragment(this).commentCreate(mStatusContent);
			}
			// 转发
			else if (v.getId() == R.id.btnRepost) {
				BizFragment.getBizFragment(this).statusRepost(mStatusContent);
			}
			// 收藏
			else if (v.getId() == R.id.btnFavor) {
				BizFragment.getBizFragment(this).favorityCreate(mStatusContent.getId(), null);
			}
			// 溢出菜单
			else if (v.getId() == R.id.btnOverflow) {
				final String[] timelineMenuArr = GlobalContext.getInstance().getResources().getStringArray(R.array.timeline_menus);
				List<String> menuList = new ArrayList<String>();
				// 原微博
				if (mStatusContent.getRetweeted_status() != null && mStatusContent.getRetweeted_status().getUser() != null) {
					menuList.add(timelineMenuArr[0]);
				}
				// 围观
				menuList.add(timelineMenuArr[8]);
				// 复制
				menuList.add(timelineMenuArr[1]);
				// 取消收藏
				menuList.add(timelineMenuArr[5]);
				// 删除
				if (mStatusContent.getUser() != null && AppContext.getUser().getIdstr().equals(mStatusContent.getUser().getIdstr()))
					menuList.add(timelineMenuArr[6]);
				
				final String[] menuArr = new String[menuList.size()];
				for (int i = 0; i < menuList.size(); i++)
					menuArr[i] = menuList.get(i);
				
				String[] pageTitleArr = getResources().getStringArray(setViewPagerTitles());
				final ABaseFragment fragment = (ABaseFragment) getFragmentManager().findFragmentByTag(pageTitleArr[0]);
				
				AisenUtil.showMenuDialog(fragment, 
											v, 
											menuArr, 
											new DialogInterface.OnClickListener() {

												@Override
												public void onClick(DialogInterface dialog, int which) {
													AisenUtil.timelineMenuSelected(fragment, menuArr[which], mStatusContent);
												}
					
											});
			}
		} catch (Exception e) {
		}
	}
	
	protected void onResume() {
		super.onResume();
		
		// 开启屏幕旋转
		if (AppSettings.isScreenRotate()) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		}
		else {
			if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_SENSOR)
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
	}
	
}
