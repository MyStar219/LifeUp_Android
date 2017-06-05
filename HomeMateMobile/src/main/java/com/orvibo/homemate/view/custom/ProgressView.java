package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.util.LogUtil;

/**
 * create by baoqi
 */
public class ProgressView extends LinearLayout {
    private String Tag = "ProgressView";
    private double mMax = 100;
    private double mProgress = 0;
    private double bfProgress_above, bfProgress_bottom;
    private boolean isProgressEnable = false;
    private Context mContext;
    private MyCountDownTimer mCountDownTimer;
    private OnProgressFinishListener mListener;
    private int cir_time;
    private int line_time;
    private Paint paint;
    private int padding = 4;
    private float width;
    private float heigth;
    private float r;
    private float lineLength;
    private float cirperimeter;
    private float perimeter;

    /**
     * 设置进度的最大值
     */
    public void setMax(long max) {
        mMax = max;
    }

    /**
     * 计算时间
     *
     * @param max
     * @param cirperimeter
     * @param perimeter
     */
    private void compute(double max, float cirperimeter, float perimeter) {
        cir_time = (int) (max * (cirperimeter / perimeter) + 0.5f);
        line_time = (int) (max * (perimeter - cirperimeter) / perimeter + 0.5f);
        bfProgress_above = cir_time / 4 + line_time / 2;
        bfProgress_bottom = bfProgress_above + cir_time / 2 + line_time / 2;

    }

    /**
     * 设置进度的最当前值
     */
    public void setProgress(double progress) {
        setProgressEnable(true);
        mProgress = progress;
        // mProgress++;
        // 重绘操作
        invalidate();
    }

    /**
     * 启动进度
     */

    public void startProgress(int time) {
        setProgressEnable(true);
        mMax = time;
        mCountDownTimer = new MyCountDownTimer(time * 1000, 1000);
//        LogUtil.d("CountdownTextView", "startCountdown() - time = " + time);
        mCountDownTimer.start();
    }

    /**
     * 取消进度
     */
    public void cancleProgress() {
        setProgress(0);
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }

    /**
     * 设置是否允许进度
     */
    public void setProgressEnable(boolean isProgressEnable) {
        this.isProgressEnable = isProgressEnable;
    }

    private ImageView mIvIcon;
    private TextView mTvNote;
    private float startPoint_above;
    private float endPoint_above;
    private float startPoint_bottom;
    private float endPoint_bottom;

    /**
     * 设置图标
     */
    public void setIcon(int resId) {
        mIvIcon.setImageResource(resId);
    }

    /**
     * 设置文本
     */
    public void setText(String content) {
        mTvNote.setText(content);
    }

    public void setText(int res) {
        mTvNote.setText(res);
    }

    public ProgressView(Context context) {
        this(context, null);
    }

