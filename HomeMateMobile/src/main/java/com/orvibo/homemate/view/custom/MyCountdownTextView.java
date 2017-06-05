package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.widget.TextView;

import com.orvibo.homemate.util.DateUtil;

/**
 * 倒计时TextView
 * Created by Allen on 2016/3/17.
 */
public class MyCountdownTextView extends TextView {
    private String format;
    private MyCountDownTimer            myCountDownTimer;
    private OnCountdownFinishedListener onCountdownFinishedListener;

    public MyCountdownTextView(Context context) {
        super(context);
    }

    public MyCountdownTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MyCountdownTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void startCountdown(String format, int time) {
        this.format = format;
        myCountDownTimer = new MyCountDownTimer(time * 1000, 1000);
        myCountDownTimer.start();
    }

    public void stopCountdown(String time) {
        setText(time);
        stopCountdown();
    }

    public void stopCountdown() {
        if (myCountDownTimer != null) {
            myCountDownTimer.cancel();
        }
    }

    /**
     * 继承 CountDownTimer 防范
     * <p/>
     * 重写 父类的方法 onTick() 、 onFinish()
     */

    class MyCountDownTimer extends CountDownTimer {
        /**
         * @param millisInFuture    表示以毫秒为单位 倒计时的总数
         *                          <p/>
         *                          例如 millisInFuture=1000 表示1秒
         * @param countDownInterval 表示 间隔 多少微秒 调用一次 onTick 方法
         *                          <p/>
         *                          例如: countDownInterval =1000 ; 表示每1000毫秒调用一次onTick()
         */
        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            String remainTime = DateUtil.getCountdownStringWithoutZeroBySecTime((int) (0 / 1000));
            if (format != null) {
                setText(String.format(format, remainTime));
            } else {
                setText(remainTime);
            }
            if (onCountdownFinishedListener != null) {
                onCountdownFinishedListener.onCountdownFinished();
            }
        }

        @Override
        public void onTick(long millisUntilFinished) {
            String remainTime = DateUtil.getCountdownStringWithoutZeroBySecTime((int) (millisUntilFinished / 1000));
            if (format != null) {
                setText(String.format(format, remainTime));
            } else {
                setText(remainTime);
            }

        }
    }

    public interface OnCountdownFinishedListener {
        void onCountdownFinished();
    }

    public void registerCountdownFinishedListener(OnCountdownFinishedListener onCountdownFinishedListener) {
        this.onCountdownFinishedListener = onCountdownFinishedListener;
    }

    public void unRegisterCountdownFinishedListener() {
        this.onCountdownFinishedListener = null;
    }
}

