package org.aisen.weibo.sina.ui.activity.publish;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.network.http.Params;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.sinasdk.bean.StatusComment;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;
import org.aisen.weibo.sina.support.bean.PublishBean;
import org.aisen.weibo.sina.support.bean.PublishType;
import org.aisen.weibo.sina.support.utils.ThemeUtils;
import org.aisen.weibo.sina.ui.activity.base.AisenActivityHelper;
import org.aisen.weibo.sina.ui.fragment.publish.APublishFragment;
import org.aisen.weibo.sina.ui.fragment.publish.PublishCommentReplyFragment;
import org.aisen.weibo.sina.ui.fragment.publish.PublishStatusCommentFragment;
import org.aisen.weibo.sina.ui.fragment.publish.PublishStatusFragment;
import org.aisen.weibo.sina.ui.fragment.publish.PublishStatusRepostFragment;

/**
 * 1、发表评论<br/>
 * 2、回复评论<br/>
 * 3、转发微博<br/>
 * 4、发布微博<br/>
 * 5、提及好友<br/>
 * 6、意见反馈<br/>
 * 7、推荐给好友<br/>
 * 
 * @author wangdan
 *
 */
public class PublishActivity extends BaseActivity implements AisenActivityHelper.EnableSwipeback {

	/**
	 * 发布微博
	 * 
	 * @param from
	 * @param bean
	 */
	public static void publishStatus(Activity from, PublishBean bean) {
		Intent intent = new Intent(from, PublishActivity.class);
		intent.putExtra("type", PublishType.status.toString());
		if (bean != null)
			intent.putExtra("bean", bean);
		from.startActivity(intent);
	}
	
	/**
	 * 意见反馈
	 * 
	 * @param from
	 */
	public static void publishFeedback(Activity from) {
		Intent intent = new Intent(from, PublishActivity.class);
		intent.putExtra("type", PublishType.status.toString());
		
		PublishBean bean = PublishStatusFragment.generateBean();
		String model = android.os.Build.MODEL;
		String sysVersion = android.os.Build.VERSION.RELEASE;
		String appVersion = SystemUtils.getVersionName(GlobalContext.getInstance());
		bean.setText(String.format("#Aisen意见反馈# %s;%s;V%s; @王_dan ", 
										model, sysVersion, appVersion));
		bean.setExtras(new Params());
		bean.getExtras().addParameter("feedback", "true");
		
		intent.putExtra("bean", bean);
		from.startActivity(intent);
	}
	
	/**
	 * 提及好友
	 * 
	 * @param from
	 * @param user
	 */
	public static void publishStatusWithMention(Activity from, WeiBoUser user) {
		Intent intent = new Intent(from, PublishActivity.class);
		intent.putExtra("type", PublishType.status.toString());
		PublishBean bean = PublishStatusFragment.generateBean();
		bean.setText(String.format("@%s ", user.getScreen_name()));
		intent.putExtra("bean", bean);
		from.startActivity(intent);
	}
	
	/**
	 * 回复评论
	 * 
	 * @param from
	 * @param bean
	 * @param comment
	 * @param append
	 */
	public static void publishCommentReply(Activity from, PublishBean bean, StatusComment comment, boolean append) {
		Intent intent = new Intent(from, PublishActivity.class);
		intent.putExtra("type", PublishType.commentReply.toString());
		
		if (bean == null)
			bean = PublishCommentReplyFragment.generateBean(comment);
		
		if (append) {
			if (bean.getExtras() == null)
				bean.setExtras(new Params());
			
			bean.getExtras().addParameter("append", String.format("//@%s:%s", comment.getUser().getScreen_name(), comment.getText()));
		}
		
		intent.putExtra("bean", bean);
		from.startActivity(intent);
	}
	
	/**
	 * 回复微博
	 * 
	 * @param from
	 * @param bean
	 * @param content
	 */
	public static void publishStatusComment(Activity from, PublishBean bean, StatusContent content) {
		Intent intent = new Intent(from, PublishActivity.class);
		intent.putExtra("type", PublishType.commentCreate.toString());
		
		if (bean == null)
			bean = PublishStatusCommentFragment.generateBean(content);
		
		intent.putExtra("bean", bean);
		from.startActivity(intent);
	}
	
	/**
	 * 转发微博
	 * 
	 * @param from
	 * @param bean
	 * @param content
	 */
	public static void publishStatusRepost(Activity from, PublishBean bean, StatusContent content) {
		Intent intent = new Intent(from, PublishActivity.class);
		intent.putExtra("type", PublishType.statusRepost.toString());
		
		if (bean == null)
			bean = PublishStatusRepostFragment.generateBean(content);
		
		intent.putExtra("bean", bean);
		from.startActivity(intent);
	}
	
