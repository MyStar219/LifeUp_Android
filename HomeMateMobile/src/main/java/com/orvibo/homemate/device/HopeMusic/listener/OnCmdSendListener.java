package com.orvibo.homemate.device.HopeMusic.listener;

import cn.nbhope.smarthomelib.app.enity.DevicePlayState;

/**
 * Created by yu on 2016/5/27.
 */
public interface OnCmdSendListener{
    void sendCmd(String cmd, boolean isSelect, DevicePlayState song);
}
