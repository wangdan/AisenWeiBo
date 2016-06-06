package org.aisen.weibo.sina.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.Utils;
import org.aisen.weibo.sina.R;

/**
 * Created by kai.wang on 6/17/14.
 */
public class WaveView extends View {

    private Path aboveWavePath = new Path();

    private Path blowWavePath = new Path();

    private Paint aboveWavePaint = new Paint();

    private Paint blowWavePaint = new Paint();

    private final int default_above_wave_alpha = 100;

    private final int default_blow_wave_alpha = 70;

    private final int default_progress = 0;

    private final int text_default_size;

    private int waveToTop;

    private int aboveWaveColor;

    private int blowWaveColor;

    private int mProgress;

    /**
     * wave length
     */
    private final int x_zoom = 60;

    /**
     * wave crest
     */
    private final int y_zoom = 10;// 控制上下起伏的高度

    /**
     * offset of X
     */
    private final float offset = 0.5f;

    private final float max_right = x_zoom * offset;

    // wave animation
    private float aboveOffset = 0.0f;

    private float blowOffset = 4.0f;

    /**
     * offset of Y
     */
//    private float animOffset = 0.05f;// 控制上下起伏的频率
    private float animOffset = 0.08f;// 控制上下起伏的频率

    // refresh thread
    private RefreshProgressRunnable mRefreshProgressRunnable;
    private boolean isStop = false;

    private Rect r;

    private float density;

