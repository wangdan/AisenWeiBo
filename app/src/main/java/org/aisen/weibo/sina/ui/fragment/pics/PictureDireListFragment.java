package org.aisen.weibo.sina.ui.fragment.pics;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.component.bitmaploader.core.ImageConfig;
import org.aisen.android.component.bitmaploader.download.SdcardDownloader;
import org.aisen.android.support.adapter.ABaseAdapter;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.AListFragment;

import org.aisen.weibo.sina.R;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by wangdan on 15-1-29.
 */
public class PictureDireListFragment extends AListFragment<PictureDireListFragment.PictureFileDire, ArrayList<PictureDireListFragment.PictureFileDire>>
                                        implements AdapterView.OnItemClickListener {

    public static PictureDireListFragment newInstance(String currentDire, ArrayList<PictureFileDire> files) {
        Bundle args = new Bundle();
        args.putSerializable("files", files);
        args.putSerializable("current", currentDire);

        PictureDireListFragment fragment = new PictureDireListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private ArrayList<PictureFileDire> files;
    private String currentDire;
    private OnPictureDireSelectedCallback callback;

    @Override
    protected int inflateContentView() {
        return R.layout.as_ui_picture_pick_dire;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        files = savedInstanceState == null ? (ArrayList<PictureFileDire>) getArguments().getSerializable("files")
                                           : (ArrayList<PictureFileDire>) savedInstanceState.getSerializable("files");
        currentDire = savedInstanceState == null ? getArguments().getString("current")
                                                 : savedInstanceState.getString("current");
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (callback != null) {
            PictureFileDire dire = getAdapterItems().get(position);

            callback.onPictureDireSelected(dire);

            currentDire = dire.getName();
            getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("files", files);
        outState.putSerializable("current", currentDire);
    }

    @Override
    protected ABaseAdapter.AbstractItemView<PictureFileDire> newItemView() {
        return new PictureDireItemView();
    }

    @Override
    protected void requestData(RefreshMode mode) {
        setItems(files);
    }

    class PictureDireItemView extends ABaseAdapter.AbstractItemView<PictureFileDire> {

        @ViewInject(id = R.id.img)
        ImageView img;
        @ViewInject(id = R.id.txtName)
        TextView txtName;
        @ViewInject(id = R.id.txtDesc)
        TextView txtDesc;
        @ViewInject(id = R.id.imgSelected)
        View imgSelected;

        @Override
        public int inflateViewId() {
            return R.layout.as_item_picture_pick_dire;
        }

        @Override
        public void bindingData(View convertView, PictureFileDire data) {
            ImageConfig config = new ImageConfig();
            config.setLoadfaildRes(R.drawable.bg_timeline_loading);
            config.setLoadingRes(R.drawable.bg_timeline_loading);
            config.setMaxWidth(SystemUtils.getScreenWidth() / 3);
            config.setMaxHeight(SystemUtils.getScreenWidth() / 3);
            config.setCacheEnable(false);
            config.setDownloaderClass(SdcardDownloader.class);
            config.setId("thumb_dir");
            BitmapLoader.getInstance().display(PictureDireListFragment.this, data.getFiles().get(0), img, config);

            txtName.setText(data.getName());
            txtDesc.setText(data.getFiles().size() == 0 ? "" : String.format("%d张", data.getFiles().size()));

            imgSelected.setVisibility(currentDire.equals(data.getName()) ? View.VISIBLE : View.GONE);
        }

    }

    public static class PictureFileDire implements Serializable {

        private static final long serialVersionUID = 1746378490588970508L;

        private String name;

        private ArrayList<String> files;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public ArrayList<String> getFiles() {
            return files;
        }

        public void setFiles(ArrayList<String> files) {
            this.files = files;
        }
    }

    public void setCallback(OnPictureDireSelectedCallback callback) {
        this.callback = callback;
    }

    public interface OnPictureDireSelectedCallback {

        public void onPictureDireSelected(PictureFileDire dire);

    }

}
