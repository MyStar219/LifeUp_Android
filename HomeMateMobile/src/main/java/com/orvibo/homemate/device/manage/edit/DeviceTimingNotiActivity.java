package com.orvibo.homemate.device.manage.edit;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Gateway;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.view.custom.NavigationCocoBar;


/**
 * Created by Allen on 2015/5/28.
 */
public class DeviceTimingNotiActivity extends BaseActivity implements NavigationCocoBar.OnLeftClickListener{
    private NavigationCocoBar navigationBar;
    private TextView deviceModelTextView, deviceHardwareTextView, deviceMacTextView, deviceFirmwareTextView;
    private Gateway gateway;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_timing_noti_activity);
        init();
    }

    private void init() {
        navigationBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        navigationBar.setOnLeftClickListener(this);
    }

    @Override
    public void onLeftClick(View v) {
        finish();
    }

    public void control(View view) {

    }
}
