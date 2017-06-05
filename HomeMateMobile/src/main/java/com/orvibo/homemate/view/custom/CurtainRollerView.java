package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.smartgateway.app.R;
import com.orvibo.homemate.util.LogUtil;

public class CurtainRollerView extends CurtainBaseView implements OnTouchListener{
    private OnProgressChangedListener onProgressChangedListener;
    private Bitmap barBitmap;//拉杆
    //    private int barBitmapWidth;//拉杆宽
//    private int barBitmapHeight;//拉杆高
    private float barBitmapY;//拉杆Y轴位置
    private Bitmap thumbMiddleBitmap;//中间触点
    private Bitmap thumbMiddleBitmapPressed;//中间触点按下
    private Bitmap thumbUpBitmap;//触点
    private Bitmap thumbUpBitmapPressed;//触点按下
    private Bitmap thumbDownBitmap;//触点
    private Bitmap thumbDownBitmapPressed;//触点按下
    private int thumbBitmapWH;//触点宽高
    private int barThumbDistance;//拉杆顶部与触点顶部的距离
    private Bitmap progressBgBitmap;//进度背景
    private int progressBgBitmapWidth;//进度背景宽
    //    private int progressBgBitmapHeight;//进度背景高
    private int barProgressDistance;//拉杆顶部与进度背景顶部的距离
    private Paint progressPaint;//进度画笔
    private Bitmap curtainBitmap;//窗帘布
    private int curtainBitmapWidth;//窗帘布宽
    private int curtainBitmapHeight;//窗帘布高
    private int width;//this宽
    private int height;//this高
    private Rect srcCurtainRect;//窗帘布剪切范围
    private RectF dstCurtainRectF;//窗帘布绘制范围
    private float adjustmentFactor;//拉杆上下可触摸范围
    private static final int INTERVAL_TIME = 200;//移动间隔时间
    private long lastTime;

    private int startProgress;//初始进度
    private boolean isPressed = false;
    private boolean isTouchable = true;
    private boolean IS_ACTION_CONTROL = false;

    public CurtainRollerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnTouchListener(this);

