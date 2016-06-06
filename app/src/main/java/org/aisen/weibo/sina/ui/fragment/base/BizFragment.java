package org.aisen.weibo.sina.ui.fragment.base;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.ViewUtils;
import org.aisen.android.component.orm.extra.Extra;
import org.aisen.android.component.orm.utils.FieldUtils;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.action.IAction;
import org.aisen.android.support.textspan.ClickableTextViewMentionLinkOnTouchListener;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.AppSettings;
import org.aisen.weibo.sina.service.UnreadService;
import org.aisen.weibo.sina.service.notifier.Notifier;
import org.aisen.weibo.sina.service.notifier.UnreadCountNotifier;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.Favority;
import org.aisen.weibo.sina.sinasdk.bean.SetCount;
import org.aisen.weibo.sina.sinasdk.bean.StatusComment;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.Token;
import org.aisen.weibo.sina.sinasdk.bean.UnreadCount;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;
import org.aisen.weibo.sina.support.action.DoLikeAction;
import org.aisen.weibo.sina.support.bean.AccountBean;
import org.aisen.weibo.sina.support.sqlit.SinaDB;
import org.aisen.weibo.sina.support.utils.AccountUtils;
import org.aisen.weibo.sina.support.utils.FabAnimator;
import org.aisen.weibo.sina.support.utils.ThemeUtils;
import org.aisen.weibo.sina.ui.activity.base.MainActivity;
import org.aisen.weibo.sina.ui.activity.picture.PicsActivity;
import org.aisen.weibo.sina.ui.activity.profile.WeiboClientActivity;
import org.aisen.weibo.sina.ui.activity.publish.PublishActivity;
import org.aisen.weibo.sina.ui.fragment.account.WebLoginFragment;
import org.aisen.weibo.sina.ui.fragment.profile.ProfilePagerFragment;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment是一个神器，是跨Activity和Fragment之前通讯的重要的桥梁
 *
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

    public static final int REQUEST_CODE_AD_AUTH = 52231;
    public static final int REQUEST_CODE_AUTH = 52232;

    private Activity mActivity;

    private FabAnimator fabAnimator;

    public void createFabAnimator(View fabBtn) {
        fabAnimator = FabAnimator.create(fabBtn, GlobalContext.getInstance().getResources().getDimensionPixelSize(R.dimen.fab_scrollthreshold));
    }

    public FabAnimator getFabAnimator() {
        return fabAnimator;
    }
    
    private Activity getRealActivity() {
        if (getActivity() != null)
            return getActivity();
        
        return mActivity;
    }
    
    private String getRealString(int resId) {
        if (getResources() != null) {
            return getString(resId);
        }
        
        return mActivity.getString(resId);
    }

    @Override
    public int inflateContentView() {
        return -1;
    }

    public static BizFragment createBizFragment(ABaseFragment fragment) {
        if (fragment != null && fragment.getActivity() != null) {
            BizFragment bizFragment = (BizFragment) fragment.getActivity().getFragmentManager().findFragmentByTag("org.aisen.android.ui.BizFragment");

            if (bizFragment == null) {
                bizFragment = new BizFragment();
                bizFragment.mActivity = fragment.getActivity();
                fragment.getActivity().getFragmentManager().beginTransaction().add(bizFragment, "org.aisen.android.ui.BizFragment").commit();
            }

            return bizFragment;
        }

        return null;
    }

    public static BizFragment createBizFragment(Activity activity) {
        BizFragment bizFragment = (BizFragment) activity.getFragmentManager().findFragmentByTag("BizFragment");
        if (bizFragment == null) {
            bizFragment = new BizFragment();
            bizFragment.mActivity = activity;

            if (activity instanceof BaseActivity) {
                if (((BaseActivity) activity).isDestory()) {
                    return bizFragment;
                }
            }

            activity.getFragmentManager().beginTransaction().add(bizFragment, "BizFragment").commit();
        }
        return bizFragment;
    }
    
    View.OnClickListener PreviousArrOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Object[] tag = (Object[]) v.getTag();
            StatusContent bean = (StatusContent) tag[0];
            int selectedIndex = Integer.parseInt(tag[1].toString());

            PicsActivity.launch(getRealActivity(), bean, selectedIndex);
        }
    };

    public void previousPics(View view, StatusContent bean, int selectedIndex) {
        Object[] tag = new Object[] { bean, selectedIndex };
        view.setTag(tag);
        view.setOnClickListener(PreviousArrOnClickListener);
    }

	/* 结束预览图片 */

    // XXX /*查看用户详情*/
    View.OnClickListener UserShowListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            final WeiBoUser user = (WeiBoUser) v.getTag();

            launchProfile(user);
        }
    };

    public void launchProfile(final WeiBoUser user) {
        if (user != null) {
            new IAction(getRealActivity(), new CheckAdTokenAction(getRealActivity(), null, null)) {

                @Override
                public void doAction() {
                    ProfilePagerFragment.launch(getRealActivity(), user);
                }

            }.run();
        }
    }

    public void checkProfile(final CheckProfileCallback callback) {
        new IAction(getRealActivity(), new CheckAdTokenAction(getRealActivity(), null, callback)) {

            @Override
            public void doAction() {
                if (callback != null) {
                    callback.onCheckProfileSuccess();
                }
            }

        }.run();
    }

    public interface CheckProfileCallback {

        void onCheckProfileSuccess();

        void onCheckProfileFaild();

    }

    CheckAdTokenAction checkAdTokenAction;
    class CheckAdTokenAction extends IAction {

        private CheckProfileCallback callback;

        public CheckAdTokenAction(Activity context, IAction parent, CheckProfileCallback callback) {
            super(context, parent);

            this.callback = callback;
        }

        @Override
        protected boolean interrupt() {
            boolean interrupt = AppContext.getAccount().getAdvancedToken() == null ||
                    AppContext.getAccount().getAdvancedToken().isExpired();

            if (interrupt) {
                doInterrupt();
            }
            else {
                checkAdTokenAction = null;
            }

            return interrupt;
        }

        @Override
        public void doInterrupt() {
            new AlertDialogWrapper.Builder(getRealActivity())
//                        .setTitle(R.string.profile_ad_title)
//                        .setMessage(R.string.profile_ad_message)
                    .setMessage(R.string.profile_ad_title)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkAdTokenAction = CheckAdTokenAction.this;

                            String account = AppContext.getAccount().getAccount();
                            String password = AppContext.getAccount().getPassword();

                            WebLoginFragment.launch(BizFragment.this, WebLoginFragment.Client.weico, account, password, REQUEST_CODE_AD_AUTH);
                        }

                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (callback != null)
                                callback.onCheckProfileFaild();
                        }

                    })
                    .show();
        }

        @Override
        public void doAction() {
            getChild().run();
        }

    }

    public void userShow(View view, WeiBoUser user) {
        view.setTag(user);
        view.setOnClickListener(UserShowListener);
    }

	/* 结束查看用户详情 */

    // XXX /*查看用户微博*/
    View.OnClickListener userTimelineListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
