package org.aisen.weibo.sina.ui.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.utils.AisenUtil;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.ImageConfigUtils;
import org.aisen.weibo.sina.ui.activity.comment.TimelineCommentsActivity;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;
import org.aisen.weibo.sina.ui.fragment.mention.MentionTimelineFragment;
import org.aisen.weibo.sina.ui.widget.AisenTextView;
import org.aisen.weibo.sina.ui.widget.TimelinePicsView;
import org.android.loader.BitmapLoader;
import org.sina.android.bean.Group;
import org.sina.android.bean.StatusContent;
import org.sina.android.bean.WeiBoUser;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.m.common.context.GlobalContext;
import com.m.support.Inject.ViewInject;
import com.m.support.adapter.ABaseAdapter.AbstractItemView;
import com.m.ui.fragment.ABaseFragment;

/**
 * timeline的ListView的item
 * 
 * @author wangdan
 *
 */
public class TimelineItemView extends AbstractItemView<StatusContent> 
											implements OnClickListener {

	private ABaseFragment fragment;
	private BizFragment bizFragment;
	
	private boolean showRetweeted;
	private StatusContent reStatus;
	
	public TimelineItemView(ABaseFragment fragment, boolean showRetweeted) {
		this(fragment, null, showRetweeted);
	}
	
	// 2014-08-24 新增这个构造方法，解决转发列表，点击转发菜单时，没有带上原微博的BUG
	public TimelineItemView(ABaseFragment fragment, StatusContent reStatue, boolean showRetweeted) {
		this();
		
		this.fragment = fragment;
		this.reStatus = reStatue;
		try {
			bizFragment = BizFragment.getBizFragment(fragment);			
		} catch (Exception e) {
		}
		this.showRetweeted = showRetweeted;
	}
	
	public TimelineItemView() {
		vPadding = GlobalContext.getInstance().getResources().getDimensionPixelSize(R.dimen.vertical_gap);
		if (AppContext.getGroups() != null && (groupMap == null || groupMap.size() != AppContext.getGroups().getLists().size())) {
			groupMap = new HashMap<String, String>();
			
			for (Group group : AppContext.getGroups().getLists())
				groupMap.put(group.getIdstr(), group.getName());
		}
	}
	
	@ViewInject(id = R.id.imgPhoto)
	ImageView imgPhoto;
	@ViewInject(id = R.id.txtName)
	TextView txtName;
	@ViewInject(id = R.id.imgVerified)
	ImageView imgVerified;
	@ViewInject(id = R.id.txtDesc)
	TextView txtDesc;

	@ViewInject(id = R.id.txtRepost)
	TextView txtRepost;
	@ViewInject(id = R.id.txtComment)
	TextView txtComment;
	
	@ViewInject(id = R.id.txtContent)
	AisenTextView txtContent;
	
	@ViewInject(id = R.id.layRe)
	View layRe;
	@ViewInject(id = R.id.imgRePhoto)
	ImageView imgRePhoto;
	@ViewInject(id = R.id.txtReName)
	TextView txtReName;
	@ViewInject(id = R.id.imgReVerified)
	ImageView imgReVerified;
	@ViewInject(id = R.id.txtReDesc)
	TextView txtReDesc;
	
	@ViewInject(id = R.id.txtReContent)
	AisenTextView txtReContent;
	
	@ViewInject(id = R.id.layPicturs)
	TimelinePicsView layPicturs;
	
	@ViewInject(id = R.id.btnMenus) 
	View btnMenus;
	
	@ViewInject(id = R.id.txtVisiable)
	TextView txtVisiable;
	private int vPadding;
	private static Map<String, String> groupMap;
	
	@Override
	public int inflateViewId() {
		return R.layout.item_timeline;
	}

	@Override
	public void bindingData(View convertView, StatusContent data) {
		if (bizFragment == null) {
			try {
				bizFragment = BizFragment.getBizFragment(fragment);
			} catch (Exception e) {
			}
			if (bizFragment == null)
				return;
		}
		
		WeiBoUser user = data.getUser();
		
		// userInfo
		setUserInfo(user, txtName, imgPhoto, imgVerified);

		// desc
		String createAt = "";
		if (!TextUtils.isEmpty(data.getCreated_at()))
			createAt = AisenUtil.convDate(data.getCreated_at());
		String from = "";
		if (!TextUtils.isEmpty(data.getSource()))
			from = String.format("%s", Html.fromHtml(data.getSource()));
		String desc = String.format("%s %s", createAt, from);
		txtDesc.setText(desc);
		
		// counter
		if (TextUtils.isEmpty(data.getReposts_count()) || Integer.parseInt(data.getReposts_count()) == 0) {
			txtRepost.setVisibility(View.GONE);
		}
		else {
			txtRepost.setVisibility(View.VISIBLE);
			txtRepost.setText(AisenUtil.getCounter(Integer.parseInt(data.getReposts_count())));
		}
		if (TextUtils.isEmpty(data.getComments_count()) || Integer.parseInt(data.getComments_count()) == 0) {
			txtComment.setVisibility(View.GONE);
		}
		else {
			txtComment.setVisibility(View.VISIBLE);
			txtComment.setText(AisenUtil.getCounter(Integer.parseInt(data.getComments_count())));
		}
		// 文本
		AisenUtil.setTextSize(txtContent);
//		txtContent.setText(data.getText());
		txtContent.setContent(data.getText());
		
		// reContent
		StatusContent reContent = data.getRetweeted_status();
		if (reContent == null || !showRetweeted) {
			layRe.setVisibility(View.GONE);
		}
		else {
			layRe.setVisibility(View.VISIBLE);
			layRe.setTag(reContent);
//			layRe.setOnClickListener(this);
			
			// reUserInfo
			WeiBoUser reUser = reContent.getUser();
			setUserInfo(reUser, txtReName, imgRePhoto, imgReVerified);
			
			// re desc
			from = "";
			createAt = "";
			if (!TextUtils.isEmpty(reContent.getCreated_at()))
				createAt = AisenUtil.convDate(reContent.getCreated_at());
			if (!TextUtils.isEmpty(reContent.getSource()))
				from = String.format("%s", Html.fromHtml(reContent.getSource()));
			desc = String.format("%s %s", createAt, from);
			txtReDesc.setText(desc);
			
			// re content
//			txtReContent.setText(reContent.getText());
			txtReContent.setContent(reContent.getText());
			AisenUtil.setTextSize(txtReContent);
		}
		
		// pictures
		StatusContent s = data.getRetweeted_status() != null ? data.getRetweeted_status() : data;
		layPicturs.setPics(showRetweeted ? s : data, bizFragment, fragment);
		
		// group visiable
		txtVisiable.setVisibility(View.GONE);
		if (data.getVisible() != null && groupMap != null) {
			String name = groupMap.get(data.getVisible().getList_id());
			if (!TextUtils.isEmpty(name)) {
				txtVisiable.setText(String.format(fragment.getString(R.string.publish_group_visiable), name));
				txtVisiable.setVisibility(View.VISIBLE);
				
				if (layPicturs.getVisibility() == View.GONE) {
					txtVisiable.setPadding(0, 0, 0, 0);
				}
				else {
					txtVisiable.setPadding(0, vPadding, 0, 0);
				}
			}
		}
		
		if (reStatus != null)
			data.setRetweeted_status(reStatus);
		btnMenus.setTag(data);
		btnMenus.setOnClickListener(this);
	}
	
	private void setUserInfo(WeiBoUser user, TextView txtName, ImageView imgPhoto, ImageView imgVerified) {
		if (user != null) {
			txtName.setText(AisenUtil.getUserScreenName(user));
			BitmapLoader.getInstance().display(fragment, AisenUtil.getUserPhoto(user), imgPhoto, ImageConfigUtils.getLargePhotoConfig());
			bizFragment.userShow(imgPhoto, user);
			
			AisenUtil.setImageVerified(imgVerified, user);
		}
		else {
			imgPhoto.setImageDrawable(new ColorDrawable(Color.GRAY));
			bizFragment.userShow(imgPhoto, null);
			
			imgVerified.setVisibility(View.GONE);
		}
	}
	
	@Override
	public void onClick(View v) {
		// 查看转发微博信息
		if (v.getId() == R.id.layRe) {
			StatusContent reContent = (StatusContent) v.getTag();
			
			TimelineCommentsActivity.launch(fragment, reContent);
		}
		// 溢出菜单
		else if (v.getId() == R.id.btnMenus) {
			final String[] timelineMenuArr = GlobalContext.getInstance().getResources().getStringArray(R.array.timeline_menus);
			final StatusContent status = (StatusContent) v.getTag();
			
			List<String> menuList = new ArrayList<String>();
			if (status.getRetweeted_status() != null && status.getRetweeted_status().getUser() != null)
				menuList.add(timelineMenuArr[0]);
			menuList.add(timelineMenuArr[1]);
			if (status.getVisible() == null || "0".equals(status.getVisible().getType())) 
				menuList.add(timelineMenuArr[2]);
			menuList.add(timelineMenuArr[3]);
			menuList.add(timelineMenuArr[4]);
			menuList.add(timelineMenuArr[5]);
			if (status.getUser() != null && status.getUser().getIdstr().equals(AppContext.getUser().getIdstr()))
				menuList.add(timelineMenuArr[6]);
			if (fragment instanceof MentionTimelineFragment)
				menuList.add(timelineMenuArr[7]);
			
			final String[] menus = new String[menuList.size()];
			for (int i = 0; i < menuList.size(); i++)
				menus[i] = menuList.get(i);
			
			AisenUtil.showMenuDialog(fragment, 
										v, 
										menus, 
										new DialogInterface.OnClickListener() {
				
												@Override
												public void onClick(DialogInterface dialog, int which) {
													AisenUtil.timelineMenuSelected(fragment, menus[which], status);
												}
											});
		}
	}
	
	

}
