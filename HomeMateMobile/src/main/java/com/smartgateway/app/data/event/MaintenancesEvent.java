package com.smartgateway.app.data.event;

/**
 * Created by Terry on 14/7/16.
 */
public class MaintenancesEvent {
    private int count;

    public MaintenancesEvent(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }
}
