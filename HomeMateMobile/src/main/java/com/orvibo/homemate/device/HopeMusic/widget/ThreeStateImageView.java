package com.orvibo.homemate.device.HopeMusic.widget;

/**
 * Created by wuliquan on 2016/5/16.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.smartgateway.app.R;

public class ThreeStateImageView extends ImageView {
    private Paint paint;
    private Bitmap orginal_bitmap_check;
    private Bitmap orginal_bitmap_check_press;
    private Bitmap orginal_bitmap_uncheck;
    private Bitmap bitmap_check;
    private Bitmap bitmap_check_press;
    private Bitmap bitmap_uncheck;
    private boolean isCheck;
    private boolean isPress;
    private float view_Width;
    public ThreeStateImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public ThreeStateImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.ThreeStateImageView);
        int uncheck_id = typedArray.getResourceId(R.styleable.ThreeStateImageView_uncheck_drawable, R.drawable.ic_launcher);
        int check_id = typedArray.getResourceId(R.styleable.ThreeStateImageView_check_normal_drawable, R.drawable.ic_launcher);
        int check_press_id = typedArray.getResourceId(R.styleable.ThreeStateImageView_check_press_drawable, R.drawable.ic_launcher);
        orginal_bitmap_check= BitmapFactory.decodeResource(getResources(),check_id);
        orginal_bitmap_check_press= BitmapFactory.decodeResource(getResources(), check_press_id);
        orginal_bitmap_uncheck= BitmapFactory.decodeResource(getResources(),uncheck_id);
        init();
    }

    public ThreeStateImageView(Context context) {
        super(context);
        init();
    }
    private void init()
    {   paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true);


    }
    private void initBitmap(float width){
        if(width==0)
            return;
        bitmap_check=scaleBitmap(orginal_bitmap_check,width);
        bitmap_check_press=scaleBitmap(orginal_bitmap_check_press,width);
        bitmap_uncheck=scaleBitmap(orginal_bitmap_uncheck,width);
        orginal_bitmap_check.recycle();
        orginal_bitmap_check_press.recycle();
        orginal_bitmap_uncheck.recycle();
    }
    private Bitmap scaleBitmap(Bitmap orignal_bitmap, float width){
        int bmpWidth=orignal_bitmap.getWidth();
        int bmpHeight=orignal_bitmap.getHeight();
        float scale=width/bmpWidth;
        Matrix matrix = new Matrix();
        matrix.postScale(scale,scale);
        Bitmap bitmap = Bitmap.createBitmap(orignal_bitmap,0,0,bmpWidth,bmpHeight,matrix,true);
        return bitmap;
    }
    public void setCheck(boolean isCheck){
        this.isCheck=isCheck;
        this.isPress=false;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if(view_Width==0){
            view_Width=getWidth();
            initBitmap(view_Width);
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if(isCheck){
            if(isPress){
                if(bitmap_check_press!=null){
                canvas.drawBitmap(bitmap_check_press,0,0,paint);}
            }else {
                if(bitmap_check!=null) {
                    canvas.drawBitmap(bitmap_check, 0, 0, paint);
                }
            }
        }else{
            if(bitmap_uncheck!=null) {
                canvas.drawBitmap(bitmap_uncheck, 0, 0, paint);
            }
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                 isPress=true;
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_OUTSIDE:
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                isPress=false;
                invalidate();
                break;
        }
        if(isCheck)
        return true;
        else
        return false;
    }


}