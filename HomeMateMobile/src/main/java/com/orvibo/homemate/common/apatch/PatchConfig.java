package com.orvibo.homemate.common.apatch;

/**
 * Created by wu on 2016/5/6.
 */
public class PatchConfig {
    //http连接超时时间
    public static final int CONNECT_OUT_TIME = 5*1000;
    //http读超时时间
    public static final int READ_OUT_TIME = 5*1000;
    //后缀名
    public static final String SUFFIX = ".apatch";
    //补丁文件夹
    public static final String DIR = "apatch";
    //从网络加载补丁列表的URL路径
    public static String DOWN_APATCH_LIST_URL = "http://firmware.orvibo.com/android/homemate_android_apatchlist.xml";

}
