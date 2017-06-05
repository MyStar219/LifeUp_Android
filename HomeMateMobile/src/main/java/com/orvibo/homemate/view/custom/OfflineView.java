package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by huangqiyao on 2015/4/14.
 */
public class OfflineView extends TextView {
    public OfflineView(Context context) {
        super(context);
    }

    public OfflineView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OfflineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public OfflineView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //倾斜度45,上下左右居中
//        canvas.rotate(-45, 0, 0);
        canvas.rotate(-45, getMeasuredWidth() /2, getMeasuredHeight() / 2);
        canvas.translate((float) (getMeasuredWidth() / 2 * Math.asin(45 / 360 * 2 * Math.PI)), (float) (getMeasuredWidth() / 2 * Math.asin(45 / 360 * 2 * Math.PI)));
        super.onDraw(canvas);
    }
}
