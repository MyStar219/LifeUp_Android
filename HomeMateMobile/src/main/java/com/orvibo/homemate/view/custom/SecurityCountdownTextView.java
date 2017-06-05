package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Countdown;
import com.orvibo.homemate.util.LogUtil;

/**
 * Created by baoqi on 2016/3/8.
 */
public class SecurityCountdownTextView extends CountdownTextView {
    private final static String TAG=SecurityCountdownTextView.class.getSimpleName();
    private Context mContext;
    private Countdown mCountdown;
    private MyCountDownTimer myCountDownTimer;
    private OnSecurityCountdownFinishedListener onSecurityCountdownFinishedListener;
    private boolean isCountdownStarted = false;
    private boolean isFinish = true;

    public SecurityCountdownTextView(Context context) {
        this(context, null);
    }

    public SecurityCountdownTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public void startCountdown(int time) {
        LogUtil.d(TAG,"startCountdown() time="+time);
        isCountdownStarted = true;
        isFinish = false;
        myCountDownTimer = new MyCountDownTimer(time * 1000, 1000);
//        LogUtil.d("CountdownTextView", "startCountdown() - time = " + time);
        myCountDownTimer.start();
    }

    public void stopCountdown(String time) {
        isCountdownStarted = false;
        isFinish = true;
        setText(time);
        if (myCountDownTimer != null) {
            myCountDownTimer.cancel();
        }
    }

    public void stopCountdown() {
        isCountdownStarted = false;
        isFinish = true;
        setText("");
        if (myCountDownTimer != null) {
            myCountDownTimer.cancel();
        }
    }

    public boolean isCountdownStarted() {
        return isCountdownStarted;
    }

    public boolean isFinish() {
        return isFinish;
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
            isCountdownStarted = false;
            isFinish = true;
            setText("");
            if (onSecurityCountdownFinishedListener != null) {
                onSecurityCountdownFinishedListener.onSecurityCountdownFinished();
            }
        }

        @Override
        public void onTick(long millisUntilFinished) {
            // if (mCountdown != null) {
            //String remainTime = DateUtil.getCountdownStringBySecTime((int) (millisUntilFinished / 1000));
//                LogUtil.d("CountdownTextView", "getView() - millisUntilFinished = " + millisUntilFinished/1000 + " remainTime = " + remainTime);
            setText((millisUntilFinished / 1000) + mContext.getResources().getString(R.string.intelligent_scene_security_tips2));
            // }
        }
    }

    public interface OnSecurityCountdownFinishedListener {
        void onSecurityCountdownFinished();
    }

    public void registerSecurityCountdownFinishedListener(OnSecurityCountdownFinishedListener onSecurityCountdownFinishedListener) {
        this.onSecurityCountdownFinishedListener = onSecurityCountdownFinishedListener;
    }

    public void unRegisterSecurityCountdownFinishedListener() {
        this.onSecurityCountdownFinishedListener = null;
    }
}
