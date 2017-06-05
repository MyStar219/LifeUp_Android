package com.orvibo.homemate.device.manage;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.smartgateway.app.R;
import com.orvibo.homemate.api.DeviceApi;
import com.orvibo.homemate.api.listener.BaseResultListener;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.FrequentlyMode;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.data.DeviceStatusConstant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.event.BaseEvent;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.EditTextWithCompound;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.orvibo.homemate.view.custom.seekbar.DiscreteSeekBar;


public class PercentCurtainSetPercentActivity extends BaseActivity implements DiscreteSeekBar.OnProgressChangeListener, NavigationCocoBar.OnRightClickListener, BaseResultListener {

    private Device mDevice;
    private String deviceId;
    private FrequentlyMode frequentlyMode;
    private NavigationCocoBar navigationCocoBar;
    private EditTextWithCompound frequentlyModeName;
    private DiscreteSeekBar discrete;

    //    private int value1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_percent_curtain_set_percent);

        init();
    }

    private void init() {
        discrete = (DiscreteSeekBar) findViewById(R.id.discrete);
        navigationCocoBar = (NavigationCocoBar) findViewById(R.id.navigationCocoBar);
        navigationCocoBar.setOnRightClickListener(this);
        frequentlyModeName = (EditTextWithCompound) findViewById(R.id.frequentlyModeName);
        frequentlyModeName.setMaxLength(16);
        mDevice = (Device) getIntent().getSerializableExtra(IntentKey.DEVICE);
        deviceId = mDevice.getDeviceId();
        frequentlyMode = (FrequentlyMode) getIntent().getSerializableExtra(IntentKey.FREQUENTLY_MODE);
        String name = frequentlyMode.getName();
        navigationCocoBar.setCenterText(name);
        frequentlyModeName.setText(name);
        if (!StringUtil.isEmpty(name)) {
            name = frequentlyModeName.getText().toString();
            frequentlyModeName.setSelection(name.length());
        }
//        value1 = frequentlyMode.getValue1();
        discrete.setProgress(frequentlyMode.getValue1());
    }

    @Override
    protected void onResume() {
        super.onResume();
        discrete.setPressed(true);
    }

    @Override
    public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
//        value1 = value;
    }

    @Override
    public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

    }

    @Override
    public void onRightClick(View v) {
        String name = frequentlyModeName.getText().toString();
        if (StringUtil.isEmpty(name)) {
            Toast.makeText(mAppContext, getString(R.string.frequently_mode_name_null_error), Toast.LENGTH_SHORT).show();
            return;
        }
        showDialog();
        int value1 = discrete.getProgress();
        String order = DeviceOrder.CLOSE;
        if (value1 == DeviceStatusConstant.CURTAIN_OFF) {
            order = DeviceOrder.CLOSE;
        } else if (value1 == DeviceStatusConstant.CURTAIN_ON) {
            order = DeviceOrder.OPEN;
        } else {
            order = DeviceOrder.STOP;
        }
        DeviceApi.setFrequentlyMode(userName, frequentlyMode.getUid(), frequentlyMode.getFrequentlyModeId(), name, order, value1, 0, 0, 0, this);
    }

    @Override
    public void onResultReturn(BaseEvent baseEvent) {
        if (isFinishingOrDestroyed()) {
            return;
        }
        dismissDialog();
        int result = baseEvent.getResult();
        if (result != ErrorCode.SUCCESS) {
            ToastUtil.toastError(result);
        }
        finish();
    }
}
