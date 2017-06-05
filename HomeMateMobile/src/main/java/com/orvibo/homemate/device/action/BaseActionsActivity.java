//package com.orvibo.homemate.device.action;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.View;
//
//import com.smartgateway.app.R;
//import com.orvibo.homemate.bo.Action;
//import com.orvibo.homemate.bo.Device;
//import com.orvibo.homemate.bo.DeviceStatus;
//import com.orvibo.homemate.common.BaseActivity;
//import com.orvibo.homemate.data.IntentKey;
//import com.orvibo.homemate.data.NavigationType;
//import com.orvibo.homemate.dao.DeviceStatusDao;
//import com.orvibo.homemate.util.BindTool;
//import com.orvibo.homemate.util.LogUtil;
//import com.orvibo.homemate.view.custom.NavigationGreenBar;
//import com.orvibo.homemate.view.custom.NavigationWhiteBar;
//
//import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * 一个设备可以选择多个动作，目前只有红外设备。
// * Created by huangqiyao on 2015/10/08
// */
//public class BaseActionsActivity extends BaseActivity {
//    private static final String TAG = BaseActionsActivity.class
//            .getSimpleName();
//    protected Device device;
//    protected DeviceStatusDao deviceStatusDao;
//    protected DeviceStatus deviceStatus;
//    protected String deviceName;
//    protected String deviceId;
//    protected String uid;
//    protected List<Action> actions;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        getData();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        initStatus();
//    }
//
//    protected void setBindBar(int barType) {
//        if (barType == NavigationType.greenType) {
//            NavigationGreenBar navigationGreenBar = (NavigationGreenBar) findViewById(R.id.nbTitle);
//            if (navigationGreenBar != null) {
//                navigationGreenBar.setText(getString(R.string.device_timing_set_action));
//                navigationGreenBar.setRightText(getString(R.string.save));
//            }
//        } else if (barType == NavigationType.whiteType) {
//            NavigationWhiteBar navigationWhiteBar = (NavigationWhiteBar) findViewById(R.id.nbTitle);
//            if (navigationWhiteBar != null) {
//                navigationWhiteBar.setText(getString(R.string.device_timing_set_action));
//                navigationWhiteBar.setRightText(getString(R.string.save));
//            }
//        }
//    }
//
//    protected void setBarRightText(String text) {
//        NavigationGreenBar navigationGreenBar = (NavigationGreenBar) findViewById(R.id.nbTitle);
//        navigationGreenBar.setRightText(text);
//    }
//
//    private void getData() {
//        Intent intent = getIntent();
//        device = (Device) intent.getSerializableExtra(IntentKey.DEVICE);
//        if (device != null) {
//            deviceId = device.getDeviceId();
//            uid = device.getUid();
//            deviceName = device.getDeviceName();
//        }
//        actions = (ArrayList<Action>) intent.getSerializableExtra(IntentKey.ACTIONS);
//        LogUtil.d(TAG, "getData()-device:" + device + ",actions:" + actions);
//    }
//
//    protected void initStatus() {
//        if (actions != null && !actions.isEmpty()) {
//            onSelectedActions(actions);
//            //device = new DeviceDao().selDevice(uid, deviceId);
//        } else {
//            deviceStatusDao = new DeviceStatusDao();
//            deviceStatus = deviceStatusDao.selDeviceStatus(uid, deviceId);
//            LogUtil.d(TAG, "initStatus()-deviceStatus:" + deviceStatus);
//            //默认值不是设备当前值
//            if (deviceStatus != null) {
//                onDeviceStatus(deviceStatus);
//            } else {
//                Action defaultActin = BindTool.getDefaultAction(device);
//                if (defaultActin != null) {
//                    String command = BindTool.getInitCommand(device, deviceStatus);
//                    if (command != null) {
//                        defaultActin.setCommand(command);
//                    }
//                    onDefaultAction(defaultActin);
//                }
//            }
//        }
//    }
//
//    /**
//     * 已被选中的状态
//     *
//     * @param actions
//     */
//    protected void onSelectedActions(List<Action> actions) {
//        LogUtil.d(TAG, "onSelectedActions()-actions:" + actions);
//    }
//
//    /**
//     * 设备当前真实的状态
//     *
//     * @param deviceStatus
//     */
//    protected void onDeviceStatus(DeviceStatus deviceStatus) {
//        LogUtil.d(TAG, "onDeviceStatus()-deviceStatus:" + deviceStatus);
//    }
//
//    /**
//     * 设备默认的状态
//     */
//    protected void onDefaultAction(Action action) {
//        LogUtil.d(TAG, "onDefaultAction()-action:" + action);
//    }
//
//    public void rightTitleClick(View view) {
//        Intent intent = new Intent();
//        Bundle bundle = new Bundle();
//        bundle.putSerializable(IntentKey.ACTIONS, (Serializable) actions);
//        intent.putExtras(bundle);
//        setResult(RESULT_OK, intent);
//        this.finish();
//    }
//}
