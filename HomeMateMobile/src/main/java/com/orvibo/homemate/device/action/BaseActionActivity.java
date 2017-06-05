package com.orvibo.homemate.device.action;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.DeviceStatusDao;
import com.orvibo.homemate.data.BindActionType;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.data.DeviceStatusConstant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.NavigationType;
import com.orvibo.homemate.util.BindTool;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.view.custom.NavigationGreenBar;
import com.orvibo.homemate.view.custom.NavigationWhiteBar;

/**
 * Created by smagret on 2015/05/09
 */
public class BaseActionActivity extends BaseActivity {
    private static final String TAG = BaseActionActivity.class
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
    protected int bindActionType;


    /**
     * 单位100ms
     */
    protected int delayTime;

    /**
     * order对应的名称
     */
    protected String keyName;

    //1.9版本的开、关、翻转页面
    private LinearLayout ll_toggleView;
    private TextView tv_rbOpen;
    private TextView tv_rbClose;
    private TextView tv_rbToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getData();
        if (device == null && savedInstanceState.getSerializable(IntentKey.DEVICE) != null) {
            device = (Device) savedInstanceState.getSerializable(IntentKey.DEVICE);
        }
        LogUtil.d(TAG, "onCreate()-device:" + device + ",action:" + action);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initStatus();
        setBindRadioGroup();
    }

    protected void setBindBar(int barType) {
        if (barType == NavigationType.greenType) {
            NavigationGreenBar navigationGreenBar = (NavigationGreenBar) findViewById(R.id.nbTitle);
            if (navigationGreenBar != null) {
                navigationGreenBar.setText(getString(R.string.device_timing_set_action));
                navigationGreenBar.setRightText("");
            }
        } else if (barType == NavigationType.whiteType) {
            NavigationWhiteBar navigationWhiteBar = (NavigationWhiteBar) findViewById(R.id.nbTitle);
            if (navigationWhiteBar != null) {
                navigationWhiteBar.setText(getString(R.string.device_timing_set_action));
                navigationWhiteBar.setRightText("");
            }
        }
    }

    protected void setBarRightText(String text) {
        NavigationGreenBar navigationGreenBar = (NavigationGreenBar) findViewById(R.id.nbTitle);
        navigationGreenBar.setRightText(text);
    }

    /**
     * 设置开、关、翻转页面状态
     */
    protected void setBindRadioGroup() {
        ll_toggleView = (LinearLayout) findViewById(R.id.toggleView);
        if (ll_toggleView != null) {
            tv_rbOpen = (TextView) findViewById(R.id.rbOpen);
            tv_rbClose = (TextView) findViewById(R.id.rbClose);
            tv_rbToggle = (TextView) findViewById(R.id.rbToggle);
            tv_rbOpen.setOnClickListener(this);
            tv_rbClose.setOnClickListener(this);
            tv_rbToggle.setOnClickListener(this);

            if (BindActionType.isSupportToggle(bindActionType)) {
                //支持翻转，显示开、关、翻转页面
                ll_toggleView.setVisibility(View.VISIBLE);
                if (!StringUtil.isEmpty(command) && command.equals(DeviceOrder.TOGGLE)) {
                    //翻转
                    tv_rbOpen.setSelected(false);
                    tv_rbClose.setSelected(false);
                    tv_rbToggle.setSelected(true);
                } else if (!StringUtil.isEmpty(command) && command.equals(DeviceOrder.OFF)) {
                    //关闭
                    tv_rbOpen.setSelected(false);
                    tv_rbClose.setSelected(true);
                    tv_rbToggle.setSelected(false);
                } else {
                    //开
                    tv_rbOpen.setSelected(true);
                    tv_rbClose.setSelected(false);
                    tv_rbToggle.setSelected(false);
                }
            } else {
                ll_toggleView.setVisibility(View.GONE);
            }
        }
    }

