package com.orvibo.homemate.util;

import android.content.Context;
import android.text.TextUtils;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.MessagePush;

/**
 * Created by allen on 2015/10/16.
 */
public class MessagePushUtil {

    public static String getTimeInterval(Context mContext, MessagePush messagePush) {
        String timeInterval = "";
        String startTime = messagePush.getStartTime();
        if (TextUtils.isEmpty(startTime)) {
            startTime = "00:00:00";
        }
        String endTime = messagePush.getEndTime();
        if (TextUtils.isEmpty(endTime)) {
            endTime = "00:00:00";
        }
        String[] startTimeSplit = startTime.split(":");
        String[] endTimeSplit = endTime.split(":");
        if (startTimeSplit.length > 1 && endTimeSplit.length > 1) {
            int startHour = Integer.valueOf(startTimeSplit[0]);
            int startMinute =  Integer.valueOf(startTimeSplit[1]);
            int endHour = Integer.valueOf(endTimeSplit[0]);
            int endMinute =  Integer.valueOf(endTimeSplit[1]);
            if (startHour > endHour || startHour == endHour && startMinute > endMinute) {
                timeInterval = TimeUtil.getTime(mContext, startHour, startMinute)+mContext.getString(R.string.time_interval_to)+mContext.getString(R.string.time_interval_tomorrow)+TimeUtil.getTime(mContext, endHour, endMinute);
            } else if(endHour == startHour && endMinute == startMinute) {
                timeInterval = mContext.getString(R.string.time_interval_all_day);
            } else {
                timeInterval = TimeUtil.getTime(mContext, startHour, startMinute)+mContext.getString(R.string.time_interval_to) +TimeUtil.getTime(mContext, endHour, endMinute);
            }
        }
        return timeInterval;
    }
}
