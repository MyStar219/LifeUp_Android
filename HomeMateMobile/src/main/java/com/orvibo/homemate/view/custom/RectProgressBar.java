package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by wuliquan on 2016/7/5.
 */
public class RectProgressBar  extends View{

    private float width;
    private float height;
    private Paint paint;
    private float progress;
    private boolean isCheck;

    public RectProgressBar(Context context) {
        super(context);
        init(context);
    }

    public RectProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RectProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        paint = new Paint();
        paint.setStrokeWidth(2);

    }

    public boolean getCheck(){
        return isCheck;
    }
    public void setCheck(boolean isCheck){
        if(isCheck==this.isCheck){
            return;
        }
        this.isCheck =isCheck;

        invalidate();
    }

    public void setProgress(float progress){
        if(progress<0){
            progress=0;
        }
        if(progress>100){
            this.progress=100;
        }else {
            this.progress = progress;
        }
        invalidate();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(width==0||height==0){
            width=getWidth();
            height=getHeight();
        }
        drawProgress(canvas);
    }

    private void drawProgress(Canvas canvas){
        float drawHeight  = progress*height;
        Path path = new Path();
        path.moveTo(0,height-drawHeight);
        path.lineTo(0,height);
        path.lineTo(width,height);
        path.lineTo(width,height-drawHeight);
        path.close();

        if(isCheck){
            drawCheckProgress(canvas,path);
        }else{
            drawDefault(canvas,path);
        }


    }
    private void drawDefault(Canvas canvas,Path path){
        paint.setColor(Color.parseColor("#ebebeb"));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(path,paint);

        paint.setColor(Color.parseColor("#e0e0e0"));
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path,paint);
    }

    private void drawCheckProgress(Canvas canvas,Path path){


        paint.setColor(Color.parseColor("#3dd38a"));
        paint.setStyle(Paint.Style.FILL);

        canvas.drawPath(path,paint);
    }
}
