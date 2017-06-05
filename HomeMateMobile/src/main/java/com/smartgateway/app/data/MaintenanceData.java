package com.smartgateway.app.data;

import com.smartgateway.app.Utils.Constants;

import java.util.Date;

import ru.johnlife.lifetools.data.AbstractData;

/**
 * Created by yanyu on 5/15/2016.
 */
public class MaintenanceData extends AbstractData {
    private int id;
    private String date;
    private int state;
    private String item;
    private String where;
    private String description;

    /** GUI constructor */
    public MaintenanceData(int id, String date, String item, String where, int state) {
        this.id = id;
        this.date = date;
        this.item = item;
        this.where = where;
        this.state = state;
    }

    public String getDate() {
        return date;
    }

    public int getState() {
        return state;
    }

    public String getDescription() {
        return description;
    }

    public String getItem() {
        return item;
    }

    public String getWhere() {
        return where;
    }

    public void setDescription(String description) {
        this.description = description;
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
}
