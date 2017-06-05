package com.orvibo.homemate.device.action;

import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.data.DeviceStatusConstant;
import com.orvibo.homemate.data.NavigationType;
import com.orvibo.homemate.util.LogUtil;

/**
 * Created by Allen on 2015/4/9.
 */
public class ActionSecurityActivity extends BaseActionActivity implements CompoundButton.OnCheckedChangeListener {
    private static final String TAG = ActionSecurityActivity.class
            .getSimpleName();
    private Switch switchSecurity;
    private ImageView ivDeployed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security);
        findViews();
        setBindBar(NavigationType.greenType);
    }

    private void findViews() {
        ivDeployed = (ImageView) findViewById(R.id.ivDeployed);
        switchSecurity = (Switch) findViewById(R.id.switchSecurity);
        switchSecurity.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        setChecked(isChecked);
        if (isChecked) {
            command = DeviceOrder.ALARM;
            value1 = DeviceStatusConstant.ARM;
        } else {
            command = DeviceOrder.DISALARM;
            value1 = DeviceStatusConstant.DISARM;
        }
        LogUtil.d(TAG, "onCheckedChanged()-command:" + command + ",value1:" + value1);
    }

    @Override
    protected void onSelectedAction(Action action) {
        super.onSelectedAction(action);
        setAction(action);
    }

    @Override
    protected void onDefaultAction(Action action) {
        super.onDefaultAction(action);
        setAction(action);
    }

    @Override
    protected void onDeviceStatus(DeviceStatus deviceStatus) {
        super.onDeviceStatus(deviceStatus);
        setAction(deviceStatus);
    }

    private void setAction(Action action) {
        command = action.getCommand();
        keyName = action.getKeyName();
        value1 = action.getValue1();
        if (command != null && command.equals(DeviceOrder.ALARM)) {
            command = DeviceOrder.ALARM;
        } else {
            command = DeviceOrder.DISALARM;
        }
        switchStatus(command.equals(DeviceOrder.ALARM));
    }

    private void setChecked(boolean isChecked) {
        if (isChecked) {
            AlphaAnimation showAnimation = new AlphaAnimation(0f, 1f);
            showAnimation.setDuration(100);
            showAnimation.setFillAfter(true);
            ivDeployed.startAnimation(showAnimation);
        } else {
            AlphaAnimation hideAnimation = new AlphaAnimation(1f, 0f);
            hideAnimation.setDuration(100);
            hideAnimation.setFillAfter(true);
            ivDeployed.startAnimation(hideAnimation);
        }
    }

    private void switchStatus(boolean isArm) {
        LogUtil.d(TAG, "switchStatus()-isArm:" + isArm);
        String oldCon = switchSecurity.getContentDescription() + "";
        String curCon = getCon(isArm);
        switchSecurity.setChecked(isArm);
        setChecked(isArm);
        if (!oldCon.equals(curCon)) {
            switchSecurity.setContentDescription(curCon);
        }
    }

    private String getCon(boolean isOn) {
        return isOn ? "arm" : "disarm";
    }
}
