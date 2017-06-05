package com.orvibo.homemate.device.allone2.view;

/**
 * Created by snown on 2016/7/21.
 *
 * @描述: 动态调整首页小方子设备间距
 */
public class ArrowViewUpdateEvent {

    private String deviceId;

    public ArrowViewUpdateEvent(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceId() {
        return deviceId;
    }
}
