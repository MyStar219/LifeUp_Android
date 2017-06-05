package com.orvibo.homemate.device.control;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.view.custom.NavigationGreenBar;

/**
 * Created by smagret on 2015/05/27
 */
public class BaseIrActivity extends BaseActivity {
    private static final String TAG = BaseIrActivity.class
            .getSimpleName();
    protected Device device;
    protected String deviceName;
    protected String deviceId;
    protected String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getData();
    }

    protected void setIrBar() {
        NavigationGreenBar navigationBar = (NavigationGreenBar) findViewById(R.id.nbTitle);
        TextView ir_learn_tips = (TextView) findViewById(R.id.ir_learn_tips);
        if (navigationBar != null) {
            navigationBar.setText(getString(R.string.learn_ir));
            navigationBar.setRightText("");
        }
        if (ir_learn_tips != null) {
            ir_learn_tips.setVisibility(View.VISIBLE);
        }
    }

    protected void setIrEditBar() {
        NavigationGreenBar navigationBar = (NavigationGreenBar) findViewById(R.id.nbTitle);
        TextView ir_edit_tips = (TextView) findViewById(R.id.ir_edit_tips);
        if (navigationBar != null) {
            navigationBar.setText(getString(R.string.device_set_self_remote_edit_key));
            navigationBar.setRightText("");
        }
        if (ir_edit_tips != null) {
            ir_edit_tips.setVisibility(View.VISIBLE);
        }
    }

    private void getData() {
        Intent intent = getIntent();
        device = (Device) intent.getSerializableExtra(IntentKey.DEVICE);
        if (device != null) {
            deviceId = device.getDeviceId();
            uid = device.getUid();
            deviceName = device.getDeviceName();
        }
    }
}
