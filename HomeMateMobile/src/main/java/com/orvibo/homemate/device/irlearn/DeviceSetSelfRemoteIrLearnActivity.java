package com.orvibo.homemate.device.irlearn;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.orvibo.homemate.device.control.BaseIrActivity;
import com.smartgateway.app.R;
import com.orvibo.homemate.device.control.infrareddevice.SelfRemoteButtonAdapter;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DeviceIr;
import com.orvibo.homemate.dao.DeviceIrDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.manage.edit.DeviceSetSelfRemoteButtonNameActivity;
import com.orvibo.homemate.device.manage.edit.DeviceSetSelfRemoteEditActivity;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;
import com.orvibo.homemate.view.custom.NavigationGreenBar;

import java.util.List;

/**
 * Created by smagret on 2015/05/07.
 */
public class DeviceSetSelfRemoteIrLearnActivity extends BaseIrActivity implements SelfRemoteButtonAdapter.OnSetSelfRemoteButtonNameListener {
    private RelativeLayout emptyView;
    private List<DeviceIr> deviceIrs;
    private SelfRemoteButtonAdapter selfRemoteButtonAdapter;
    private GridView gridView;
    private Device device;
    private String deviceId;
    private DeviceIr deviceIr;
    private DeviceIrDao deviceIrDao;
    private String uid;
    private ToastPopup toastPopup;
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
        setIrBar();
        gridView = (GridView) findViewById(R.id.gridView);
        emptyView = (RelativeLayout) findViewById(R.id.emptyView);
        emptyView.setVisibility(View.GONE);
        NavigationGreenBar navigationBar = (NavigationGreenBar) findViewById(R.id.nbTitle);
        if (navigationBar != null) {
            navigationBar.setRightText(getResources().getString(R.string.device_set_self_remote_edit));
        }
        toastPopup = new ToastPopup();
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    private void init() {
        deviceIrDao = new DeviceIrDao();
        deviceIrs = deviceIrDao.selDeviceIrs(uid,deviceId);
        selfRemoteButtonAdapter = new SelfRemoteButtonAdapter(this, deviceIrs,SelfRemoteButtonAdapter.TYPE_LEARN);
        selfRemoteButtonAdapter.setOnToActivityListener(this);
        gridView.setAdapter(selfRemoteButtonAdapter);
    }

    @Override
    public void onSetSelfRemoteButtonName() {

        if (deviceIrs.size() >= Constant.IR_KEY_MAX) {
            toastPopup.showPopup(DeviceSetSelfRemoteIrLearnActivity.this, getResources()
                    .getString(R.string.ir_key_max_tips), getResources()
                    .getString(R.string.know),null);
            return;
        }
        Intent intent = new Intent(DeviceSetSelfRemoteIrLearnActivity.this, DeviceSetSelfRemoteButtonNameActivity.class);
        intent.putExtra("device", device);
        startActivity(intent);
        finish();
    }

    public void rightTitleClick(View view) {
        Intent intent = new Intent(this, DeviceSetSelfRemoteEditActivity.class);
        intent.putExtra(IntentKey.DEVICE, device);
        startActivity(intent);
    }

    private class ToastPopup extends ConfirmAndCancelPopup {
        /**
         * 点击确定按钮
         */
        public void confirm() {
            dismiss();
        }
    }
}