    public WaveView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.waveViewStyle);

        // 关闭硬件加速
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public WaveView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        density = getDensity(context);

        aboveWaveColor = context.getResources().getColor(R.color.wave);
        blowWaveColor = context.getResources().getColor(R.color.wave);
        text_default_size = Utils.dip2px(GlobalContext.getInstance(), 14);
        setProgress(default_progress);

        initializePainters();

        // 关闭硬件加速
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    Paint paintR, paintT, paintP;
    Path path1, path2;
    int textBaseLine;

    public boolean needWave() {
        if (mProgress <= 0 || mProgress > 100) {
            return false;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // 切割画布
        // 裁剪成圆区域
        if (path1 == null) {
            path1 = new Path();
            path2 = new Path();
            path1.reset();
            path1.addCircle(width / 2, height / 2, (float) (width / 2), Direction.CCW);
            path2.reset();
            path2.addCircle(width / 2, height / 2, (float) (width / 2) + 3, Direction.CCW);
        }
        canvas.clipPath(path1, Op.REPLACE);

        if (needWave()) {
            canvas.drawPath(blowWavePath, blowWavePaint);
            canvas.drawPath(aboveWavePath, aboveWavePaint);
        }

        if (paintR == null) {
            // 定义画笔2
            paintR = new Paint();
            // 消除锯齿
            paintR.setAntiAlias(true);
            // 设置画笔的颜色
            paintR.setColor(Color.parseColor("#ffffff"));
            paintR.setStrokeWidth(Utils.dip2px(GlobalContext.getInstance(), 1.5f));
            paintR.setStyle(Paint.Style.STROKE);
            paintR.setTextSize(text_default_size);
        }

        canvas.save();
        canvas.clipPath(path2, Op.REPLACE);

        // 画一个空心圆
        canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, getWidth() / 2f - paintR.getStrokeWidth() / 2, paintR);

        //画背景
        if (pressed) {
            if (paintP == null) {
                paintP = new Paint();
                paintP.setColor(Color.parseColor("#80ffffff"));
            }
            canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, getWidth() / 2f - paintR.getStrokeWidth() / 2, paintP);
        }

        if (getBackground() == null) {
            // 定义画笔2
            if (paintT == null) {
                paintT = new Paint();
                paintT.setAntiAlias(true);
                paintT.setColor(Color.parseColor("#ffffff"));
                paintT.setStrokeWidth(Utils.dip2px(GlobalContext.getInstance(), 1.5f));
                paintT.setTextSize(text_default_size);

                Paint.FontMetricsInt fontMetrics = paintT.getFontMetricsInt();
                textBaseLine = (getHeight() - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
            }
            canvas.drawText(mProgress + "%", width / 2 - paintT.measureText(mProgress + "%") / 2, textBaseLine, paintT);
        }
    }

    boolean pressed = false;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                pressed = true;
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                pressed = false;
                invalidate();
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    public static float getDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measure(widthMeasureSpec, true), measure(heightMeasureSpec, false));
    }

    private int measure(int measureSpec, boolean isWidth) {
        int result;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        int padding = isWidth ? getPaddingLeft() + getPaddingRight() : getPaddingTop() + getPaddingBottom();
        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            result = isWidth ? getSuggestedMinimumWidth() : getSuggestedMinimumHeight();
            result += padding;
            if (mode == MeasureSpec.AT_MOST) {
                if (isWidth) {
                    result = Math.max(result, size);
                } else {
                    result = Math.min(result, size);
                }
            }
        }
        return result;
    }

    private void initializePainters() {
        aboveWavePaint.setColor(aboveWaveColor);
        aboveWavePaint.setAlpha(default_above_wave_alpha);
        aboveWavePaint.setStyle(Paint.Style.FILL);
        aboveWavePaint.setAntiAlias(true);

        blowWavePaint.setColor(blowWaveColor);
        blowWavePaint.setAlpha(default_blow_wave_alpha);
        blowWavePaint.setStyle(Paint.Style.FILL);
        blowWavePaint.setAntiAlias(true);
    }

    /**
     * calculate wave track
     */
    private void calculatePath() {
        aboveWavePath.reset();
        blowWavePath.reset();
        getWaveOffset();
        aboveWavePath.moveTo(0, getHeight());
        for (float i = 0; x_zoom * i <= getRight() + max_right; i += offset) {
            aboveWavePath.lineTo((x_zoom * i), (float) (y_zoom * Math.cos(i + aboveOffset)) + waveToTop);
        }
        aboveWavePath.lineTo(getRight(), getHeight());

        blowWavePath.moveTo(0, getHeight());
        for (float i = 0; x_zoom * i <= getRight() + max_right; i += offset) {
            blowWavePath.lineTo((x_zoom * i), (float) (y_zoom * Math.cos(i + blowOffset)) + waveToTop);
        }
        blowWavePath.lineTo(getRight(), getHeight());
    }

    public void setProgress(int progress) {
        if (progress > 100)
            progress = 100;
//        else if (progress < 10)
//            progress = 10;
        this.mProgress = progress;
    }

    public void setProgress0() {
        this.mProgress = 0;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
//        LLog.e("waveview onAttachedToWindow()");
        mRefreshProgressRunnable = new RefreshProgressRunnable();
        post(mRefreshProgressRunnable);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
//        LLog.e("waveview onDetachedFromWindow()");
        removeCallbacks(mRefreshProgressRunnable);
        isStop = true;
    }

    private void getWaveOffset() {
        if (blowOffset > Float.MAX_VALUE - 100) {
            blowOffset = 0;
        } else {
            blowOffset += animOffset;
        }

        if (aboveOffset > Float.MAX_VALUE - 100) {
            aboveOffset = 0;
        } else {
            aboveOffset += animOffset;
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        // Force our ancestor class to save its state
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        ss.progress = mProgress;

        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setProgress(ss.progress);
    }

    private class RefreshProgressRunnable implements Runnable {
        public void run() {
            if (isStop)
                return;

            synchronized (WaveView.this) {
                waveToTop = (int) (getHeight() * (1f - mProgress / 100f));
                if (needWave()) {
                    calculatePath();
                    invalidate();
                }
                postDelayed(this, 32);
            }
        }
    }

    private static class SavedState extends BaseSavedState {
        int progress;

        /**
         * Constructor called from {@link ProgressBar#onSaveInstanceState()}
         */
        SavedState(Parcelable superState) {
            super(superState);
        }

        /**
         * Constructor called from {@link #CREATOR}
         */
        private SavedState(Parcel in) {
            super(in);
            progress = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(progress);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    private boolean running = false;

    // addbywangdan 2015-10-25 新增一个字段，标志当前的WaveView正在Progress
    // 根据原来已有代码逻辑添加
    @Override
    public void setBackgroundResource(int resid) {
        super.setBackgroundResource(resid);

        running = false;
    }

    @Override
    public void setBackground(Drawable background) {
        super.setBackground(background);

        running = true;
    }

    public boolean isRunning() {
        return running;
    }

}
