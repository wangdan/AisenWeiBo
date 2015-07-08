package org.aisen.weibo.sina.ui.fragment.basic;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.ViewUtils;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.action.IAction;
import org.aisen.android.support.textspan.ClickableTextViewMentionLinkOnTouchListener;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.orm.extra.Extra;
import org.aisen.orm.utils.FieldUtils;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.Favority;
import org.aisen.weibo.sina.sinasdk.bean.SetCount;
import org.aisen.weibo.sina.sinasdk.bean.StatusComment;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.sinasdk.bean.Token;
import org.aisen.weibo.sina.sinasdk.bean.UnreadCount;
import org.aisen.weibo.sina.sinasdk.bean.WeiBoUser;
import org.aisen.weibo.sina.support.action.DoLikeAction;
import org.aisen.weibo.sina.support.action.WebLoginAction;
import org.aisen.weibo.sina.support.db.SinaDB;
import org.aisen.weibo.sina.support.notifier.UnreadCountNotifier;
import org.aisen.weibo.sina.support.publisher.Notifier;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.support.utils.ThemeUtils;
import org.aisen.weibo.sina.sys.service.UnreadService;
import org.aisen.weibo.sina.ui.activity.basic.MainActivity;
import org.aisen.weibo.sina.ui.activity.pics.PicsActivity;
import org.aisen.weibo.sina.ui.activity.profile.UserProfileActivity;
import org.aisen.weibo.sina.ui.activity.publish.PublishActivity;
import org.aisen.weibo.sina.ui.fragment.profile.UserProfilePagerFragment;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
            BizFragment bizFragment = (BizFragment) fragment.getActivity().getFragmentManager().findFragmentByTag("org.aisen.android.ui.BizFragment");

            if (bizFragment == null) {
                bizFragment = new BizFragment();
                fragment.getActivity().getFragmentManager().beginTransaction().add(bizFragment, "org.aisen.android.ui.BizFragment").commit();
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

    View.OnClickListener PreviousArrOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Object[] tag = (Object[]) v.getTag();
            StatusContent bean = (StatusContent) tag[0];
            int selectedIndex = Integer.parseInt(tag[1].toString());

            PicsActivity.launch(getActivity(), bean, selectedIndex);
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
            WeiBoUser user = (WeiBoUser) v.getTag();
            if (user != null) {
                UserProfilePagerFragment.launch(getActivity(), user);
            }
        }
    };

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
//			FragmentContainerActivity.launch(BizFragment.this, UserTimelineFragment.class, args);
        }
    };

    public void userTimeline(View view, WeiBoUser user) {
        view.setTag(user);
        view.setOnClickListener(userTimelineListener);
    }

	/* 结束查看用户微博 */

    // XXX /*取消对某用户的关注*/
    public void destoryFriendship(final WeiBoUser user, final OnDestoryFriendshipCallback callback) {
        Token token = AppContext.getToken();
        if (AppContext.getAccount().getAdvancedToken() != null)
            token = AppContext.getAccount().getAdvancedToken();
        final Token trueToken = token;

        new AlertDialogWrapper.Builder(getActivity()).setMessage(R.string.biz_destory_friend)
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

                                ViewUtils.createProgressDialog(getActivity(), getString(R.string.biz_destory_friendship), ThemeUtils.getThemeColor()).show();
                            };

                            @Override
                            protected void onFailure(TaskException exception) {
                                super.onFailure(exception);
                                showMessage(exception.getMessage());
                            };

                            @Override
                            protected void onSuccess(WeiBoUser result) {
                                super.onSuccess(result);
                                if (getActivity() == null) {
                                    return;
                                }

                                callback.onFriendshipDestoryed(result);
                            };

                            @Override
                            protected void onFinished() {
                                super.onFinished();
                                if (getActivity() == null) {
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
        Token token = AppContext.getToken();
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

                ViewUtils.createProgressDialog(getActivity(), getString(R.string.biz_create_friendship), ThemeUtils.getThemeColor()).show();
            };

            @Override
            protected void onFailure(TaskException exception) {
                super.onFailure(exception);
                if (getActivity() == null) {
                    return;
                }

                showMessage(exception.getMessage());
            };

            @Override
            protected void onSuccess(WeiBoUser result) {
                super.onSuccess(result);
                if (getActivity() == null) {
                    return;
                }

                callback.onFriendshipCreated(result);
            };

            @Override
            protected void onFinished() {
                super.onFinished();
                if (getActivity() == null) {
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
            View entryView = View.inflate(getActivity(), R.layout.as_lay_dialog_remark_entry, null);
            final EditText editRemark = (EditText) entryView.findViewById(R.id.editRemark);
            editRemark.setHint(R.string.profile_remark_hint);
            editRemark.setText(TextUtils.isEmpty(user.getRemark()) ? "" : user.getRemark());
            editRemark.setSelection(editRemark.getText().toString().length());
            new AlertDialogWrapper.Builder(getActivity()).setTitle(R.string.biz_remark_update)
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

                                    ViewUtils.createProgressDialog(getActivity(), getString(R.string.biz_remark_loading), ThemeUtils.getThemeColor()).show();
                                };

                                @Override
                                public WeiBoUser workInBackground(Void... params) throws TaskException {
                                    return SinaSDK.getInstance(AppContext.getToken()).friendshipsRemarkUpdate(user.getIdstr(),
                                            editRemark.getText().toString());
                                }

                                @Override
                                protected void onFailure(TaskException exception) {
                                    super.onFailure(exception);
                                    if (getActivity() == null) {
                                        return;
                                    }

                                    showMessage(exception.getMessage());
                                };

                                @Override
                                protected void onSuccess(WeiBoUser result) {
                                    super.onSuccess(result);

                                    if (getActivity() == null) {
                                        return;
                                    }

                                    callback.onModifyUserRemark(editRemark.getText().toString());
                                };

                                @Override
                                protected void onFinished() {
                                    super.onFinished();

                                    if (getActivity() == null) {
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
//			FriendshipActivity.luncheActivity(getActivity(), user, type);

            // 如果是登录用户，且有未读粉丝信息，清零
            if (user.getIdstr().equals(AppContext.getUser().getIdstr())) {
                List<UnreadCount> unreadCountList = SinaDB.getSqlite().select(UnreadCount.class,
                        String.format(" %s = ? ", FieldUtils.OWNER), new String[] { AppContext.getUser().getIdstr() });
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
        Token token = AppContext.getToken();
        if (AppContext.getAccount().getAdvancedToken() != null)
            token = AppContext.getAccount().getAdvancedToken();
        final Token trueToken = token;

        new AlertDialogWrapper.Builder(getActivity())
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

                                ViewUtils.createProgressDialog(getActivity(), getString(R.string.biz_destory_follower_loading), ThemeUtils.getThemeColor()).show();
                            };

                            @Override
                            protected void onFailure(TaskException exception) {
                                super.onFailure(exception);

                                showMessage(exception.getMessage());
                            };

                            @Override
                            protected void onSuccess(WeiBoUser result) {
                                super.onSuccess(result);
                                if (getActivity() != null)
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
        PublishActivity.publishCommentReply(getActivity(), null, comment, true);
    }

    // XXX /*删除评论*/
	/* 删除评论 */

    public void commentDestory(final StatusComment commnet, final OnCommentDestoryCallback callback) {
        final WeiBoUser user = AppContext.getUser();

        new WorkTask<Void, Void, StatusComment>() {

            protected void onPrepare() {
                super.onPrepare();

                ViewUtils.createProgressDialog(getActivity(), getString(R.string.biz_delete_cmt_loading), ThemeUtils.getThemeColor()).show();
            };

            protected void onFinished() {
                super.onFinished();

                ViewUtils.dismissProgressDialog();
            };

            protected void onSuccess(StatusComment result) {
                super.onSuccess(result);
                if (getActivity() == null) {
                    return;
                }

                if (callback != null)
                    callback.onCommentDestory(commnet);

                showMessage(R.string.delete_success);

                // 删除成功后，DB同时也删除
                SinaDB.getTimelineSqlite().deleteById(new Extra(user.getIdstr(), null),
                                                              StatusComment.class, result.getId());
            };

            protected void onFailure(TaskException exception) {
                super.onFailure(exception);
                if (getActivity() == null) {
                    return;
                }

                if (!TextUtils.isEmpty(exception.getMessage()))
                    showMessage(exception.getMessage());
                else
                    showMessage(R.string.delete_faild);
            };

            @Override
            public StatusComment workInBackground(Void... params) throws TaskException {
                return SinaSDK.getInstance(AppContext.getToken()).commentsDestory(commnet.getId());
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
        if (comment.getUser() != null && !comment.getUser().getIdstr().equals(AppContext.getUser().getIdstr())) {
            if (status != null)
                comment.setStatus(status);
            PublishActivity.publishCommentReply(getActivity(), null, comment, false);
        }
    }

	/* 结束回复评论 */

    // XXX /*删除微博*/
	/* 开始删除微博 */

    public void statusDestory(final String id, final OnStatusDestoryCallback callback) {
        final WeiBoUser user = AppContext.getUser();

        new WorkTask<String, Void, StatusContent>() {

            protected void onPrepare() {
                super.onPrepare();

                ViewUtils.createProgressDialog(getActivity(), getString(R.string.biz_delete_status_loading), ThemeUtils.getThemeColor()).show();
            };

            protected void onFinished() {
                super.onFinished();
                ViewUtils.dismissProgressDialog();
            };

            protected void onSuccess(StatusContent result) {
                super.onSuccess(result);
                if (getActivity() == null) {
                    return;
                }

                callback.onStatusDestory(result);
                showMessage(R.string.delete_success);

                SinaDB.getTimelineSqlite().deleteById(new Extra(user.getIdstr(), null),
                                                            StatusContent.class, result.getId());
            };

            protected void onFailure(TaskException exception) {
                super.onFailure(exception);
                if (getActivity() == null) {
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
                return SinaSDK.getInstance(AppContext.getToken()).statusDestroy(params[0]);
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

                ViewUtils.createProgressDialog(getActivity(), getString(R.string.biz_add_fav), ThemeUtils.getThemeColor()).show();
            };

            protected void onFinished() {
                super.onFinished();

                ViewUtils.dismissProgressDialog();
            };

            protected void onSuccess(Favority result) {
                super.onSuccess(result);
                if (getActivity() == null) {
                    return;
                }

                ViewUtils.showMessage(R.string.biz_fav_success);

                if (callback != null)
                    callback.onFavorityCreate(result);
            };

            protected void onFailure(TaskException exception) {
                super.onFailure(exception);
                if (getActivity() == null) {
                    return;
                }

                if (callback == null || !callback.onFaild(exception)) {
                    showMessage(exception.getMessage());
                }
            };

            @Override
            public Favority workInBackground(String... params) throws TaskException {
                return SinaSDK.getInstance(AppContext.getToken()).favoritesCreate(params[0]);
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

                ViewUtils.createProgressDialog(getActivity(), getString(R.string.biz_remove_fav), ThemeUtils.getThemeColor()).show();
            };

            protected void onFinished() {
                super.onFinished();

                ViewUtils.dismissProgressDialog();
            };

            protected void onSuccess(Favority result) {
                super.onSuccess(result);
                if (getActivity() == null) {
                    return;
                }

                Intent intent = new Intent();
                intent.setAction("org.aisen.weibo.sina.FAV_DESTORY");
                intent.putExtra("statusId", result.getStatus().getId());
                // 如果是评论页面，就设置回执
                if (getActivity() instanceof UserProfileActivity) {
                    // 加入是收藏列表界面，发出广播更新它
                    GlobalContext.getInstance().sendBroadcast(intent);
                }
                else {
                    getActivity().setResult(Activity.RESULT_OK, intent);
                }

                ViewUtils.showMessage(R.string.biz_fav_removed);

                if (callback != null)
                    callback.onFavorityDestory(result);
            };

            protected void onFailure(TaskException exception) {
                super.onFailure(exception);
                if (getActivity() == null) {
                    return;
                }

                if (callback == null || !callback.onFaild(exception)) {
                    showMessage(exception.getMessage());
                }
                else {
                    ViewUtils.showMessage(R.string.biz_fav_remove_faild);
                }
            };

            @Override
            public Favority workInBackground(String... params) throws TaskException {
                return SinaSDK.getInstance(AppContext.getToken()).favoritesDestory(params[0]);
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
        PublishActivity.publishStatusComment(getActivity(), null, status);
    }

	/* 结束回复微博 */

    // XXX /*转发微博*/

	/* 转发微博 */

    public void statusRepost(StatusContent status) {
        PublishActivity.publishStatusRepost(getActivity(), null, status);
//		StatusRepostFragment.lunchPublishRepost(getActivity(), status, append);
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
        final String uid = AppContext.getUser().getIdstr();
        new WorkTask<RemindType, Void, SetCount>() {

            @Override
            public SetCount workInBackground(RemindType... params) throws TaskException {
                return SinaSDK.getInstance(AppContext.getToken()).remindSetCount(params[0].toString());
            }

            @Override
            protected void onSuccess(SetCount result) {
                super.onSuccess(result);
                if (getActivity() == null)
                    return;

                UnreadCount count = AppContext.getUnreadCount();
                if (count != null) {
                    UnreadCountNotifier notifier = new UnreadCountNotifier(getActivity());
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
                    SinaDB.getSqlite().insert(new Extra(uid, null), count);
                    AppContext.setUnreadCount(count);
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

    private WeakReference<IAction> dmLoginAction;

    public void setDMLogin(IAction action) {
        dmLoginAction = new WeakReference<IAction>(action);
    }

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            Set<String> keySet = likeActionMap.keySet();
            for (String key : keySet) {
                DoLikeAction action = likeActionMap.get(key).get();
                if (action != null && ((WebLoginAction) action.getParent()).getRequestCode() == requestCode)
                    action.run();
            }

            if (dmLoginAction != null && dmLoginAction.get() != null) {
                dmLoginAction.get().run();

                dmLoginAction = null;
            }
        }
    }

}