	/**
	 * 围观
	 * 
	 * @param from
	 * @param bean
	 * @param content
	 */
	public static void publishStatusRepostAndWeiguan(Activity from, PublishBean bean, StatusContent content) {
		Intent intent = new Intent(from, PublishActivity.class);
		intent.putExtra("type", PublishType.statusRepost.toString());
		
		if (bean == null)
			bean = PublishStatusRepostFragment.generateBean(content);
		
		if (bean.getExtras() == null)
			bean.setExtras(new Params());
		bean.getExtras().addParameter("weiguan", "true");
		
		intent.putExtra("bean", bean);
		from.startActivity(intent);
	}
	
	private String typeStr;
	private PublishBean bean;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.comm_ui_fragment_container);
		
		getSupportActionBar().setDisplayShowHomeEnabled(false);

		// 接收分享
		if (savedInstanceState == null) {
            Intent intent = getIntent();
            String action = intent.getAction();
            String type = intent.getType();
            if (!TextUtils.isEmpty(action)) {
                if (action.equals(Intent.ACTION_SEND) && !TextUtils.isEmpty(type)) {
                	if (!AppContext.isLoggedIn()) {
//                		AccountFragment.launch(this);
                		
                		showMessage(R.string.publish_please_login);
                		
                		finish();
                		return;
                	}
                	
                	if ("text/plain".equals(type)) {
                        handleSendText(intent);
                    } else if (type.startsWith("image/")) {
                        handleSendImage(intent);
                    } else {
                    	finish();
                    }
                    return;
                }
            }
        }
		
		typeStr = savedInstanceState == null ? getIntent().getStringExtra("type") : savedInstanceState.getString("type");
		bean = savedInstanceState == null ? (PublishBean) getIntent().getSerializableExtra("bean") : (PublishBean) savedInstanceState.getSerializable("bean");
		
		if (savedInstanceState == null) {
			if (TextUtils.isEmpty(typeStr)) {
				finish();
				return;
			}

			PublishType type = PublishType.valueOf(typeStr);

			ABaseFragment fragment = null;
			switch (type) {
			case status:
				fragment = PublishStatusFragment.newInstance(bean);
				break;
			case commentReply:
				fragment = PublishCommentReplyFragment.newInstance(bean);
				break;
			case commentCreate:
				fragment = PublishStatusCommentFragment.newInstance(bean);
				break;
			case statusRepost:
				fragment = PublishStatusRepostFragment.newInstance(bean);
				break;
			default:
				break;
			}
			
			if (fragment != null)
				getFragmentManager().beginTransaction().add(R.id.fragmentContainer, fragment, "PublishFragment").commit();
		}
		
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putString("type", typeStr);
		outState.putSerializable("bean", bean);
	}
	
	private void handleSendText(Intent intent) {
		String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
		Logger.v(APublishFragment.TAG, String.format("分享内容， TEXT = %s", sharedText + ""));
        if (!TextUtils.isEmpty(sharedText)) {
    		PublishBean bean = PublishStatusFragment.generateBean();
    		bean.setText(sharedText);
    		bean.setExtras(new Params());
    		bean.getExtras().addParameter("share", "true");
    		
    		getFragmentManager().beginTransaction()
    			.add(R.id.fragmentContainer, PublishStatusFragment.newInstance(bean), "PublishFragment").commit();
        }
	}
	
	private void handleSendImage(Intent intent) {
		Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
		String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
		Logger.v(APublishFragment.TAG, String.format("分享内容， TEXT = %s", sharedText + ""));
		String imageURL = intent.getStringExtra("imageURL");
		Logger.v(APublishFragment.TAG, String.format("分享图片， URL = %s", imageURL + ""));
		 
		if (imageUri != null) {
        	PublishBean bean = PublishStatusFragment.generateBean();
    		if (TextUtils.isEmpty(sharedText)) {
//				bean.setText(getString(R.string.publish_share_pic));
				bean.setText("");
			}
    		else {
				bean.setText(sharedText);
			}
    		bean.setParams(new Params());
    		bean.setExtras(new Params());
    		bean.getExtras().addParameter("share", "true");

            bean.setPics(new String[]{ imageUri.toString() });
    		bean.getExtras().addParameter("images", imageUri.toString());
    		if (!TextUtils.isEmpty(imageURL)) {
    			bean.getExtras().addParameter("shareImage", "true");
    			
    			bean.getParams().addParameter("url", imageURL);
    		}
            
    		getFragmentManager().beginTransaction()
				.add(R.id.fragmentContainer, PublishStatusFragment.newInstance(bean), "PublishFragment").commit();
		}
	}

    @Override
    protected int configTheme() {
        return ThemeUtils.themeArr[AppSettings.getThemeColor()][0];
    }

	@Override
	public boolean canSwipe() {
		return false;
	}

}
