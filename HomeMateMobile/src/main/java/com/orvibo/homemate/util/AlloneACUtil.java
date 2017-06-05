package com.orvibo.homemate.util;

import android.content.Context;

import com.hzy.tvmao.ir.ac.ACConstants;
import com.smartgateway.app.R;
import com.orvibo.homemate.common.ViHomeProApp;

/**
 * Created by snown on 2016/6/22.
 *
 * @描述: 空调数据和本地协议数据辅助类
 */
public class AlloneACUtil {

    public static final int AC_TYPE_SIMPLE = 1;//空调类型为简单的按钮

    protected static final int AC_POWER = 1;//开关
    protected static final int AC_MODE = 2;//模式
    protected static final int AC_WIND_SPEED = 3;//风量
    protected static final int AC_WIND_direction = 4;//风向
    protected static final int AC_TEMPTURE = 5;//温湿度值


    public static final int AC_POWER_ON = 2;//开机
    public static final int AC_POWER_OFF = 1;//关机

    public static final int AC_MODE_AUTO = 0;//自动
    public static final int AC_MODE_COOL = 1;//制冷
    public static final int AC_MODE_DRY = 2;//抽湿
    public static final int AC_MODE_FAN = 3;//送风
    public static final int AC_MODE_HEAT = 4;//制热

    public static final int AC_WIND_SPEED_AUTO = 0;//自动
    public static final int AC_WIND_SPEED_LOW = 1;//低
    public static final int AC_WIND_SPEED_MEDIUM = 2;//中
    public static final int AC_WIND_SPEED_HIGH = 3;//高

    private static Context context = ViHomeProApp.getContext();


    /**
     * 酷控模式转智家
     *
     * @param value
     * @return
     */
    public static int getHomematPower(int value) {
        switch (value) {
            case ACConstants.AC_POWER_ON:
                value = AC_POWER_ON;
                break;
            case ACConstants.AC_POWER_OFF:
                value = AC_POWER_OFF;
                break;

        }
        if (value <= 0) {
            value = AC_POWER_OFF;
        }
        return value;
    }

    /**
     * 模式智家转酷控
     *
     * @param value
     * @return
     */
    public static int getKKPower(int value) {
        switch (value) {
            case AC_POWER_ON:
                value = ACConstants.AC_POWER_ON;
                break;
            case AC_POWER_OFF:
                value = ACConstants.AC_POWER_OFF;
                break;
        }
        return value;
    }

    /**
     * 酷控模式转智家
     *
     * @param value
     * @return
     */
    public static int getHomemateMode(int value) {
        switch (value) {
            case ACConstants.AC_MODE_AUTO:
                value = AC_MODE_AUTO;
                break;
            case ACConstants.AC_MODE_COOL:
                value = AC_MODE_COOL;
                break;
            case ACConstants.AC_MODE_DRY:
                value = AC_MODE_DRY;
                break;
            case ACConstants.AC_MODE_FAN:
                value = AC_MODE_FAN;
                break;
            case ACConstants.AC_MODE_HEAT:
                value = AC_MODE_HEAT;
                break;
        }
        return value < 0 ? AC_MODE_AUTO : value;
    }

    /**
     * 模式智家转酷控
     *
     * @param value
     * @return
     */
    public static int getKKMode(int value) {
        switch (value) {
            case AC_MODE_AUTO:
                value = ACConstants.AC_MODE_AUTO;
                break;
            case AC_MODE_COOL:
                value = ACConstants.AC_MODE_COOL;
                break;
            case AC_MODE_DRY:
                value = ACConstants.AC_MODE_DRY;
                break;
            case AC_MODE_FAN:
                value = ACConstants.AC_MODE_FAN;
                break;
            case AC_MODE_HEAT:
                value = ACConstants.AC_MODE_HEAT;
                break;
        }
        return value;
    }

    /**
     * 拼接动作名称
     *
     * @param value
     * @return
     */
    public static String getAcActionName(int value) {
        StringBuilder stringBuilder = new StringBuilder();
        int mode = Integer.parseInt(String.valueOf(value).substring(1, 2));
        int power = Integer.parseInt(String.valueOf(value).substring(0, 1));
        if (power == AC_POWER_OFF) {
            stringBuilder.append(context.getString(R.string.action_off));
        } else {
            String tmp = String.valueOf(value).substring(4);
            int rid = R.string.conditioner_auto;
            switch (mode) {
                case AC_MODE_AUTO:
                    rid = R.string.conditioner_auto;
                    break;
                case AC_MODE_COOL:
                    rid = R.string.conditioner_cold;
                    break;
                case AC_MODE_DRY:
                    rid = R.string.conditioner_dehumidifier;
                    break;
                case AC_MODE_FAN:
                    rid = R.string.conditioner_wind;
                    break;
                case AC_MODE_HEAT:
                    rid = R.string.conditioner_hot;
                    break;
            }
            stringBuilder.append(context.getString(rid));
            if (!tmp.equalsIgnoreCase("0"))
                stringBuilder.append(" ").append(tmp).append(context.getString(R.string.conditioner_temperature_unit));
        }
        return stringBuilder.toString();
    }

    /**
     * 获取对应的值
     *
     * @param value
     * @return
     */
    public static int[] getAcValue(int value) {
        int[] data = new int[5];
        String value1 = String.valueOf(value);
        if (value1 != null && value1.length() >= 5) {
            data[0] = Integer.parseInt(value1.substring(0, 1));
            data[1] = Integer.parseInt(value1.substring(1, 2));
            data[2] = Integer.parseInt(value1.substring(2, 3));
            data[3] = Integer.parseInt(value1.substring(3, 4));
            data[4] = Integer.parseInt(value1.substring(4));
        }
        return data;
    }

}
