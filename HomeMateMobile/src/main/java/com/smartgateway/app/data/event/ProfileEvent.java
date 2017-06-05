package com.smartgateway.app.data.event;

/**
 * Created by Terry on 18/7/16.
 */
public class ProfileEvent {

    private boolean profilechange;

    public ProfileEvent(boolean profilechange) {
        this.profilechange = profilechange;
    }

    public boolean isProfilechange() {
        return profilechange;
    }
}
