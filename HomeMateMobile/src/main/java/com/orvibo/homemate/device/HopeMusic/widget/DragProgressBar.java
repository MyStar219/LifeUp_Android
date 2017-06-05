package com.orvibo.homemate.device.HopeMusic.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.smartgateway.app.R;
import com.orvibo.homemate.device.HopeMusic.util.CommonUtil;

/**
 * Created by wuliquan on 2016/6/27.
 */
public class DragProgressBar extends View {
    private int thumbLeft = 0;
    private int thumbTop  = 0;
    private int  mThumbWidth=100;
    private int  mThumbHeight=50;
    private int paintHeight=8;
    private float rightTextSize;
    private float thumbTextSize;
    private Paint paint;
    private Paint mTextPaint;
    private Paint mReachedBarPaint;
    private Paint mUnreachedBarPaint;
    private String right_title="";
    private int  mCurrentprogress;
    private int mMaxProgress;
    private OnDragProgressListener onDragProgressListener;
    /**
     * 上面的游标rect
     */
    private RectF mThumbRectF = new RectF(thumbLeft,thumbTop,thumbLeft+mThumbWidth,thumbTop+mThumbHeight);
    public DragProgressBar(Context context) {
        super(context);
        init(context);
    }

    public DragProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DragProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measure(widthMeasureSpec, true), measure(heightMeasureSpec, false));

    }

    private void init(Context context){
        paintHeight  = CommonUtil.dp2px(context,3);
        mThumbWidth  = CommonUtil.dp2px(context,35);
        mThumbHeight  =mThumbWidth /2;
        rightTextSize= CommonUtil.sp2px(context,15);
        thumbTextSize= CommonUtil.sp2px(context,12);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.parseColor("#ffffff"));
        mTextPaint = new Paint();
        mTextPaint.setColor(getResources().getColor(R.color.green));
        mReachedBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mReachedBarPaint.setStrokeWidth(paintHeight);
        mReachedBarPaint.setStrokeCap(Paint.Cap.ROUND);
        mReachedBarPaint.setColor(getResources().getColor(R.color.white));
        mUnreachedBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mUnreachedBarPaint.setStrokeWidth(paintHeight);
        mUnreachedBarPaint.setStrokeCap(Paint.Cap.ROUND);
        mUnreachedBarPaint.setColor(getResources().getColor(R.color.gray));
    }

    public void setOnDragProgressListener(OnDragProgressListener listener) {
        this.onDragProgressListener = listener;

    }
    public void setRight_title(String right_title){
        if(right_title!=null) {
            this.right_title = right_title;
            invalidate();
        }
    }
    public void setMax(int maxProgress) {
        if (maxProgress > 0) {
            this.mMaxProgress = maxProgress;
            invalidate();
        }
    }

    public void setProgress(float progress){
        if(progress>0){
            int value = (int)(progress/100*mMaxProgress);
            setProgress(value);
        }
    }
    public void setProgress(int value){
        if(value<0) {
            mCurrentprogress = 0;
        }
        else if(value>mMaxProgress) {
            mCurrentprogress = mMaxProgress;
        }
        else {
            this.mCurrentprogress = value;
          }
        invalidate();
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

    @Override
    protected void onDraw(Canvas canvas) {
        if(thumbTop==0){
            float height = getHeight();
            if(height!=0){
                thumbTop = (int)(height/2-mThumbHeight/2);
                mThumbRectF.top=thumbTop;
                mThumbRectF.bottom = mThumbRectF.top+mThumbHeight;
            }
        }
        //先绘制一条底色
        canvas.drawLine(getPaddingLeft()+mThumbWidth/2,getHeight()/2,getWidth()-getPaddingRight()-mThumbWidth/2,getHeight()/2,mUnreachedBarPaint);
        //再绘制一条已经到达的线
        float progress = 0;
        if(mMaxProgress!=0){
            progress = (float)mCurrentprogress/(float)mMaxProgress;
        }
        float dif =progress*(getWidth()-getPaddingLeft()-getPaddingRight()-mThumbWidth);
        mThumbRectF.left = dif+getPaddingLeft();
        mThumbRectF.top  = getHeight()/2-mThumbHeight/2;
        mThumbRectF.right = mThumbRectF.left+mThumbWidth;
        mThumbRectF.bottom = mThumbRectF.top+mThumbHeight;

        canvas.drawLine(getPaddingLeft()+mThumbWidth/2,getHeight()/2,(float)(mThumbRectF.left+mThumbWidth/2),getHeight()/2,mReachedBarPaint);
        canvas.drawRoundRect(mThumbRectF,mThumbRectF.height()/2,mThumbRectF.height()/2,paint);
        Paint.FontMetrics fm = mTextPaint.getFontMetrics();
        int textHeight = (int) (Math.ceil(fm.descent - fm.ascent) + 2);
        mTextPaint.setColor(getResources().getColor(R.color.white));
        mTextPaint.setTextSize(rightTextSize);
        canvas.drawText(right_title,getWidth()-getPaddingRight()-mThumbWidth/2-mTextPaint.measureText(right_title),getHeight()-textHeight/2,mTextPaint);
        String mCurrentDrawText = getProgressText();
        mTextPaint.setColor(getResources().getColor(R.color.green));
        mTextPaint.setTextSize(thumbTextSize);
        int mDrawTextEnd = (int) ((getHeight() / 2.0f) - ((mTextPaint.descent() + mTextPaint.ascent()) / 2.0f));
        canvas.drawText(mCurrentDrawText, mThumbRectF.left+mThumbWidth/2-mTextPaint.measureText(mCurrentDrawText)/2, mDrawTextEnd, mTextPaint);
        calcuteProgress();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                calcuteThumbRect(event,false);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                calcuteThumbRect(event,true);
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }

    private void calcuteThumbRect(MotionEvent event,boolean isUp){
        if(event!=null){
            float x = event.getX();
            float y = event.getY();
            if(x<getPaddingLeft()){
              mCurrentprogress=0;
            }else if(x>getWidth()-getPaddingRight()){
               mCurrentprogress=mMaxProgress;
            }else {
                float valueLeft=x-getPaddingLeft();
                float valueTotal=getWidth() - getPaddingLeft()-getPaddingRight();
                float value = valueLeft/valueTotal;
                mCurrentprogress = (int) (value* mMaxProgress);
            }
            if(onDragProgressListener!=null&&isUp){
                onDragProgressListener.onProgressChange(mCurrentprogress);
            }
        }
    }

    private float  calcuteProgress(){
        float totalLength = getWidth()-getPaddingLeft()-getPaddingRight()-mThumbWidth;
        //计算划过的长度
        float dif= mThumbRectF.left+mThumbWidth/2-getPaddingLeft()-mThumbWidth/2;
        if(totalLength==0)
            return 0;
        else{
            return dif/totalLength;
        }
    }

    private String getProgressText(){
        float progress = calcuteProgress();
        int  time = (int)(progress*mMaxProgress);
        //由于这里是以秒为单位的
        int min = time/60;
        int sec = time%60;
        String m = min+"";
        String s = sec+"";
        if(min<10)
            m="0"+min;
        if (sec<10)
            s="0"+sec;
        return m+":"+s;
    }

    public interface OnDragProgressListener{
       void onProgressChange(int position);
    }
}
