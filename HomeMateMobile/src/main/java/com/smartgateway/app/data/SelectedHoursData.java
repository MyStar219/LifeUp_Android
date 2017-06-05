package com.smartgateway.app.data;

import java.util.ArrayList;
import java.util.List;

import ru.johnlife.lifetools.data.AbstractData;

/**
 * Created by yanyu on 5/14/2016.
 */
public class SelectedHoursData extends AbstractData {
    public static final String format = "%d:00 - %d:00, ";
    List<BookingHourData> hours = new ArrayList<>();

    public void add(BookingHourData hour) {
        hours.add(hour);
    }

    public boolean isEmpty() {
        return hours.isEmpty();
    }

    @Override
    public String toString() {
        //TODO: sort
        int start = -1;
        int stop = -1;
        StringBuilder b = new StringBuilder();
        for (BookingHourData hour : hours) {
            int hr = hour.getId();
            if (start == -1) {
                start = hr;
            } else if (stop == -1) {
                if (hr == start + 1) {
                    stop = hr +1;
                } else {
                    b.append(String.format(format, start, start+1));
                    start = hr;
                }
            } else {
                if (hr == stop) {
                    stop = hr +1;
                    continue;
                } else {
                    b.append(String.format(format, start, stop));
                    start = hr;
                    stop = -1;
                }
            }
        }
        if (stop != -1) {
            b.append(String.format(format, start, stop));
        } else if (start != -1) {
            b.append(String.format(format, start, start+1));
        }
        b.delete(b.length()-2, b.length());
        return b.toString();
    }
}