        int barBitmapWidth = getResources().getDimensionPixelSize(R.dimen.curtain_roller_width);
        int barBitmapHeight = getResources().getDimensionPixelSize(R.dimen.curtain_bar_height);
        Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.curtain_dropdown_bar);
        barBitmap = Bitmap.createScaledBitmap(bitmap2, barBitmapWidth, barBitmapHeight, true);

        curtainBitmapWidth = getResources().getDimensionPixelSize(R.dimen.curtain_roller_width);
        curtainBitmapHeight = getResources().getDimensionPixelSize(R.dimen.curtain_roller_height)-barBitmapHeight;
        Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.curtain_dropdown);
        curtainBitmap = Bitmap.createScaledBitmap(bitmap1, curtainBitmapWidth, curtainBitmapHeight, true);
        srcCurtainRect = new Rect();
        dstCurtainRectF = new RectF();

        thumbMiddleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.curtain_thumb);
        thumbMiddleBitmapPressed = BitmapFactory.decodeResource(getResources(), R.drawable.curtain_thumb);
        thumbUpBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.curtain_thumb);
        thumbUpBitmapPressed = BitmapFactory.decodeResource(getResources(), R.drawable.curtain_thumb);
        thumbDownBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.curtain_thumb);
        thumbDownBitmapPressed = BitmapFactory.decodeResource(getResources(), R.drawable.curtain_thumb);
        thumbBitmapWH = thumbMiddleBitmap.getWidth();
        adjustmentFactor = thumbBitmapWH;
        barThumbDistance = thumbBitmapWH / 5 + barBitmapHeight;
        progressBgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.curtain_seekbar_2halves);
        progressBgBitmapWidth = progressBgBitmap.getWidth();
        int progressBgBitmapHeight = progressBgBitmap.getHeight();
        barProgressDistance = progressBgBitmapHeight * 11 / 10;
        progressPaint = new Paint();
        progressPaint.setTextSize(getResources().getDimension(R.dimen.text_normal));
        progressPaint.setColor(getResources().getColor(R.color.font_black));
    }

    /**
     * 设置为百叶窗
     */
    public void setAsWindowShades() {
        Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.curtain_dropdown);
        this.curtainBitmap = Bitmap.createScaledBitmap(bitmap1, curtainBitmapWidth, curtainBitmapHeight, true);
        invalidate();
    }

    /**
     * 设置为动作控制
     */
    public void setActionControl() {
        IS_ACTION_CONTROL = true;
    }

    /**
     * 设置是否可以触摸操作
     * @param touchable
     */
    public void setTouchable(boolean touchable) {
        if (touchable) {
            setOnTouchListener(this);
            isTouchable = true;
        } else {
            setOnTouchListener(null);
            isTouchable = false;
        }
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec,heightMeasureSpec);
//        width = getWidth();
//        height = getHeight();
//        if (IS_ACTION_CONTROL) {
//            setProgress(startProgress);
//        }
    }



    @Override
    protected void onDraw(Canvas canvas) {
        width = getWidth();
        height = getHeight();
        srcCurtainRect.set(0, 0, curtainBitmapWidth, (int) barBitmapY - (height - curtainBitmapHeight) / 2);
        dstCurtainRectF.set(0, (height - curtainBitmapHeight) / 2, curtainBitmapWidth, barBitmapY);
        canvas.drawBitmap(curtainBitmap, srcCurtainRect, dstCurtainRectF, null);

        canvas.drawBitmap(barBitmap, 0, barBitmapY, null);

        if (isTouchable) {
            Bitmap bitmap;
            if (isPressed) {
                if (getProgress() == 0) {
                    bitmap = thumbDownBitmapPressed;
                } else if (getProgress() == 100) {
                    bitmap = thumbUpBitmapPressed;
                } else {
                    bitmap = thumbMiddleBitmapPressed;
                }
            } else {
                if (getProgress() == 0) {
                    bitmap = thumbDownBitmap;
                } else if (getProgress() == 100) {
                    bitmap = thumbUpBitmap;
                } else {
                    bitmap = thumbMiddleBitmap;
                }
            }
            canvas.drawBitmap(bitmap, (width - thumbBitmapWH) / 2f, barBitmapY + barThumbDistance, null);

            canvas.drawBitmap(progressBgBitmap, (width - progressBgBitmapWidth) / 2f, barBitmapY - barProgressDistance, null);

            float progressTextWidth = progressPaint.measureText(getProgress() + "%");
            canvas.drawText(getProgress() + "%", (width - progressTextWidth) / 2f, barBitmapY - barProgressDistance / 2, progressPaint);
        }
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                float thumbBitmapX = (width - thumbBitmapWH) / 2f;
                float thumbBitmapY = barBitmapY + barThumbDistance + thumbBitmapWH / 2;
                float distance = (float) Math.sqrt(Math.pow((event.getX() - thumbBitmapX), 2) + Math.pow((event.getY() - thumbBitmapY), 2));//触摸点到中心的距离
                if (distance > adjustmentFactor) {//距离大于可触摸范围
                    return false;
                }
                isPressed = true;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                calculate(event.getY());
                long currentTime = System.currentTimeMillis();
                if (currentTime > lastTime + INTERVAL_TIME) {//前后移动时间超过规定间隔时间才通知进度
                    lastTime = currentTime;
                    int progress = getProgress();
                    if (onProgressChangedListener != null) {
                        onProgressChangedListener.onProgressChanged(progress);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                isPressed = false;
                calculate(event.getY());
                int progress = getProgress();
                if (onProgressChangedListener != null) {
                    onProgressChangedListener.onProgressFinish(progress);
                }
                break;
            }
        }
        return true;
    }

    /**
     * 计算拉杆位置
     *
     * @param y 触摸点Y轴坐标
     */
    private void calculate(float y) {
        barBitmapY = y - barThumbDistance - thumbBitmapWH / 2;
        if (barBitmapY > (height + curtainBitmapHeight) / 2) {//限制最下位置
            barBitmapY = (height + curtainBitmapHeight) / 2;
        } else if (barBitmapY < (height - curtainBitmapHeight) / 2) {//限制最上位置
            barBitmapY = (height - curtainBitmapHeight) / 2;
        }
        invalidate();
    }

    /**
     * 计算窗帘打开的百分比
     *
     * @return 0-100
     */
    private int getProgress() {
//        int progress = (int)(barBitmapY - (height - curtainBitmapHeight) / 2)*100 / curtainBitmapHeight;
        return 100- (int) (barBitmapY - (height - curtainBitmapHeight) / 2) * 100 / curtainBitmapHeight;
    }
    @Override
    public void setProgress(int progress) {
        startProgress = progress;
        barBitmapY = (100-progress) * curtainBitmapHeight / 100 - 2;
        invalidate();
    }

    public void setOnProgressChangedListener(OnProgressChangedListener listener) {
        onProgressChangedListener = listener;
    }


    public interface OnProgressChangedListener {
        void onProgressChanged(int progress);

        void onProgressFinish(int progress);
    }
}
