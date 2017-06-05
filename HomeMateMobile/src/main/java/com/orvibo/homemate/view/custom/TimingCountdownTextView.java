package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Countdown;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.Timing;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.dao.CountdownDao;
import com.orvibo.homemate.dao.TimingDao;
import com.orvibo.homemate.data.CountdownConstant;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.data.TimingConstant;
import com.orvibo.homemate.util.DateUtil;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.TimeUtil;
import com.orvibo.homemate.util.TimingCountdownUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 显示最近执行的定时或者倒计时TextView
 * Created by zhaoxiaowei on 2016/2/25.
 */
public class TimingCountdownTextView extends TextView {

    private static final String TAG = TimingCountdownTextView.class.getSimpleName();
    private Countdown mCountdown;
    private MyCountDownTimer myCountDownTimer;
    private Context mContext;
    private OnSetLatestTypeListener onSetLatestTypeListener;
    private Device mDevice;
    private String actionString;

    public TimingCountdownTextView(Context context) {
        super(context);
        mContext = context;
    }

    public TimingCountdownTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    public TimingCountdownTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public void startCountdown(Countdown countdown) {
        mCountdown = countdown;
        int time = DateUtil.getCountdownInt(mCountdown.getStartTime(), mCountdown.getTime());
        if (myCountDownTimer != null) {
            myCountDownTimer.cancel();
        }
        myCountDownTimer = new MyCountDownTimer(time * 1000, 1000);
//        LogUtil.d("CountdownTextView", "startCountdown() - time = " + time);
        myCountDownTimer.start();

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
            setText("00:00:00");
            LogUtil.d(TAG, "onFinish()");
            setTiming(mDevice);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (mCountdown != null) {
                String remainTime = DateUtil.getCountdownStringBySecTime((int) (millisUntilFinished / 1000));
                LogUtil.d(TAG, "onTick() - millisUntilFinished = " + millisUntilFinished/1000 + " remainTime = " + remainTime);
                setText(remainTime + actionString);
            }
        }
    }

    public void refresh(Device device) {
        setTiming(device);
        mDevice = device;
        postDelayed(new Runnable() {
            @Override
            public void run() {
                setTiming(mDevice);
            }
        }, 2000);
    }

    /**
     * 显示最近的定时或者倒计时
     */
    private void setTiming(Device device) {
        List<Timing> timings = new TimingDao().selTimingsByDevice(device.getUid(), device.getDeviceId());
        List<Countdown> countdowns = new CountdownDao().selCountdownsByDevice(device.getUid(), device.getDeviceId());
        List<Timing> effectTimings = new ArrayList<Timing>();
        List<Countdown> effectCountdowns = new ArrayList<Countdown>();
        //筛选有效的定时
        for (Timing timing : timings) {
            if (timing.getIsPause() == TimingConstant.TIMEING_EFFECT) {
                effectTimings.add(timing);
            }
        }
        //筛选有效的倒计时
        for (Countdown countdown : countdowns) {
            long countdownInMillis = TimingCountdownUtil.getLatestCountdownInMills(countdown);
            if (countdown.getIsPause() == CountdownConstant.COUNTDOWN_EFFECT && countdownInMillis > System.currentTimeMillis()) {
                effectCountdowns.add(countdown);
            }
        }
        Timing latestTiming = null;
        Countdown latestCountdown = null;
        long min = Long.MAX_VALUE;
        final int count1 = effectTimings.size();
        for (int i = 0; i < count1; i++) {
            Timing timing = effectTimings.get(i);
            long timeInMillis = TimingCountdownUtil.getLatestTimeInMills(timing);
            if (timeInMillis < min) {
                min = timeInMillis;
                //LogUtil.d(TAG, "setTiming()-timeInMillis:" + timeInMillis);
                latestTiming = timing;
            }
        }
        final int count2 = effectCountdowns.size();
        for (int i = 0; i < count2; i++) {
            Countdown countdown = effectCountdowns.get(i);
            long countdownInMillis = TimingCountdownUtil.getLatestCountdownInMills(countdown);
            if (countdownInMillis < min) {
                min = countdownInMillis;
                //LogUtil.d(TAG, "setTiming()-countdownInMillis:" + countdownInMillis);
                latestCountdown = countdown;
            }
        }
        setVisibility(View.VISIBLE);
        if (latestCountdown != null) {
            String remainTime = DateUtil.getRemainCountdownString(latestCountdown.getStartTime(), latestCountdown.getTime());
            String tempAction = mContext.getResources().getString(R.string.device_countdown_action_content);
            actionString = String.format(tempAction, DeviceTool.getActionName(mContext, latestCountdown));
            setText(remainTime + actionString);
            startCountdown(latestCountdown);
            onSetLatestTypeListener.onSetLatestType(device.getDeviceId(), TimingCountdownTabView.COUNTDOWN_POSITION);
        } else {
            stopCountdown();
            onSetLatestTypeListener.onSetLatestType(device.getDeviceId(), TimingCountdownTabView.TIMING_POSITION);
            if (latestTiming == null) {
                if (ProductManage.getInstance().isWifiDevice(device)) {
                    setText(R.string.device_timing_countdown_add);
                } else {
                    setText(R.string.device_timing_add);
                }
            } else {
                String order = latestTiming.getCommand().equals(DeviceOrder.ON) ? mContext.getString(R.string.timing_action_on) : mContext.getString(R.string.timing_action_off);
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(min);
                String formatString;
                int format = DateUtil.getTimingDateFormat(calendar);
//                LogUtil.d(TAG, "setTiming()-latestTiming:" + latestTiming);
//                LogUtil.d(TAG, "setTiming()-min:" + min + ",format:" + format);
                if (format == 0) {
                    formatString = TimeUtil.getTime(mContext, latestTiming.getHour(), latestTiming.getMinute());
                } else if (format == 1) {
                    formatString = mContext.getString(R.string.timing_tomorrow) + TimeUtil.getTime(mContext, latestTiming.getHour(), latestTiming.getMinute());
                } else if (format == 2) {
                    formatString = mContext.getString(R.string.timing_thedayaftertomorrow) + TimeUtil.getTime(mContext, latestTiming.getHour(), latestTiming.getMinute());
                } else {
                    formatString = (calendar.get(Calendar.MONTH) + 1) + "." + calendar.get(Calendar.DAY_OF_MONTH) + " " + TimeUtil.getTime(mContext, latestTiming.getHour(), latestTiming.getMinute());
                }
                setText(formatString + " " + order);
            }
        }
    }

    public interface OnSetLatestTypeListener {
        void onSetLatestType(String deviceId, int tabViewType);
    }

    public void registerSetLatestTypeListener(OnSetLatestTypeListener onSetLatestTypeListener) {
        this.onSetLatestTypeListener = onSetLatestTypeListener;
    }
}

