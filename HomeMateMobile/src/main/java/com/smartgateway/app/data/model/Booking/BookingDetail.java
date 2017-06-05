package com.smartgateway.app.data.model.Booking;

import com.smartgateway.app.Utils.Constants;

/**
 * BookingDetail
 * Created by Terry on 1/7/16.
 */
public class BookingDetail {

    /**
     * refunded :
     * confirmed :
     * Booking : {"facility":"Tennis Court 1","booking_id":85,"state":"reserved","booking_time":"2016-06-30 00:00:00","deposit":"S$ 100.0","time":"09:00 - 11:00","date":"2016-07-03"}
     */

    private String refunded;
    private String confirmed;
    /**
     * facility : Tennis Court 1
     * booking_id : 85
     * state : reserved
     * booking_time : 2016-06-30 00:00:00
     * deposit : S$ 100.0
     * time : 09:00 - 11:00
     * date : 2016-07-03
     */

    private BookingBean Booking;

    public String getRefunded() {
        return refunded;
    }

    public void setRefunded(String refunded) {
        this.refunded = refunded;
    }

    public String getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(String confirmed) {
        this.confirmed = confirmed;
    }

    public BookingBean getBooking() {
        return Booking;
    }

    public void setBooking(BookingBean Booking) {
        this.Booking = Booking;
    }

    public static class BookingBean {
        private String facility;
        private int booking_id;
        private String state;
        private String booking_time;
        private String payment_msg;
        private String deposit;
        private String date;
        private String time;
        private String payment_amt;

        public String getFacility() {
            return facility;
        }

        public void setFacility(String facility) {
            this.facility = facility;
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

        public int getBookingState() {
            switch (getState()){
                case Constants.RESERVED:
                    return Constants.BookingState.RESERVED;
                case Constants.CONFIRMED:
                    return Constants.BookingState.CONFIRMED;
                case Constants.CONSUMED:
                    return Constants.BookingState.CONSUMED;
                case Constants.COMPLETED:
                    return Constants.BookingState.COMPLETED;
                case Constants.CANCELLED:
                    return Constants.BookingState.CANCELLED;
                case Constants.EXPIRED:
                    return Constants.BookingState.EXPIRED;
                default:
                    return Constants.BookingState.RESERVED;
            }
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getBooking_time() {
            return booking_time;
        }

        public void setBooking_time(String booking_time) {
            this.booking_time = booking_time;
        }

        public String getPayment_msg() {
            return payment_msg;
        }
        public void setPayment_msg(String payment_msg) {
            this.payment_msg = payment_msg;
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

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getPayment_amt() {
            return payment_amt;
        }

        public void setPayment_amt(String payment_amt) {
            this.payment_amt = payment_amt;
        }
    }
}
