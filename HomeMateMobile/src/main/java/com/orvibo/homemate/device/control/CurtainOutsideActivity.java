//package com.orvibo.homemate.device.control;
//
//import android.graphics.drawable.AnimationDrawable;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.ImageView;
//
//import com.smartgateway.app.R;
//import com.orvibo.homemate.bo.DeviceStatus;
//import com.orvibo.homemate.dao.DeviceStatusDao;
//import com.orvibo.homemate.data.DeviceStatusConstant;
//import com.orvibo.homemate.view.custom.CurtainControlOldView;
//import com.orvibo.homemate.view.custom.NavigationGreenBar;
//
///**
// * 推窗器
// * Created by Allen on 2015/4/20.
// */
//public class CurtainOutsideActivity extends BaseControlActivity implements CurtainControlOldView.OnStatusChangedListener {
//    private ImageView curtainOutsideView;
//    private CurtainControlOldView curtainControlOldView;
//    private DeviceStatusDao deviceStatusDao;
//    private DeviceStatus deviceStatus;
//    private int currentStatus;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_curtain_outside);
//        findViews();
//        init();
//    }
//
//    private void findViews() {
//        curtainOutsideView = (ImageView) findViewById(R.id.curtainOutsideView);
//        curtainControlOldView = (CurtainControlOldView) findViewById(R.id.curtainControlOldView);
//        curtainControlOldView.setStopButtonVisible(true);
//        NavigationGreenBar navigationGreenBar = (NavigationGreenBar) findViewById(R.id.nbTitle);
//        if (navigationGreenBar != null) {
//            navigationGreenBar.setRightImageViewVisibility(View.VISIBLE);
//            navigationGreenBar.setRightImageViewRes(R.drawable.more_white_selector);
//            navigationGreenBar.setRightTextVisibility(View.GONE);
//            navigationGreenBar.setText(deviceName);
//        }
//}
//
//    private void init() {
//        curtainControlOldView.setOnStatusChangedListener(this);
//
//        deviceStatusDao = new DeviceStatusDao();
//        deviceStatus = deviceStatusDao.selDeviceStatus(uid, deviceId);
//        if (deviceStatus != null) {
//            currentStatus = deviceStatus.getValue1();
//            if (currentStatus == DeviceStatusConstant.CURTAIN_ON) {
//                curtainOutsideView.setImageResource(R.drawable.curtain_outside_on);
//                curtainControlOldView.open();
//            }else if (currentStatus == DeviceStatusConstant.CURTAIN_OFF) {
//                curtainOutsideView.setImageResource(R.drawable.curtain_outside_off);
//                curtainControlOldView.close();
//            }else if (currentStatus == DeviceStatusConstant.CURTAIN_STOP) {
//                curtainOutsideView.setImageResource(R.drawable.curtain_outside_stop);
//                curtainControlOldView.stop();
//            }
//        }
//    }
//    @Override
//    protected boolean onControlResult(String uid, String deviceId, int result) {
//        return super.onControlResult(uid, deviceId, result);
//    }
//
//    @Override
//    public void onPropertyReport(String deviceId, int statusType, int value1, int value2, int value3, int value4, int alarmType) {
//        if (device != null && device.getUid().equals(uid) && device.getDeviceId().equals(deviceId)) {
//            if (value1 == DeviceStatusConstant.CURTAIN_ON) {
//                open();
//                currentStatus = DeviceStatusConstant.CURTAIN_ON;
//            }else if (value1 == DeviceStatusConstant.CURTAIN_OFF) {
//                close();
//                currentStatus = DeviceStatusConstant.CURTAIN_OFF;
//            }else if (value1 == DeviceStatusConstant.CURTAIN_STOP) {
//                stop();
//                currentStatus = DeviceStatusConstant.CURTAIN_STOP;
//            }
//        }
//    }
//
//    private void open() {
//        curtainControlOldView.open();
//        if (currentStatus == DeviceStatusConstant.CURTAIN_OFF) {
//            curtainOutsideView.setImageResource(R.drawable.curtain_outside_on_anim);
//        }else if(currentStatus == DeviceStatusConstant.CURTAIN_STOP){
//            curtainOutsideView.setImageResource(R.drawable.curtain_outside_stop_to_on_anim);
//        }
//        ((AnimationDrawable) curtainOutsideView.getDrawable()).start();
//    }
//
//    private void close() {
//        curtainControlOldView.close();
//
//        if (currentStatus == DeviceStatusConstant.CURTAIN_ON) {
//            curtainOutsideView.setImageResource(R.drawable.curtain_outside_off_anim);
//        }else if(currentStatus == DeviceStatusConstant.CURTAIN_STOP){
//            curtainOutsideView.setImageResource(R.drawable.curtain_outside_stop_to_off_anim);
//        }
//        ((AnimationDrawable) curtainOutsideView.getDrawable()).start();
//    }
//
//    private void stop() {
//        curtainControlOldView.stop();
//        if (currentStatus == DeviceStatusConstant.CURTAIN_ON) {
//            curtainOutsideView.setImageResource(R.drawable.curtain_outside_on_to_stop_anim);
//        } else if (currentStatus == DeviceStatusConstant.CURTAIN_OFF) {
//            curtainOutsideView.setImageResource(R.drawable.curtain_outside_off_to_stop_anim);
//        }
//        ((AnimationDrawable) curtainOutsideView.getDrawable()).start();
//    }
//
//    @Override
//    public void onCurtainOpen() {
//        controlDevice.curtainOpen(uid, deviceId);
//        showDialog();
//    }
//
//    @Override
//    public void onCurtainClose() {
//        controlDevice.curtainClose(uid, deviceId);
//        showDialog();
//    }
//
//    @Override
//    public void onCurtainStop() {
//        controlDevice.curtainStop(uid, deviceId);
//        showDialog();
//    }
//
//}

