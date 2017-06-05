package com.orvibo.homemate.device.light.action;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.data.BindActionType;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.NavigationType;
import com.orvibo.homemate.device.control.BaseControlActivity;
import com.orvibo.homemate.view.custom.FragmentTabHost;

import java.util.List;

/**
 * Created by snown on 2016/6/23.
 *
 * @描述: 多个设备在同一个activity中，创维灯相关
 */
public class MultipleActionLightActivity extends BaseControlActivity {
    private FragmentTabHost mTabHost = null;
    private List<Device> devices;

    private Action action;
    private int bindActionType;
    private OnResultListener onResultListener;

    private int selectIndex;//选中的tab

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
        findViewById(R.id.confirm_image_tv).setVisibility(View.GONE);
    }


    protected void initData() {
        action = (Action) getIntent().getSerializableExtra(IntentKey.ACTION);
        bindActionType = getIntent().getIntExtra(IntentKey.BIND_ACTION_TYPE, BindActionType.SCENE);
        String deviceIdAction = null;
        if (action != null) {
            deviceIdAction = action.getDeviceId();
        }

        if (device != null) {
            devices = new DeviceDao().selDevicesByExtAddr(uid, device.getExtAddr());
            if (devices != null) {
                for (int i = 0; i < devices.size(); i++) {
                    Device device = devices.get(i);
                    int deviceType = device.getDeviceType();
                    String deviceId = device.getDeviceId();
                    if (deviceIdAction != null && deviceId.equalsIgnoreCase(deviceIdAction)) {
                        selectIndex = i;
                    }
                    mTabHost.addTab(mTabHost.newTabSpec(String.valueOf(deviceType)).setIndicator(getTabItemView(deviceType)), getFragment(deviceType), getBundle(i));
                }
            }
        }
        mTabHost.setCurrentTab(selectIndex);
    }

    private Class<?> getFragment(int deviceType) {
        Class<?> cla = ActionRgbLightFragment.class;
        switch (deviceType) {
            case DeviceType.RGB:
                cla = ActionRgbLightFragment.class;
                break;
            case DeviceType.DIMMER:
                cla = ActionDimmingLightFragment.class;
                break;
        }
        return cla;
    }


    private Bundle getBundle(int index) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(IntentKey.DEVICE, devices.get(index));
        bundle.putSerializable(IntentKey.ACTION, action);
        bundle.putSerializable(IntentKey.BIND_ACTION_TYPE, bindActionType);
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
    public void onAttachFragment(Fragment fragment) {
        try {
            onResultListener = (OnResultListener) fragment;
        } catch (Exception e) {
            throw new ClassCastException(this.toString() + " must implement onRefreshListener");
        }
        super.onAttachFragment(fragment);
    }


    /**
     * 标题栏返回事件
     *
     * @param v
     */
    public void leftTitleClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        //多次切换回调接口会失效，导致返回的数据异常，先采用直接获取fragment的回调action方法
        if (mTabHost.getCurrentFragment() != null)
            mTabHost.getCurrentFragment().onActionResult();
        else
            onResultListener.onResult();
        super.onBackPressed();
    }

}
