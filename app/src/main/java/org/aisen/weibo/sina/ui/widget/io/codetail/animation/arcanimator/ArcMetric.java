package org.aisen.weibo.sina.ui.widget.io.codetail.animation.arcanimator;

import android.graphics.PointF;

import java.util.Arrays;

class ArcMetric {

    PointF mStartPoint = new PointF();

    PointF mEndPoint = new PointF();

    PointF mMidPoint = new PointF();

    PointF mAxisPoint[] = new PointF[2];

    PointF mZeroPoint = new PointF();

    //SEGMENTS. This Segments create virtual triangle except mZeroStartSegment

    float mStartEndSegment;

    float mRadius;

    float mMidAxisSegment;

    float mZeroStartSegment;

    //DEGREES.

    float mAnimationDegree;

    float mSideDegree;

    float mZeroStartDegree;

    float mStartDegree;

    float mEndDegree;

    //Side of animation
    Side mSide;

    private void createAxisVariables(){
        for(int i=0; i<mAxisPoint.length; i++)
            mAxisPoint[i] = new PointF();
    }


    private void calcStartEndSeg(){
        mStartEndSegment = (float) Math.sqrt(Math.pow(mStartPoint.x - mEndPoint.x, 2)+
                Math.pow(mStartPoint.y - mEndPoint.y, 2));

    }

    private void calcRadius(){
        mSideDegree = (180 - mAnimationDegree)/2;
        mRadius = mStartEndSegment/ Utils.sin(mAnimationDegree)* Utils.sin(mSideDegree);
    }

    private void calcMidAxisSeg(){
        mMidAxisSegment = mRadius * Utils.sin(mSideDegree);
    }

    private void calcMidPoint(){
        mMidPoint.x = mStartPoint.x + mStartEndSegment/2*(mEndPoint.x - mStartPoint.x)/mStartEndSegment;
        mMidPoint.y = mStartPoint.y + mStartEndSegment/2*(mEndPoint.y - mStartPoint.y)/mStartEndSegment;
    }

    private void calcAxisPoints(){
        if(mStartPoint.y > mEndPoint.y || mStartPoint.y == mEndPoint.y){
            mAxisPoint[0].x = mMidPoint.x + mMidAxisSegment*(mEndPoint.y-mStartPoint.y)/mStartEndSegment;
            mAxisPoint[0].y = mMidPoint.y - mMidAxisSegment*(mEndPoint.x-mStartPoint.x)/mStartEndSegment;

            mAxisPoint[1].x = mMidPoint.x - mMidAxisSegment*(mEndPoint.y-mStartPoint.y)/mStartEndSegment;
            mAxisPoint[1].y = mMidPoint.y + mMidAxisSegment*(mEndPoint.x-mStartPoint.x)/mStartEndSegment;
        }else{
            mAxisPoint[0].x = mMidPoint.x - mMidAxisSegment*(mEndPoint.y-mStartPoint.y)/mStartEndSegment;
            mAxisPoint[0].y = mMidPoint.y + mMidAxisSegment*(mEndPoint.x-mStartPoint.x)/mStartEndSegment;

            mAxisPoint[1].x = mMidPoint.x + mMidAxisSegment*(mEndPoint.y-mStartPoint.y)/mStartEndSegment;
            mAxisPoint[1].y = mMidPoint.y - mMidAxisSegment*(mEndPoint.x-mStartPoint.x)/mStartEndSegment;
        }
    }

    private void calcZeroPoint(){
        switch (mSide){
            case RIGHT:
                mZeroPoint.x = mAxisPoint[Side.RIGHT.value].x + mRadius;
                mZeroPoint.y = mAxisPoint[Side.RIGHT.value].y;
                break;
            case LEFT:
                mZeroPoint.x = mAxisPoint[Side.LEFT.value].x - mRadius;
                mZeroPoint.y = mAxisPoint[Side.LEFT.value].y;
                break;
        }
    }

