package com.orvibo.homemate.device.HopeMusic.socket;

/**
 * Created by wuliquan on 2016/6/27.
 */
public interface OnRequestListener {
    void sendSuccess();
    void sendFail();
    void outTime();
}