//			WeiBoUser user = (WeiBoUser) v.getTag();
//			FragmentArgs args = new FragmentArgs();
//			args.add(UserTimelineFragment.KEY_USER, user);
//			args.add(ABaseFragment.SHOW_ACTIONBAR, true);
//			SinaCommonActivity.launch(BizFragment.this, UserTimelineFragment.class, args);
        }
    };

    public void userTimeline(View view, WeiBoUser user) {
        view.setTag(user);
        view.setOnClickListener(userTimelineListener);
    }

	/* 结束查看用户微博 */

    // XXX /*取消对某用户的关注*/
    public void destoryFriendship(final WeiBoUser user, final OnDestoryFriendshipCallback callback) {
        Token token = AppContext.getAccount().getAccessToken();
        if (AppContext.getAccount().getAdvancedToken() != null)
            token = AppContext.getAccount().getAdvancedToken();
        final Token trueToken = token;

        new AlertDialogWrapper.Builder(getRealActivity()).setMessage(R.string.biz_destory_friend)
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new WorkTask<Void, Void, WeiBoUser>() {

                            @Override
                            public WeiBoUser workInBackground(Void... params) throws TaskException {
                                return SinaSDK.getInstance(trueToken).friendshipsDestroy(user.getIdstr());
                            }

                            @Override
                            protected void onPrepare() {
                                super.onPrepare();

                                ViewUtils.createProgressDialog(getRealActivity(), getRealString(R.string.biz_destory_friendship), ThemeUtils.getThemeColor()).show();
                            };

                            @Override
                            protected void onFailure(TaskException exception) {
                                super.onFailure(exception);
                                showMessage(exception.getMessage());
                            };

                            @Override
                            protected void onSuccess(WeiBoUser result) {
                                super.onSuccess(result);
                                if (getRealActivity() == null) {
                                    return;
                                }

                                callback.onFriendshipDestoryed(result);
                            };

                            @Override
                            protected void onFinished() {
                                super.onFinished();
                                if (getRealActivity() == null) {
                                    return;
                                }

                                ViewUtils.dismissProgressDialog();
                            };

                        }.execute();
                    }
                })
                .show();
    }

    public interface OnDestoryFriendshipCallback {

        public void onFriendshipDestoryed(WeiBoUser targetUser);

    }

	/* 结束取消对某用户的关注 */

    // XXX /*添加关注*/
    public void createFriendship(final WeiBoUser user, final OnCreateFriendshipCallback callback) {
        Token token = AppContext.getAccount().getAccessToken();
        if (AppContext.getAccount().getAdvancedToken() != null)
            token = AppContext.getAccount().getAdvancedToken();
        final Token trueToken = token;

        new WorkTask<Void, Void, WeiBoUser>() {

            @Override
            public WeiBoUser workInBackground(Void... params) throws TaskException {
                return SinaSDK.getInstance(trueToken).friendshipsCreate(user.getIdstr());
            }

            @Override
            protected void onPrepare() {
                super.onPrepare();

                ViewUtils.createProgressDialog(getRealActivity(), getRealString(R.string.biz_create_friendship), ThemeUtils.getThemeColor()).show();
            };

            @Override
            protected void onFailure(TaskException exception) {
                super.onFailure(exception);
                if (getRealActivity() == null) {
                    return;
                }

                showMessage(exception.getMessage());
            };

            @Override
            protected void onSuccess(WeiBoUser result) {
                super.onSuccess(result);
                if (getRealActivity() == null) {
                    return;
                }

                callback.onFriendshipCreated(result);
            };

            @Override
            protected void onFinished() {
                super.onFinished();
                if (getRealActivity() == null) {
                    return;
                }

                ViewUtils.dismissProgressDialog();
            };

        }.execute();
    }

    public interface OnCreateFriendshipCallback {

        public void onFriendshipCreated(WeiBoUser targetUser);

    }

	/* 结束添加关注 */

    // XXX /*添加备注*/
    public void modifyUserMark(View view, WeiBoUser user, OnModifyUserRemarkCallback callback) {
        Object[] tag = new Object[] { user, callback };
        view.setTag(tag);
        view.setOnClickListener(modifyUserRemarkListener);
    }

    View.OnClickListener modifyUserRemarkListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Object[] tag = (Object[]) v.getTag();
            final WeiBoUser user = (WeiBoUser) tag[0];
            final OnModifyUserRemarkCallback callback = (OnModifyUserRemarkCallback) tag[1];
            View entryView = View.inflate(getRealActivity(), R.layout.lay_dialog_remark_entry, null);
            final EditText editRemark = (EditText) entryView.findViewById(R.id.editRemark);
            editRemark.setHint(R.string.profile_remark_hint);
            editRemark.setText(TextUtils.isEmpty(user.getRemark()) ? "" : user.getRemark());
            editRemark.setSelection(editRemark.getText().toString().length());
            new AlertDialogWrapper.Builder(getRealActivity()).setTitle(R.string.biz_remark_update)
                    .setView(entryView)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
