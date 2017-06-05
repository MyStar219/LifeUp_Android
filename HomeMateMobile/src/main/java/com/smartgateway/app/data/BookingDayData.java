package com.smartgateway.app.data;

import android.util.Log;

import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.Utils.Constants.State;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import ru.johnlife.lifetools.data.AbstractData;
import ru.johnlife.lifetools.tools.DateUtil;

/**
 * Created by yanyu on 5/13/2016.
 */
public class BookingDayData extends AbstractData {

    private static final Calendar tmp = Calendar.getInstance();

    private long timestamp;
    private String number;
    private int state = -1;

    public BookingDayData(long timestamp, List<String> dates, List<String> states) {
        this.timestamp = DateUtil.getBeginOfTheDay(timestamp);
        synchronized (tmp) {
            tmp.setTimeInMillis(this.timestamp);
            number = String.valueOf(tmp.get(Calendar.DAY_OF_MONTH));
            for (int i=0; i<dates.size(); i++) {
                String date = dates.get(i).substring(dates.get(i).lastIndexOf("-") + 1);
                if (tmp.get(Calendar.DAY_OF_MONTH) == Integer.valueOf(date)) {
                    state = getState(states.get(i));
                }
            }
        }
    }

    public int getState(String state) {
        if (state.equals("available")) {
            return State.AVAILABLE;
        } else if (state.equals("my_booking")) {
            return State.MY_BOOKING;
        } else if (state.equals("partially_blocked")) {
            return State.PARTIALLY_BLOCKED;
        } else if (state.equals("partially_booked")) {
            return State.PARTIALLY_BOOKED;
        } else if (state.equals("fully_blocked")) {
            return State.PARTIALLY_BLOCKED;
        } else if (state.equals("fully_booked")) {
            return State.PARTIALLY_BOOKED;
        } else if (state.equals("blocked")) {
            return State.BLOCKED;
        } else if (state.equals("booked")) {
            return State.BOOKED;
        }
        return 0;
    }

    public String getNumber() {
        return number;
    }

    public int getState() {
        if (state == -1) state = 0;
        return state;
    }

    public int getColorTag(long now) {

        return now > timestamp ? 0 : getState();
    }

    public String getDisplayName() {
        return Constants.dateFormat.format(new Date(timestamp));
    }

}
