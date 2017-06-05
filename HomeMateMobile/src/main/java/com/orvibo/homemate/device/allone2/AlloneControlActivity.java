package com.orvibo.homemate.device.allone2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.allone2.add.RemoteAddActivity;
import com.orvibo.homemate.device.control.BaseControlActivity;
import com.orvibo.homemate.device.manage.SetDeviceActivity;
import com.orvibo.homemate.view.custom.NavigationCocoBar;

import java.io.Serializable;

/**
 * Created by snown on 2016/3/30.
 * allone2界面
 */
public class AlloneControlActivity extends BaseControlActivity implements NavigationCocoBar.OnRightClickListener {

    private NavigationCocoBar navigationBar;
    private TextView deviceAddedNum;
    private LinearLayout deviceAdd;
    private int deviceNum;//allone下子设备数量

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allone_control);
        initView();
    }

    /**
     * 初始化控件、参数
     */
    private void initView() {
        this.deviceAdd = (LinearLayout) findViewById(R.id.deviceAdd);
        this.deviceAddedNum = (TextView) findViewById(R.id.deviceAddedNum);
        this.navigationBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        navigationBar.setOnRightClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (device != null) {
            deviceNum = new DeviceDao().selAlloneDevice(device).size();
            deviceAddedNum.setText(deviceNum+"");
            navigationBar.setCenterText(device.getDeviceName());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Serializable serializable = intent.getSerializableExtra(IntentKey.DEVICE);
        if (serializable != null && serializable instanceof Device) {
            device = (Device) serializable;
            deviceId = device.getDeviceId();
            uid = device.getUid();
            deviceName = device.getDeviceName();
        }
    }


    @Override
    public void onRightClick(View v) {
        Intent intent = new Intent(this, SetDeviceActivity.class);
        intent.putExtra(Constant.DEVICE, device);
        startActivity(intent);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.deviceAdd:
                Intent intent = new Intent(AlloneControlActivity.this, RemoteAddActivity.class);
                intent.putExtra(IntentKey.DEVICE, device);
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 添加遥控器成功后finish掉添加的activity
     * @param event
     */
    public void onEventMainThread(ActivityFinishEvent event) {
            finish();
    }
}
