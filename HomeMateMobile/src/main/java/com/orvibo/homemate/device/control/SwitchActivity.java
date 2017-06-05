//package com.orvibo.homemate;
//
//import android.graphics.drawable.AnimationDrawable;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.ImageView;
//
//import com.orvibo.homemate.bo.DeviceStatus;
//import com.orvibo.homemate.dao.DeviceStatusDao;
//import com.orvibo.homemate.data.DeviceStatusConstant;
//import com.orvibo.homemate.data.ErrorCode;
//import com.orvibo.homemate.util.LogUtil;
//import com.orvibo.homemate.view.IrButton;
//import com.orvibo.homemate.view.NavigationGreenBar;
//
///**
// * @deprecated
// * Created by Allen on 2015/4/8.
// */
//public class SwitchActivity extends BaseControlActivity {
//    private static final String TAG = SwitchActivity.class
//            .getSimpleName();
//    private ImageView ivSwitch;
//    private IrButton btnSwitchOn, btnSwitchOff;
//    private boolean isOn = false;
//    private NavigationGreenBar nb_title;
//    private DeviceStatusDao deviceStatusDao;
//    private DeviceStatus deviceStatus;
//    private int currentStatus;
//    private boolean inited = false;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_switch);
//        LogUtil.d(TAG, "onCreate()");
//        init();
//    }
//
//    private void init() {
//        initView();
//        initListener();
//        initData();
//    }
//
//    private void initView() {
//        ivSwitch = (ImageView) findViewById(R.id.ivSwitch);
//        btnSwitchOn = (IrButton) findViewById(R.id.btnSwitchOn);
//        btnSwitchOff = (IrButton) findViewById(R.id.btnSwitchOff);
//        nb_title = (NavigationGreenBar) findViewById(R.id.nb_title);
//    }
//
//    private void initListener() {
//        btnSwitchOn.setOnClickListener(this);
//        btnSwitchOff.setOnClickListener(this);
//    }
//
//    private void initData() {
//        nb_title.setText(deviceName);
//        nb_title.setRightText(getResources().getString(R.string.device_timing));
//        deviceStatusDao = new DeviceStatusDao();
//        deviceStatus = deviceStatusDao.selDeviceStatus(uid, deviceId);
//        if (deviceStatus != null) {
//            currentStatus = deviceStatus.getValue1();
//            switchStatus(currentStatus == DeviceStatusConstant.ON);
//        }
//    }
//
//    @Override
//    protected boolean onControlResult(String uid, String deviceId, int result) {
//        LogUtil.i(TAG, "onControlResult()-uid:" + uid + ",deviceId:" + deviceId + ",result:" + result);
//        dismissDialog();
//        if (result == ErrorCode.SUCCESS) {
////                ToastUtil.toastError(ErrorCode.SUCCESS);
//            switchStatus(!isOn);
//        }
//        return super.onControlResult(uid, deviceId, result);
//    }
//
//    @Override
//    public void onPropertyReport(String deviceId, int statusType, int value1, int value2, int value3, int value4, int alarmType) {
//        LogUtil.d(TAG, "onPropertyReport()-value1:" + value1);
//        switchStatus(value1 == DeviceStatusConstant.ON);
//    }
//
//    @Override
//    public void onClick(View v) {
//        //压力测试
////         mTestHandler.sendEmptyMessage(1);
//        switch (v.getId()) {
//            case R.id.btnSwitchOn:
//                control(true);
//                break;
//            case R.id.btnSwitchOff:
//                control(false);
//                break;
//        }
//    }
//
//    private void control(boolean isOn) {
//        this.isOn = isOn;
//        LogUtil.d(TAG, "control()-isOn:" + isOn);
//        showDialog();
//        if (isOn) {
//            controlDevice.on(uid, deviceId);
//        } else {
//            controlDevice.off(uid, deviceId);
//        }
//    }
//
//    private void switchStatus(boolean isOn) {
//        //LogUtil.d(TAG, "switchStatus()-isOn:" + isOn);
//        String oldCon = ivSwitch.getContentDescription() + "";
//        String curCon = getCon(isOn);
//        if (!oldCon.equals(curCon)) {
//            try {
//                if (inited) {
//                    ivSwitch.setImageResource(isOn ? R.drawable.switch_on_anim : R.drawable.switch_off_anim);
//                    ivSwitch.setContentDescription(getCon(isOn));
//                    ((AnimationDrawable) ivSwitch.getDrawable()).start();
//                } else {
//                    inited = true;
//                    ivSwitch.setImageResource(isOn ? R.drawable.switch_on : R.drawable.switch_off);
//                }
//            } catch (Exception e) {
//                // 异常扑捉，防止引起崩溃
//                ivSwitch.setImageResource(isOn ? R.drawable.switch_on : R.drawable.switch_off);
//            }
//        }
//    }
//
//    private String getCon(boolean isOn) {
//        return isOn ? "on" : "off";
//    }
//
//    //测试
////    int count = 0;
////    int totalCount = 50;
////    int time = 200;
////    private Handler mTestHandler = new Handler() {
////        @Override
////        public void handleMessage(Message msg) {
////            if (msg.what == 1) {
////                if (count < totalCount) {
////                    control(count % 2 == 0);
////                    sendEmptyMessageDelayed(1, time);
////                    count++;
////                } else {
////                    count = 0;
////                }
////            }
////        }
////    };
//}
