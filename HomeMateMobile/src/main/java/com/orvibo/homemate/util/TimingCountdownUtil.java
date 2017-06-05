package com.orvibo.homemate.util;

import com.orvibo.homemate.bo.Countdown;
import com.orvibo.homemate.bo.Timing;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaoxiaowei on 2016/2/25.
 */
public class TimingCountdownUtil {

    private static final String TAG = TimingCountdownUtil.class.getSimpleName();

    /**
     * 有些定时可能是需要重复执行的定时，计算现在以后每次定时的时间，选择距离现在最近一次
     *
     * @param timing
     * @return
     */
    public static long getLatestTimeInMills(Timing timing) {
        long timeInMillis = getTimeInMillis(timing);
        //LogUtil.d(TAG, "getLatestTimeInMills()-timeInMillis:" + timeInMillis);
        int week = timing.getWeek();
        Map<Integer, Integer> weekMap = WeekUtil.weekIntToMap(week);
        if (weekMap.get(0) > 0) {//重复执行的定时
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, timing.getHour());
            calendar.set(Calendar.MINUTE, timing.getMinute());
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            //转化为自定义的星期
            if (dayOfWeek == 1) {
                dayOfWeek = 7;
            } else {
                dayOfWeek = dayOfWeek - 1;
            }
            long now = System.currentTimeMillis();
            long timingTodayTimeMillis = calendar.getTimeInMillis();
            List<Long> weekTimeInMillis = new ArrayList<Long>();
            for (int j = 1; j < 7 + 1; j++) {
                long time;
                Integer integer = weekMap.get(j);
                if (integer != null) {
                    int deltaDay = 0;
                    if (integer > dayOfWeek) {
                        deltaDay = integer - dayOfWeek;
                    } else if (integer == dayOfWeek) {
                        if (now > timingTodayTimeMillis) {
                            deltaDay = integer + 7 - dayOfWeek;
                        }
                    } else {
                        deltaDay = integer + 7 - dayOfWeek;
                    }
                    time = timingTodayTimeMillis + deltaDay * 24 * 60 * 60 * 1000;
                    weekTimeInMillis.add(time);
                }
            }
            //LogUtil.d(TAG, "getLatestTimeInMills()-weekTimeInMillis:" + weekTimeInMillis);
            long min = Long.MAX_VALUE;
            final int size = weekTimeInMillis.size();
            for (int k = 0; k < size; k++) {
                long wTimeInMillis = weekTimeInMillis.get(k);
                if (wTimeInMillis < min) {
                    min = wTimeInMillis;
                    timeInMillis = wTimeInMillis;
                }
            }
        }
        return timeInMillis;
    }

    /**
     * 获取最近的一次倒计时执行时间
     *
     * @param countdown
     * @return
     */
    public static long getLatestCountdownInMills(Countdown countdown) {
        long countdownInMillis = ((long) (countdown.getStartTime() + countdown.getTime() * 60)) * 1000;
        //LogUtil.d(TAG, "getLatestCountdownInMills()-countdownInMillis:" + countdownInMillis);
        return countdownInMillis;
    }

    /**
     * 获取毫秒
     */
    public static long getTimeInMillis(Timing timing) {
        long now = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, timing.getHour());
        calendar.set(Calendar.MINUTE, timing.getMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long time = calendar.getTimeInMillis();
       // LogUtil.d(TAG, "getTimeInMillis()-time:" + time + ",now:" + now);
        if (time < now) {
            time += 24 * 60 * 60 * 1000;
        }
        return time;
    }
}