//    protected void setBindRadioGroup() {
//        rgAction = (RadioGroup) findViewById(R.id.rgAction);
//        if (rgAction != null) {
//            openRb = (RadioButton) rgAction.getChildAt(0);
//            closeRb = (RadioButton) rgAction.getChildAt(1);
//            toggleRb = (RadioButton) rgAction.getChildAt(2);
//            viewAction = findViewById(R.id.rlAction);
//            rgAction.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//
//                @Override
//                public void onCheckedChanged(RadioGroup arg0, int arg1) {
//                    //获取变更后的选中项的ID
//                    int radioButtonId = arg0.getCheckedRadioButtonId();
//                    //更新文本内容，以符合选中项
//                    if (radioButtonId == R.id.rbOpen) {
//                        viewAction.setVisibility(View.VISIBLE);
//                        value1 = DeviceStatusConstant.ON;
//                        //rgb 色温灯 调光灯没有ON，只有对应如下的order by keeu
//                        final int deviceType = device.getDeviceType();
//                        if (deviceType == DeviceType.RGB) {
//                            command = DeviceOrder.COLOR_CONTROL;
//                        } else if (deviceType == DeviceType.COLOR_TEMPERATURE_LAMP) {
//                            command = DeviceOrder.COLOR_TEMPERATURE;
//                        } else if (deviceType == DeviceType.DIMMER) {
//                            command = DeviceOrder.MOVE_TO_LEVEL;
//                        } else {
//                            command = DeviceOrder.ON;
//                        }
//                        if (value2 <= 0) {
//                            value2 = 1;
//                            LogUtil.e(TAG, "changeView()-value2:" + value2 + " is less than 0.Set it to 1.");
//                        }
//                        action.setValue1(value1);
//                        action.setCommand(command);
//                        action.setValue2(value2);
//                        initStatus();
//                    } else if (radioButtonId == R.id.rbClose) {
//                        viewAction.setVisibility(View.GONE);
//                        command = DeviceOrder.OFF;
//                        value1 = DeviceStatusConstant.OFF;
//                        // value2 = value4 = 0;
//                    } else if (radioButtonId == R.id.rbToggle) {
//                        viewAction.setVisibility(View.GONE);
//                        command = DeviceOrder.TOGGLE;
//                        // value1 = value2 = value4 = 0;
//                    }
//                }
//            });
//
//            if (BindActionType.isSupportToggle(bindActionType)) {
//                rgAction.setVisibility(View.VISIBLE);
//                if (viewAction != null) {
//                    if (!StringUtil.isEmpty(command) && command.equals(DeviceOrder.TOGGLE)) {
//                        viewAction.setVisibility(View.GONE);
//                        toggleRb.setChecked(true);
//                    } else if (!StringUtil.isEmpty(command) && command.equals(DeviceOrder.OFF)) {
//                        viewAction.setVisibility(View.GONE);
//                        closeRb.setChecked(true);
//                    } else {
//                        viewAction.setVisibility(View.VISIBLE);
//                        openRb.setChecked(true);
//                    }
//                }
//            } else {
//                rgAction.setVisibility(View.GONE);
//            }
//        }
//    }


    private void getData() {
        Intent intent = getIntent();
        device = (Device) intent.getSerializableExtra(IntentKey.DEVICE);
        if (device != null) {
            deviceId = device.getDeviceId();
            uid = device.getUid();
            deviceName = device.getDeviceName();
        }
        action = (Action) intent.getSerializableExtra(IntentKey.ACTION);
        bindActionType = intent.getIntExtra(IntentKey.BIND_ACTION_TYPE, BindActionType.SCENE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (device != null) {
            outState.putSerializable(IntentKey.DEVICE, device);
        }
        super.onSaveInstanceState(outState);
    }


    protected void initStatus() {
        if (action != null) {
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
                        deviceStatus.setCommand(command);
                    }
                    deviceStatus.setValue1(action.getValue1());
                    deviceStatus.setValue2(action.getValue2());
                    deviceStatus.setValue3(action.getValue3());
                    deviceStatus.setValue4(action.getValue4());
                    deviceStatus.setAlarmType(action.getAlarmType());
                    if (!TextUtils.isEmpty(action.getDeviceId())) {
                        deviceStatus.setDeviceId(action.getDeviceId());
                    }
                }
                onDeviceStatus(deviceStatus);
            } else {
                Action defaultActin = BindTool.getDefaultAction(device);
                onDefaultAction(defaultActin);
            }
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.rbOpen:
                //选择开
                value1 = DeviceStatusConstant.ON;
                //rgb 色温灯 调光灯没有ON，只有对应如下的order by keeu
                final int deviceType = device.getDeviceType();
                if (deviceType == DeviceType.RGB) {
                    command = DeviceOrder.COLOR_CONTROL;
                } else if (deviceType == DeviceType.COLOR_TEMPERATURE_LAMP) {
                    command = DeviceOrder.COLOR_TEMPERATURE;
                } else if (deviceType == DeviceType.DIMMER) {
                    command = DeviceOrder.MOVE_TO_LEVEL;
                } else {
                    command = DeviceOrder.ON;
                }
                //TODO
                if (value2 <= 0) {
                    value2 = 1;
                    LogUtil.e(TAG, "changeView()-value2:" + value2 + " is less than 0.Set it to 1.");
                }
                action.setValue1(value1);
                action.setCommand(command);
                action.setValue2(value2);
                initStatus();
                setBindRadioGroup();
                break;
            case R.id.rbClose:
                //选择关
                command = DeviceOrder.OFF;
                value1 = DeviceStatusConstant.OFF;
                // value2 = value4 = 0;
                setBindRadioGroup();
                break;
            case R.id.rbToggle:
                //选择翻转
                command = DeviceOrder.TOGGLE;
                // value1 = value2 = value4 = 0;
                setBindRadioGroup();
                break;
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

    @Override
    public void onBackPressed() {
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
            keyName = DeviceTool.getActionName(mContext, action);
            action.setKeyName(keyName);
            LogUtil.d(TAG, "onBackPressed()-action:" + action);
            Bundle bundle = new Bundle();
            bundle.putSerializable(IntentKey.ACTION, action);
            intent.putExtras(bundle);
            setResult(RESULT_OK, intent);
        }
        this.finish();
        super.onBackPressed();
    }

    /**
     * 标题栏返回事件
     *
     * @param v
     */
    public void leftTitleClick(View v) {
        onBackPressed();
    }

}
