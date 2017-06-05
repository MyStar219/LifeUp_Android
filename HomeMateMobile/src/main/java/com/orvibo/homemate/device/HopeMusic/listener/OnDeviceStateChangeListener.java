package com.orvibo.homemate.device.HopeMusic.listener;

import cn.nbhope.smarthomelib.app.enity.DevicePlayState;

/**
 * Created by yu on 2016/5/28.
 */
public interface OnDeviceStateChangeListener {
    void stateChange(DevicePlayState devicePlayState);
}