    private void calcDegrees(){
        mZeroStartSegment = (float) Math.sqrt(Math.pow(mZeroPoint.x - mStartPoint.x, 2)+
                Math.pow(mZeroPoint.y - mStartPoint.y, 2));
        mZeroStartDegree = Utils.acos((2*Math.pow(mRadius,2)-Math.pow(mZeroStartSegment,2))/(2*Math.pow(mRadius,2)));
        switch(mSide){
            case RIGHT:
                if(mStartPoint.y <= mZeroPoint.y){
                    if(mStartPoint.y > mEndPoint.y ||
                            (mStartPoint.y == mEndPoint.y && mStartPoint.x > mEndPoint.x)){
                        mStartDegree = mZeroStartDegree;
                        mEndDegree = mStartDegree + mAnimationDegree;
                    }else{
                        mStartDegree = mZeroStartDegree;
                        mEndDegree = mStartDegree - mAnimationDegree;
                    }
                }else if(mStartPoint.y >= mZeroPoint.y){
                    if(mStartPoint.y < mEndPoint.y ||
                            (mStartPoint.y == mEndPoint.y && mStartPoint.x > mEndPoint.x)){
                        mStartDegree = 0 - mZeroStartDegree;
                        mEndDegree = mStartDegree - mAnimationDegree;
                    }else {
                        mStartDegree = 0 - mZeroStartDegree;
                        mEndDegree = mStartDegree + mAnimationDegree;
                    }
                }
                break;
            case LEFT:
                if(mStartPoint.y <= mZeroPoint.y){
                    if(mStartPoint.y > mEndPoint.y ||
                            (mStartPoint.y == mEndPoint.y && mStartPoint.x < mEndPoint.x)){
                        mStartDegree = 180 - mZeroStartDegree;
                        mEndDegree = mStartDegree - mAnimationDegree;
                    }else{
                        mStartDegree = 180 - mZeroStartDegree;
                        mEndDegree = mStartDegree + mAnimationDegree;
                    }
                }else if(mStartPoint.y >= mZeroPoint.y){
                    if(mStartPoint.y < mEndPoint.y ||
                            (mStartPoint.y == mEndPoint.y && mStartPoint.x < mEndPoint.x)){
                        mStartDegree = 180 + mZeroStartDegree;
                        mEndDegree = mStartDegree + mAnimationDegree;
                    }else{
                        mStartDegree = 180 + mZeroStartDegree;
                        mEndDegree = mStartDegree - mAnimationDegree;
                    }
                }
                break;
        }
    }

    /**
     * Create new {@link org.aisen.android.component.io.codetail.animation.arcanimator.ArcMetric} instance and do all calculations below
     * and finally return ready to use object
     */
    public static ArcMetric evaluate(float startX, float startY,
                                     float endX, float endY,
                                     float degree, Side side){
        //TODO return ready to use object with have done computations
        ArcMetric arcMetric = new ArcMetric();
        arcMetric.mStartPoint.set(startX, startY);
        arcMetric.mEndPoint.set(endX, endY);
        arcMetric.setDegree(degree);
        arcMetric.mSide = side;
        arcMetric.createAxisVariables();

        arcMetric.calcStartEndSeg();
        arcMetric.calcRadius();
        arcMetric.calcMidAxisSeg();
        arcMetric.calcMidPoint();
        arcMetric.calcAxisPoints();
        arcMetric.calcZeroPoint();
        arcMetric.calcDegrees();

        return arcMetric;
    }

    public void setDegree(float degree) {
        degree = Math.abs(degree);
        if(degree>180)
            setDegree(degree%180);
        else if(degree == 180)
            setDegree(degree-1);
        else if(degree<30)
            setDegree(30);
        else
            this.mAnimationDegree = degree;
    }

    PointF getAxisPoint(){
        return mAxisPoint[mSide.value];
    }

    /**
     * Return evaluated start degree
     *
     * @return the start degree
     */
    public float getStartDegree(){
        return mStartDegree;
    }

    /**
     * Return evaluated end degree
     *
     * @return the end degree
     */
    public float getEndDegree(){
        return mEndDegree;
    }

    @Override
    public String toString() {
        return "ArcMetric{" +
                "\nmStartPoint=" + mStartPoint +
                "\n mEndPoint=" + mEndPoint +
                "\n mMidPoint=" + mMidPoint +
                "\n mAxisPoint=" + Arrays.toString(mAxisPoint) +
                "\n mZeroPoint=" + mZeroPoint +
                "\n mStartEndSegment=" + mStartEndSegment +
                "\n mRadius=" + mRadius +
                "\n mMidAxisSegment=" + mMidAxisSegment +
                "\n mZeroStartSegment=" + mZeroStartSegment +
                "\n mAnimationDegree=" + mAnimationDegree +
                "\n mSideDegree=" + mSideDegree +
                "\n mZeroStartDegree=" + mZeroStartDegree +
                "\n mStartDegree=" + mStartDegree +
                "\n mEndDegree=" + mEndDegree +
                "\n mSide=" + mSide +
                '}';
    }

}
