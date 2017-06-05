package com.orvibo.homemate.device.allone2.epg;

import com.orvibo.homemate.sharedPreferences.BaseCache;

/**
 * Created by yuwei on 2016/4/18.
 */
public class AlloneStbCache extends BaseCache {

    private static final String ISHD = "ishd";
    private static final String ISHDFIRST = "ishdfirst";
    private static final String ISPAY = "ispay";
    private static final String IS_EPG_DISPLAY = "is_epg_display";


    /**
     * 判断退出时是否是epg还是遥控显示
     *
     * @param deviceId
     * @return
     */
    public static boolean isEpgDisplay(String deviceId) {
        return getBoolean(getKey(deviceId, IS_EPG_DISPLAY), true);
    }

    /**
     * 判断退出时是否是epg还是遥控显示
     *
     * @param deviceId
     * @return
     */
    public static boolean setEpgDisplay(String deviceId, boolean isDisplay) {
        return putBoolean(getKey(deviceId, IS_EPG_DISPLAY), isDisplay);
    }

    /**
     * 获取对应机顶盒设备节目筛选条件（是否高清）
     *
     * @param deviceID
     * @return
     */
    public static boolean getNeedToGetHDProgram(String deviceID) {
        return getBoolean(getKey(deviceID, ISHD), false);
    }

    /**
     * 保存机顶盒设备节目的筛选条件(是否高清)
     *
     * @param deviceID
     * @param ishd
     */
    public static void setNeedToHDProgram(String deviceID, boolean ishd) {
        putBoolean(getKey(deviceID, ISHD), ishd);
    }

    /**
     * 保存机顶盒设备节目的筛选条件(是否付款)
     *
     * @param deviceID
     * @param ispay
     */
    public static void setIsPayProgram(String deviceID, boolean ispay) {
        putBoolean(getKey(deviceID, ISPAY), ispay);
    }

    /**
     * 获取机顶盒设备节目的筛选条件(是否付款)
     *
     * @param deviceID
     * @return
     */
    public static boolean getIsPayProgram(String deviceID) {
        return getBoolean(getKey(deviceID, ISPAY), false);
    }

    /**
     * 设置是否高清优先
     *
     * @param deviceID
     * @param isHDFirst
     */
    public static void setIsProgramHDFirst(String deviceID, boolean isHDFirst) {
        putBoolean(getKey(deviceID, ISHDFIRST), isHDFirst);
    }

    /**
     * 获取是否高清优先（机顶盒节目）
     *
     * @param deviceID
     * @return
     */
    public static boolean getIsProgramHDFirst(String deviceID) {
        return getBoolean(getKey(deviceID, ISHDFIRST), false);
    }
}
