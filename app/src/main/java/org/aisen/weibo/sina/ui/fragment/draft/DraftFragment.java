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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.DateUtils;
import org.aisen.android.common.utils.ViewUtils;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.adapter.ABaseAdapter.AbstractItemView;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.android.ui.fragment.AListFragment;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.base.MyApplication;
import org.aisen.weibo.sina.support.bean.PublishBean;
import org.aisen.weibo.sina.support.bean.PublishType;
import org.aisen.weibo.sina.support.db.PublishDB;
import org.aisen.weibo.sina.support.publisher.PublishManager;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.support.utils.ThemeUtils;
import org.aisen.weibo.sina.sys.service.PublishService;
import org.aisen.weibo.sina.ui.activity.publish.PublishActivity;
import org.aisen.weibo.sina.ui.fragment.basic.BizFragment;
import org.aisen.weibo.sina.ui.widget.AisenTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * 草稿箱
 *
 * @author wangdan
 */
public class DraftFragment extends AListFragment<PublishBean, ArrayList<PublishBean>>
        implements OnItemClickListener, View.OnClickListener {

    public static ABaseFragment newInstance() {
        return new DraftFragment();
    }

    private BizFragment bizFragment;

    @Override
    protected int inflateContentView() {
        return R.layout.as_ui_draft;
    }

    @Override
    protected void setInitRefreshView(AbsListView refreshView, Bundle savedInstanceSate) {
        super.setInitRefreshView(refreshView, savedInstanceSate);

        setPadding(refreshView);
        setPadding(findViewById(R.id.layoutEmpty));
        setPadding(findViewById(R.id.layoutLoadFailed));
        setPadding(findViewById(R.id.layoutLoading));
    }

    private void setPadding(View view) {
        if (view == null)
            return;

        int toolbarHeight = getResources().getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material);

        view.setPadding(view.getPaddingLeft(),
                            toolbarHeight,
                            view.getPaddingRight(),
                            view.getPaddingBottom());
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        getRefreshView().setOnItemClickListener(this);

        try {
            bizFragment = BizFragment.getBizFragment(this);
        } catch (Exception e) {
        }

//        AisenTextView.stringMemoryCache.evictAll();
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
    protected AbstractItemView<PublishBean> newItemView() {
        return new DraftboxItemView();
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(PublishManager.ACTION_PUBLISH_CHANNGED);
        getActivity().registerReceiver(receiver, filter);

        new DraftTask(RefreshMode.reset).execute();
    }

    @Override
    public void onPause() {
        super.onPause();

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
    protected void requestData(RefreshMode mode) {
    }

    class DraftTask extends PagingTask<Void, Void, ArrayList<PublishBean>> {

        public DraftTask(RefreshMode mode) {
            super("DraftTask", mode);
        }

        @Override
        protected List<PublishBean> parseResult(ArrayList<PublishBean> result) {
            return result;
        }

        @Override
        protected ArrayList<PublishBean> workInBackground(RefreshMode mode, String previousPage,
                                                          String nextPage, Void... params) throws TaskException {
            return PublishDB.getPublishList(AppContext.getUser());
        }

    }

    class DraftboxItemView extends AbstractItemView<PublishBean> {

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

        @Override
        public int inflateViewId() {
            return R.layout.as_item_draft;
        }

        @Override
        public void bindingData(View convertView, PublishBean data) {
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

            if (bizFragment != null)
                bizFragment.bindOnTouchListener(txtContent);

            btnDel.setTag(data);
            btnDel.setOnClickListener(DraftFragment.this);
            btnResend.setTag(data);
            btnResend.setOnClickListener(DraftFragment.this);
        }

    }

    @Override
    public void onClick(View v) {
        PublishBean bean = (PublishBean) v.getTag();
        // 删除
        if (v.getId() == R.id.btnDel) {
            deleteDraft(bean);
        }
        // 重新发送
        else if (v.getId() == R.id.btnResend) {
            PublishService.publish(getActivity(), bean);
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
                                notifyDataSetChanged();

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

                                PublishDB.deletePublish(bean, AppContext.getUser());

                                return null;
                            }

                        }.execute();
                    }

                })
                .show();
    }

}
