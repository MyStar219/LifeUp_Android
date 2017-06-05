package com.orvibo.homemate.device.manage.edit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.model.ModifyDevice;
import com.orvibo.homemate.util.ToastUtil;

/**
 * Created by allen on 2015/10/10.
 */
public class SensorDevicePositionActivity extends BaseActivity {
    private TextView doorTextView,windowTextView,drawerTextView,otherTextView;
    private View door,window,drawer,other;
    private ModifyDevice modifyDevice;
    private Device device;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_device_position_activity);

        init();
    }

    private void init() {
        device = (Device) getIntent().getSerializableExtra(Constant.DEVICE);
        doorTextView = (TextView) findViewById(R.id.doorTextView);
        door = findViewById(R.id.door);
        door.setOnClickListener(this);
        windowTextView = (TextView) findViewById(R.id.windowTextView);
        window = findViewById(R.id.window);
        window.setOnClickListener(this);
        drawerTextView = (TextView) findViewById(R.id.drawerTextView);
        drawer = findViewById(R.id.drawer);
        drawer.setOnClickListener(this);
        otherTextView = (TextView) findViewById(R.id.otherTextView);
        other = findViewById(R.id.other);
        other.setOnClickListener(this);
        int deviceType = device.getDeviceType();
        switch (deviceType) {
            case DeviceType.MAGNETIC:
                selectDoor();
                break;
            case DeviceType.MAGNETIC_DRAWER:
                selectDrawer();
                break;
            case DeviceType.MAGNETIC_WINDOW:
                selectWindow();
                break;
            case DeviceType.MAGNETIC_OTHER:
                selectOther();
                break;
        }
        initModifyDevice();
    }

    private void initModifyDevice() {
        modifyDevice = new ModifyDevice(mAppContext) {
            @Override
            public void onModifyDeviceResult(String uid, int serial, int result) {
                super.onModifyDeviceResult(uid, serial, result);
                dismissDialog();
                if (result == ErrorCode.SUCCESS) {
                    Intent intent = new Intent();
                    intent.putExtra(Constant.DEVICE, device);
                    setResult(RESULT_OK, intent);
                    finish();
                } else if (result == ErrorCode.TIMEOUT_MD) {
                    ToastUtil.showToast(getString(R.string.TIMEOUT));
                } else {
                    if (!ToastUtil.toastCommonError(result)) {
                        ToastUtil.showToast(getString(R.string.device_name_change_fail));
                    }
                }
            }
        };
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int deviceType = DeviceType.MAGNETIC;
        switch(v.getId()) {
            case R.id.door:
                selectDoor();
                deviceType = DeviceType.MAGNETIC;
                break;
            case R.id.window:
                selectWindow();
                deviceType = DeviceType.MAGNETIC_WINDOW;
                break;
            case R.id.drawer:
                selectDrawer();
                deviceType = DeviceType.MAGNETIC_DRAWER;
                break;
            case R.id.other:
                selectOther();
                deviceType = DeviceType.MAGNETIC_OTHER;
                break;
        }
        showDialog();
        device.setDeviceType(deviceType);
        modifyDevice.modify(device.getUid(), device.getUserName(), device.getDeviceName(), deviceType, device.getRoomId(), device.getIrDeviceId(), device.getDeviceId());
    }

    private void selectDoor() {
        reset();
        doorTextView.setTextColor(getResources().getColor(R.color.black));
        doorTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.device_position_door_selected, 0, 0);
        door.setBackgroundResource(R.drawable.device_position_selected);
    }
    private void selectWindow() {
        reset();
        windowTextView.setTextColor(getResources().getColor(R.color.black));
        windowTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.device_position_window_selected, 0, 0);
        window.setBackgroundResource(R.drawable.device_position_selected);
    }
    private void selectDrawer() {
        reset();
        drawerTextView.setTextColor(getResources().getColor(R.color.black));
        drawerTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.device_position_drawer_selected, 0, 0);
        drawer.setBackgroundResource(R.drawable.device_position_selected);
    }
    private void selectOther() {
        reset();
        otherTextView.setTextColor(getResources().getColor(R.color.black));
        otherTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.device_position_other_selected, 0, 0);
        other.setBackgroundResource(R.drawable.device_position_selected);
    }

    private void reset() {
        doorTextView.setTextColor(getResources().getColor(R.color.gray));
        doorTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.device_position_door_normal, 0, 0);
        door.setBackgroundColor(getResources().getColor(R.color.white));
        windowTextView.setTextColor(getResources().getColor(R.color.gray));
        windowTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.device_position_window_normal, 0 , 0);
        window.setBackgroundColor(getResources().getColor(R.color.white));
        drawerTextView.setTextColor(getResources().getColor(R.color.gray));
        drawerTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.device_position_drawer_normal, 0 , 0);
        drawer.setBackgroundColor(getResources().getColor(R.color.white));
        otherTextView.setTextColor(getResources().getColor(R.color.gray));
        otherTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.device_position_other_normal, 0 , 0);
        other.setBackgroundColor(getResources().getColor(R.color.white));
    }
}
