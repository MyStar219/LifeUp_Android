package com.smartgateway.app.data.event;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Terry on 12/7/16.
 */
public class DashboardBannerEvent {

    private List<String> imageArray = new ArrayList<>();

    public DashboardBannerEvent(List<String> imageArray) {
        this.imageArray = imageArray;
    }

    public List<String> getImageArray() {
        return imageArray;
    }
}