    public ProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 挂载布局
        View view = View.inflate(context, R.layout.progressview, this);
        // 找出孩子对象
        mIvIcon = (ImageView) view.findViewById(R.id.ivIcon);
        mTvNote = (TextView) view.findViewById(R.id.tvText);
        mContext = context;

    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);// (绘制背景)
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {

        super.dispatchDraw(canvas);// 图标+文字
        if (isProgressEnable) {
            paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Style.STROKE);
            paint.setStrokeWidth(6);
            paint.setAntiAlias(true);// 消除锯齿
            // 画弧
            width = mIvIcon.getRight() - mIvIcon.getLeft();
            heigth = mIvIcon.getBottom() - mIvIcon.getTop();
            r = (heigth / 2);
            lineLength = width - 2 * r;
            cirperimeter = (float) (2 * Math.PI * r);
            //控件的周长
            perimeter = (float) ((2 * Math.PI * r) + 2 * lineLength);
            //  LogUtil.d(Tag, "控件的周长（perimeter）：" + cirperimeter);
            //初始化上下直线的起点
            startPoint_above = mIvIcon.getLeft() + r;
            startPoint_bottom = mIvIcon.getRight() - r;
//            endPoint_above = (float) (startPoint_above + (1 / mMax) * perimeter);
//            endPoint_bottom = (float) (startPoint_bottom - (1 / mMax) * perimeter);
            compute(mMax, cirperimeter, perimeter);
            //RectF oval = new RectF(mIvIcon.getLeft(), mIvIcon.getTop(), mIvIcon.getRight(), mIvIcon.getBottom());// 控制圆弧显示范围
            //1/4圆弧
            float start1 = (float) ((mProgress) / mMax);
            float end1 = (float) ((cirperimeter / 4) / perimeter);
            if (start1 < end1) {
                RectF oval = new RectF(mIvIcon.getLeft() + padding, mIvIcon.getTop() + padding, mIvIcon.getLeft() + 2 * r - padding, mIvIcon.getTop() + 2 * r - padding);// 控制圆弧显示范围
                float startAngle = -180;
                // 32是一个估值
                float sweepAngle = (float) ((mProgress) / cir_time * 360);// 动态计算
                boolean useCenter = false;
                canvas.drawArc(oval, startAngle, sweepAngle, useCenter, paint);//其实这个接口是
                //bug 9259
//                startPoint = mIvIcon.getLeft() + r;
//                endPoint = (float) (startPoint + (1 / mMax) * perimeter);
                //上直线
            } else if ((float) (mProgress / mMax) > ((cirperimeter / 4) / perimeter) && ((mProgress) / mMax) < (((cirperimeter / 4) + lineLength) / perimeter)) {
                RectF oval = new RectF(mIvIcon.getLeft() + padding, mIvIcon.getTop() + padding, mIvIcon.getLeft() + 2 * r - padding, mIvIcon.getTop() + 2 * r - padding);// 控制圆弧显示范围
                float startAngle = -180;
                float sweepAngle = (float) 90;// 动态计算
                boolean useCenter = false;
                canvas.drawArc(oval, startAngle, sweepAngle, useCenter, paint);
                LogUtil.d(Tag, "上直线：startPoint_above=***********************" + startPoint_above + "**************************");
                LogUtil.d(Tag, "上直线：endPoint_above=***********************" + endPoint_above + "**************************");
                canvas.drawLine(startPoint_above, mIvIcon.getTop() + padding, endPoint_above, mIvIcon.getTop() + padding, paint);
                // endPoint_above = (float) (endPoint_above + (1 / mMax) * perimeter);
                // bfProgress = mProgress;
                // bfProgress = 21;
                //同下
                // bfProgress_above = cir_time / 4 + line_time / 2;
                //1/2圆弧 //+1（最后超出圆弧范围，进度这里慢了）
            } else if ((float) (mProgress / mMax) > (((cirperimeter / 4) + lineLength) / perimeter) && (float) ((mProgress + 1) / mMax) < (((3 * cirperimeter / 4) + lineLength) / (perimeter))) {
                LogUtil.d(Tag, "上直线：bfProgress=***********************" + bfProgress_above + "**************************");

                RectF oval1 = new RectF(mIvIcon.getLeft() + padding, mIvIcon.getTop() + padding, mIvIcon.getLeft() + 2 * r - padding, mIvIcon.getTop() + 2 * r - padding);// 控制圆弧显示范围
                float startAngle1 = -180;
                float sweepAngle1 = (float) 90;// 动态计算
                boolean useCenter1 = false;
                canvas.drawArc(oval1, startAngle1, sweepAngle1, useCenter1, paint);

                canvas.drawLine(mIvIcon.getLeft() + r, mIvIcon.getTop() + padding, mIvIcon.getLeft() + r + lineLength, mIvIcon.getTop() + padding, paint);

                RectF oval = new RectF(mIvIcon.getRight() - 2 * r + padding, mIvIcon.getTop() + padding, mIvIcon.getRight() - padding, mIvIcon.getTop() + 2 * r - padding);// 控制圆弧显示范围
                float startAngle = -90;
                float sweepAngle = (float) ((mProgress - bfProgress_above) / cir_time * 360);// 动态计算
                boolean useCenter = false;
                canvas.drawArc(oval, startAngle, sweepAngle, useCenter, paint);
                //bug 9259 同下
//                startPoint = mIvIcon.getRight() - r;
//                endPoint = (float) (startPoint - (1 / mMax) * perimeter);
                //下直线
            } else if ((float) ((mProgress + 1) / mMax) > (((3 * cirperimeter / 4) + lineLength) / (perimeter)) && (float) (mProgress / mMax) < (((3 * cirperimeter / 4) + 2 * lineLength) / perimeter)) {

                RectF oval1 = new RectF(mIvIcon.getLeft() + padding, mIvIcon.getTop() + padding, mIvIcon.getLeft() + 2 * r - padding, mIvIcon.getTop() + 2 * r - padding);// 控制圆弧显示范围
                float startAngle1 = -180;
                float sweepAngle1 = (float) 90;// 动态计算
                boolean useCenter1 = false;
                canvas.drawArc(oval1, startAngle1, sweepAngle1, useCenter1, paint);

                canvas.drawLine(mIvIcon.getLeft() + r, mIvIcon.getTop() + padding, mIvIcon.getLeft() + r + lineLength, mIvIcon.getTop() + padding, paint);

                RectF oval = new RectF(mIvIcon.getRight() - 2 * r + padding, mIvIcon.getTop() + padding, mIvIcon.getRight() - padding, mIvIcon.getTop() + 2 * r - padding);// 控制圆弧显示范围
                float startAngle = -90;
                float sweepAngle = (float) 180;// 动态计算
                boolean useCenter = false;
                canvas.drawArc(oval, startAngle, sweepAngle, useCenter, paint);
                LogUtil.d(Tag, "下直线：startPoint_bottom=***********************" + startPoint_bottom + "**************************");
                LogUtil.d(Tag, "下直线：endPoint_bottom=***********************" + endPoint_bottom + "**************************");
                canvas.drawLine(startPoint_bottom, mIvIcon.getBottom() - padding, endPoint_bottom, mIvIcon.getBottom() - padding, paint);
//                endPoint_bottom = (float) (endPoint_bottom - (1 / mMax) * perimeter);
                //bug 9259 如果item隐藏了 progressView就不会绘制了，即不会进入这个条件内，所以bfProgress_bottom就不会赋值
                //bfProgress_bottom = bfProgress_above + cir_time / 2 + line_time / 2;
                //1/4圆弧
            } else {
                LogUtil.d(Tag, "下直线 ：bfProgress=***********************" + bfProgress_bottom + "**************************");
                RectF oval1 = new RectF(mIvIcon.getLeft() + padding, mIvIcon.getTop() + padding, mIvIcon.getLeft() + 2 * r - padding, mIvIcon.getTop() + 2 * r - padding);// 控制圆弧显示范围
                float startAngle1 = -180;
                float sweepAngle1 = (float) 90;// 动态计算
                boolean useCenter1 = false;
                canvas.drawArc(oval1, startAngle1, sweepAngle1, useCenter1, paint);

                canvas.drawLine(mIvIcon.getLeft() + r, mIvIcon.getTop() + padding, mIvIcon.getLeft() + r + lineLength, mIvIcon.getTop() + padding, paint);

                RectF oval2 = new RectF(mIvIcon.getRight() - 2 * r + padding, mIvIcon.getTop() + padding, mIvIcon.getRight() - padding, mIvIcon.getTop() + 2 * r - padding);// 控制圆弧显示范围
                float startAngle2 = -90;
                float sweepAngle2 = (float) 180;// 动态计算
                boolean useCenter2 = false;
                canvas.drawArc(oval2, startAngle2, sweepAngle2, useCenter2, paint);

                canvas.drawLine(mIvIcon.getRight() - r, mIvIcon.getBottom() - padding, mIvIcon.getLeft() + r, mIvIcon.getBottom() - padding, paint);

                RectF oval = new RectF(mIvIcon.getLeft() + padding, mIvIcon.getTop() + padding, mIvIcon.getLeft() + 2 * r - padding, mIvIcon.getTop() + 2 * r - padding);// 控制圆弧显示范围
                float startAngle = -270;
                float sweepAngle = (float) ((mProgress - bfProgress_bottom + 1) / cir_time * 360);// 动态计算
                boolean useCenter = false;
                canvas.drawArc(oval, startAngle, sweepAngle, useCenter, paint);
            }

        }
    }


    /**
     * 继承 CountDownTimer 防范
     * <p/>
     * 重写 父类的方法 onTick() 、 onFinish()
     */

    class MyCountDownTimer extends CountDownTimer {

        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            //setText("");
            setProgress(0);
            mListener.onProgressFinish();
            setProgressEnable(false);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (mMax == (millisUntilFinished / 1000) + 1) {
                endPoint_above = (float) (startPoint_above + (1 / mMax) * perimeter);
                endPoint_bottom = (float) (startPoint_bottom - (1 / mMax) * perimeter);
            }
            if ((float) (mProgress / mMax) > ((cirperimeter / 4) / perimeter) && ((mProgress) / mMax) < (((cirperimeter / 4) + lineLength) / perimeter)) {
                endPoint_above = (float) (endPoint_above + (1 / mMax) * perimeter);
            }
            if ((float) ((mProgress + 1) / mMax) > (((3 * cirperimeter / 4) + lineLength) / (perimeter)) && (float) (mProgress / mMax) < (((3 * cirperimeter / 4) + 2 * lineLength) / perimeter)) {
                endPoint_bottom = (float) (endPoint_bottom - (1 / mMax) * perimeter);
            }
            // setText((millisUntilFinished / 1000) + mContext.getResources().getString(R.string.intelligent_scene_security_tips2));
            setProgress(mMax - (millisUntilFinished / 1000));
            LogUtil.d(Tag, "progress:=============" + (mMax - (millisUntilFinished / 1000)) + "================");
        }
    }

    public interface OnProgressFinishListener {
        void onProgressFinish();
    }

    public void setOnProgressFinishListener(OnProgressFinishListener listener) {
        mListener = listener;
    }

}
