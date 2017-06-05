package com.smartgateway.app.data.event;

/**
 * Created by Terry on 12/7/16.
 */
public class FacilityBannerEvent {

    private String image_url;

    public FacilityBannerEvent(String image_url) {
        this.image_url = image_url;
    }

    public String getImage_url() {
        return image_url;
    }
}
