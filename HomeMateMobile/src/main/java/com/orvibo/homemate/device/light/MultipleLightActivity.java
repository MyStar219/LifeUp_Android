package com.orvibo.homemate.device.light;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.NavigationType;
import com.orvibo.homemate.device.control.BaseControlActivity;
import com.orvibo.homemate.device.timing.DeviceTimingListActivity;
import com.orvibo.homemate.sharedPreferences.RgbwCache;
import com.orvibo.homemate.view.custom.FragmentTabHost;

import java.util.List;

/**
 * Created by snown on 2016/7/5.
 *
 * @描述: 多个设备在同一个activity中，创维灯相关
 */
public class MultipleLightActivity extends BaseControlActivity implements TabHost.OnTabChangeListener {
    private FragmentTabHost mTabHost = null;
    private List<Device> devices;
    //传递的是rgbw灯中的rgb的deviceid
    public final static String KEY_RGB_DEVICE_ID = "key_rgb_device_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiple_light);
        initView();
        initData();
    }

    protected void initView() {
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getFragmentManager(), android.R.id.tabcontent);
    }


    protected void initData() {
        useBaseSetting = true;
        if (device != null) {
            devices = new DeviceDao().selDevicesByExtAddr(uid, device.getExtAddr());
            if (devices != null) {
                for (int i = 0; i < devices.size(); i++) {
                    int deviceType = devices.get(i).getDeviceType();
                    mTabHost.addTab(mTabHost.newTabSpec(String.valueOf(deviceType)).setIndicator(getTabItemView(deviceType)), getFragment(deviceType), getBundle(i));
                }
            }
            mTabHost.setOnTabChangedListener(this);
            if (RgbwCache.isRgbDisplay(deviceId))
                mTabHost.setCurrentTab(0);
            else
                mTabHost.setCurrentTab(1);
        }
    }

    private Class<?> getFragment(int deviceType) {
        Class<?> cla = RgbLightFragment.class;
        switch (deviceType) {
            case DeviceType.RGB:
                cla = RgbLightFragment.class;
                break;
            case DeviceType.DIMMER:
                cla = DimmingLightFragment.class;
                break;
        }
        return cla;
    }

    private Bundle getBundle(int index) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(IntentKey.DEVICE, devices.get(index));
        bundle.putSerializable(KEY_RGB_DEVICE_ID, device.getDeviceId());
        return bundle;
    }

    /**
     * 给Tab按钮设置图标和文字
     */
    private View getTabItemView(int deviceType) {
        View view = LayoutInflater.from(this).inflate(R.layout.tab_item_view, null);
        TextView textView = (TextView) view.findViewById(R.id.textview);
        switch (deviceType) {
            case DeviceType.RGB:
                textView.setText(getString(R.string.rgb));
                textView.setBackgroundResource(R.drawable.btn_tab_left_selector);
                break;
            case DeviceType.DIMMER:
                textView.setText(getString(R.string.white_light));
                textView.setBackgroundResource(R.drawable.btn_tab_right_selector);
                break;
        }

        return view;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setControlDeviceBar(NavigationType.greenType, deviceName);
    }

    @Override
    public void onTabChanged(String tabId) {
        if (tabId.equalsIgnoreCase(String.valueOf(DeviceType.RGB)))
            RgbwCache.setRgbDisplay(deviceId, true);
        else
            RgbwCache.setRgbDisplay(deviceId, false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.shareTextView: {
                popupWindow.dismiss();
                Intent intent = new Intent(this, DeviceTimingListActivity.class);
                intent.putExtra(IntentKey.DEVICE, devices.get(mTabHost.getCurrentTab()));
                startActivity(intent);
                break;
            }
            case R.id.timingModeTextView:
            case R.id.settingsTextView:
                super.onClick(v);
                break;
        }
    }
}
