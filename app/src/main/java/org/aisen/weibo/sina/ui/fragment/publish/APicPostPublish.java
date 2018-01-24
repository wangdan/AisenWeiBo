package org.aisen.weibo.sina.ui.fragment.publish;

import android.os.Bundle;
import android.text.TextUtils;

import org.aisen.android.common.utils.KeyGenerator;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.ViewUtils;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.sinasdk.SinaSDK;
import org.aisen.weibo.sina.sinasdk.bean.UploadPictureBean;
import org.aisen.weibo.sina.sinasdk.bean.UploadPictureResultBean;
import org.aisen.weibo.sina.support.sqlit.SinaDB;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.support.utils.ThemeUtils;
import org.aisen.weibo.sina.ui.fragment.base.BizFragment;

import java.io.File;

/**
 * 先把图片上传，再插入内容
 *
 * Created by wangdan on 16/8/16.
 */
public abstract class APicPostPublish extends APublishFragment {

    static final String TAG = "APicPostPublish";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        BizFragment.createBizFragment(getActivity());
    }

    @Override
    protected void onPicChanged(final String[] pics) {
        BizFragment.createBizFragment(this).checkProfile(new BizFragment.CheckProfileCallback() {

            @Override
            public void onCheckProfileSuccess() {
                picUploadRemind(pics);
            }

            @Override
            public void onCheckProfileFaild() {
                showMessage("高级授权失败");
            }

        });
    }

    private void picUploadRemind(String[] pics) {
        if (pics == null || pics.length != 1)
            return;

        getPublishBean().setPics(null);

        new UploadPicTask().execute(pics);
    }

    class UploadPicTask extends WorkTask<String, String, Boolean> {

        @Override
        protected void onPrepare() {
            super.onPrepare();

            ViewUtils.createProgressDialog(getActivity(), "正在上传图片...", ThemeUtils.getThemeColor()).show();
        }

        @Override
        public Boolean workInBackground(String... params) throws TaskException {
            String thumbnailUrl = null;

            String filePath = params[0];
            filePath = filePath.replace("file://", "");

            String key = KeyGenerator.generateMD5(filePath) + "_short";

            Logger.d(TAG, "开始上传图片， key = %s, path = %s", key, filePath);

            UploadPictureBean uploadPictureBean = SinaDB.getDB().selectById(null, UploadPictureBean.class, key);
            if (uploadPictureBean == null) {
                uploadPictureBean = new UploadPictureBean();
                uploadPictureBean.setKey(key);
                uploadPictureBean.setPath(getParams()[0]);
            }

//            if (uploadPictureBean != null && !TextUtils.isEmpty(uploadPictureBean.getShortUrl())) {
//                publishProgress(uploadPictureBean.getShortUrl());
//
//                Logger.d(TAG, "短链接已存在，直接返回 short = %s", uploadPictureBean.getShortUrl());
//
//                return true;
//            }
            if (!TextUtils.isEmpty(uploadPictureBean.getThumbnail_pic())) {
                thumbnailUrl = uploadPictureBean.getThumbnail_pic();

                Logger.d(TAG, "图片链接已存在，thumb url = " + thumbnailUrl);
            }
            else {
                File file = new File(filePath);
                file = AisenUtils.getUploadFile(getActivity(), file);
                Logger.w(TAG, "上传图片大小" + (file.length() / 1024) + "KB");
                UploadPictureResultBean resultBean = SinaSDK.getInstance(AppContext.getAccount().getAdvancedToken()).uploadPicture(file);

                thumbnailUrl = resultBean.getThumbnail_pic();

                Logger.d(TAG, "上传图片成功，thumb url = %s", thumbnailUrl);

                uploadPictureBean.setThumbnail_pic(thumbnailUrl);

                SinaDB.getDB().insertOrReplace(null, uploadPictureBean);
            }

            if (!TextUtils.isEmpty(thumbnailUrl)) {
//                UrlsBean result = SinaSDK.getInstance(AppContext.getAccount().getAccessToken()).urlLong2Short(thumbnailUrl);
//                if (result != null && result.getUrls() != null && result.getUrls().size() > 0) {
//                    uploadPictureBean.setShortUrl(result.getUrls().get(0).getUrl_short());
//
//                    SinaDB.getDB().insertOrReplace(null, uploadPictureBean);
//
//                    publishProgress(uploadPictureBean.getShortUrl());
//
//                    Logger.d(TAG, "获取短链接 short url = %s", uploadPictureBean.getShortUrl());
//
//                    return true;
//                }

                thumbnailUrl = thumbnailUrl.replace("bmiddle", "large").replace("thumbnail", "large");
                publishProgress(thumbnailUrl);

                return true;
            }

            return false;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            if (values != null && values.length > 0) {
                String text = editContent.getText().toString();
                if (!TextUtils.isEmpty(text)) {
                    text = text + " " + values[0] + " ";
                }
                else {
                    text = values[0] + " ";
                }

                editContent.setText(text);
                editContent.setSelection(text.length());
            }
        }

        @Override
        protected void onSuccess(Boolean aBoolean) {
            super.onSuccess(aBoolean);

            if (!aBoolean) {
                showMessage("图片上传失败，请再试一次");
            }
        }

        @Override
        protected void onFinished() {
            super.onFinished();

            ViewUtils.dismissProgressDialog();
        }

        @Override
        protected void onFailure(TaskException exception) {
            super.onFailure(exception);

            showMessage(exception.getMessage());
        }

    }

    @Override
    int picPickerSize() {
        return 1;
    }
}
