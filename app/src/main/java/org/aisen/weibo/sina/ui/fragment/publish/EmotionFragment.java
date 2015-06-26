package org.aisen.weibo.sina.ui.fragment.publish;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;

import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.adapter.ABaseAdapter.AbstractItemView;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.AGridFragment;
import org.aisen.android.ui.fragment.ARefreshFragment;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.bean.Emotion;
import org.aisen.weibo.sina.support.bean.Emotions;
import org.aisen.weibo.sina.support.db.EmotionsDB;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 表情窗口
 * 
 * @author wangdan
 *
 */
public class EmotionFragment extends AGridFragment<Emotion, Emotions>
								implements OnItemClickListener, OnItemLongClickListener {

	public static EmotionFragment newInstance() {
		return new EmotionFragment();
	}
	
	private OnEmotionSelectedListener onEmotionSelectedListener;
	
	@Override
	protected int inflateContentView() {
		return R.layout.as_lay_emotions;
	}

	@Override
	protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
		super.layoutInit(inflater, savedInstanceSate);
		
		getRefreshView().setOnItemClickListener(this);
		getRefreshView().setOnItemLongClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (onEmotionSelectedListener != null)
			onEmotionSelectedListener.onEmotionSelected(getAdapterItems().get(position));
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		showMessage(getAdapterItems().get(position).getKey());
		return true;
	}

	@Override
	protected AbstractItemView<Emotion> newItemView() {
		return new EmotionItemView();
	}

	@Override
	protected void requestData(ARefreshFragment.RefreshMode mode) {
		// 浪小花-lxh_
		new EmotionTask(mode).execute("d_");
	}
	
	class EmotionItemView extends AbstractItemView<Emotion> {

		@ViewInject(id = R.id.imgEmotion)
		ImageView imgEmotion;
		
		@Override
		public int inflateViewId() {
			return R.layout.as_item_emotion;
		}

		@Override
		public void bindingData(View convertView, Emotion data) {
			imgEmotion.setImageBitmap(BitmapFactory.decodeByteArray(data.getData(), 0, data.getData().length));
		}
		
	}
	
	class EmotionTask extends PagingTask<String, Void, Emotions> {

		public EmotionTask(ARefreshFragment.RefreshMode mode) {
			super(UUID.randomUUID().toString(), mode);
		}

		@Override
		protected List<Emotion> parseResult(Emotions result) {
			return result.getEmotions();
		}

		@Override
		protected Emotions workInBackground(RefreshMode mode, String previousPage, String nextPage, String... params) throws TaskException {
//			return EmotionsDB.getEmotions(params[0]);
			Emotions es = new Emotions();
			es.setEmotions(new ArrayList<Emotion>());
			
			Emotions emotions = EmotionsDB.getEmotions("d_");
			es.getEmotions().addAll(emotions.getEmotions());
			emotions = EmotionsDB.getEmotions("hs_");
			es.getEmotions().addAll(emotions.getEmotions());
			emotions = EmotionsDB.getEmotions("h_");
			es.getEmotions().addAll(emotions.getEmotions());
			emotions = EmotionsDB.getEmotions("f_");
			es.getEmotions().addAll(emotions.getEmotions());
			emotions = EmotionsDB.getEmotions("o_");
			es.getEmotions().addAll(emotions.getEmotions());
			emotions = EmotionsDB.getEmotions("w_");
			es.getEmotions().addAll(emotions.getEmotions());
			emotions = EmotionsDB.getEmotions("l_");
			es.getEmotions().addAll(emotions.getEmotions());
//			emotions = EmotionsDB.getEmotions("lxh_");
//			es.getEmotions().addAll(emotions.getEmotions());
			
			return es;
		}

	}
	
	public void setOnEmotionListener(OnEmotionSelectedListener onEmotionSelectedListener) {
		this.onEmotionSelectedListener = onEmotionSelectedListener;
	}

	public interface OnEmotionSelectedListener {
		
		public void onEmotionSelected(Emotion emotion);
		
	}
	
}