//										if (TextUtils.isEmpty(dialog.getInput())) {
//											showMessage("备注名称不能为空");
//											return true;
//										}
                            new WorkTask<Void, Void, WeiBoUser>() {

                                @Override
                                protected void onPrepare() {
                                    super.onPrepare();

                                    ViewUtils.createProgressDialog(getRealActivity(), getRealString(R.string.biz_remark_loading), ThemeUtils.getThemeColor()).show();
                                };

                                @Override
                                public WeiBoUser workInBackground(Void... params) throws TaskException {
                                    return SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).friendshipsRemarkUpdate(user.getIdstr(),
                                            editRemark.getText().toString());
                                }

                                @Override
                                protected void onFailure(TaskException exception) {
                                    super.onFailure(exception);
                                    if (getRealActivity() == null) {
                                        return;
                                    }

                                    showMessage(exception.getMessage());
                                };

                                @Override
                                protected void onSuccess(WeiBoUser result) {
                                    super.onSuccess(result);

                                    if (getRealActivity() == null) {
                                        return;
                                    }

                                    callback.onModifyUserRemark(editRemark.getText().toString());
                                };

                                @Override
                                protected void onFinished() {
                                    super.onFinished();

                                    if (getRealActivity() == null) {
                                        return;
                                    }

                                    ViewUtils.dismissProgressDialog();
                                };

                            }.execute();
                        }

                    })
                    .show();
        }
    };

    public interface OnModifyUserRemarkCallback {

        public void onModifyUserRemark(String remark);

    }

	/* 结束添加备注 */

    // XXX /*查看用户关系*/
	/* 查看用户好友列表 */
    public void userFriends(View view, WeiBoUser user) {
//		Object[] tag = new Object[] { user, FriendshipType.friends };
//		view.setTag(tag);
//		view.setOnClickListener(userFriendsListener);
    }

    /* 查看用户好友列表 */
    public void userFollowers(View view, WeiBoUser user) {
//		Object[] tag = new Object[] { user, FriendshipType.followers };
//		view.setTag(tag);
//		view.setOnClickListener(userFriendsListener);
    }

    View.OnClickListener userFriendsListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Object[] tag = (Object[]) v.getTag();
            WeiBoUser user = (WeiBoUser) tag[0];
