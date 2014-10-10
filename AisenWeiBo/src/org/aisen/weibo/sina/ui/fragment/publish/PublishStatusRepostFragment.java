package org.aisen.weibo.sina.ui.fragment.publish;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.bean.PublishBean;
import org.aisen.weibo.sina.support.bean.PublishBean.PublishStatus;
import org.aisen.weibo.sina.support.bean.PublishType;
import org.aisen.weibo.sina.support.utils.AisenUtil;
import org.aisen.weibo.sina.support.utils.ImageConfigUtils;
import org.android.loader.BitmapLoader;
import org.sina.android.bean.StatusContent;
import org.sina.android.bean.WeiBoUser;

import android.os.Bundle;
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
 * 转发微博
 * 
 * @author wangdan
 *
 */
public class PublishStatusRepostFragment extends APublishFragment implements OnCheckedChangeListener {

	public static ABaseFragment newInstance(PublishBean bean) {
		PublishStatusRepostFragment fragment = new PublishStatusRepostFragment();
		
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
		
		editContent.setHint(R.string.publish_share_def);
		if (getPublishBean().getStatusContent().getRetweeted_status() != null || 
				getPublishBean().getExtras() != null && getPublishBean().getExtras().containsKey("weiguan")) {
			WeiBoUser user = getPublishBean().getStatusContent().getUser();
			if (user != null) { 
				editContent.setText(String.format("//@%s:%s", user.getScreen_name(), getPublishBean().getStatusContent().getText()));
				if (getPublishBean().getExtras() != null && getPublishBean().getExtras().containsKey("weiguan"))
					editContent.setText(String.format("→_→%s", editContent.getText().toString()));
			}
			else {
				showMessage(R.string.publish_status_none);
				return;
			}
		}
		
		StatusContent status = getPublishBean().getStatusContent().getRetweeted_status() != null ? getPublishBean().getStatusContent().getRetweeted_status() 
																								 : getPublishBean().getStatusContent();
		txtContent.setText(AisenUtil.getCommentText(status.getText()));
		if (status.getUser() != null)
			BitmapLoader.getInstance().display(this, AisenUtil.getUserPhoto(status.getUser()), imgPhoto, ImageConfigUtils.getLargePhotoConfig());
		
		checkBox.setText(R.string.publish_cmt_to_user);
		checkBox.setChecked("3".equals(getPublishBean().getParams().getParameter("is_comment")));
		checkBox.setOnCheckedChangeListener(this);
		
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		getActivity().getActionBar().setTitle(R.string.title_repost);
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		getPublishBean().getParams().addParameter("is_comment", isChecked ? "3" : "0");
	}
	
	@Override
	PublishBean newPublishBean() {
		return null;
	}
	
	public static PublishBean generateBean(StatusContent status) {
		PublishBean bean = new PublishBean();
		bean.setStatusContent(status);
		
		bean.setType(PublishType.statusRepost);
		bean.setStatus(PublishStatus.create);
		
		bean.setParams(new Params());
//		bean.getParams().addParameter("id", status.getRetweeted_status() != null ? status.getRetweeted_status().getId() : status.getId());
		bean.getParams().addParameter("id", status.getId());
		// 是否在转发的同时发表评论，0：否、1：评论给当前微博、2：评论给原微博、3：都评论，默认为0 。
		bean.getParams().addParameter("is_comment", "0");

		return bean;
	}

	@Override
	void popOverflowMenu(View v) {
		
	}

	@Override
	boolean checkValid(PublishBean bean) {
		String content = editContent.getText().toString();
		
		// status
		bean.getParams().addParameter("status", content);
		
		return true;
	}

	@Override
	protected int inflateContentView() {
		return R.layout.ui_publish_status_comment;
	}

}
