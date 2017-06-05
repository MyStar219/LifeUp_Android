package com.smartgateway.app.data;

import ru.johnlife.lifetools.data.AbstractData;

/**
 * Created by yanyu on 5/13/2016.
 */
public class BookingHourData extends AbstractData {
    private int startHour;
    private final String name;
    private boolean peak;
    private String status;
    private int session_id;
    private int state = 0;

    public BookingHourData(String startTime, String endTime, boolean peak, String status, int session_id) {
        this.startHour = Integer.valueOf(startTime.substring(0, 2));
        this.peak = peak;
        this.status = status;
        this.session_id = session_id;
        this.name = startTime.substring(0,5) + " - " + endTime.substring(0,5);
    }

    public BookingHourData(int h, int state) {
        this.state = state;
        this.name = String.format("%d:00 - %d:00", h, h+1);
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public boolean isPeak() {
        return peak;
    }

    public int getSession_id() {
        return session_id;
    }

    public int getId() {
        return startHour;
    }

    public String getStatus() {
        return status;
    }
}
