package com.orvibo.homemate.device.allone2.add;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.smartgateway.app.R;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.allone2.ActivityFinishEvent;
import com.orvibo.homemate.device.control.BaseControlActivity;
import com.orvibo.homemate.util.AlloneUtil;

/**
 * allone设备添加类
 * Created by snown on 2016/2/22.
 * update by yuwei
 */
public class RemoteAddActivity extends BaseControlActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_one_device_add);
    }


    @Override
    public void onClick(View v) {
        Intent intent = AlloneUtil.getAddIntent(v.getId(), this);
        intent.putExtra(IntentKey.DEVICE, device);
        startActivity(intent);
    }

    /**
     * 添加遥控器成功后finish掉添加的activity
     * @param event
     */
    public void onEventMainThread(ActivityFinishEvent event) {
        finish();
    }
}
