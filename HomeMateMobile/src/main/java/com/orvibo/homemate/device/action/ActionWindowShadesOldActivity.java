//package com.orvibo.homemate;
//
//import android.os.Bundle;
//
//import com.orvibo.homemate.bo.Action;
//import com.orvibo.homemate.bo.DeviceStatus;
//import com.orvibo.homemate.data.DeviceOrder;
//import com.orvibo.homemate.data.DeviceStatusConstant;
//import com.orvibo.homemate.data.NavigationType;
//import com.orvibo.homemate.util.LogUtil;
//import com.orvibo.homemate.view.CurtainControlOldView;
//import com.orvibo.homemate.view.CurtainRollerView;
//
///**
// * Created by smagret on 2015/04/18
// */
//public class ActionWindowShadesOldActivity extends BaseActionActivity implements CurtainControlOldView.OnStatusChangedListener{
//    private static final String TAG = ActionWindowShadesOldActivity.class
//            .getSimpleName();
//    private CurtainRollerView curtainDropdownView;
//    private CurtainControlOldView curtainControlOldView;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_curtain_dropdown_old);
//        init();
//    }
//
//    private void init() {
//        initView();
//        initListener();
//    }
//
//    private void initView() {
//        curtainDropdownView = (CurtainRollerView) findViewById(R.id.curtainDropdownView);
//        curtainDropdownView.setTouchable(false);
//        curtainDropdownView.setAsWindowShades();
//        curtainControlOldView = (CurtainControlOldView) findViewById(R.id.curtainControlOldView);
//        curtainControlOldView.setStopButtonVisible(false);
//        setBindBar(NavigationType.greenType);
//    }
//
//    private void initListener() {
//        curtainControlOldView.setOnStatusChangedListener(this);
//    }
//
//
//    @Override
//    protected void onSelectedAction(Action action) {
//        value1 = action.getValue1();
//        setStatus(value1);
//    }
//
//    @Override
//    protected void onDeviceStatus(DeviceStatus deviceStatus) {
//        super.onDeviceStatus(deviceStatus);
//        value1 = deviceStatus.getValue1();
//        setStatus(value1);
//    }
//
//    @Override
//    protected void onDefaultAction(Action defaultAction) {
//        super.onDefaultAction(defaultAction);
//        value1 = defaultAction.getValue1();
////        value1 = DeviceStatusConstant.CURTAIN_ON;
//        setStatus(value1);
//    }
//
//    private void setStatus(int value1) {
//        LogUtil.d(TAG, "setStatus()-value1:" + value1);
//        if (value1 == DeviceStatusConstant.CURTAIN_OFF) {
//            command = DeviceOrder.CLOSE;
//            curtainControlOldView.close();
//            curtainDropdownView.setProgress(0);
//            keyName = getResources().getString(R.string.action_off);
//        } else {
//            command = DeviceOrder.OPEN;
//            curtainControlOldView.open();
//            curtainDropdownView.setProgress(100);
//            keyName = getResources().getString(R.string.action_on);
//        }
//    }
//
//    @Override
//    public void onCurtainOpen() {
//        curtainDropdownView.setProgress(100);
//        command = DeviceOrder.OPEN;
//        value1 = 100;
//    }
//
//    @Override
//    public void onCurtainClose() {
//        curtainDropdownView.setProgress(0);
//        command = DeviceOrder.CLOSE;
//        value1 = 0;
//    }
//
//    @Override
//    public void onCurtainStop() {
//        curtainDropdownView.setProgress(50);
//        command = DeviceOrder.STOP;
//        value1 = 50;
//    }
//}
