package com.orvibo.homemate.common.crash;

import android.content.Context;

import com.orvibo.homemate.sharedPreferences.CrashCache;

/**
 * Created by huangqiyao on 2015/7/3.
 */
public class CrashTool {
    /**
     * 清除crash
     *
     * @param context
     */
    public static void clearCrash(Context context) {
        CrashCache.saveCrash(context, false);
    }

    /**
     * 闪退
     *
     * @param context
     */
    public static void crashHappen(Context context) {
        CrashCache.saveCrash(context, true);
        CrashCache.saveCrashCount(context);
    }

    /**
     * @param context
     * @return true出现闪退
     */
    public static boolean isCrashHappen(Context context) {
        return CrashCache.getCrash(context);
    }

    public static int getCrashCount(Context context) {
        return CrashCache.getCrashCount(context);
    }

}
