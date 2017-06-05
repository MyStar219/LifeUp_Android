package com.orvibo.homemate.device.HopeMusic.widget;

/**
 * Created by wuliquan on 2016/5/16.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.smartgateway.app.R;


public class SelectImageView extends ImageView {
    private Paint paint;
    private Bitmap bitmap;
    private boolean isCheck;
    public SelectImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public SelectImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SelectImageView(Context context) {
        super(context);
        init();
    }
    private void init()
    {   paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true);
        bitmap= BitmapFactory.decodeResource(getResources(), R.drawable.bt_choice);
    }
    public void setCheck(boolean isCheck){
        this.isCheck=isCheck;
        invalidate();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(isCheck)
        canvas.drawBitmap(bitmap,getWidth()-bitmap.getWidth(),0,paint);
    }

}