//			FriendshipType type = (FriendshipType) tag[1];
//			FriendshipActivity.luncheActivity(getRealActivity(), user, type);

            // 如果是登录用户，且有未读粉丝信息，清零
            if (user.getIdstr().equals(AppContext.getAccount().getUser().getIdstr())) {
                List<UnreadCount> unreadCountList = SinaDB.getDB().select(UnreadCount.class,
                        String.format(" %s = ? ", FieldUtils.OWNER), new String[] { AppContext.getAccount().getUser().getIdstr() });
                if (unreadCountList.size() > 0) {
                    UnreadCount count = unreadCountList.get(0);
                    if (count.getFollower() > 0)
                        remindSetCount(RemindType.follower);
                }
            }
        }
    };

	/* 结束查看用户好友列表 */

    // XXX /*开始移除粉丝*/
    public void destoryFollower(final WeiBoUser user, final OnDestoryFollowerCallback callback) {
        Token token = AppContext.getAccount().getAccessToken();
        if (AppContext.getAccount().getAdvancedToken() != null)
            token = AppContext.getAccount().getAdvancedToken();
        final Token trueToken = token;

        new AlertDialogWrapper.Builder(getRealActivity())
                .setTitle(R.string.title_destory_friend)
                .setMessage(R.string.biz_destory_follower)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new WorkTask<Void, Void, WeiBoUser>() {

                            @Override
                            public WeiBoUser workInBackground(Void... params) throws TaskException {
                                return SinaSDK.getInstance(trueToken).friendshipsFollowersDestory(user.getIdstr());
                            }

                            @Override
                            protected void onPrepare() {
                                super.onPrepare();

                                ViewUtils.createProgressDialog(getRealActivity(), getRealString(R.string.biz_destory_follower_loading), ThemeUtils.getThemeColor()).show();
                            };

                            @Override
                            protected void onFailure(TaskException exception) {
                                super.onFailure(exception);

                                showMessage(exception.getMessage());
                            };

                            @Override
                            protected void onSuccess(WeiBoUser result) {
                                super.onSuccess(result);
                                if (getRealActivity() != null)
                                    callback.onDestoryFollower(result);
                            };

                            @Override
                            protected void onFinished() {
                                super.onFinished();

                                ViewUtils.dismissProgressDialog();
                            };

                        }.execute();
                    }
                })
                .show();
    }

    public interface OnDestoryFollowerCallback {

        public void onDestoryFollower(WeiBoUser user);

    }

	/* 结束移除粉丝 */

    // XXX /*转发评论*/
	/* 转发评论 */
    public void commentRepost(final StatusComment comment) {
        PublishActivity.publishCommentReply(getRealActivity(), null, comment, true);
    }

    // XXX /*删除评论*/
	/* 删除评论 */

    public void commentDestory(final StatusComment commnet, final OnCommentDestoryCallback callback) {
        final WeiBoUser user = AppContext.getAccount().getUser();

        new WorkTask<Void, Void, StatusComment>() {

            protected void onPrepare() {
                super.onPrepare();

                ViewUtils.createProgressDialog(getRealActivity(), getRealString(R.string.biz_delete_cmt_loading), ThemeUtils.getThemeColor()).show();
            };

            protected void onFinished() {
                super.onFinished();

                ViewUtils.dismissProgressDialog();
            };

            protected void onSuccess(StatusComment result) {
                super.onSuccess(result);
                if (getRealActivity() == null) {
                    return;
                }

                if (callback != null)
                    callback.onCommentDestory(commnet);

                showMessage(R.string.delete_success);

                // 删除成功后，DB同时也删除
                SinaDB.getTimelineDB().deleteById(new Extra(user.getIdstr(), null),
                        StatusComment.class, result.getId());
            };

            protected void onFailure(TaskException exception) {
                super.onFailure(exception);
                if (getRealActivity() == null) {
                    return;
                }

                if (!TextUtils.isEmpty(exception.getMessage()))
                    showMessage(exception.getMessage());
                else
                    showMessage(R.string.delete_faild);
            };

            @Override
            public StatusComment workInBackground(Void... params) throws TaskException {
                return SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).commentsDestory(commnet.getId());
            }

        }.execute();
    }

    public interface OnCommentDestoryCallback {

        public void onCommentDestory(StatusComment commnet);

    }

	/* 结束删除评论 */

    // XXX /*回复评论*/
	/* 回复评论 */

    public void replyComment(StatusContent status, StatusComment comment) {
        if (comment.getUser() != null && !comment.getUser().getIdstr().equals(AppContext.getAccount().getUser().getIdstr())) {
            if (status != null)
                comment.setStatus(status);
            PublishActivity.publishCommentReply(getRealActivity(), null, comment, false);
        }
    }

	/* 结束回复评论 */

    // XXX /*删除微博*/
	/* 开始删除微博 */

    public void statusDestory(final String id, final OnStatusDestoryCallback callback) {
        final WeiBoUser user = AppContext.getAccount().getUser();

        new WorkTask<String, Void, StatusContent>() {

            protected void onPrepare() {
                super.onPrepare();

                ViewUtils.createProgressDialog(getRealActivity(), getRealString(R.string.biz_delete_status_loading), ThemeUtils.getThemeColor()).show();
            };

            protected void onFinished() {
                super.onFinished();
                ViewUtils.dismissProgressDialog();
            };

            protected void onSuccess(StatusContent result) {
                super.onSuccess(result);
                if (getRealActivity() == null) {
                    return;
                }

                callback.onStatusDestory(result);
                showMessage(R.string.delete_success);

                SinaDB.getTimelineDB().deleteById(new Extra(user.getIdstr(), null),
                        StatusContent.class, result.getId());
            };

            protected void onFailure(TaskException exception) {
                super.onFailure(exception);
                if (getRealActivity() == null) {
                    return;
                }

                if (exception != null && "20101".equals(exception.getMessage())) {
                    StatusContent status = new StatusContent();
                    status.setId(Long.parseLong(id));

                    onSuccess(status);
                }
                else if (!callback.onFaild(exception)) {
                    showMessage(exception.getMessage());
                }
            };

            @Override
            public StatusContent workInBackground(String... params) throws TaskException {
                return SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).statusDestroy(params[0]);
            }

        }.execute(id);
    }

    public interface OnStatusDestoryCallback {

        public void onStatusDestory(StatusContent status);

        public boolean onFaild(TaskException e);

    }

	/* 结束删除微博 */

    // XXX /*收藏微博*/
	/* 开始收藏微博 */

    public void favorityCreate(String id, final OnFavorityCreateCallback callback) {
        new WorkTask<String, Void, Favority>() {

            protected void onPrepare() {
                super.onPrepare();

                ViewUtils.createProgressDialog(getRealActivity(), getRealString(R.string.biz_add_fav), ThemeUtils.getThemeColor()).show();
            };

            protected void onFinished() {
                super.onFinished();

                ViewUtils.dismissProgressDialog();
            };

            protected void onSuccess(Favority result) {
                super.onSuccess(result);
                if (getRealActivity() == null) {
                    return;
                }

                ViewUtils.showMessage(getRealActivity(), R.string.biz_fav_success);

                if (callback != null)
                    callback.onFavorityCreate(result);
            };

            protected void onFailure(TaskException exception) {
                super.onFailure(exception);
                if (getRealActivity() == null) {
                    return;
                }

                if (callback == null || !callback.onFaild(exception)) {
                    showMessage(exception.getMessage());
                }
            };

            @Override
            public Favority workInBackground(String... params) throws TaskException {
                return SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).favoritesCreate(params[0]);
            }

        }.execute(id);
    }

    public interface OnFavorityCreateCallback {

        public void onFavorityCreate(Favority status);

        public boolean onFaild(TaskException exception);

    }

	/* 结束收藏微博 */

    // XXX /*取消收藏微博*/
	/* 开始取消收藏微博 */

    public void favorityDestory(String id, final OnFavorityDestoryCallback callback) {
        new WorkTask<String, Void, Favority>() {

            protected void onPrepare() {
                super.onPrepare();

                ViewUtils.createProgressDialog(getRealActivity(), getRealString(R.string.biz_remove_fav), ThemeUtils.getThemeColor()).show();
            };

            protected void onFinished() {
                super.onFinished();

                ViewUtils.dismissProgressDialog();
            };

            protected void onSuccess(Favority result) {
                super.onSuccess(result);
                if (getRealActivity() == null) {
                    return;
                }

                Intent intent = new Intent();
                intent.setAction("org.aisen.weibo.sina.FAV_DESTORY");
                intent.putExtra("statusId", result.getStatus().getId());
                getRealActivity().setResult(Activity.RESULT_OK, intent);
                GlobalContext.getInstance().sendBroadcast(intent);

                ViewUtils.showMessage(getRealActivity(), R.string.biz_fav_removed);

                if (callback != null)
                    callback.onFavorityDestory(result);
            };

            protected void onFailure(TaskException exception) {
                super.onFailure(exception);
                if (getRealActivity() == null) {
                    return;
                }

                if (callback == null || !callback.onFaild(exception)) {
                    showMessage(exception.getMessage());
                }
                else {
                    ViewUtils.showMessage(getRealActivity(), R.string.biz_fav_remove_faild);
                }
            };

            @Override
            public Favority workInBackground(String... params) throws TaskException {
                return SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).favoritesDestory(params[0]);
            }

        }.execute(id);
    }

    public interface OnFavorityDestoryCallback {

        public void onFavorityDestory(Favority status);

        public boolean onFaild(TaskException exception);

    }

	/* 结束取消收藏微博 */

    // XXX /*回复微博*/
	/* 回复微博 */

    public void commentCreate(StatusContent status) {
        PublishActivity.publishStatusComment(getRealActivity(), null, status);
    }

	/* 结束回复微博 */

    // XXX /*转发微博*/

	/* 转发微博 */

    public void statusRepost(StatusContent status) {
        PublishActivity.publishStatusRepost(getRealActivity(), null, status);
//		StatusRepostFragment.lunchPublishRepost(getRealActivity(), status, append);
    }

	/* 结束转发微博 */

    // XXX /*回到首页*/

	/* 回到首页 */

    public void backToMainActivity(Activity context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        Logger.v("回到首页");
    }

	/* 回到首页 */

    // XXX 清零未读信息
	/* 清零未读信息 */

    public enum RemindType {
        follower, cmt, mention_status, mention_cmt
    }

    public void remindSetCount(final RemindType remindType) {
        // 测试通知功能时，不清零
        if (AppSettings.ignoreUnread()) {
            return;
        }

        final String uid = AppContext.getAccount().getUser().getIdstr();
        new WorkTask<RemindType, Void, SetCount>() {

            @Override
            public SetCount workInBackground(RemindType... params) throws TaskException {
                return SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).remindSetCount(params[0].toString());
            }

            @Override
            protected void onSuccess(SetCount result) {
                super.onSuccess(result);
                if (getRealActivity() == null)
                    return;

                UnreadCount count = AppContext.getAccount().getUnreadCount();
                if (count != null) {
                    UnreadCountNotifier notifier = new UnreadCountNotifier(getRealActivity());
                    if (remindType == RemindType.cmt) {
                        count.setCmt(0);
                        notifier.cancelNotification(Notifier.RemindUnreadComments);
                    } else if (remindType == RemindType.follower) {
                        count.setFollower(0);
                        notifier.cancelNotification(Notifier.RemindUnreadForFollowers);
                    } else if (remindType == RemindType.mention_cmt) {
                        count.setMention_cmt(0);
                        notifier.cancelNotification(Notifier.RemindUnreadForMentionComments);
                    } else if (remindType == RemindType.mention_status) {
                        count.setMention_status(0);
                        notifier.cancelNotification(Notifier.RemindUnreadForMentionStatus);
                    }

                    // 更新DB
                    SinaDB.getDB().insert(new Extra(uid, null), count);
                    AppContext.getAccount().setUnreadCount(count);
                    UnreadCountNotifier.mCount = count;

                    // 发出广播更新状态
                    UnreadService.sendUnreadBroadcast();
                }

            };

        }.execute(remindType);
    }

	/* 清零未读信息 */

    /* 设置有@用户、话题的onTouch事件 */
    public void bindOnTouchListener(TextView textView) {
        textView.setClickable(false);
        textView.setOnTouchListener(onTouchListener);
    }

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {

        ClickableTextViewMentionLinkOnTouchListener listener = new ClickableTextViewMentionLinkOnTouchListener();

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return listener.onTouch(v, event);

        }
    };

    // XXX \@用户
	/* \@用户 */
    public void mentionUser(Activity from, WeiBoUser user) {
        PublishActivity.publishStatusWithMention(from, user);
    }

    /******************以下是点赞的逻辑***************/

    private Map<String, WeakReference<DoLikeAction>> likeActionMap = new HashMap<>();

    /**
     * 点赞或者取消点赞
     *
     */
    public void doLike(final StatusContent data, final boolean like, View likeView, final DoLikeAction.OnLikeCallback callback) {
        String key = String.valueOf(data.getId());
        DoLikeAction action = likeActionMap.containsKey(key) ? likeActionMap.get(key).get() : null;
        if (action != null && action.isRunning())
            return;

        action = new DoLikeAction(getActivity(), this, likeView, data, like, callback);
        likeActionMap.put(key, new WeakReference<DoLikeAction>(action));
        action.run();
    }

    private IAction requestWebLoginAction;
    public void requestWebLogin(IAction action) {
        requestWebLoginAction = action;

        WeiboClientActivity.launchForAuth(this, 123123);
    }

    public void animScale(final View likeView) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 1.5f, 1.0f, 1.5f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(200);
        scaleAnimation.setFillAfter(true);
        scaleAnimation.start();
        likeView.startAnimation(scaleAnimation);
        likeView.postDelayed(new Runnable() {

            @Override
            public void run() {
                ScaleAnimation scaleAnimation = new ScaleAnimation(1.5f, 1.0f, 1.5f, 1.0f,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(200);
                scaleAnimation.setFillAfter(true);
                likeView.startAnimation(scaleAnimation);
            }

        }, 200);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            // 请求授权
            if (requestCode == REQUEST_CODE_AD_AUTH) {
                AccountBean accountBean = (AccountBean) data.getSerializableExtra("account");

                AppContext.getAccount().setAdvancedToken(accountBean.getAccessToken());

                AccountUtils.newAccount(AppContext.getAccount());
                AccountUtils.setLogedinAccount(AppContext.getAccount());

                if (checkAdTokenAction != null) {
                    checkAdTokenAction.run();
                }
            }
            // 处理点赞
            else if (123123 == requestCode) {
                if (requestWebLoginAction != null) {
                    requestWebLoginAction.run();
                }
            }
        }

        checkAdTokenAction = null;
        requestWebLoginAction = null;
    }

}
