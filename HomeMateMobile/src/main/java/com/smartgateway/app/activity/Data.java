package com.smartgateway.app.activity;

import java.io.Serializable;

/**
 * Created by mac-772 on 9/30/16.
 */
public class Data implements Serializable {
    String title;
    String url;
    public Data() {

    }
    public Data(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public String getTitle() { return title; }

    public String getUrl() { return url; }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
