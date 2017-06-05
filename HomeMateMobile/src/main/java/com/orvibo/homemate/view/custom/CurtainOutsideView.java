package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.smartgateway.app.R;
import com.orvibo.homemate.data.DeviceStatusConstant;

/**
 * @author zhaoxiaowei
 * @date 2016/01/04
 */
public class CurtainOutsideView extends CurtainBaseView {

    private int startProgress;
    private int curtainBitmapWidth;
    private int curtainBitmapHeight;
    private Bitmap bitmapOff;
    private Bitmap bitmapOn;
    private Bitmap bitmapStop;
    private Bitmap currentBitmap;

    private Bitmap curtainBitmap;//左窗帘布

    public CurtainOutsideView(Context context, AttributeSet attrs) {
        super(context, attrs);
        curtainBitmapWidth = getResources().getDimensionPixelSize(R.dimen.curtain_outside_width);
        curtainBitmapHeight = getResources().getDimensionPixelSize(R.dimen.curtain_outside_height);
        bitmapOff = BitmapFactory.decodeResource(getResources(), R.drawable.curtain_outside_off);
        bitmapOn = BitmapFactory.decodeResource(getResources(), R.drawable.curtain_outside_on);
        bitmapStop = BitmapFactory.decodeResource(getResources(), R.drawable.curtain_outside_stop);
        currentBitmap = bitmapOff;
        //最后一个参数为false会失真
        curtainBitmap = Bitmap.createScaledBitmap(currentBitmap, curtainBitmapWidth, curtainBitmapHeight, true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setProgress(startProgress);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(curtainBitmap, 0, 0, null);
        super.onDraw(canvas);
    }


    /**
     * 根据进度百分比，绘制窗帘布
     *
     * @param progress 进度百分比0-100
     */
    public void setProgress(int progress) {
        startProgress = progress;
        if (progress == DeviceStatusConstant.CURTAIN_ON) {
            currentBitmap = bitmapOn;
            open();
        }else if (progress == DeviceStatusConstant.CURTAIN_OFF) {
            currentBitmap = bitmapOff;
            close();
        }else if (progress == DeviceStatusConstant.CURTAIN_STOP) {
            currentBitmap = bitmapStop;
            stop();
        }
        curtainBitmap = Bitmap.createScaledBitmap(currentBitmap, curtainBitmapWidth, curtainBitmapHeight, true);
        invalidate();
    }

    private void open() {
        if (startProgress == DeviceStatusConstant.CURTAIN_OFF) {
            setImageResource(R.drawable.curtain_outside_on_anim);

        }else if(startProgress == DeviceStatusConstant.CURTAIN_STOP) {
            setImageResource(R.drawable.curtain_outside_stop_to_on_anim);
        }
        //((AnimationDrawable) getDrawable()).start();
    }

    private void close() {
        if (startProgress == DeviceStatusConstant.CURTAIN_ON) {
            setImageResource(R.drawable.curtain_outside_off_anim);
        }else if(startProgress == DeviceStatusConstant.CURTAIN_STOP) {
            setImageResource(R.drawable.curtain_outside_stop_to_off_anim);
        }
        //((AnimationDrawable) getDrawable()).start();
    }

    private void stop() {
        if (startProgress == DeviceStatusConstant.CURTAIN_ON) {
            setImageResource(R.drawable.curtain_outside_on_to_stop_anim);
        } else if (startProgress == DeviceStatusConstant.CURTAIN_OFF) {
            setImageResource(R.drawable.curtain_outside_off_to_stop_anim);
        }
        //((AnimationDrawable) getDrawable()).start();
    }

}
