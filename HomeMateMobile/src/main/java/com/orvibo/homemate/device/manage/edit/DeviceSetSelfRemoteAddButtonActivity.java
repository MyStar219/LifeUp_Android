package com.orvibo.homemate.device.manage.edit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.common.BaseActivity;

/**
 * Created by smagret on 2015/05/07.
 */
public class DeviceSetSelfRemoteAddButtonActivity extends BaseActivity {
    private TextView tvAddButton;
    private Device device;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_set_self_remote_add_button);
        device = (Device) getIntent().getSerializableExtra("device");
        findViews();
        init();
    }

    private void findViews() {
        tvAddButton = (TextView) findViewById(R.id.tvAddButton);
    }

    private void init() {
        tvAddButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvAddButton:
                Intent intent = new Intent(DeviceSetSelfRemoteAddButtonActivity.this,DeviceSetSelfRemoteButtonNameActivity.class);
                intent.putExtra("device", device);
                startActivity(intent);
                finish();
                break;
        }

    }

}
