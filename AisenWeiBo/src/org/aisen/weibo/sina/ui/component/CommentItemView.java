package org.aisen.weibo.sina.ui.component;

import java.util.ArrayList;
import java.util.List;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.utils.AisenUtil;
import org.aisen.weibo.sina.support.utils.AppContext;
import org.aisen.weibo.sina.support.utils.ImageConfigUtils;
import org.aisen.weibo.sina.ui.activity.comment.TimelineCommentsActivity;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;
import org.aisen.weibo.sina.ui.widget.AisenTextView;
import org.aisen.weibo.sina.ui.widget.CommentPicsView;
import org.android.loader.BitmapLoader;
import org.sina.android.bean.StatusComment;
import org.sina.android.bean.StatusContent;
import org.sina.android.bean.WeiBoUser;

import android.content.DialogInterface;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.m.common.context.GlobalContext;
import com.m.support.Inject.ViewInject;
import com.m.support.adapter.ABaseAdapter.AbstractItemView;
import com.m.ui.fragment.ABaseFragment;

public class CommentItemView extends AbstractItemView<StatusComment> implements OnClickListener {

	@ViewInject(id = R.id.imgPhoto)
	ImageView imgPhoto;
	@ViewInject(id = R.id.txtName)
	TextView txtName;
	@ViewInject(id = R.id.txtDesc)
	TextView txtDesc;
	@ViewInject(id = R.id.txtContent)
	AisenTextView txtContent;
	
	@ViewInject(id = R.id.layRe)
	View layRe;
	@ViewInject(id = R.id.imgRePhoto)
	ImageView imgRePhoto;
	@ViewInject(id = R.id.txtReContent)
	AisenTextView txtReContent;
	
	@ViewInject(id = R.id.layStatus)
	View layStatus;
	@ViewInject(id = R.id.txtStatusUserName)
	TextView txtStatusUserName;
	@ViewInject(id = R.id.txtStatusContent)
	AisenTextView txtStatusContent;
	@ViewInject(id = R.id.layRightPicture)
	CommentPicsView picsView;
	
	@ViewInject(id = R.id.btnMenus) 
	View btnMenus;
	
	private ABaseFragment fragment;
	private BizFragment bizFragment;
	
	private StatusContent mStatus;
	
	public CommentItemView(ABaseFragment fragment) {
		this.fragment = fragment;
	}
	
	public CommentItemView(ABaseFragment fragment, StatusContent status) {
		this.fragment = fragment;
		this.mStatus = status;
	}
	
	@Override
	public int inflateViewId() {
		return R.layout.item_timeline_comment;
	}

	@Override
	public void bindingData(View convertView, StatusComment data) {
		try {
			if (bizFragment == null) 
				bizFragment = BizFragment.getBizFragment(fragment);
			
			if (bizFragment == null)
				return;
		} catch (Exception e) {
		}
		
		WeiBoUser user = data.getUser();
		if (user != null) {
			BitmapLoader.getInstance().display(fragment, 
													AisenUtil.getUserPhoto(user), 
													imgPhoto, ImageConfigUtils.getLargePhotoConfig());
			bizFragment.userShow(imgPhoto, user);
			txtName.setText(AisenUtil.getUserScreenName(user));
		}
		else {
			bizFragment.userShow(imgPhoto, null);
			txtName.setText(R.string.error_cmts);
			imgPhoto.setImageResource(R.drawable.user_placeholder);
		}
		
		txtContent.setContent(AisenUtil.getCommentText(data.getText()));
		AisenUtil.setTextSize(txtContent);
		
		String createAt = AisenUtil.convDate(data.getCreated_at());
		String from = String.format("%s", Html.fromHtml(data.getSource()));
		String desc = String.format("%s %s", createAt, from);
		txtDesc.setText(desc);
		
		// 源评论
		if (data.getReply_comment() != null) {
			layRe.setVisibility(View.VISIBLE);
			
			txtReContent.setContent(AisenUtil.getCommentText(data.getReply_comment().getText()));
			AisenUtil.setTextSize(txtReContent);
			
			if (data.getReply_comment().getUser() != null) {
				BitmapLoader.getInstance().display(fragment, 
						AisenUtil.getUserPhoto(data.getReply_comment().getUser()), 
						imgRePhoto, ImageConfigUtils.getLargePhotoConfig());
				bizFragment.userShow(imgRePhoto, data.getReply_comment().getUser());
			}
			else {
				bizFragment.userShow(imgRePhoto, null);
			}
		}
		else {
			layRe.setVisibility(View.GONE);
		}
		
		if (data.getStatus() != null && mStatus == null) {
			layStatus.setVisibility(View.VISIBLE);
			layStatus.setTag(data.getStatus());
			layStatus.setOnClickListener(this);
//			if (data.getStatus().getUser() != null)
//				txtStatusUserName.setText(AisenUtil.getUserScreenName(data.getStatus().getUser()));
//			else
//				txtStatusUserName.setText("-");
			
			if (data.getStatus().getUser() != null)
				txtStatusUserName.setText(AisenUtil.getUserScreenName(data.getStatus().getUser()));
//				txtStatusUserName.setText(String.format("%s 的原微博", AisenUtil.getUserScreenName(data.getStatus().getUser())));
			else 
				txtStatusUserName.setText("-");
			
			txtStatusContent.setContent(data.getStatus().getText());
			AisenUtil.setTextSize(txtStatusContent);

			picsView.setPics(data.getStatus());
		
			bizFragment.bindOnTouchListener(txtStatusContent);
		}
		else {
			layStatus.setVisibility(View.GONE);
		}
		
		btnMenus.setTag(data);
		btnMenus.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.layStatus) {
			final StatusContent status = (StatusContent) v.getTag();
			TimelineCommentsActivity.launch(fragment, status);
		}
		else if (v.getId() == R.id.btnMenus) {
			final String[] commentMenuArr = GlobalContext.getInstance().getResources().getStringArray(R.array.cmt_menus);
			final StatusComment comment = (StatusComment) v.getTag();
			if (mStatus != null)
				comment.setStatus(mStatus);
			
			// 复制
			List<String> menuList = new ArrayList<String>();
			menuList.add(commentMenuArr[0]);
			// 转发
//			if (fragment instanceof TimelineCommentsFragment)
			if (comment.getStatus() != null &&
					(comment.getUser() != null && !comment.getUser().getIdstr().equals(AppContext.getUser().getIdstr())))
				menuList.add(commentMenuArr[1]);
			// 删除
			if (comment.getUser() != null && AppContext.getUser().getIdstr().equals(comment.getUser().getIdstr()))
				menuList.add(commentMenuArr[2]);
			
			final String[] menus = new String[menuList.size()];
			for (int i = 0; i < menuList.size(); i++)
				menus[i] = menuList.get(i);
			
			AisenUtil.showMenuDialog(fragment, 
										v, 
										menus, 
										new DialogInterface.OnClickListener() {
				
											@Override
											public void onClick(DialogInterface dialog, int which) {
												AisenUtil.commentMenuSelected(fragment, menus[which], comment);
											}
										});
		}
	}
	
}
