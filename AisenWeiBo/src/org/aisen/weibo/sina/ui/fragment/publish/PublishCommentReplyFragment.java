package org.aisen.weibo.sina.ui.fragment.publish;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.bean.PublishBean;
import org.aisen.weibo.sina.support.bean.PublishBean.PublishStatus;
import org.aisen.weibo.sina.support.bean.PublishType;
import org.aisen.weibo.sina.support.utils.AisenUtil;
import org.aisen.weibo.sina.support.utils.ImageConfigUtils;
import org.android.loader.BitmapLoader;
import org.sina.android.bean.StatusComment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.m.common.params.Params;
import com.m.support.Inject.ViewInject;
import com.m.ui.fragment.ABaseFragment;

/**
 * 回复评论
 * 
 * @author wangdan
 *
 */
public class PublishCommentReplyFragment extends APublishFragment implements OnCheckedChangeListener {

	public static ABaseFragment newInstance(PublishBean bean) {
		PublishCommentReplyFragment fragment = new PublishCommentReplyFragment();
		
		Bundle args = new Bundle();
		args.putSerializable("bean", bean);
		fragment.setArguments(args);
		
		return fragment;
	}
	
	@ViewInject(id = R.id.checkbox)
	CheckBox checkBox;
	@ViewInject(id = R.id.txtContent)
	TextView txtContent;
	@ViewInject(id = R.id.imgPhoto)
	ImageView imgPhoto;
	
	@Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceState) {
		super.layoutInit(inflater, savedInstanceState);
		
		btnCamera.setVisibility(View.GONE);
		btnOverflow.setVisibility(View.GONE);
		
		editContent.setHint(R.string.publish_cmt_def);
		if (getPublishBean().getExtras() != null && getPublishBean().getExtras().containsKey("append"))
			editContent.setText(getPublishBean().getExtras().getParameter("append"));
		
		StatusComment mComment = getPublishBean().getStatusComment();
		txtContent.setText(AisenUtil.getCommentText(mComment.getText()));
		if (mComment.getUser() != null)
			BitmapLoader.getInstance().display(this, AisenUtil.getUserPhoto(mComment.getUser()), imgPhoto, ImageConfigUtils.getLargePhotoConfig());
		
		checkBox.setChecked(Boolean.parseBoolean(getPublishBean().getExtras().getParameter("forward")));
		checkBox.setOnCheckedChangeListener(this);
		
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		getActivity().getActionBar().setTitle(R.string.title_reply_cmt);
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		getPublishBean().getExtras().addParameter("forward", String.valueOf(isChecked));
	}
	
	@Override
	PublishBean newPublishBean() {
		return null;
	}
	
	public static PublishBean generateBean(StatusComment comment) {
		PublishBean bean = new PublishBean();
		bean.setStatusComment(comment);
		
		bean.setType(PublishType.commentReply);
		bean.setStatus(PublishStatus.create);
		
		bean.setParams(new Params());
		bean.getParams().addParameter("cid", comment.getId());
		bean.getParams().addParameter("id", comment.getStatus().getId());
		// 回复中是否自动加入“回复@用户名”，0：是、1：否，默认为0
		bean.getParams().addParameter("without_mention", "0");

		bean.setExtras(new Params());
		// 评论是否同时转发
		bean.getExtras().addParameter("forward", "false");
		
		return bean;
	}

	@Override
	void popOverflowMenu(View v) {
		
	}

	@Override
	boolean checkValid(PublishBean bean) {
		String content = editContent.getText().toString();
		
		if (TextUtils.isEmpty(content)) {
			showMessage(R.string.error_none_comment);
			return false;
		}
		
		// comment
		bean.getParams().addParameter("comment", content);
		
		return true;
	}

	@Override
	protected int inflateContentView() {
		return R.layout.ui_publish_comment_replay;
	}

}
