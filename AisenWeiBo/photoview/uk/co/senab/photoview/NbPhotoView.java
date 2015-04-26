package uk.co.senab.photoview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.widget.ImageView;

import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import uk.co.senab.photoview.PhotoViewAttacher.OnMatrixChangedListener;
import uk.co.senab.photoview.log.LogManager;

public class NbPhotoView extends PhotoView implements OnMatrixChangedListener {

	static final String TAG = NbPhotoView.class.getSimpleName();

	public NbPhotoView(Context context, AttributeSet attr, int defStyle) {
		super(context, attr, defStyle);
	}

	public NbPhotoView(Context context, AttributeSet attr) {
		super(context, attr);
	}

	public NbPhotoView(Context context) {
		super(context);
	}

	@Override
	public void setImageDrawable(Drawable drawable) {
		LogManager.getLogger().w(TAG, "The NbPhotoView does not support this method");
	}

	@Override
	public void setImageResource(int resId) {
		LogManager.getLogger().w(TAG, "The NbPhotoView does not support this method");
	}

	@Override
	public void setImageURI(Uri uri) {
		LogManager.getLogger().w(TAG, "The NbPhotoView does not support this method");
	}

	private int getImageViewWidth(ImageView imageView) {
		if (null == imageView)
			return 0;
		return imageView.getWidth() - imageView.getPaddingLeft() - imageView.getPaddingRight();
	}

	private int getImageViewHeight(ImageView imageView) {
		if (null == imageView)
			return 0;
		return imageView.getHeight() - imageView.getPaddingTop() - imageView.getPaddingBottom();
	}

