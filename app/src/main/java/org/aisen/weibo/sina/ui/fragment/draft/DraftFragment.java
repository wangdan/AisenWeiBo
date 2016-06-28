package org.aisen.weibo.sina.ui.fragment.draft;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.DateUtils;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.common.utils.ViewUtils;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.android.ui.fragment.ARecycleViewFragment;
import org.aisen.android.ui.fragment.adapter.ARecycleViewItemView;
import org.aisen.android.ui.fragment.itemview.IITemView;
import org.aisen.android.ui.fragment.itemview.IItemViewCreator;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.MyApplication;
import org.aisen.weibo.sina.service.PublishService;
import org.aisen.weibo.sina.service.publisher.PublishManager;
import org.aisen.weibo.sina.support.bean.PublishBean;
import org.aisen.weibo.sina.support.bean.PublishType;
import org.aisen.weibo.sina.support.sqlit.PublishDB;
import org.aisen.weibo.sina.support.utils.ThemeUtils;
import org.aisen.weibo.sina.support.utils.UMengUtil;
import org.aisen.weibo.sina.ui.activity.publish.PublishActivity;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;
import org.aisen.weibo.sina.ui.widget.AisenTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * 草稿箱
 *
 * @author wangdan
 */
public class DraftFragment extends ARecycleViewFragment<PublishBean, ArrayList<PublishBean>>
                                implements View.OnClickListener {

    public static ABaseFragment newInstance() {
        return new DraftFragment();
    }

    @Override
    public int inflateContentView() {
        return R.layout.ui_draft;
    }

    private void setViewPadding(View viewGroup) {
        viewGroup.setPadding(viewGroup.getPaddingLeft(), viewGroup.getPaddingTop(),
                viewGroup.getPaddingRight(), SystemUtils.getNavigationBarHeight(getActivity()));
    }

    @Override
    protected void setupRefreshConfig(RefreshConfig config) {
        super.setupRefreshConfig(config);

        config.emptyHint = getString(R.string.empty_draft);
        config.footerMoreEnable = false;
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        setViewPadding(getEmptyLayout());
        setViewPadding(getLoadingLayout());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PublishBean bean = getAdapterItems().get(position);

        switch (bean.getType()) {
            case status:
                PublishActivity.publishStatus(getActivity(), bean);
                break;
            case statusRepost:
                PublishActivity.publishStatusRepost(getActivity(), bean, bean.getStatusContent());
                break;
            case commentReply:
                PublishActivity.publishCommentReply(getActivity(), bean, bean.getStatusComment(), false);
                break;
            case commentCreate:
                PublishActivity.publishStatusComment(getActivity(), bean, bean.getStatusContent());
                break;
            default:
                break;
        }
    }

    @Override
    public IItemViewCreator<PublishBean> configItemViewCreator() {
        return new IItemViewCreator<PublishBean>() {

            @Override
            public View newContentView(LayoutInflater inflater, ViewGroup parent, int viewType) {
                return inflater.inflate(R.layout.item_draft, parent, false);
            }

            @Override
            public IITemView<PublishBean> newItemView(View convertView, int viewType) {
                return new DraftboxItemView(convertView);
            }

        };
    }

    @Override
    public void onResume() {
        super.onResume();

        UMengUtil.onPageStart(getActivity(), "草稿箱页");

        IntentFilter filter = new IntentFilter();
        filter.addAction(PublishManager.ACTION_PUBLISH_CHANNGED);
        getActivity().registerReceiver(receiver, filter);

        new DraftTask(RefreshMode.reset).execute();
    }

    @Override
    public void onPause() {
        super.onPause();

        UMengUtil.onPageEnd(getActivity(), "草稿箱页");

        getActivity().unregisterReceiver(receiver);
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && PublishManager.ACTION_PUBLISH_CHANNGED.equals(intent.getAction()))
                new DraftTask(RefreshMode.reset).execute();
        }

    };

    @Override
    public void requestData(RefreshMode mode) {
    }

    class DraftTask extends APagingTask<Void, Void, ArrayList<PublishBean>> {

        public DraftTask(RefreshMode mode) {
            super(mode);
        }

        @Override
        protected List<PublishBean> parseResult(ArrayList<PublishBean> result) {
            return result;
        }

        @Override
        protected ArrayList<PublishBean> workInBackground(RefreshMode mode, String previousPage,
                                                          String nextPage, Void... params) throws TaskException {
            return PublishDB.getPublishList(AppContext.getAccount().getUser());
        }

    }

    class DraftboxItemView extends ARecycleViewItemView<PublishBean> {

        @ViewInject(id = R.id.txtType)
        TextView txtType;
        @ViewInject(id = R.id.txtTiming)
        TextView txtTiming;
        @ViewInject(id = R.id.txtContent)
        AisenTextView txtContent;
        @ViewInject(id = R.id.txtError)
        TextView txtError;
        @ViewInject(id = R.id.container)
        View container;
        @ViewInject(id = R.id.btnDel)
        View btnDel;
        @ViewInject(id = R.id.btnResend)
        View btnResend;

        public DraftboxItemView(View itemView) {
            super(getActivity(), itemView);
        }

        @Override
        public void onBindData(View convertView, PublishBean data, int position) {
            txtTiming.setVisibility(View.GONE);

            PublishType type = data.getType();
            if (type == PublishType.status) {
                txtType.setText(R.string.draft_type_status);
                if (data.getTiming() > 0) {
                    txtTiming.setText(String.format(
                            getString(R.string.draft_timing_hint),
                            DateUtils.formatDate(data.getTiming(), getString(R.string.draft_date_format))));
                    txtTiming.setVisibility(View.VISIBLE);
                }
            } else if (type == PublishType.commentCreate)
                txtType.setText(R.string.draft_type_create_cmt);
            else if (type == PublishType.commentReply)
                txtType.setText(R.string.draft_type_reply_cmt);
            else if (type == PublishType.statusRepost)
                txtType.setText(R.string.draft_type_repost_status);

            txtContent.setContent(data.getText());

            if (data.getTiming() > 0 && data.getTiming() < System.currentTimeMillis()) {
                txtError.setVisibility(View.VISIBLE);

                txtError.setText(R.string.draft_timing_expired);
            } else {
                if (!TextUtils.isEmpty(data.getErrorMsg()))
                    txtError.setText(data.getErrorMsg());
                txtError.setVisibility(TextUtils.isEmpty(data.getErrorMsg()) ? View.GONE : View.VISIBLE);
            }

            BizFragment.createBizFragment(DraftFragment.this).bindOnTouchListener(txtContent);

            btnDel.setTag(data);
            btnDel.setOnClickListener(DraftFragment.this);
            btnResend.setTag(data);
            btnResend.setOnClickListener(DraftFragment.this);
        }

    }

    @Override
    public void onClick(View v) {
        final PublishBean bean = (PublishBean) v.getTag();
        // 删除
        if (v.getId() == R.id.btnDel) {
            deleteDraft(bean);
        }
        // 重新发送
        else if (v.getId() == R.id.btnResend) {
            BizFragment.createBizFragment(this).checkProfile(new BizFragment.CheckProfileCallback() {

                @Override
                public void onCheckProfileSuccess() {
                    PublishService.publish(getActivity(), bean);
                }

                @Override
                public void onCheckProfileFaild() {
                    showMessage(R.string.publish_request_ad_auth_faild);
                }

            });
        }
    }

    private void deleteDraft(final PublishBean bean) {
        new AlertDialogWrapper.Builder(getActivity())
                .setMessage(R.string.draft_del_confirm)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new WorkTask<Void, Void, Void>() {

                            @Override
                            protected void onPrepare() {
                                ViewUtils.createProgressDialog(getActivity(), getString(R.string.draft_del_draft_loading), ThemeUtils.getThemeColor()).show();
                            }

                            @Override
                            protected void onSuccess(Void result) {
                                getAdapterItems().remove(bean);
                                getAdapter().notifyDataSetChanged();

                                showMessage(R.string.draft_del_draft_hint);

                                Intent intent = new Intent();
                                intent.setAction(PublishManager.ACTION_PUBLISH_CHANNGED);
                                GlobalContext.getInstance().sendBroadcast(intent);
                            }

                            @Override
                            protected void onFinished() {
                                ViewUtils.dismissProgressDialog();

                                new DraftTask(RefreshMode.reset).execute();
                            }

                            ;

                            @Override
                            public Void workInBackground(Void... params) throws TaskException {
                                try {
                                    Thread.sleep(500);
                                } catch (Exception e) {
                                }

                                if (bean.getTiming() > 0)
                                    MyApplication.removePublishAlarm(bean);

                                PublishDB.deletePublish(bean, AppContext.getAccount().getUser());

                                return null;
                            }

                        }.execute();
                    }

                })
                .show();
    }

}
