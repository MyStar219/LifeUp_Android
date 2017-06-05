package com.orvibo.homemate.device.light.action;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.common.BaseFragment;
import com.orvibo.homemate.common.ViHomeProApp;
import com.orvibo.homemate.dao.DeviceStatusDao;
import com.orvibo.homemate.data.BindActionType;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.util.BindTool;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.LogUtil;

/**
 * Created by snown on 2016/7/1.
 *
 * @描述: 基础绑定fragment
 */
public class BaseActionFragment extends BaseFragment implements View.OnClickListener, OnResultListener {
    private static final String TAG = BaseActionFragment.class
            .getSimpleName();
    protected Device device;
    protected DeviceStatusDao deviceStatusDao;
    protected DeviceStatus deviceStatus;
    protected String deviceName;
    protected String deviceId;
    protected String uid;
    protected Action action;
    protected String command = "";
    protected int value1;
    protected int value2;
    protected int value3;
    protected int value4;
    /**
     * {@link BindActionType}
     */
    protected int bindActionType = BindActionType.SCENE;


    /**
     * 单位100ms
     */
    protected int delayTime;

    /**
     * order对应的名称
     */
    protected String keyName;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getData();
        if (device == null && savedInstanceState.getSerializable(IntentKey.DEVICE) != null) {
            device = (Device) savedInstanceState.getSerializable(IntentKey.DEVICE);
        }
        LogUtil.d(TAG, "onCreate()-device:" + device + ",action:" + action);
    }

    @Override
    public void onResume() {
        super.onResume();
        initStatus();
    }

    private void getData() {
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }
        device = (Device) bundle.getSerializable(IntentKey.DEVICE);
        if (device != null) {
            deviceId = device.getDeviceId();
            uid = device.getUid();
            deviceName = device.getDeviceName();
        }
        action = (Action) bundle.getSerializable(IntentKey.ACTION);
        bindActionType = bundle.getInt(IntentKey.BIND_ACTION_TYPE, BindActionType.SCENE);
        LogUtil.d(TAG, "getData()-action:" + action + ",bindActionType:" + bindActionType);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (device != null) {
            outState.putSerializable(IntentKey.DEVICE, device);
        }
        super.onSaveInstanceState(outState);
    }

    protected void initStatus() {
        if (action != null && action.getDeviceId().equalsIgnoreCase(deviceId)) {
            command = action.getCommand();
            value1 = action.getValue1();
            value2 = action.getValue2();
            value3 = action.getValue3();
            value4 = action.getValue4();
            delayTime = action.getDelayTime();
            keyName = action.getKeyName();
            onSelectedAction(action);
            //device = new DeviceDao().selDevice(uid, deviceId);
        } else {
            deviceStatusDao = new DeviceStatusDao();
            deviceStatus = deviceStatusDao.selDeviceStatus(uid, deviceId);
            LogUtil.d(TAG, "initStatus()-deviceStatus:" + deviceStatus);
            //默认值不是设备当前值
            if (deviceStatus != null) {
                //int value1 = deviceStatus.getValue1();
                Action action = BindTool.getDefaultAction(device, deviceStatus, bindActionType);
                if (action != null) {
                    if (action.getCommand() != null) {
                        deviceStatus.setCommand(action.getCommand());
                    }
                    deviceStatus.setValue1(action.getValue1());
                    deviceStatus.setValue2(action.getValue2());
                    deviceStatus.setValue3(action.getValue3());
                    deviceStatus.setValue4(action.getValue4());
                    deviceStatus.setAlarmType(action.getAlarmType());
                    if (!TextUtils.isEmpty(action.getDeviceId())) {
                        deviceStatus.setDeviceId(action.getDeviceId());
                    }
                } else {
                    LogUtil.e(TAG, "initStatus()-action is null");
                }
                onDeviceStatus(deviceStatus);
            } else {
                Action defaultActin = BindTool.getDefaultAction(device);
                onDefaultAction(defaultActin);
            }
        }
    }


    /**
     * 已被选中的状态
     *
     * @param action
     */
    protected void onSelectedAction(Action action) {
        LogUtil.d(TAG, "onSelectedAction()-action:" + action);
    }

    /**
     * 设备当前真实的状态
     *
     * @param deviceStatus
     */
    protected void onDeviceStatus(DeviceStatus deviceStatus) {
        LogUtil.d(TAG, "onDeviceStatus()-deviceStatus:" + deviceStatus);
    }

    /**
     * 设备默认的状态
     */
    protected void onDefaultAction(Action action) {
        LogUtil.d(TAG, "onDefaultAction()-action:" + action);
    }


    /**
     * action返回
     */
    public void onActionResult() {
        if (command != null) {
//        if (!StringUtil.isEmpty(command)) {
            Intent intent = new Intent();
            if (device != null) {
                final int deviceType = device.getDeviceType();
                if (deviceType == DeviceType.DIMMER
                        || deviceType == DeviceType.COLOR_TEMPERATURE_LAMP
                        || deviceType == DeviceType.RGB) {
                    if (!command.equals(DeviceOrder.TOGGLE) && value2 == 0) {
                        command = DeviceOrder.OFF;
                    }
                }
            }
            action = new Action(deviceId, command, value1, value2, value3, value4, keyName);
            keyName = DeviceTool.getActionName(ViHomeProApp.getContext(), action);
            action.setKeyName(keyName);
            LogUtil.d(TAG, "onActionResult()-action:" + action);
            Bundle bundle = new Bundle();
            bundle.putSerializable(IntentKey.ACTION, action);
            intent.putExtras(bundle);
            getActivity().setResult(Activity.RESULT_OK, intent);
        }
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onResult() {
        onActionResult();
    }

}
