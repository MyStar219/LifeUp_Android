package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.smartgateway.app.R;

public class Curtain2HalvesInsideView extends CurtainBaseView {
    private Bitmap leftCurtainBitmap;//左窗帘布
    private int leftCurtainX;//左窗帘布X轴坐标
    private Bitmap rightCurtainBitmap;//右窗帘布
    private int rightCurtainX;//右窗帘布X轴坐标
    private int curtainBitmapHeight;//窗帘布高
    private int width;//this宽
    private int height;//this高
    private int startProgress;

    public Curtain2HalvesInsideView(Context context, AttributeSet attrs) {
        super(context, attrs);

        int curtainBitmapWidth = getResources().getDimensionPixelSize(R.dimen.curtain_2halves_width) / 2;
        curtainBitmapHeight = getResources().getDimensionPixelSize(R.dimen.curtain_2halves_height);
        Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.curtain_2halves);
        //最后一个参数为false会失真
        leftCurtainBitmap = Bitmap.createScaledBitmap(bitmap1, curtainBitmapWidth, curtainBitmapHeight, true);
        rightCurtainBitmap = Bitmap.createScaledBitmap(bitmap1, curtainBitmapWidth, curtainBitmapHeight, true);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getWidth();
        height = getHeight();
        setProgress(startProgress);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        width = getWidth();
        height = getHeight();
        canvas.drawBitmap(leftCurtainBitmap, leftCurtainX, (height - curtainBitmapHeight) / 2, null);
        canvas.drawBitmap(rightCurtainBitmap, rightCurtainX, (height - curtainBitmapHeight) / 2, null);
        super.onDraw(canvas);
    }

    /**
     * 根据进度百分比，绘制窗帘布
     *
     * @param progress 进度百分比0-100
     */
    public void setProgress(int progress) {
        startProgress = progress;
        leftCurtainX = -progress * (width - 63) / 200;
        rightCurtainX = width / 2 + progress * (width - 63) / 200;
        invalidate();
    }

}
