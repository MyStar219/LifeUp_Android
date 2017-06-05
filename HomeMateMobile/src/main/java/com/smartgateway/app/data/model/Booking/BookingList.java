package com.smartgateway.app.data.model.Booking;

import java.util.List;

/**
 * Created by Terry on 1/7/16.
 */
public class BookingList {

    /**
     * date : 2016-07-03
     * booking_id : 85
     * state : reserved
     * time : 09:00 - 11:00
     * facility : Tennis Court 1
     */

    private List<BookingsBean> Bookings;

    public List<BookingsBean> getBookings() {
        return Bookings;
    }

    public void setBookings(List<BookingsBean> Bookings) {
        this.Bookings = Bookings;
    }

    public static class BookingsBean {
        private String date;
        private int booking_id;
        private String state;
        private String time;
        private String facility;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public int getBooking_id() {
            return booking_id;
        }

        public void setBooking_id(int booking_id) {
            this.booking_id = booking_id;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getFacility() {
            return facility;
        }

        public void setFacility(String facility) {
            this.facility = facility;
        }
    }
}
