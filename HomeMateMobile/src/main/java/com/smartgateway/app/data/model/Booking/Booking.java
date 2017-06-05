package com.smartgateway.app.data.model.Booking;

import com.google.gson.Gson;

/**
 * Created by Terry on 1/7/16.
 */
public class Booking {

    /**
     * date : 2016-07-18
     * facility : BBQ Pit 1
     * fee : S$ 10.0
     * deposit : S$ 100.0
     * time : 11:00 - 12:00
     */

    private BookingBean Booking;

    public static Booking objectFromData(String str) {

        return new Gson().fromJson(str, Booking.class);
    }

    public BookingBean getBooking() {
        return Booking;
    }

    public void setBooking(BookingBean Booking) {
        this.Booking = Booking;
    }

    public static class BookingBean {
        private String date;
        private String facility;
        private String fee;
        private String deposit;
        private String time;

        public static BookingBean objectFromData(String str) {

            return new Gson().fromJson(str, BookingBean.class);
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getFacility() {
            return facility;
        }

        public void setFacility(String facility) {
            this.facility = facility;
        }

        public String getFee() {
            return fee;
        }

        public void setFee(String fee) {
            this.fee = fee;
        }

        public String getDeposit() {
            return deposit;
        }

        public void setDeposit(String deposit) {
            this.deposit = deposit;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }
    }
}
