package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.orvibo.homemate.util.LogUtil;

/**
 * 色温灯使用的圆
 * Created by huangqiyao on 2015/6/5.
 */
public class ColorTempertureView extends View {
    private static final String TAG = "ColorTempertureView";
    private Paint mPaint;
    private int alpha, red, green, blue;

    public ColorTempertureView(Context context) {
        super(context);
        init(context);
    }

    public ColorTempertureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ColorTempertureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public ColorTempertureView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mPaint = new Paint();
        mPaint.setColor(Color.YELLOW);
        mPaint.setStrokeWidth(3);
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = getMeasuredWidth() / 2;
        int h = getMeasuredHeight() / 2;
        int min = Math.min(w, h);
        mPaint.setARGB(alpha, red, green, blue);
        canvas.drawCircle(w, h, min, mPaint);
        super.onDraw(canvas);
    }

    public void setARGB(int alpha, int red, int green, int blue) {
        //透明度过低的话页面上看不出圆界面
        if (alpha <= 30) {
            alpha = 30;
        }
        this.alpha = alpha;
        this.red = red;
        this.green = green;
        this.blue = blue;
        LogUtil.i(TAG, "setARGB()-alpha:" + alpha
                + ",red:" + red
                + ",green:" + green
                + ",blue:" + blue);
        postInvalidate();
    }
}
