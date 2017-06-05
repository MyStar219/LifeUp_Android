package com.orvibo.homemate.device.control.infrareddevice;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.DeviceIr;
import com.orvibo.homemate.dao.DeviceIrDao;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.NavigationType;
import com.orvibo.homemate.device.control.BaseControlActivity;
import com.orvibo.homemate.device.manage.edit.DeviceSetSelfRemoteButtonNameActivity;
import com.orvibo.homemate.view.custom.NavigationGreenBar;

import java.util.List;

/**
 * Created by smagret on 2015/05/07.
 */
public class DeviceSetSelfRemoteActivity extends BaseControlActivity implements SelfRemoteButtonAdapter.OnSetSelfRemoteButtonNameListener {
    private List<DeviceIr> deviceIrs;
    private SelfRemoteButtonAdapter selfRemoteButtonAdapter;
    private GridView gridView;
    private DeviceIrDao deviceIrDao;
    private View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_set_self_remote);
        findViews();
    }

    private void findViews() {
        gridView = (GridView) findViewById(R.id.gridView);
        emptyView = findViewById(R.id.emptyView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setControlDeviceBar(NavigationType.greenType, deviceName);
        init();
    }

    private void init() {
        deviceIrDao = new DeviceIrDao();
        deviceIrs = deviceIrDao.selDeviceIrs(uid, deviceId);
        selfRemoteButtonAdapter = new SelfRemoteButtonAdapter(this, deviceIrs, SelfRemoteButtonAdapter.TYPE_CONTROL);
        selfRemoteButtonAdapter.setOnToActivityListener(this);
        gridView.setAdapter(selfRemoteButtonAdapter);
        gridView.setEmptyView(emptyView);
        NavigationGreenBar navigationGreenBar = (NavigationGreenBar) findViewById(R.id.nbTitle);
        if (navigationGreenBar != null) {
            navigationGreenBar.setRightImageViewVisibility(View.VISIBLE);
            navigationGreenBar.setRightImageViewRes(R.drawable.more_white_selector);
            navigationGreenBar.setRightTextVisibility(View.GONE);
        }
    }

    @Override
    public void onSetSelfRemoteButtonName() {
        Intent intent = new Intent(DeviceSetSelfRemoteActivity.this, DeviceSetSelfRemoteButtonNameActivity.class);
        intent.putExtra(IntentKey.DEVICE, device);
        startActivity(intent);
    }

//    public void rightTitleClick(View view) {
//        Intent intent = new Intent(this, DeviceTimingListActivity.class);
//        intent.putExtra(IntentKey.DEVICE, device);
//        startActivity(intent);
//    }
}
