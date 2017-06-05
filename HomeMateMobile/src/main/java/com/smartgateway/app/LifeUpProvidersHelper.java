package com.smartgateway.app;


import android.content.Context;

public class LifeUpProvidersHelper {

    private HomeMateHelper homeMateHelper;
    private WeijuHelper weijuHelper;

    public LifeUpProvidersHelper() {
        homeMateHelper = new HomeMateHelper();
        weijuHelper = new WeijuHelper();
    }

    public void login(Context context) {
        homeMateHelper.login(context);
        weijuHelper.login(context);
    }

    public void signOut(Context context) {
        homeMateHelper.signOut(context);
        weijuHelper.signOut();
    }
}
