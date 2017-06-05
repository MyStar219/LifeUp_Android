package com.orvibo.homemate.device.manage.edit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.orvibo.homemate.device.control.BaseIrActivity;
import com.orvibo.homemate.device.manage.ModifySelfRemoteActivity;
import com.smartgateway.app.R;
import com.orvibo.homemate.device.control.infrareddevice.SelfRemoteButtonAdapter;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DeviceIr;
import com.orvibo.homemate.dao.DeviceIrDao;
import com.orvibo.homemate.view.custom.NavigationGreenBar;

import java.util.List;

/**
 * Created by smagret on 2015/05/07.
 */
public class DeviceSetSelfRemoteEditActivity extends BaseIrActivity implements AdapterView.OnItemClickListener {
    private RelativeLayout emptyView;
    private List<DeviceIr> deviceIrs;
    private DeviceIr deviceIr;
    private SelfRemoteButtonAdapter selfRemoteButtonAdapter;
    private GridView gridView;
    private Device device;
    private String deviceId;
    private DeviceIrDao deviceIrDao;
    private String uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_set_self_remote);
        device = (Device) getIntent().getSerializableExtra("device");
        deviceId = device.getDeviceId();
        uid = device.getUid();
        findViews();
    }

    private void findViews() {
        setIrEditBar();
        gridView = (GridView) findViewById(R.id.gridView);
        emptyView = (RelativeLayout) findViewById(R.id.emptyView);
        emptyView.setVisibility(View.GONE);
        NavigationGreenBar navigationBar = (NavigationGreenBar) findViewById(R.id.nbTitle);
        if (navigationBar != null) {
            navigationBar.setRightText(getResources().getString(R.string.device_set_self_remote_finish));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    private void init() {
        deviceIrDao = new DeviceIrDao();
        deviceIrs = deviceIrDao.selDeviceIrs(uid,deviceId);
        selfRemoteButtonAdapter = new SelfRemoteButtonAdapter(this, deviceIrs,SelfRemoteButtonAdapter.TYPE_EDIT);
        gridView.setAdapter(selfRemoteButtonAdapter);
        gridView.setOnItemClickListener(this);
    }

    public void rightTitleClick(View view) {
//        Intent intent = new Intent(this, DeviceSetSelfRemoteIrLearnActivity.class);
//        intent.putExtra(IntentKey.DEVICE, device);
//        startActivity(intent);
        finish();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        deviceIr = deviceIrs.get(position);
        Intent intent = new Intent(this, ModifySelfRemoteActivity.class);
        intent.putExtra("deviceIr", deviceIr);
        startActivity(intent);
    }
}
