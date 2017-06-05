package com.orvibo.homemate.device;

import android.text.TextUtils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by huangqiyao on 2016/7/29 10:27.
 * 计划控制的设备动作
 *
 * @version v1.10
 */
public class ControlRecord {
    /**
     * 控制的动作。key:deviceId,value:value1
     */
    private ConcurrentHashMap<String, Integer> mCtrlActions = new ConcurrentHashMap<>();

    /**
     * 记录控制设备的动作
     *
     * @param deviceId
     * @param value1
     */
    public void recordDeviceAction(String deviceId, int value1) {
        if (!TextUtils.isEmpty(deviceId)) {
            mCtrlActions.put(deviceId, value1);
        }
    }

    /**
     * 判断是否有此设备的控制记录。如果有记录则不再控制
     *
     * @param deviceId
     * @return
     */
    public boolean hasDeviceAction(String deviceId) {
        return !TextUtils.isEmpty(deviceId) && mCtrlActions.containsKey(deviceId);
    }

    /**
     * 获取设备控制的动作值
     *
     * @param deviceId
     * @return
     */
    public int getDeviceAction(String deviceId) {
        return mCtrlActions.get(deviceId);
    }

    /**
     * 当控制返回结果或者属性报告过来时将对应的设备动作移除
     *
     * @param deviceId
     */
    public void removeCtrlAction(String deviceId) {
        if (!TextUtils.isEmpty(deviceId)) {
            mCtrlActions.remove(deviceId);
        }
    }

    public void reset() {
        mCtrlActions.clear();
    }

    @Override
    public String toString() {
        return "ControlRecord{" +
                "mCtrlActions=" + mCtrlActions +
                '}';
    }
}
