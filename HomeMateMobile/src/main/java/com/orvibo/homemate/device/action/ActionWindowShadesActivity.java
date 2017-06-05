//package com.orvibo.homemate;
//
//import android.os.Bundle;
//
//import com.orvibo.homemate.bo.Action;
//import com.orvibo.homemate.bo.DeviceStatus;
//import com.orvibo.homemate.data.DeviceOrder;
//import com.orvibo.homemate.data.NavigationType;
//import com.orvibo.homemate.util.LogUtil;
//import com.orvibo.homemate.view.CurtainRollerView;
//
///**
// * Created by smagret on 2015/04/18
// */
//public class ActionWindowShadesActivity extends BaseActionActivity implements CurtainRollerView.OnProgressChangedListener {
//    private static final String TAG = ActionWindowShadesActivity.class
//            .getSimpleName();
//    private CurtainRollerView curtainDropdownView;
//    private int currentProgress;
//    private int progress;
//    private float percent;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_curtain_dropdown);
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
//        curtainDropdownView.setAsWindowShades();
//        setBindBar(NavigationType.greenType);
//    }
//
//    private void initListener() {
//        curtainDropdownView.setOnProgressChangedListener(this);
//    }
//
//    @Override
//    public void onProgressChanged(int progress) {
//        LogUtil.d(TAG, "progress:" + progress);
//    }
//
//    @Override
//    public void onProgressFinish(int progress) {
//        LogUtil.d(TAG, "progress:" + progress);
//        percent = progress;
//        value1 = progress;
//        if (currentProgress < progress) {
//            command = DeviceOrder.CLOSE;
//            String curtainString = getResources().getString(R.string.device_timing_action_curtain_percent);
//            keyName = String.format(curtainString, deviceName, command, percent + "%");
//
//        } else if (currentProgress > progress) {
//            command = DeviceOrder.OPEN;
//            String curtainString = getResources().getString(R.string.device_timing_action_curtain_percent);
//            keyName = String.format(curtainString, deviceName, command, percent + "%");
//        }
//    }
//
//    @Override
//    protected void onSelectedAction(Action action) {
//        super.onSelectedAction(action);
//        setAction(deviceStatus);
//    }
//
//    @Override
//    protected void onDeviceStatus(DeviceStatus deviceStatus) {
//        super.onDeviceStatus(deviceStatus);
//        setAction(deviceStatus);
//    }
//
//    @Override
//    protected void onDefaultAction(Action action) {
//        super.onDefaultAction(action);
//        setAction(action);
//    }
//
//    private void setAction(Action action) {
//        command = action.getCommand();
//        keyName = action.getKeyName();
//        progress = action.getValue1();
//        currentProgress = progress;
//        curtainDropdownView.setProgress(currentProgress);
//    }
//}
