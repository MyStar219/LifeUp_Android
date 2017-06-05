package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.widget.TextView;

import com.orvibo.homemate.bo.Countdown;
import com.orvibo.homemate.util.DateUtil;
import com.orvibo.homemate.util.LogUtil;

/**
 * 倒计时专用TextView
 * Created by zhaoxiaowei on 2016/2/19.
 */
public class CountdownTextView extends TextView {

    private Countdown                   mCountdown;
    private MyCountDownTimer            myCountDownTimer;
    private OnCountdownFinishedListener onCountdownFinishedListener;

    public CountdownTextView(Context context) {
        super(context);
    }

    public CountdownTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CountdownTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void startCountdown(Countdown countdown) {
        mCountdown = countdown;
        int time = DateUtil.getCountdownInt(mCountdown.getStartTime(), mCountdown.getTime());
        myCountDownTimer = new MyCountDownTimer(time * 1000, 1000);
//        LogUtil.d("CountdownTextView", "startCountdown() - time = " + time);
        myCountDownTimer.start();
    }

    public void startCountdown(int time) {
        myCountDownTimer = new MyCountDownTimer(time * 1000, 1000);
//        LogUtil.d("CountdownTextView", "startCountdown() - time = " + time);
        myCountDownTimer.start();
    }

    public void stopCountdown(String time) {
        setText(time);
        if (myCountDownTimer != null) {
            myCountDownTimer.cancel();
        }
    }

    public void stopCountdown() {
        setText("");
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
            setText("00:00:00");
            onCountdownFinishedListener.onCountdownFinished();
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (mCountdown != null) {
                String remainTime = DateUtil.getCountdownStringBySecTime((int) (millisUntilFinished / 1000));
               // LogUtil.d("CountdownTextView", "getView() - millisUntilFinished = " + millisUntilFinished / 1000 + " remainTime = " + remainTime);
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

