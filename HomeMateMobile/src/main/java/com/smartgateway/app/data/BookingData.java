package com.smartgateway.app.data;

import com.smartgateway.app.Utils.Constants;

import java.util.Date;

import ru.johnlife.lifetools.data.AbstractData;

/**
 * Created by yanyu on 5/15/2016.
 */
public class BookingData extends AbstractData{
//    private FacilityData facility;
//    private FacilityVariantData variant;
//    private SelectedHoursData selectedHours;

    private String facility;
    private Date day;
    private String selectedHours;
    private String bookingState;
    private int id;


    public BookingData(String facility, long day, String selectedHours, String bookingState, int id) {
//        this.facility = facility;
//        this.variant = variant;
//        this.selectedHours = selectedHours;
        this.facility = facility;
        this.day = new Date(day);
        this.selectedHours = selectedHours;
        this.bookingState = bookingState;
        this.id = id;
    }

    public int getId(){
        return id;
    }

    public String getDate() {
        return Constants.dateFormat.format(day);
    }

    public String getWhere() {
        StringBuilder b = new StringBuilder();
        b.append(facility).append(" ")
            .append('(').append(selectedHours).append(')');
        return b.toString();
    }

    public int getState() {
        switch (bookingState){
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

    public long getWhen() {
        return day.getTime();
    }
}
