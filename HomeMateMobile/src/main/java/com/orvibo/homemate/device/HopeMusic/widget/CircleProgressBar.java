package com.orvibo.homemate.device.HopeMusic.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;

import com.smartgateway.app.R;
import com.orvibo.homemate.device.HopeMusic.util.CommonUtil;

/**
 * Created by wuliquan on 2016/5/15.
 */
public class CircleProgressBar extends CompoundButton {
    private Context context;
    //圆环的圆心x坐标
    private float cx;
    //圆环的圆心的y坐标
    private float cy;
    //控件的宽度
    private float width;
    //控件的高度
    private float height;
    //圆的半径
    private float circle_radius;
    //圆环的宽度
    private float bound_width=1;
    //圆形进度条的宽度
    private float progress_width=6;
    //  "||"暂停两条竖线距离圆形的值
    private float margin_x ;
    private float margin_y;
    //圆环的颜色
    private int bound_color;
    private int bound_color_default= Color.parseColor("#31c37c");
    //圆环点击透明度颜色
    private int bound_color_press;
    private int bound_color_press_default= Color.parseColor("#7f31c37c");
    //圆环的进度颜色
    private int progress_color;
    //圆环的进度
    private float progress=0;
    //总长度
    private int max;
    //画笔
    private Paint paint,bound_paint;
    //
    private RectF rectF,rectF_Progress;

    private boolean isCheck;
    //状态监听
    private StateChangeListener changeListener;

    private static final int NO_ALPHA = 0xFF;



    public CircleProgressBar(Context context) {
        super(context);
        init(context);
    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.CircleProgressBar);
        bound_color=typedArray.getColor(R.styleable.CircleProgressBar_bound_color,bound_color_default);
        bound_color_press=typedArray.getColor(R.styleable.CircleProgressBar_bound_color_press,bound_color_press_default);
        init(context);
        typedArray.recycle();
    }

    public void setStateChangeListener(StateChangeListener listener){
        this.changeListener=listener;
    }
    public float getCircle_radius() {
        return circle_radius;
    }
    public void setCircle_radius(float circle_radius) {
        this.circle_radius = circle_radius;
        invalidate();
    }
    public float getBound_width() {
        return bound_width;
    }
    public void setBound_width(float bound_width) {
        this.bound_width = bound_width;
        invalidate();
    }
    public int getBound_color() {
        return bound_color;
    }
    public void setBound_color(int bound_color) {
        this.bound_color = bound_color;
        invalidate();
    }
    public int getProgress_color() {
        return progress_color;
    }
    public void setProgress_color(int progress_color) {
        this.progress_color = progress_color;
        invalidate();
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(int progress){
        if(progress>0){
            float value = ((float)progress*100/(float) max);
            setProgress(value);
        }
    }

    public void setProgress(float progress) {
        if(isCheck) {
            this.progress = progress;
            bound_paint.setStrokeWidth(progress_width);
            bound_paint.setColor(bound_color);
            invalidate();
        }
    }
    public void setMax(int max){
        this.max = max;
    }
    public void finsh(){
        isCheck=false;
        bound_paint.setStrokeWidth(0);
        invalidate();
    }

    private void init(Context context){
        this.context=context;
        progress_width = CommonUtil.dp2px(context,3);
        bound_width= CommonUtil.dp2px(context,1);

        paint=new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(bound_width);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(bound_color);

        bound_paint =new Paint();
        bound_paint.setAntiAlias(true);
        bound_paint.setStrokeWidth(bound_width);
        bound_paint.setStyle(Paint.Style.STROKE);
        bound_paint.setColor(bound_color);

    }
    public void recover(){
        isCheck=false;
        setProgress(0);
    }
    @Override
    public void setChecked(boolean checked) {
        isCheck=checked;
        invalidate();
    }




    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if(width==0) {
            width = getWidth();
            height = getHeight();
            float min = Math.min(width, height);
            rectF = new RectF(getPaddingLeft(), getPaddingTop(), width - getPaddingRight(), height - getPaddingBottom());
            float padding = progress_width / 2;
            rectF_Progress = new RectF(rectF.left + padding, rectF.top + padding, rectF.right - padding, rectF.bottom - padding);
            cx = rectF.centerX();
            cy = rectF.centerY();
            circle_radius = min / 2;
            margin_x = (circle_radius-bound_width-progress_width)/3;
            margin_y = margin_x*6/7;
        }
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setStrokeWidth(bound_width);
        canvas.drawArc(rectF,-90,360,false,paint);

        paint.setStrokeWidth(2*bound_width);
        if(isCheck){
            drawPause(canvas);
        }else{
            drawPlay(canvas);
        }
        drawProgress(canvas);

    }

    private void drawProgress(Canvas canvas){
        if(canvas!=null){
            float swapAngle= progress*360/100;
            canvas.drawArc(rectF_Progress,-90,swapAngle,false,bound_paint);
        }
    }

    private void drawPause(Canvas canvas){
        Path path = new Path();
        path.moveTo(cx-margin_x,cy-margin_y);
        path.lineTo(cx-margin_x,cy+margin_y);

        path.moveTo(cx+margin_x,cy-margin_y);
        path.lineTo(cx+margin_x,cy+margin_y);
        canvas.drawPath(path,paint);

    }

    private void drawPlay(Canvas canvas){
        Path path = new Path();
        path.moveTo(cx-margin_x,cy-margin_y);
        path.lineTo(cx-margin_x,cy+margin_y);
        path.lineTo(cx+margin_x,cy);
        path.close();
        canvas.drawPath(path,paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                paint.setColor(bound_color_press);
                bound_paint.setColor(bound_color_press);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_OUTSIDE:
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                paint.setColor(bound_color);
                bound_paint.setColor(bound_color);
                if(inRangeOfView(this,event)) {
                    boolean state = isCheck;
                    if(changeListener!=null){
                        changeListener.stateChange(!state);
                    }
                }
                invalidate();
                break;
        }
        return true;
    }
    private boolean inRangeOfView(View view, MotionEvent ev){
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        if(ev.getRawX() < x || ev.getRawX() > (x + view.getWidth()) || ev.getRawY() < y || ev.getRawY() > (y + view.getHeight())){
            return false;
        }
        return true;
    }

    public interface StateChangeListener{
        void stateChange(boolean isCheck);
    }


}
