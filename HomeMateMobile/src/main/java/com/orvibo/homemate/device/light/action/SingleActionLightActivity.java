package com.orvibo.homemate.device.light.action;

import android.app.Fragment;
import android.os.Bundle;
import android.view.View;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.data.BindActionType;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.NavigationType;
import com.orvibo.homemate.device.control.BaseControlActivity;
import com.orvibo.homemate.view.custom.NavigationGreenBar;

/**
 * Created by snown on 2016/6/23.
 *
 * @描述: 单个设备灯的绑定activity
 */
public class SingleActionLightActivity extends BaseControlActivity {
    protected NavigationGreenBar navigationGreenBar;
    private BaseLightActionFragment fragment;
    private Action action;
    private int bindActionType;
    private OnResultListener onResultListener;

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
            navigationGreenBar.setRightImageViewVisibility(View.GONE);
            navigationGreenBar.setRightTextVisibility(View.GONE);
        }
    }


    protected void initData() {
        action = (Action) getIntent().getSerializableExtra(IntentKey.ACTION);
        bindActionType = getIntent().getIntExtra(IntentKey.BIND_ACTION_TYPE, BindActionType.SCENE);
        Bundle bundle = new Bundle();
        bundle.putSerializable(IntentKey.DEVICE, device);
        bundle.putSerializable(IntentKey.ACTION, action);
        bundle.putSerializable(IntentKey.BIND_ACTION_TYPE, bindActionType);
        switch (device.getDeviceType()) {
            case DeviceType.COLOR_TEMPERATURE_LAMP:
                fragment = new ActionColorLightFragment();
                break;
            case DeviceType.DIMMER:
                fragment = new ActionDimmingLightFragment();
                break;
            case DeviceType.RGB:
                fragment = new ActionRgbLightFragment();
                break;
        }
        fragment.setArguments(bundle);
        getFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commitAllowingStateLoss();
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
        onResultListener.onResult();
        super.onBackPressed();
    }


}
