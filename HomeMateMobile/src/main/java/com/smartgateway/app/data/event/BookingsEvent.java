package com.smartgateway.app.data.event;

/**
 * Created by Terry on 14/7/16.
 */
public class BookingsEvent {

    private int bookings;

    public BookingsEvent(int bookings) {
        this.bookings = bookings;
    }

    public int getBookings() {
        return bookings;
    }
}
