package com.smartgateway.app.data;

import ru.johnlife.lifetools.data.AbstractData;

/**
 * Created by yanyu on 5/17/2016.
 */
public class AnnouncementData extends AbstractData{
    private String date;
    private String title;
    private String content;
    private String id;

    public AnnouncementData(String title, String content, String date, String id) {
        this.date = date;
        this.title = title;
        this.content = content;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getContent() {
        return content;
    }

    public String getId() {
        return id;
    }
}
