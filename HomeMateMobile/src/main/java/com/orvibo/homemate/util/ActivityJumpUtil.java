package com.orvibo.homemate.util;

import android.app.Activity;
import android.content.Intent;

/**
 * 界面跳转辅助类
 * Created by snown on 2015/11/27.
 */
public class ActivityJumpUtil {
    public static void jumpAct(Activity act, Class<?> cls) {
        Intent intent = new Intent(act, cls);
        act.startActivity(intent);
    }

    public static void jumpActFinish(Activity act, Class<?> cls) {
        Intent intent = new Intent(act, cls);
        act.startActivity(intent);
        act.finish();
    }

    public static void jumpAct(Activity act, Class<?> cls, Intent intent) {
        intent.setClass(act, cls);
        act.startActivity(intent);
    }

    public static void jumpActFinish(Activity act, Class<?> cls, Intent intent) {
        intent.setClass(act, cls);
        act.startActivity(intent);
        act.finish();
    }

}