	public static Bitmap zoomBitmap(Bitmap source, float scale) {
		Matrix matrix = new Matrix();
		matrix.setScale(scale, scale);
		Bitmap result = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, false);
//		source.recycle();
		return result;
	}

	@Override
	public void setOnMatrixChangeListener(OnMatrixChangedListener listener) {
		this.onMatrixChangedListener = listener;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		checkBitmapArr();

		if (isPieceDecorde) {
			removeCallbacks(drawPieceRunnable);
			postDelayed(drawPieceRunnable, 500);

//			List<PieceBitmap> pieceBitmaps = pieceBitmapMap.get((int) (getScale() * 10));
//			if (pieceBitmaps != null) {
//				for (PieceBitmap bitmap : pieceBitmaps)
//					canvas.drawBitmap(bitmap.bitmap, bitmap.getX(), bitmap.getY(), null);
//			}
		}

		// && (checkScale(getScale()) || Math.abs(lastTop -
		// getDisplayRect().top) >= 10)
		if (isPieceDecorde) {
			if (getScale() <= getMinimumScale())
				return;

//			LogManager.getLogger().i(TAG, "rawbitmap");

			// 获取位图数据
			SparseArray<SparseArray<UnitBitmap>> bitmaps = bitmapHashtable.get(scaleStr);
//			LogManager.getLogger().e("bitmaps", bitmaps + "");
			if (bitmaps == null)
				return;

			// 图片的显示区域
			RectF displayRect = getDisplayRect();
			if (displayRect == null)
				return;

			// ImageView大小
			int imgW = getImageViewWidth(this);
			int imgH = getImageViewHeight(this);

			// 分块大小
			float pieceW = origWidth * 1.0f / col / origToDisplayScale * getScale();
			float pieceH = origHeight * 1.0f / row / origToDisplayScale * getScale();

			// 相对ImageView原点坐标
			float bitmapX = displayRect.left >= 0 ? 0 : (0 - displayRect.left);
			float bitmapY = displayRect.top >= 0 ? 0 : (0 - displayRect.top);

//			LogManager.getLogger().w(TAG, String.format("bitmapX=%f, bitmapY=%f", bitmapX, bitmapY));

			for (int r = 0; r < row; r++) {
				// 根据Y坐标判断当前行是否在显示区域，否则剔除位图数据
				rowRectF.left = displayRect.left;
				rowRectF.right = displayRect.right;
				rowRectF.top = r * pieceH;
				rowRectF.bottom = (r + 1) * pieceH;

				// 剔除行
				if (rowRectF.top > bitmapY + imgH || rowRectF.bottom < bitmapY) {
					for (int i = 0; i < col; i++) {
						Hashtable<String, Boolean> runningMap = bitmapRunningMap.get(scaleStr);
						if (runningMap != null && runningMap.containsKey(getKey(r, i))) {
							runningMap.remove(getKey(r, i));

							LogManager.getLogger().i(TAG, String.format("remove running row=%d,col=%d", r, i));
						}
					}

					if (bitmaps.get(r) != null)
						LogManager.getLogger().i(TAG, String.format("remove row---%d", r));

					bitmaps.remove(r);
				}
				// 新增行
				else {
					SparseArray<UnitBitmap> rowBitmaps = bitmaps.get(r);

					// 判断显示行的每一列数据是否在显示区域，且是否已存在图片，如果不存在，则push到加载队列
					for (int c = 0; c < col; c++) {
						// 根据X坐标判断当前显示列是否在显示区域，否则剔除位图数据
						colRectF.left = c * pieceW;
						colRectF.right = (c + 1) * pieceW;

						// 剔除列
						if (colRectF.left > bitmapX + imgW || colRectF.right < bitmapX) {
							Hashtable<String, Boolean> runningMap = bitmapRunningMap.get(scaleStr);
							if (runningMap != null)
								runningMap.put(getKey(r, c), false);

							if (rowBitmaps != null)
								rowBitmaps.remove(c);
							LogManager.getLogger().i(TAG, String.format("remove col---%d", c));
						}
						// 绘制或者push队列
						else {
							UnitBitmap pieceBitmap = null;
							if (rowBitmaps != null)
								pieceBitmap = rowBitmaps.get(c);

							if (pieceBitmap != null) {
								float pieceX = pieceBitmap.col * pieceW;
								float pieceY = pieceBitmap.row * pieceH;

								float drawX;
								if (bitmapX == 0)
									drawX = pieceX + displayRect.left;
								else
									drawX = pieceX - bitmapX;
								float drawY;
								if (bitmapY == 0)
									drawY = pieceY + displayRect.top;
								else
									drawY = pieceY - bitmapY;

//								if (!pieceBitmap.bitmap.isRecycled())
								canvas.drawBitmap(pieceBitmap.bitmap, drawX, drawY, null);
							} else {
								pushQueue(scaleStr, r, c, pieceW, pieceH);
							}
						}
					}
				}
			}
		}
	}

	/****************************************************************************************************/
	private static final int maxMemory = 15 * 1024 * 1024;// 需要解析图片能容许的最大内存
	private static final int maxSize = 1500;// 最大显示尺寸
	private static final int piece = 3;// 图片分成3块加载

	private BlockingQueue<Runnable> runnableQueue = new LinkedBlockingQueue<Runnable>();// 线程队列
	private Runnable runningTask = null;

	private BitmapRegionDecoder bitmapRegionDecoder;

	private RectF rowRectF = new RectF();
	private RectF colRectF = new RectF();

	private Hashtable<String, SparseArray<SparseArray<UnitBitmap>>> bitmapHashtable = new Hashtable<String, SparseArray<SparseArray<UnitBitmap>>>();// 保存位图地址
	private Hashtable<String, Hashtable<String, Boolean>> bitmapRunningMap = new Hashtable<String, Hashtable<String, Boolean>>();
	private String scaleStr = "";// 缩放级别发生改变时，重绘图片
	private int row;// 分割行
	private int col;// 分割列

	private OnMatrixChangedListener onMatrixChangedListener;
	private int origWidth;// 图片原始宽度
	private int origHeight;// 图片原始高度
	private float origToDisplayScale;// 原始图转换成显示图的缩放比例

	// 设置位图分块地址
	private void checkBitmapArr() {
		if (!checkScale(getScale())) {

			runnableQueue.clear();
			if (runningTask != null)
				mHandler.removeCallbacks(runningTask);
			runningTask = null;
			bitmapRunningMap.clear();
			bitmapHashtable.clear();
		}
	}

	private boolean checkScale(float scale) {
		return scaleStr.equals(formatFloat(scale));
	}

	private void saveScale(float scale) {
		scaleStr = formatFloat(scale);
	}

	private String formatFloat(float f) {
		return new DecimalFormat("0.0").format(f);
	}

	private String getKey(int row, int col) {
		return row + "-" + col;
	}

	private void pushQueue(String scaleStr, int row, int col, float width, float height) {
		Hashtable<String, Boolean> runningMap = bitmapRunningMap.get(scaleStr);
		if (runningMap == null) {
			runningMap = new Hashtable<String, Boolean>();
			bitmapRunningMap.put(scaleStr, runningMap);
		}
		Boolean isRunning = runningMap.get(getKey(row, col));
		if (isRunning == null || !isRunning) {
			LogManager.getLogger().i(TAG, String.format("push runnable, row = %d, col = %d, scale = %s", row, col, scaleStr));
			runningMap.put(getKey(row, col), true);
			runnableQueue.add(new CarveRunnable(scaleStr, row, col, width, height));
		}
		runRunnable();
	}

	private void runRunnable() {
		if (runningTask == null) {
			runningTask = runnableQueue.poll();
			if (runningTask != null) {
				mHandler.postDelayed(runningTask, 50);
			}
		}
	}

	class CarveRunnable implements Runnable {

		String scaleStr;
		int row;
		int col;
		float width;
		float height;

		CarveRunnable(String scaleStr, int row, int col, float width, float height) {
			this.scaleStr = scaleStr;
			this.row = row;
			this.col = col;
			this.width = width;
			this.height = height;
		}

		@Override
		public void run() {
			SparseArray<SparseArray<UnitBitmap>> bitmaps = bitmapHashtable.get(scaleStr);
			if (bitmaps != null) {
				RectF displayRect = getDisplayRect();
				if (displayRect == null)
					return;

				float origW = width / getScale() * origToDisplayScale;
				float origH = height / getScale() * origToDisplayScale;

				float displayLeft = displayRect.left <= 0 ? 0 : displayRect.left;
				float displayTop = displayRect.top <= 0 ? 0 : displayRect.top;

				float displayX = displayLeft + width * col;
				float displayY = displayTop + height * row;

				float x = origW * col;
				float y = origH * row;
				Rect rect = new Rect();
				rect.left = (int) x;
				rect.top = (int) y;
				rect.right = (int) (x + origW);
				rect.bottom = (int) (y + origH);
				Bitmap origBitmap = bitmapRegionDecoder.decodeRegion(rect, null);
				Bitmap drawBitmap = zoomBitmap(origBitmap, 1 / origToDisplayScale * getScale());

				if (scaleStr.equals(NbPhotoView.this.scaleStr)) {
					runningTask = null;

					SparseArray<UnitBitmap> rowBitmaps = bitmaps.get(row);
					if (rowBitmaps == null) {
						rowBitmaps = new SparseArray<UnitBitmap>();
						bitmaps.put(row, rowBitmaps);
					}
					RectF rectF = new RectF(displayRect.left, displayRect.top, displayRect.right, displayRect.bottom);
					rowBitmaps.put(col, new UnitBitmap(displayX, displayY, row, col, rectF, drawBitmap));

					runRunnable();
					invalidate();
				}
			}
		}

	}

	private Handler mHandler = new Handler() {
	};

	/**
	 * 绘制的单元位图
	 * 
	 * @author wangdan
	 * 
	 */
	class UnitBitmap {

		UnitBitmap(float x, float y, int row, int col, RectF displayRect, Bitmap bitmap) {
			this.x = x;
			this.y = y;
			this.row = row;
			this.col = col;
			this.displayRect = displayRect;
			this.bitmap = bitmap;
		}

		Bitmap bitmap;
		int row;
		int col;

		RectF displayRect;
		float x;
		float y;

		float getX() {
			if (getDisplayRect() == null)
				return 0.0f;
			return x + (getDisplayRect().left - displayRect.left);
		}

		float getY() {
			if (getDisplayRect() == null)
				return 0.0f;
			return y + (getDisplayRect().top - displayRect.top);
		}

	}

	public void setImageBytes(byte[] bytes) {
		bitmapRunningMap.clear();
		mHandler.removeCallbacks(runningTask);
		mHandler.removeCallbacks(drawPieceRunnable);

		removeCallbacks(drawPieceRunnable);
		removeCallbacks(runningTask);

		mAttacher.setOnMatrixChangeListener(this);

		Options opts = new Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);

		LogManager.getLogger().d(TAG, String.format("Bitmap's memory = %d", opts.outWidth * opts.outHeight * 4 / 1024 / 1024));

		int imgWidth = getImageViewWidth(this);
		int imgHeight = getImageViewHeight(this);

		// 占用内存偏大或者尺寸偏大
		if (opts.outWidth * opts.outHeight * 4 > maxMemory || opts.outHeight > maxSize || opts.outWidth > maxSize) {
			isPieceDecorde = true;
			LogManager.getLogger().d(TAG, String.format("ImageView's widht = %d, height = %d", imgWidth, imgHeight));

			origWidth = opts.outWidth;
			origHeight = opts.outHeight;
			LogManager.getLogger().d(TAG, String.format("Bitmap's widht = %d, height = %d", origWidth, origHeight));

			// 计算高宽度的缩放比例、计算行列数
			float widthRatio = origWidth * 1.0f / imgWidth;
			widthRatio = widthRatio < 1.0f ? 1.0f : widthRatio;
			float heightRatio = origHeight * 1.0f / imgHeight;
			heightRatio = heightRatio < 1.0f ? 1.0f : heightRatio;

			// 这里计算的显示比例，是针对显示图片相对原始图片的一个缩放比例
			// 将缩放比例，修改为原始比例的1.5倍
//			widthRatio = widthRatio * 1.5f;
//			heightRatio = heightRatio * 1.5f;

			// 行列为缩放比例的piece倍
			col = Math.round(widthRatio <= 1.5f ? 1 : widthRatio * piece);
			row = Math.round(heightRatio <= 1.5f ? 1 : heightRatio * piece);

			LogManager.getLogger().i(TAG, String.format("row=%d, col=%d", row, col));

			LogManager.getLogger().d(TAG, String.format("row = %d, col = %d", row, col));
			origToDisplayScale = widthRatio < heightRatio ? heightRatio : widthRatio;

			// 设置最大放大比例
			try {
				setMinimumScale(1);
				if (origToDisplayScale / 2 <= PhotoViewAttacher.DEFAULT_MID_SCALE) {
					setMediumScale(PhotoViewAttacher.DEFAULT_MID_SCALE);
					setMaximumScale(origToDisplayScale);
				} else {
					setMediumScale(origToDisplayScale / 2);
					setMaximumScale(origToDisplayScale);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			// 显示图片的尺寸
			int outWidth = (int) (origWidth / origToDisplayScale);
			int outHeight = (int) (origHeight / origToDisplayScale);
			LogManager.getLogger().d(TAG, String.format("OutBitmap's widht = %d, height = %d", outWidth, outHeight));

			// 构造显示的图片
			try {
				bitmapRegionDecoder = BitmapRegionDecoder.newInstance(bytes, 0, bytes.length, false);
				Rect rect = new Rect();
				Bitmap bitmap = Bitmap.createBitmap(outWidth, outHeight, Config.ARGB_8888);
				Canvas canvas = new Canvas(bitmap);
				Paint paint = new Paint();

				float pieceWidth = outWidth * 1.0f / col;
				float pieceHeight = outHeight * 1.0f / row;
				for (int r = 0; r < row; r++) {
					for (int c = 0; c < col; c++) {
						float x = pieceWidth * c;
						float y = pieceHeight * r;
						rect.left = Math.round(x * origToDisplayScale);
						rect.top = Math.round(y * origToDisplayScale);
						rect.right = Math.round((x + pieceWidth) * origToDisplayScale);
						rect.bottom = Math.round((y + pieceHeight) * origToDisplayScale);
						Bitmap pieceBitmap = bitmapRegionDecoder.decodeRegion(rect, null);
						LogManager.getLogger().d(TAG,
								String.format("PieceBitmap's widht = %d, height = %d", pieceBitmap.getWidth(), pieceBitmap.getHeight()));
						pieceBitmap = zoomBitmap(pieceBitmap, 1 / origToDisplayScale);
						LogManager.getLogger().v(TAG,
								String.format("PieceBitmap's widht = %d, height = %d", pieceBitmap.getWidth(), pieceBitmap.getHeight()));
						canvas.drawBitmap(pieceBitmap, x, y, paint);
					}
				}
				super.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
			} catch (Exception e) {
				e.printStackTrace();
			}

			// 检查图片的宽度和屏幕的宽度，如果屏幕的宽度比缩略的宽度大，则默认缩放至屏幕宽度大小
			if (true) {
				// 初始化图片大小最大至屏幕宽度
				initScale = origToDisplayScale;
				if (outWidth * initScale > imgWidth) {
					initScale = imgWidth * 1.0f / outWidth;
				}

				mHandler.removeCallbacks(setInitRunnable);
				mHandler.postDelayed(setInitRunnable, 700);
			}
		} else {
			// 检查图片的宽度和屏幕的宽度，如果屏幕的宽度比缩略的宽度大，则默认缩放至屏幕宽度大小
			if (true) {
				if (opts.outWidth < imgWidth) {
					LogManager.getLogger().e(TAG, String.format("outWidth=%s,imgWidth=%s", opts.outWidth + "", imgWidth + ""));
					initScale = imgWidth * 1.0f / opts.outWidth;
					setMinimumScale(initScale);
					setMediumScale(initScale * 1.5f);
					setMaximumScale(initScale * 3.0f);
				}
			}

			isPieceDecorde = false;
			opts.inJustDecodeBounds = false;
			Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
			super.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
		}

	}

	private float initScale = 0.0f;

	Runnable setInitRunnable = new Runnable() {

		@Override
		public void run() {
			LogManager.getLogger().e(TAG, initScale + "");
			if (initScale != 0.0f)
				mAttacher.setScale(initScale, 0, 0, true);
		}
	};

	/****************************************************************************************************/

	private boolean isPieceDecorde = false;
	Runnable drawPieceRunnable = new Runnable() {

		@Override
		public void run() {
			SparseArray<SparseArray<UnitBitmap>> bitmaps = bitmapHashtable.get(formatFloat(getScale()));
			if (bitmaps == null) {

				saveScale(getScale());

				bitmaps = new SparseArray<SparseArray<UnitBitmap>>();
				bitmapHashtable.put(scaleStr, bitmaps);
				invalidate();
			}

		}
	};

	@Override
	public void onMatrixChanged(RectF rect) {
		if (onMatrixChangedListener != null)
			onMatrixChangedListener.onMatrixChanged(rect);

//		LogManager.getLogger().v(TAG,
//				String.format("MatrixChanged , left = %f, top = %f, right = %f, bottom = %f", rect.left, rect.top, rect.right, rect.bottom));
//		LogManager.getLogger().v(TAG, String.format("currentScale = %f", getScale()));
//		RectF r = getDisplayRect();
//		LogManager.getLogger().v(TAG, String.format("DisplayRect , left = %f, top = %f, right = %f, bottom = %f", r.left, r.top, r.right, r.bottom));
	}

}
