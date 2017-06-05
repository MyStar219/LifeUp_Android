package com.orvibo.homemate.device.light;

import android.os.Bundle;
import android.view.View;

import com.smartgateway.app.R;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.NavigationType;
import com.orvibo.homemate.device.control.BaseControlActivity;
import com.orvibo.homemate.view.custom.NavigationGreenBar;

/**
 * Created by snown on 2016/6/23.
 *
 * @描述: 单个灯的控制界面，rgb,调光，色温灯
 */
public class SingleLightActivity extends BaseControlActivity {
    protected NavigationGreenBar navigationGreenBar;
    private BaseLightFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_light);
        initView();
        initData();
    }

    protected void initView() {
        navigationGreenBar = (NavigationGreenBar) findViewById(R.id.nbTitle);
        if (navigationGreenBar != null) {
            navigationGreenBar.showWhiteStyle();
            navigationGreenBar.setRightImageViewVisibility(View.VISIBLE);
            navigationGreenBar.setRightImageViewRes(R.drawable.more_green_selector);
            navigationGreenBar.setRightTextVisibility(View.GONE);
        }
    }


    protected void initData() {
        useBaseSetting = true;
        Bundle bundle = new Bundle();
        bundle.putSerializable(IntentKey.DEVICE, device);
        switch (device.getDeviceType()) {
            case DeviceType.COLOR_TEMPERATURE_LAMP:
                fragment = new ColorLightFragment();
                break;
            case DeviceType.DIMMER:
                fragment = new DimmingLightFragment();
                break;
            case DeviceType.RGB:
                fragment = new RgbLightFragment();
                break;
        }
        fragment.setArguments(bundle);
        getFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commitAllowingStateLoss();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setControlDeviceBar(NavigationType.greenType, deviceName);
    }
}
