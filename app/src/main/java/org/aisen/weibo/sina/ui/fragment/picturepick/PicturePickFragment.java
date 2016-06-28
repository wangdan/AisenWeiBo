package org.aisen.weibo.sina.ui.fragment.picturepick;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.component.bitmaploader.core.ImageConfig;
import org.aisen.android.component.bitmaploader.download.SdcardDownloader;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.activity.container.FragmentArgs;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.android.ui.fragment.AGridFragment;
import org.aisen.android.ui.fragment.adapter.ARecycleViewItemView;
import org.aisen.android.ui.fragment.itemview.IITemView;
import org.aisen.android.ui.fragment.itemview.IItemViewCreator;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.ui.activity.base.SinaCommonActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图片选择器
 *
 * Created by wangdan on 15-1-29.
 */
public class PicturePickFragment extends AGridFragment<String, ArrayList<String>>
                                    implements AdapterView.OnItemClickListener, PictureDireListFragment.OnPictureDireSelectedCallback {

    public static void launch(ABaseFragment from, int maxSize, String[] pics, int requestCode) {
        FragmentArgs args = new FragmentArgs();
        args.add("maxSize", maxSize + "");
        args.add("pics", pics);

        SinaCommonActivity.launchForResult(from, PicturePickFragment.class, args, requestCode);
    }

//    @ViewInject(id = R.id.btnCounter, click = "savePictures")
//    View btnCounter;
//    @ViewInject(id = R.id.txtCounter)
//    TextView txtCounter;
    @ViewInject(id = R.id.layCurrent, click = "switchDireListFragment")
    View layCurrent;
    @ViewInject(id = R.id.layFileDires)
    View layFileDires;
    @ViewInject(id = R.id.layFileDireBg, click = "switchDireListFragment")
    View layFileDireBg;
    @ViewInject(id = R.id.txtCurrent)
    TextView txtCurrent;

    private int maxSize = 9;// 最大选择多少张图片

    private ArrayList<String> selectedFile = new ArrayList<String>();// 已经选中的图片

    ImageConfig imageConfig = new ImageConfig();
    int gap = GlobalContext.getInstance().getResources().getDimensionPixelSize(R.dimen.gap_photo);

    private PictureDireListFragment diresFragment;

    private String selectedDirName;

    @Override
    public int inflateContentView() {
        return R.layout.ui_picture_pick;
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        final BaseActivity baseActivity = (BaseActivity) getActivity();

        baseActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        baseActivity.getSupportActionBar().setDisplayShowHomeEnabled(false);
        baseActivity.getSupportActionBar().setTitle(R.string.title_mobile_photos);

        setHasOptionsMenu(true);

        maxSize = Integer.parseInt(getArguments().getString("maxSize"));

        imageConfig.setId("thumb");
        imageConfig.setCacheEnable(false);
        imageConfig.setLoadingRes(R.drawable.bg_timeline_loading);
        imageConfig.setLoadfaildRes(R.drawable.bg_timeline_loading);
        imageConfig.setDownloaderClass(SdcardDownloader.class);
        imageConfig.setMaxWidth(SystemUtils.getScreenWidth(getActivity()) / 4);
        imageConfig.setMaxHeight(SystemUtils.getScreenWidth(getActivity()) / 4);

        diresFragment = (PictureDireListFragment) getActivity().getFragmentManager().findFragmentByTag("diresFragment");
        if (diresFragment != null)
            diresFragment.setCallback(this);

        setHasOptionsMenu(true);

        selectedDirName = savedInstanceSate == null ? "所有图片" : savedInstanceSate.getString("selectedDirName");
        txtCurrent.setText(selectedDirName);
        getActivity().invalidateOptionsMenu();
//        btnCounter.setVisibility(selectedFile.size() == 0 ? View.GONE : View.VISIBLE);
//        txtCounter.setText(String.format("预览(%d/%d)", selectedFile.size(), maxSize));

        if (getArguments() != null) {
            String[] pics = getArguments().getStringArray("pics");
            if (pics != null) {
                for (String pic : pics)
                    selectedFile.add(pic);
            }
        }
        else if (savedInstanceSate != null) {
            selectedFile = (ArrayList) savedInstanceSate.getSerializable("selectedFile");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("selectedDirName", selectedDirName);
        outState.putSerializable("selectedFile", selectedFile);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String path = getAdapterItems().get(position);

        // 多选
        if (maxSize > 1) {
            onPictureSelectedChange(path);
            getActivity().invalidateOptionsMenu();
        }
        // 单选
        else {
            selectedFile.add(path);
            savePics();
            // 设置头像
//            PhotoSettingFragment.launch(this, path, 3000);
        }
    }

    @Override
    public void onPictureDireSelected(PictureDireListFragment.PictureFileDire dire) {
        selectedDirName = dire.getName();
        txtCurrent.setText(selectedDirName);
        setItems(dire.getFiles());
        getRefreshView().smoothScrollToPosition(0);

        switchDireListFragment(layFileDires);
    }

    boolean expandDir = false;
    void switchDireListFragment(View v) {
        if (layFileDires.getVisibility() == View.GONE) {
            layFileDires.setVisibility(View.VISIBLE);
        }

        AnimatorSet animSet = new AnimatorSet();
        animSet.setDuration(300);

        PropertyValuesHolder alphaPvh = null;
        PropertyValuesHolder yPvh = null;
        // 切换至隐藏
        if (layFileDireBg.getAlpha() > 0.0f) {
            expandDir = false;
            alphaPvh = PropertyValuesHolder.ofFloat(View.ALPHA, 0.6f, 0.0f);
            yPvh = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0, getRefreshView().getHeight());
        }
        else {
            expandDir = true;
            alphaPvh = PropertyValuesHolder.ofFloat(View.ALPHA, 0.0f, 0.6f);
            yPvh = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, getRefreshView().getHeight(), 0);
        }
        // 背景视图的动画
        ObjectAnimator alphaAnim = ObjectAnimator.ofPropertyValuesHolder(layFileDireBg, alphaPvh);
        animSet.playTogether(alphaAnim);
        // 文件夹视图的动画
        ObjectAnimator yAnim = ObjectAnimator.ofPropertyValuesHolder(layFileDires, yPvh);
        animSet.playTogether(yAnim);
        animSet.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                if (expandDir)
                    layFileDireBg.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                diresFragment.refreshUI();

                if (!expandDir) {
                    layFileDireBg.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

        });

        animSet.start();
    }

    private void onPictureSelectedChange(String path) {
        if (!selectedFile.contains(path)) {
            if (selectedFile.size() >= maxSize) {
                showMessage(String.format("最多只能选%d张相片", maxSize));
                return;
            }

            selectedFile.add(path);
        }
        else
            selectedFile.remove(path);

        getAdapter().notifyDataSetChanged();
        getActivity().invalidateOptionsMenu();
//        btnCounter.setVisibility(selectedFile.size() == 0 ? View.GONE : View.VISIBLE);
//        txtCounter.setText(String.format("预览(%d/%d)", selectedFile.size(), maxSize));
    }

    @Override
    public IItemViewCreator<String> configItemViewCreator() {
        return new IItemViewCreator<String>() {

            @Override
            public View newContentView(LayoutInflater inflater, ViewGroup parent, int viewType) {
                return inflater.inflate(R.layout.item_picture_pick, parent, false);
            }

            @Override
            public IITemView<String> newItemView(View convertView, int viewType) {
                return new PicturePickItenView(convertView);
            }

        };
    }

    @Override
    public void requestData(RefreshMode mode) {
        new PicturePickTask().execute();
    }

    void savePictures(View v) {
        String[] pathArr = new String[selectedFile.size()];
        for (int i = 0; i < pathArr.length; i++)
            pathArr[i] = selectedFile.get(i);

//        PicsPickPreviousActivity.launch(this, pathArr, 2000);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == 2000) {
            String[] pics = data.getStringArrayExtra("images");

            selectedFile.clear();
            for (String pic : pics) {
                selectedFile.add(pic);
            }
//            btnCounter.setVisibility(selectedFile.size() == 0 ? View.GONE : View.VISIBLE);
//            txtCounter.setText(String.format("预览(%d/%d)", selectedFile.size(), maxSize));
            getAdapter().notifyDataSetChanged();
            getActivity().invalidateOptionsMenu();

            // 如果在预览已经确定了，就直接返回数据
            if (data.getBooleanExtra("confirm", false)) {
                savePics();
            }
        }
        // 设置头像成功
        else if (resultCode == Activity.RESULT_OK && requestCode == 3000) {
            getActivity().finish();
        }
    }

    class PicturePickItenView extends ARecycleViewItemView<String> {

        @ViewInject(id = R.id.img)
        ImageView img;
        @ViewInject(id = R.id.viewCover)
        View cover;
        @ViewInject(id = R.id.btnCheckbox)
        View btnCheckbox;

        public PicturePickItenView(View itemView) {
            super(getActivity(), itemView);
        }

        @Override
        public void onBindData(View convertView, String data, int position) {
            BitmapLoader.getInstance().display(PicturePickFragment.this, data, img, imageConfig);
            // 多选
            if (maxSize > 1) {
                if (selectedFile.contains(data)) {
                    cover.setVisibility(View.VISIBLE);
                    btnCheckbox.setSelected(true);
                }
                else {
                    cover.setVisibility(View.GONE);
                    btnCheckbox.setSelected(false);
                }
                btnCheckbox.setTag(data);
            }
            // 单选
            else {
                cover.setVisibility(View.GONE);
                btnCheckbox.setVisibility(View.GONE);
            }

            int width = (SystemUtils.getScreenWidth(getActivity()) - gap * 2) / 3;
            convertView.setLayoutParams(new AbsListView.LayoutParams(width, width));
        }

    }

    class PicturePickTask extends APagingTask<Void, Void, ArrayList<String>> {

        PicturePickTask() {
            super(RefreshMode.reset);
        }

        @Override
        protected List<String> parseResult(ArrayList<String> strings) {
            return strings;
        }

        @Override
        protected ArrayList<String> workInBackground(RefreshMode mode, String previousPage, String nextPage, Void... params) throws TaskException {
            ArrayList picFileList = new ArrayList<String>();

            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            Uri uri = intent.getData();
            String[] proj = { MediaStore.Images.Media.DATA };
            Cursor cursor = GlobalContext.getInstance().getContentResolver().query(uri, proj, null, null, null);
            while (cursor.moveToNext()) {
                String path = cursor.getString(0);
                if (TextUtils.isEmpty(path)) {
                    continue;
                }
                Logger.d(path);
                picFileList.add(new File(path).getAbsolutePath());
            }
            cursor.close();

            Collections.reverse(picFileList);

            return picFileList;
        }

        @Override
        protected void onSuccess(ArrayList<String> strings) {
            super.onSuccess(strings);

            if (getActivity() == null)
                return;

            ArrayList<PictureDireListFragment.PictureFileDire> files = new ArrayList<PictureDireListFragment.PictureFileDire>();

            PictureDireListFragment.PictureFileDire all = new PictureDireListFragment.PictureFileDire();
            all.setName("所有图片");
            all.setFiles(new ArrayList<String>());
            files.add(all);
            txtCurrent.setText(all.getName());

            Map<String, PictureDireListFragment.PictureFileDire> map = new HashMap<String, PictureDireListFragment.PictureFileDire>();

            for (String path : strings) {
                all.getFiles().add(path);

                File file = new File(path);
                PictureDireListFragment.PictureFileDire dire = map.get(file.getParentFile().getName());
                if (dire == null) {
                    dire = new PictureDireListFragment.PictureFileDire();
                    dire.setName(file.getParentFile().getName());
                    dire.setFiles(new ArrayList<String>());
                    files.add(dire);
                    map.put(file.getParentFile().getName(), dire);
                }
                dire.getFiles().add(path);
            }

            diresFragment = PictureDireListFragment.newInstance(all.getName(), files);
            diresFragment.setCallback(PicturePickFragment.this);
            getActivity().getFragmentManager().beginTransaction().add(R.id.layFileDires, diresFragment, "diresFragment").commit();
        }
    }

    private void savePics() {
        Intent data = new Intent();
        data.putExtra("pics", selectedFile.toArray(new String[0]));
        getActivity().setResult(Activity.RESULT_OK, data);
        getActivity().finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        BitmapLoader.getInstance().clearCache();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_picture_pick, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem item = menu.findItem(R.id.menu_confirm);
        item.setVisible(selectedFile.size() > 0);
        item.setTitle(String.format("完成(%d/%d)", selectedFile.size(), maxSize));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_confirm) {
            savePics();
        }

        return super.onOptionsItemSelected(item);
    }
}
