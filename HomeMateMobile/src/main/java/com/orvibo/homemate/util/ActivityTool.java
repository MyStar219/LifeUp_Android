package com.orvibo.homemate.util;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.AuthUnlockData;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DeviceIr;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.AuthUnlockDao;
import com.orvibo.homemate.dao.DeviceIrDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.hub.HubUpdateActivity;
import com.orvibo.homemate.device.smartlock.AuthLockActivity;
import com.orvibo.homemate.device.smartlock.AuthLockResultActivity;
import com.orvibo.homemate.device.smartlock.GestureActivity;
import com.orvibo.homemate.sharedPreferences.SmartLockCache;
import com.orvibo.homemate.user.LoginActivity;

import java.io.Serializable;
import java.util.List;


/**
 * Created by huangqiyao on 2015/4/24.
 */
public class ActivityTool {
    private static final String TAG = ActivityTool.class.getSimpleName();
    public static final int GET_ACTION = 1;

    /**
     * 判断是否可以显示家庭成员邀请、设备报警弹框
     *
     * @param activity
     * @return true可以显示
     */
    public static boolean canShowActivity(BaseActivity activity) {
        boolean canShow = false;
        if (activity != null) {
            canShow = !HubUpdateActivity.isHubUpdating && AppTool.isAppOnForeground(activity);
        }
        MyLogger.kLog().d(activity + ",canShow:" + canShow);
        return canShow;

    }

    /**
     * 跳转到登陆界面
     *
     * @param source
     * @param userName
     * @param loginEntry
     * @param errorCode  错误码，由什么原因引起登录。目前只有密码错误。
     */
    public static void toLoginActivity(Context source, String userName, String loginEntry, int errorCode) {
        Intent intent = new Intent();
        intent.putExtra(IntentKey.USERNAME, userName);
        intent.putExtra(IntentKey.ERROR_CODE, errorCode);
        intent.putExtra(IntentKey.USERNAME_SHOW, true);
        intent.putExtra(Constant.LOGIN_ENTRY, loginEntry);
        toLoginActivity(source, intent);
    }

    /**
     * 跳转到登陆界面
     *
     * @param source
     * @param param
     */
    public static void toLoginActivity(Context source, Intent param) {
        if (source == null || param == null) {
            return;
        }
        if (source instanceof Activity) {
            param.setClass(source, LoginActivity.class);
            source.startActivity(param);
        } else {
            LogUtil.w(TAG, "toLoginActivity()-source:" + source);
        }
    }

    public static void toActionsActivity(Activity source, int requestCode, int bindActionType, Device device, List<Action> actions) {
        if (device == null) {
            LogUtil.e(TAG, "toActionsActivity()-device is null");
            return;
        }
        Intent intent = new Intent();
        String actionActivityName = DeviceTool.getActionActivityNameByDeviceType(device);
        if (!StringUtil.isEmpty(actionActivityName)) {
            intent.setClassName(source, actionActivityName);
            intent.putExtra(IntentKey.DEVICE, device);
            intent.putExtra(IntentKey.ACTIONS, (Serializable) actions);
            intent.putExtra(IntentKey.BIND_ACTION_TYPE, bindActionType);
            source.startActivityForResult(intent, requestCode);
//        source.startActivityForResult(intent, GET_ACTION);
        } else {
            LogUtil.e(TAG, "toActionsActivity()-actionActivityName:" + actionActivityName + ",device:" + device);
        }
    }

    /**
     * 跳转到选择动作的activity
     *
     * @param source
     * @param requestCode
     * @param bindActionType {@link com.orvibo.homemate.data.BindActionType}
     * @param device
     * @param action
     */
    public static void toSelectActionActivity(Activity source, int requestCode, int bindActionType, Device device, Action action) {
        if (device == null) {
            LogUtil.e(TAG, "toSelectActionActivity()-device is null");
            return;
        }
        Intent intent = new Intent();
        String actionActivityName = DeviceTool.getActionActivityNameByDeviceType(device);
        if (!StringUtil.isEmpty(actionActivityName)) {
            intent.setClassName(source, actionActivityName);
            intent.putExtra(IntentKey.DEVICE, device);
            intent.putExtra(IntentKey.ACTION, action);
            intent.putExtra(IntentKey.BIND_ACTION_TYPE, bindActionType);
            intent.putExtra(IntentKey.IS_ACTION, true);
            intent.putExtra(IntentKey.IS_HOME_CLICK, true);
            source.startActivityForResult(intent, requestCode);
//        source.startActivityForResult(intent, GET_ACTION);
        } else {
            LogUtil.e(TAG, "toSelectActionActivity()-actionActivityName:" + actionActivityName + ",device:" + device);
        }
    }

    public static void toSelectActionsActivity(Activity source, int requestCode, int bindActionType, Device device, Action action, List<Action> actions) {
        if (device == null) {
            LogUtil.e(TAG, "toSelectActionsActivity()-device is null");
            return;
        }
        Intent intent = new Intent();
        String actionActivityName = DeviceTool.getActionActivityNameByDeviceType(device);
        if (!StringUtil.isEmpty(actionActivityName)) {
            intent.setClassName(source, actionActivityName);
            intent.putExtra(IntentKey.DEVICE, device);
            intent.putExtra(IntentKey.ACTION, action);
            intent.putExtra(IntentKey.ACTIONS, (Serializable) actions);
            intent.putExtra(IntentKey.BIND_ACTION_TYPE, bindActionType);
            source.startActivityForResult(intent, requestCode);
//        source.startActivityForResult(intent, GET_ACTION);
        } else {
            LogUtil.e(TAG, "toSelectActionsActivity()-actionActivityName:" + actionActivityName + ",device:" + device);
        }
    }

