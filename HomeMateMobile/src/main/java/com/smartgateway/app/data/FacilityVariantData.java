package com.smartgateway.app.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ru.johnlife.lifetools.data.AbstractData;

/**
 * Created by yanyu on 5/13/2016.
 */
public class FacilityVariantData extends AbstractData {
    private int fid;
    private String image_url;
    private int capacity; //total days to display
    private String name; //"court 1, court 2"
    private List<BookingDayData> days = new ArrayList<>(capacity); //daily status
    private List<String> dates = new ArrayList<>();
    private List<String> states = new ArrayList<>();

    public FacilityVariantData(int fid, int capacity,String name, String image_url, List<String> dates, List<String> states) {
        this.dates = dates;
        this.states = states;
        this.fid = fid;
        this.image_url = image_url;
        this.capacity = capacity;
        this.name = name;
        Calendar tmp = Calendar.getInstance();
        tmp.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        for (int i=0; i< capacity; i++) {
            BookingDayData dData = new BookingDayData(tmp.getTimeInMillis(), dates, states);
            if (dData.getState() <= 0)
                i--;
            days.add(dData);
            tmp.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    public List<String> getDates() {
        return dates;
    }

    public List<String> getStates() {
        return states;
    }

    public int getFid() {
        return fid;
    }

    public List<BookingDayData> getDays() {
        return days;
    }

    public String getName() {
        return name;
    }

    public void setDays(List<BookingDayData> days) {
        this.days = days;
    }

    public String getImage_url() {
        return image_url;
    }
}
