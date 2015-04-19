package org.aisen.weibo.sina.ui.fragment.basic;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.m.component.bitmaploader.core.LruMemoryCache;
import com.m.component.sqlite.extra.Extra;
import com.m.component.sqlite.utils.FieldUtils;
import com.m.network.task.TaskException;
import com.m.network.task.WorkTask;
import com.m.ui.fragment.ABaseFragment;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.support.bean.LikeBean;
import org.aisen.weibo.sina.support.bean.LikeResultBean;
import org.aisen.weibo.sina.support.db.LikeDB;
import org.aisen.weibo.sina.support.db.SinaDB;
import org.aisen.weibo.sina.support.sdk.BizLogic;
import org.sina.android.bean.StatusContent;
import org.sina.android.bean.WeiBoUser;

import java.util.List;

/**
 * 程序一系列业务逻辑处理，如下:<br/>
 * <br/>
 * 1、预览图片<br/>
 * 2、查看用户资料--完成<br/>
 * 3、查看用户微博--完成<br/>
 * 4、取消关注--完成<br/>
 * 5、添加关注--完成<br/>
 * 6、更新备注--完成<br/>
 * 7、查看用户关系--完成<br/>
 * 8、移除粉丝--完成<br/>
 * 9、删除评论<br/>
 * 10、回复评论<br/>
 * 11、删除微博<br/>
 * 12、收藏微博<br/>
 * 13、取消收藏<br/>
 * 14、回复微博<br/>
 * 15、转发微博<br/>
 * 16、清零未读信息<br/>
 * 17、转发评论<br/>
 * 18、\@用户
 *
 * @author wangdan
 */
public class BizFragment extends ABaseFragment {

	@Override
	protected int inflateContentView() {
		return 0;
	}

	public static BizFragment getBizFragment(ABaseFragment fragment) {
		if (fragment != null && fragment.getActivity() != null) {
			BizFragment bizFragment = (BizFragment) fragment.getActivity().getFragmentManager().findFragmentByTag("com.m.ui.BizFragment");

			if (bizFragment == null) {
				bizFragment = new BizFragment();
				fragment.getActivity().getFragmentManager().beginTransaction().add(bizFragment, "com.m.ui.BizFragment").commit();
			}

			return bizFragment;
		}

		return null;
	}

	public static BizFragment getBizFragment(Activity context) {
		BizFragment bizFragment = (BizFragment) context.getFragmentManager().findFragmentByTag("BizFragment");
		if (bizFragment == null) {
			bizFragment = new BizFragment();
			context.getFragmentManager().beginTransaction().add(bizFragment, "BizFragment").commit();
		}
		return bizFragment;
	}

    /******************以下是点赞的逻辑***************/

    public static LruMemoryCache<String, LikeBean> likeCache = new LruMemoryCache<String, LikeBean>(30);

    /**
     * 点赞或者取消点赞
     *
     */
    public void doLike(final StatusContent data, final boolean like, final OnLikeCallback callback) {
        String cookie = AppContext.getAccount().getCookie();

        final String key = String.valueOf(data.getId());

        LikeBean likeBean = likeCache.get(key);

        // 没有登录cookie
        if (TextUtils.isEmpty(cookie)) {
            callback.onLikeRefreshUI();

            askWebAuth();
        }
        else {
            // 如果还没有点赞过，首先添加一个点赞
            if (likeBean == null) {
                likeBean = new LikeBean();
                likeBean.setLiked(like);
                likeBean.setStatusId(key);

                LikeDB.insert(likeBean);
                likeCache.put(key, likeBean);
            }
            // 点赞过了，刷新内存，刷新DB
            else {
                likeBean.setLiked(like);

                LikeDB.insert(likeBean);
            }

            // 开始处理点赞
            new WorkTask<Void, Void, LikeResultBean>() {

                @Override
                public LikeResultBean workInBackground(Void... params) throws TaskException {
                    return BizLogic.newInstance().doLike(data.getId() + "", like, AppContext.getAccount().getCookie());
                }

                @Override
                protected void onFailure(TaskException exception) {
                    super.onFailure(exception);

                    LikeBean likeBean = likeCache.get(key);
                    if (likeBean != null) {
                        likeBean.setLiked(!like);
                        LikeDB.insert(likeBean);
                    }

                    if (getActivity() == null)
                        return;

                    // 未登录，或者登录失效
                    if ("-100".equalsIgnoreCase(exception.getMessage())) {

                        askWebAuth();
                    }
                    else {
                        showMessage(exception.getMessage());
                    }

                    callback.onLikeRefreshUI();
                }

            }.execute();
        }
    }

    public void askWebAuth() {
        if (getActivity() != null) {
            new AlertDialogWrapper.Builder(getActivity()).setMessage(R.string.acount_timeout)
                    .setNegativeButton(R.string.no, null)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
//                            WeiboClientActivity.launchForAuth(BizFragment.this, 9000);
                        }

                    })
                    .show();
        }
    }

    public static void refreshLikeCache() {
        likeCache.evictAll();

        new WorkTask<Void, Void, Void>() {

            @Override
            public Void workInBackground(Void... params) throws TaskException {
                String uid = AppContext.getUser().getIdstr();

                String selection = String.format(" %s = ? ", FieldUtils.OWNER);
                String[] args = new String[]{ uid };
                List<LikeBean> likeBeans = SinaDB.getSqlite().select(LikeBean.class, selection, args, null, null, " id desc ", "30");
                SinaDB.getSqlite().deleteAll(new Extra(uid, null), LikeBean.class);
                SinaDB.getSqlite().insert(new Extra(uid, null), likeBeans);

                for (LikeBean likeBean : likeBeans)
                    BizFragment.likeCache.put(likeBean.getStatusId(), likeBean);
                return null;
            }

        }.execute();
    }

    public interface OnLikeCallback {

        // 点赞失败或者成功后，有必要时会回调这个方法刷新UI
        public void onLikeRefreshUI();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 9000) {
                showMessage("登录成功");
            }
        }
    }

    View.OnClickListener PreviousArrOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Object[] tag = (Object[]) v.getTag();
            StatusContent bean = (StatusContent) tag[0];
            int selectedIndex = Integer.parseInt(tag[1].toString());

//            PicsActivity.launch(getActivity(), bean, selectedIndex);
        }
    };

    public void previousPics(View view, StatusContent bean, int selectedIndex) {
        Object[] tag = new Object[] { bean, selectedIndex };
        view.setTag(tag);
        view.setOnClickListener(PreviousArrOnClickListener);
    }

    public void userShow(View view, WeiBoUser user) {
    }

    public enum RemindType {
        follower, cmt, mention_status, mention_cmt
    }
    public void remindSetCount(final RemindType remindType) {

    }

}