    /**
     * 跳转到选择动作的activity
     *
     * @param source
     * @param requestCode
     * @param device
     * @param action
     */
    public static void toSelectActionActivity(Activity source, Fragment fragment, int requestCode, int bindActionType, Device device, Action action) {
        if (device == null) {
            LogUtil.e(TAG, "toSelectActionActivity()-device is null");
            return;
        }
        Intent intent = new Intent();
        String actionActivityName = DeviceTool.getActionActivityNameByDeviceType(device);
        if (!StringUtil.isEmpty(actionActivityName)) {
            intent.setClassName(source, actionActivityName);
            intent.putExtra(IntentKey.DEVICE, device);
            intent.putExtra(IntentKey.ACTION, action);
            intent.putExtra(IntentKey.BIND_ACTION_TYPE, bindActionType);
            intent.putExtra(IntentKey.IS_ACTION, true);
            intent.putExtra(IntentKey.IS_HOME_CLICK, true);
            fragment.startActivityForResult(intent, requestCode);
//        source.startActivityForResult(intent, GET_ACTION);
        } else {
            LogUtil.e(TAG, "toSelectActionActivity()-actionActivityName:" + actionActivityName + ",device:" + device);
        }
    }

    public static void toSelectActionsActivity(Activity source, Fragment fragment, int requestCode, int bindActionType, Device device, Action action, List<Action> actions) {
        if (device == null) {
            LogUtil.e(TAG, "toSelectActionActivity()-device is null");
            return;
        }
        Intent intent = new Intent();
        String actionActivityName = DeviceTool.getActionActivityNameByDeviceType(device);
        if (!StringUtil.isEmpty(actionActivityName)) {
            intent.setClassName(source, actionActivityName);
            intent.putExtra(IntentKey.DEVICE, device);
            intent.putExtra(IntentKey.ACTION, action);
            intent.putExtra(IntentKey.ACTIONS, (Serializable) actions);
            intent.putExtra(IntentKey.BIND_ACTION_TYPE, bindActionType);
            intent.putExtra(IntentKey.IS_ACTION, true);
            intent.putExtra(IntentKey.IS_HOME_CLICK, true);
            fragment.startActivityForResult(intent, requestCode);
//        source.startActivityForResult(intent, GET_ACTION);
        } else {
            LogUtil.e(TAG, "toSelectActionActivity()-actionActivityName:" + actionActivityName + ",device:" + device);
        }
    }

    /**
     * smagret
     * 跳转到学习红外码的activity
     *
     * @param source
     * @param requestCode
     * @param device
     */
    public static void toLearnIrActivity(Activity source, int requestCode, Device device) {
        if (device == null) {
            LogUtil.e(TAG, "toLearnIrActivity()-device is null");
            return;
        }
        LogUtil.e(TAG, "toLearnIrActivity()-device = " + device);

        boolean hasKey = false;//

        List<DeviceIr> deviceIrs = new DeviceIrDao().selDeviceIrs(device.getUid(), device.getDeviceId());
        hasKey = deviceIrs != null && deviceIrs.size() > 0;
        Intent intent = new Intent();
        String actionActivityName = DeviceTool.getLearnIrActivityNameByDeviceType(device.getDeviceType(), hasKey);
        if (!TextUtils.isEmpty(actionActivityName)) {
            intent.setClassName(source, actionActivityName);
            intent.putExtra(IntentKey.DEVICE, device);
            source.startActivityForResult(intent, requestCode);
        } else {
            LogUtil.e(TAG, "toLearnIrActivity()-actionActivityName is " + actionActivityName + ",device:" + device);
        }
    }

    /**
     * 门锁跳转手势密码逻辑
     */
    public static void lockJump(Activity from, AuthUnlockData authUnlockData, Device device) {
        Intent intent = new Intent();
        intent.putExtra(IntentKey.AUTH_UNLOCK_DATA, authUnlockData);
        intent.putExtra(IntentKey.DEVICE, device);
        if (SmartLockCache.hasGesture() && !SmartLockCache.isTimeOut()) {
            lockStartJump(from, authUnlockData, device);
        } else {
            ActivityJumpUtil.jumpAct(from, GestureActivity.class, intent);
        }
    }

    /**
     * 门锁跳转授权门锁逻辑
     */
    public static void lockStartJump(Activity from, AuthUnlockData authUnlockData, Device device) {
        Intent intent = new Intent();
        intent.putExtra(IntentKey.AUTH_UNLOCK_DATA, authUnlockData);
        intent.putExtra(IntentKey.DEVICE, device);
        if (AuthUnlockDao.getInstance().isAvailableData(authUnlockData)) {
            if (from instanceof GestureActivity)
                ActivityJumpUtil.jumpActFinish(from, AuthLockResultActivity.class, intent);
            else
                ActivityJumpUtil.jumpAct(from, AuthLockResultActivity.class, intent);
        } else {
            if (from instanceof GestureActivity)
                ActivityJumpUtil.jumpActFinish(from, AuthLockActivity.class, intent);
            else
                ActivityJumpUtil.jumpAct(from, AuthLockActivity.class, intent);
        }
    }

}
