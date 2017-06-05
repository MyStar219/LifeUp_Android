package com.orvibo.homemate.device.action;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.ImageView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.data.BindType;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.data.DeviceStatusConstant;
import com.orvibo.homemate.data.NavigationType;
import com.orvibo.homemate.view.custom.CurtainControlOldView;

/**
 * 推窗器非百分比控制
 * Created by Allen on 2015/4/20.
 */
public class ActionCurtainOutsideActivity extends BaseActionActivity implements CurtainControlOldView.OnStatusChangedListener {
    private ImageView curtainOutsideView;
    private CurtainControlOldView curtainControlOldView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curtain_outside);
        setBindBar(NavigationType.greenType);
        findViews();
        init();
    }

    private void findViews() {
        curtainOutsideView = (ImageView) findViewById(R.id.curtainOutsideView);
        curtainControlOldView = (CurtainControlOldView) findViewById(R.id.curtainControlOldView);
//        if (bindType == BindType.NEED_TOGGLE) {
//            curtainControlOldView.setStopButtonVisible(true);
//        } else if (bindType == BindType.NOT_NEED_TOGGLE) {
//            curtainControlOldView.setStopButtonVisible(false);
//        }
    }

    private void init() {
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
        if (value1 == DeviceStatusConstant.CURTAIN_OFF) {
            command = DeviceOrder.CLOSE;
            curtainControlOldView.close();
            curtainOutsideView.setImageResource(R.drawable.curtain_outside_off);
            keyName = getResources().getString(R.string.action_off);
        } else if (value1 == DeviceStatusConstant.CURTAIN_ON){
            command = DeviceOrder.OPEN;
            curtainControlOldView.open();
            curtainOutsideView.setImageResource(R.drawable.curtain_outside_on);
            keyName = getResources().getString(R.string.action_on);
        } else if (value1 == DeviceStatusConstant.CURTAIN_STOP) {
            command = DeviceOrder.STOP;
            curtainControlOldView.stop();
            curtainOutsideView.setImageResource(R.drawable.curtain_outside_stop);
            keyName = getResources().getString(R.string.action_stop);
        }
    }

    @Override
    public void onCurtainOpen() {
        curtainControlOldView.open();
        if (value1 == DeviceStatusConstant.CURTAIN_OFF) {
            curtainOutsideView.setImageResource(R.drawable.curtain_outside_on_anim);
        }else if(value1 == DeviceStatusConstant.CURTAIN_STOP){
            curtainOutsideView.setImageResource(R.drawable.curtain_outside_stop_to_on_anim);
        } else {
            return;
        }
        command = DeviceOrder.OPEN;
        value1 = 100;
        ((AnimationDrawable) curtainOutsideView.getDrawable()).start();
        keyName = getResources().getString(R.string.action_on);
    }

    @Override
    public void onCurtainClose() {
        curtainControlOldView.close();
        if (value1 == DeviceStatusConstant.CURTAIN_ON) {
            curtainOutsideView.setImageResource(R.drawable.curtain_outside_off_anim);
        }else if(value1 == DeviceStatusConstant.CURTAIN_STOP){
            curtainOutsideView.setImageResource(R.drawable.curtain_outside_stop_to_off_anim);
        } else {
            return;
        }
        command =  DeviceOrder.CLOSE;
        value1 = 0;
        ((AnimationDrawable) curtainOutsideView.getDrawable()).start();
        keyName = getResources().getString(R.string.action_off);
    }

    @Override
    public void onCurtainStop() {
        curtainControlOldView.stop();
        if (value1 == DeviceStatusConstant.CURTAIN_ON) {
            curtainOutsideView.setImageResource(R.drawable.curtain_outside_on_to_stop_anim);
        } else if (value1 == DeviceStatusConstant.CURTAIN_OFF) {
            curtainOutsideView.setImageResource(R.drawable.curtain_outside_off_to_stop_anim);
        } else {
            return;
        }
        command = DeviceOrder.STOP;
        value1 = 50;
        ((AnimationDrawable) curtainOutsideView.getDrawable()).start();
        keyName = getResources().getString(R.string.action_stop);
    }
}
