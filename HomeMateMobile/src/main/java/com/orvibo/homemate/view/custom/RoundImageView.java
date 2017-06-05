package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Smagret on 2016/7/14.
 * 圆形ImageView 并且设置边框（边框通过布局来实现，即在RoundImageView下面放置一个圆，直径比此view多一个像素）
 */
public class RoundImageView extends TextView {

    private int w;
    private int h;
    private int color;
    private int white = Color.rgb(255, 255, 255);
    public RoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RoundImageView(Context context) {
        super(context);
        init();
    }

    private final RectF roundRect = new RectF();
    private float rect_adius = 24;
    private final Paint maskPaint = new Paint();
    private final Paint zonePaint = new Paint();

    private void init() {
        maskPaint.setAntiAlias(true);
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        maskPaint.setFilterBitmap(true);
        zonePaint.setAntiAlias(true);
        zonePaint.setColor(Color.BLACK);
        zonePaint.setFilterBitmap(true);
        float density = getResources().getDisplayMetrics().density;
        rect_adius = rect_adius * density;
    }

    public void setRectAdius(float adius) {
        rect_adius = adius;
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        w = getWidth();
        h = getHeight();

        if (color == white) {
            roundRect.set(1, 1, w - 1, h - 1);
        } else {
            roundRect.set(0, 0, w, h);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.saveLayer(roundRect, zonePaint, Canvas.ALL_SAVE_FLAG);
        canvas.drawRoundRect(roundRect, rect_adius, rect_adius, zonePaint);
        canvas.saveLayer(roundRect, maskPaint, Canvas.ALL_SAVE_FLAG);
        super.draw(canvas);
        canvas.restore();
    }

    @Override
    public void setBackgroundColor(int color) {
        super.setBackgroundColor(color);
        this.color = color;

        if (color == white) {
            roundRect.set(1, 1, w - 1, h - 1);
        } else {
            roundRect.set(0, 0, w, h);
        }
        invalidate();
    }
}
