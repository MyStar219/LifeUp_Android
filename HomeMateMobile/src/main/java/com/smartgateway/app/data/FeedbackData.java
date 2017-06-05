package com.smartgateway.app.data;

import com.smartgateway.app.Utils.Constants;

import java.util.Date;

import ru.johnlife.lifetools.data.AbstractData;

/**
 * Created by yanyu on 5/15/2016.
 */
public class FeedbackData extends AbstractData {
    private int id;
    private String date;
    private int state;
    private String item;
    private String where;

    public FeedbackData(int id,String date,String item, String where, int state) {
        this.date = date;
        this.item = item;
        this.where = where;
        this.state = state;
        this.id = id;
    }

    /** GUI constructor */
//    public FeedbackData(String date, String item, String where, String type) {
//        this.date = date;
//        this.item = item;
//        this.where = where;
//        this.state = Constants.MaintenanceState.FIXED;
//        this.type = type;
//    }

    public int getState() {
        return state;
    }

    public String getItem() {
        return item;
    }

    public String getWhere() {
        return where;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setState(int state) {
        this.state = state;
    }
}
