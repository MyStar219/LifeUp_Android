package com.orvibo.homemate.update;

import android.content.Context;

import com.smartgateway.app.R;

/**
 * @deprecated Created by baoqi on 2016/6/24.
 */
public class UpdateUrlConstant {
    /**
     * 升级url
     */
//    public static final String ORVIBO_UPDATE_URL = "http://192.168.2.150:8080/homemate/update.json";
    public static final String ORVIBO_UPDATE_URL_BASE = "http://firmware.orvibo.com/androidapk/";
//    public static final String ORVIBO_UPDATE_URL_BASE = "http://192.168.2.150:8080/homemate/";
    /**
     *
     */
    public static final String ORVIBO_UPDATE_URL = "http://firmware.orvibo.com/androidapk/com.orvibo.homemate.json";

    /**
     * 获取升级的url。格式：http://firmware.orvibo.com/androidapk/ + appPackageName +".json"
     *
     * @param context
     * @return app升级url
     */
    public static String getUpdateUrl(Context context) {
        String update_url = context.getResources().getString(R.string.oem_update_url);
        return update_url;
    }
}
