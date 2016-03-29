package org.aisen.weibo.sina.ui.fragment.publish;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;

import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.network.http.Params;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;
import org.aisen.weibo.sina.support.bean.PublishBean;
import org.aisen.weibo.sina.support.bean.PublishBean.PublishStatus;
import org.aisen.weibo.sina.support.bean.PublishType;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.support.utils.ImageConfigUtils;
import org.aisen.weibo.sina.support.utils.UMengUtil;

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
		
		// 当转发，内容为空时，自动添加"转发微博"
		if (TextUtils.isEmpty(editContent.getText().toString().trim())) {
//			editContent.setText(R.string.publish_also_repost);
//			editContent.setSelection(editContent.getText().toString().length());
		}
		
		StatusContent status = getPublishBean().getStatusContent().getRetweeted_status() != null ? getPublishBean().getStatusContent().getRetweeted_status()
																								 : getPublishBean().getStatusContent();
		txtContent.setText(AisenUtils.getCommentText(status.getText()));
		if (status.getUser() != null)
			BitmapLoader.getInstance().display(this, AisenUtils.getUserPhoto(status.getUser()), imgPhoto, ImageConfigUtils.getLargePhotoConfig());

		checkBox.setText(R.string.publish_cmt_to_user);
		checkBox.setChecked("3".equals(getPublishBean().getParams().getParameter("is_comment")));
		checkBox.setOnCheckedChangeListener(this);

        ((BaseActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((BaseActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_repost);
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
		bean.getParams().addParameter("id", status.getId() + "");
		// 是否在转发的同时发表评论，0：否、1：评论给当前微博、2：评论给原微博、3：都评论，默认为0 。
		bean.getParams().addParameter("is_comment", "0");

		return bean;
	}

	@Override
	boolean checkValid(PublishBean bean) {
		String content = editContent.getText().toString();
		
		// status
		bean.getParams().addParameter("status", content);
		
		return true;
	}

	@Override
	public int inflateContentView() {
		return R.layout.ui_publish_status_comment;
	}

	@Override
	public void onResume() {
		super.onResume();

		UMengUtil.onPageStart(getActivity(), "发布转发微博页");
	}

	@Override
	public void onPause() {
		super.onPause();

		UMengUtil.onPageEnd(getActivity(), "发布转发微博页");
	}

}
