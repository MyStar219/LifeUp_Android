package com.orvibo.homemate.device.action;

import android.os.Bundle;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.data.BindType;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.data.DeviceStatusConstant;
import com.orvibo.homemate.data.NavigationType;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.view.custom.Curtain2HalvesInsideView;
import com.orvibo.homemate.view.custom.CurtainControlOldView;

/**
 * 对开窗帘不支持百分比控制
 * Created by smagret on 2015/04/18
 */
public class ActionCurtain2HalvesOldActivity extends BaseActionActivity implements CurtainControlOldView.OnStatusChangedListener {
    private static final String TAG = ActionCurtain2HalvesOldActivity.class
            .getSimpleName();
    private Curtain2HalvesInsideView curtain2HalvesView;
    private CurtainControlOldView curtainControlOldView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curtain_2halves_old);
        setBindBar(NavigationType.greenType);
        init();
    }

    private void init() {
        mContext = ActionCurtain2HalvesOldActivity.this;
        initView();
        initListener();
    }

    private void initView() {
        curtain2HalvesView = (Curtain2HalvesInsideView) findViewById(R.id.curtain2HalvesInsideView);
        curtainControlOldView = (CurtainControlOldView) findViewById(R.id.curtainControlOldView);
//        if (bindType == BindType.NEED_TOGGLE) {
//            curtainControlOldView.setStopButtonVisible(true);
//        } else if (bindType == BindType.NOT_NEED_TOGGLE) {
//            curtainControlOldView.setStopButtonVisible(false);
//        }
    }

    private void initListener() {
        curtainControlOldView.setOnStatusChangedListener(this);
    }

    @Override
    protected void onSelectedAction(Action action) {
        value1 = action.getValue1();
        setStatus(value1);
    }

    @Override
    protected void onDeviceStatus(DeviceStatus deviceStatus) {
        super.onDeviceStatus(deviceStatus);
        value1 = deviceStatus.getValue1();
        setStatus(value1);
    }

    @Override
    protected void onDefaultAction(Action defaultAction) {
        super.onDefaultAction(defaultAction);
        value1 = defaultAction.getValue1();
//        value1 = DeviceStatusConstant.CURTAIN_ON;
        setStatus(value1);
    }

    private void setStatus(int value1) {
        LogUtil.d(TAG, "setStatus()-value1:" + value1);
        if (value1 == DeviceStatusConstant.CURTAIN_OFF) {
            command = DeviceOrder.CLOSE;
            curtainControlOldView.close();
            curtain2HalvesView.setProgress(0);
            keyName = getResources().getString(R.string.action_off);
        } else if (value1 == DeviceStatusConstant.CURTAIN_ON) {
            command = DeviceOrder.OPEN;
            curtainControlOldView.open();
            curtain2HalvesView.setProgress(100);
            keyName = getResources().getString(R.string.action_on);
        } else if (value1 == DeviceStatusConstant.CURTAIN_STOP) {
//            if (bindType == BindType.NEED_TOGGLE) {
                command = DeviceOrder.STOP;
                curtainControlOldView.stop();
                curtain2HalvesView.setProgress(50);
                keyName = getResources().getString(R.string.action_stop);
//            } else if (bindType == BindType.NOT_NEED_TOGGLE) {
//                //产品设计问题到导致情景联动普通窗帘没有停，所以停默认显示开
//                command = DeviceOrder.OPEN;
//                curtainControlOldView.open();
//                curtain2HalvesView.setProgress(100);
//                keyName = getResources().getString(R.string.action_on);
//            }
        }
    }

    @Override
    public void onCurtainOpen() {
        curtainControlOldView.open();
        curtain2HalvesView.setProgress(100);
        command = DeviceOrder.OPEN;
        value1 = 100;
        keyName = getResources().getString(R.string.action_on);
    }

    @Override
    public void onCurtainClose() {
        curtainControlOldView.close();
        curtain2HalvesView.setProgress(0);
        command = DeviceOrder.CLOSE;
        value1 = 0;
        keyName = getResources().getString(R.string.action_off);
    }

    @Override
    public void onCurtainStop() {
        curtainControlOldView.stop();
        curtain2HalvesView.setProgress(50);
        command = DeviceOrder.STOP;
        value1 = 50;
        keyName = getResources().getString(R.string.action_stop);
    }
}
