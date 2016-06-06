package org.aisen.weibo.sina.ui.widget.io.codetail.animation.arcanimator;

import android.view.View;
import android.view.animation.Interpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

import java.lang.ref.WeakReference;

public class ArcAnimator extends Animator {

    public static ArcAnimator createArcAnimator(View clipView, View nestView,
                                                float degree, Side side){

        return createArcAnimator(clipView, Utils.centerX(nestView), Utils.centerY(nestView),
                degree, side);
    }

    public static ArcAnimator createArcAnimator(View clipView, float endX, float endY,
                                                float degree, Side side){

        ArcMetric arcMetric = ArcMetric.evaluate(Utils.centerX(clipView), Utils.centerY(clipView),
                endX, endY, degree, side);
        return new ArcAnimator(arcMetric, clipView);
    }

    ArcMetric mArcMetric;
    WeakReference<View> mTarget;
    WeakReference<ValueAnimator> mAnimator;
    float mValue;


    private ArcAnimator(ArcMetric arcmetric, View target) {
        mArcMetric = arcmetric;
        mTarget = new WeakReference<>(target);

        mAnimator = new WeakReference<>(
                ValueAnimator.ofFloat(arcmetric.getStartDegree(), arcmetric.getEndDegree())
        );
        mAnimator.get().addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                setDegree((Float) animation.getAnimatedValue());
            }
        });
    }

    void setDegree(float degree){
        mValue = degree;
        View clipView = mTarget.get();
        float x = mArcMetric.getAxisPoint().x + mArcMetric.mRadius
                * Utils.cos(degree);
        float y = mArcMetric.getAxisPoint().y - mArcMetric.mRadius
                * Utils.sin(degree);
        ViewHelper.setX(clipView,x - clipView.getWidth() / 2);
        ViewHelper.setY(clipView,y - clipView.getHeight() / 2);
    }

    float getDegree(){
        return mValue;
    }

    @Override
    public long getStartDelay() {
        Animator a = mAnimator.get();
        return a == null ? 0 : a.getDuration();
    }

    @Override
    public void setStartDelay(long startDelay) {
        Animator a = mAnimator.get();
        if(a != null) a.setStartDelay(startDelay);
    }

    @Override
    public ArcAnimator setDuration(long duration) {
        Animator a = mAnimator.get();
        if(a != null) a.setDuration(duration);
        return this;
    }

    @Override
    public long getDuration() {
        Animator a = mAnimator.get();
        return a == null ? 0 : a.getDuration();
    }

    @Override
    public void setInterpolator(Interpolator value) {
        Animator a = mAnimator.get();
        if(a != null) a.setInterpolator(value);
    }

    @Override
    public void start() {
        super.start();
        Animator a = mAnimator.get();
        if(a != null) a.start();
    }

    @Override
    public void end() {
        super.end();
        Animator a = mAnimator.get();
        if(a != null) a.end();
    }

    @Override
    public void cancel() {
        super.cancel();
        Animator a = mAnimator.get();
        if(a != null) a.cancel();
    }

    @Override
    public void addListener(AnimatorListener listener) {
        Animator a = mAnimator.get();
        if(a != null) a.addListener(listener);
    }

    @Override
    public void setupEndValues() {
        super.setupEndValues();
        Animator a = mAnimator.get();
        if(a != null) a.setupEndValues();
    }

    @Override
    public void setupStartValues() {
        super.setupStartValues();
        Animator a = mAnimator.get();
        if(a != null) a.setupStartValues();
    }

    @Override
    public boolean isRunning() {
        Animator a = mAnimator.get();
        return a != null && a.isRunning();
    }

    @Override
    public String toString() {
        return mArcMetric.toString();
    }

}
