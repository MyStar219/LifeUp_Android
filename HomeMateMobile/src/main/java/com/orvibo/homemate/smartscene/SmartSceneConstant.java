package com.orvibo.homemate.smartscene;

/**
 * Created by Keeu on 2016/3/10.
 */
public class SmartSceneConstant {
    /**
     * 默认温度
     */
    public static final int TEMPERATURE_DEFAULT_VALUE = 30;

    /**
     * 默认湿度％
     */
    public static final int HUMIDITY_DEFAULT_VALUE = 90;

    /**
     * 触发联动的条件类型
     */
    public static class LinkageType {
        /**
         * 按照设备状态来触发
         */
        public static final int DEVICE_STATUS = 0;

        /**
         * 按照时间条件触发
         */
        public static final int TIME = 1;

        /**
         * 按照星期来触发
         */
        public static final int WEEK = 2;
    }

    /**
     * 判定条件
     */
    public static class Condition {
        /**
         * 表示等于
         */
        public static final int EQUAL = 0;

        /**
         * 表示大于
         */
        public static final int GREATER_THAN = 1;

        /**
         * 表示小于
         */
        public static final int LESS_THAN = 2;

        /**
         * 表示大于或者等于
         */
        public static final int GREATER_THAN_EQUAL = 3;

        /**
         * 表示小于或者等于
         */
        public static final int LESS_THAN_EQUAL = 4;
    }
}
