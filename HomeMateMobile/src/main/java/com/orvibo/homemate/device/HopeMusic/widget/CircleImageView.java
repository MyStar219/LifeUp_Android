package com.orvibo.homemate.device.HopeMusic.widget;

/**
 * Created by wuliquan on 2016/5/16.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.smartgateway.app.R;

public class CircleImageView extends View {

    public CircleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public CircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleImageView(Context context) {
        super(context);
        init();
    }
    private Bitmap bitmap;
    private Rect bitmapRect=new Rect();
    private PaintFlagsDrawFilter pdf=new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG| Paint.FILTER_BITMAP_FLAG);
    private Paint paint = new Paint();
    {
        paint.setStyle(Paint.Style.STROKE);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true);// 设置画笔的锯齿效果。 true是去除，大家一看效果就明白了
    }
    private Bitmap mDstB=null;
    private PorterDuffXfermode xfermode=new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY);


    private void init()
    {
        try {
            if(android.os.Build.VERSION.SDK_INT>=11)
            {
                setLayerType(LAYER_TYPE_SOFTWARE, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setImageBitmap(Bitmap bitmap)
    {
        this.bitmap=bitmap;
        invalidate();
    }

    private Bitmap makeDst(int w, int h)
    {
        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.parseColor("#ffffffff"));
        c.drawOval(new RectF(0, 0, w, h), p);
        return bm;
    }
    @Override
    protected void onDraw(Canvas canvas) {

        if(null==bitmap)
        {
             bitmap= BitmapFactory.decodeResource(getResources(), R.drawable.bg_picture);
        }
        if(null==mDstB)
        {   int min = Math.min(getWidth(),getHeight());
            mDstB=makeDst(min, min);
        }

        int min = Math.min(getWidth(),getHeight());
        bitmapRect.set(getWidth()/2-min/2, getHeight()/2-min/2,getWidth()/2+min/2, getHeight()/2+min/2);
        canvas.save();
        canvas.setDrawFilter(pdf);
        canvas.drawBitmap(mDstB, getWidth()/2-min/2, getHeight()/2-min/2, paint);
        paint.setXfermode(xfermode);
        canvas.drawBitmap(bitmap, null, bitmapRect, paint);
        paint.setXfermode(null);
        canvas.restore();
    }
}