package com.orvibo.homemate.device.allone2.add;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.smartgateway.app.R;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.allone2.ActivityFinishEvent;
import com.orvibo.homemate.device.control.BaseControlActivity;

/**
 * Created by yuwei on 2016/4/13.
 * 匹配和复制遥控器界面
 */
public class RemoteMatchOrCopyActivity extends BaseControlActivity {

    private int device_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_remote_control);

        device_type = getIntent().getIntExtra(IntentKey.DEVICE_ADD_TYPE, -1);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_match_remote_control:
                matchRemoteControl(device_type);
                break;
            case R.id.ll_copy_remote_control:
                Intent intent = new Intent(this, RemoteNameAddActivity.class);
                intent.putExtra(IntentKey.DEVICE, device);
                intent.putExtra(IntentKey.DEVICE_ADD_TYPE,device_type);
                startActivity(intent);
                finish();
                break;
        }
    }

    private void matchRemoteControl(int devicetype) {
        switch (devicetype) {
            case DeviceType.STB:
                Intent intent = new Intent(RemoteMatchOrCopyActivity.this, StbBrandListActivity.class);
                intent.putExtra(IntentKey.DEVICE, device);
                intent.putExtra(IntentKey.DEVICE_ADD_TYPE, DeviceType.STB);
                startActivity(intent);
                break;
            case DeviceType.TV:
                Intent tvIntent = new Intent(RemoteMatchOrCopyActivity.this, DeviceBrandListActivity.class);
                tvIntent.putExtra(IntentKey.DEVICE, device);
                tvIntent.putExtra(IntentKey.DEVICE_ADD_TYPE, DeviceType.TV);
                startActivity(tvIntent);
                break;
        }
    }

    /**
     * 添加遥控器成功后finish掉添加的activity
     * @param event
     */
    public void onEventMainThread(ActivityFinishEvent event) {
        finish();
    }
}
