package com.orvibo.homemate.device.light;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.api.DeviceApi;
import com.orvibo.homemate.api.listener.BaseResultListener;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DeviceSetting;
import com.orvibo.homemate.common.BaseFragment;
import com.orvibo.homemate.common.ViHomeProApp;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.dao.DeviceSettingDao;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.event.BaseEvent;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.popup.DeviceSelectTimePopup;

/**
 * Created by snown on 2016/7/5.
 *
 * @描述: rgbw灯设置
 */
public class RgbwLightSettingFragment extends BaseFragment implements View.OnClickListener, DeviceSelectTimePopup.ITimeListener, BaseResultListener {
    private Device device;
    private Context mContext;

    private CheckBox offDelayCheck, levelDelayCheck;

    private View levelDalayTimeView, offDalayTimeView;

    private TextView levelDalayTimeText, offDalayTimeText;

    private DeviceSelectTimePopup mDeviceSelectTimePopup;

    private DeviceSetting offDelay, offDelayEnable, levelDelay, levelDelayEnable;

    private String deviceId;


    //延时关默认时间
    private static final int OFF_DELAY_TIME = 5 * 60;
    //缓亮缓灭默认时间
    private static final int LEVEL_DELAY_TIME = 5;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = ViHomeProApp.getContext();
        device = (Device) getArguments().getSerializable(IntentKey.DEVICE);
        deviceId = device.getDeviceId();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rgbw_setting, container, false);
        levelDelayCheck = (CheckBox) view.findViewById(R.id.levelDelayCheck);
        offDelayCheck = (CheckBox) view.findViewById(R.id.offDelayCheck);
        offDelayCheck.setOnClickListener(this);
        levelDelayCheck.setOnClickListener(this);

        levelDalayTimeView = view.findViewById(R.id.levelDalayTimeView);
        offDalayTimeView = view.findViewById(R.id.offDalayTimeView);
        levelDalayTimeView.setOnClickListener(this);
        offDalayTimeView.setOnClickListener(this);

        levelDalayTimeText = (TextView) view.findViewById(R.id.levelDalayTimeText);
        offDalayTimeText = (TextView) view.findViewById(R.id.offDalayTimeText);
        if (ProductManage.isSkySingleLight(device))
            view.findViewById(R.id.levelDalayTextView).setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDeviceSelectTimePopup = new DeviceSelectTimePopup(getActivity(), this);
        initData();
    }

    private void initData() {


        offDelayEnable = DeviceSettingDao.getInstance().getSingleData(deviceId, DeviceSetting.OFF_DELAY_ENABLE);
        if (offDelayEnable == null) {
            offDelayEnable = new DeviceSetting();
            offDelayEnable.setDeviceId(deviceId);
            offDelayEnable.setParamId(DeviceSetting.OFF_DELAY_ENABLE);
            offDelayEnable.setParamType(DeviceSetting.ParmType.BOOL.ordinal());
            offDelayEnable.setParamValue(String.valueOf(false));
        }
        levelDelayEnable = DeviceSettingDao.getInstance().getSingleData(deviceId, DeviceSetting.LEVEL_DELAY_ENABLE);
        if (levelDelayEnable == null) {
            levelDelayEnable = new DeviceSetting();
            levelDelayEnable.setDeviceId(deviceId);
            levelDelayEnable.setParamId(DeviceSetting.LEVEL_DELAY_ENABLE);
            levelDelayEnable.setParamType(DeviceSetting.ParmType.BOOL.ordinal());
            levelDelayEnable.setParamValue(String.valueOf(false));
        }

        offDelay = DeviceSettingDao.getInstance().getSingleData(deviceId, DeviceSetting.OFF_DELAY_TIME);
        if (offDelay == null) {
            offDelay = new DeviceSetting();
            offDelay.setDeviceId(deviceId);
            offDelay.setParamId(DeviceSetting.OFF_DELAY_TIME);
            offDelay.setParamType(DeviceSetting.ParmType.INT.ordinal());
            offDelay.setParamValue(String.valueOf(OFF_DELAY_TIME));
        }

        levelDelay = DeviceSettingDao.getInstance().getSingleData(deviceId, DeviceSetting.LEVEL_DELAY_TIME);
        if (levelDelay == null) {
            levelDelay = new DeviceSetting();
            levelDelay.setDeviceId(deviceId);
            levelDelay.setParamId(DeviceSetting.LEVEL_DELAY_TIME);
            levelDelay.setParamType(DeviceSetting.ParmType.INT.ordinal());
            levelDelay.setParamValue(String.valueOf(LEVEL_DELAY_TIME));
        }
        offDelayCheck.setChecked(offDelayEnable.isEnable());
        offDalayTimeView.setVisibility(offDelayCheck.isChecked() ? View.VISIBLE : View.GONE);

        levelDelayCheck.setChecked(levelDelayEnable.isEnable());
        levelDalayTimeView.setVisibility(levelDelayCheck.isChecked() ? View.VISIBLE : View.GONE);

        offDalayTimeText.setText(Integer.parseInt(offDelay.getParamValue()) / 60 + mContext.getString(R.string.time_minutes));
        levelDalayTimeText.setText(levelDelay.getParamValue() + mContext.getString(R.string.time_second));

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.levelDelayCheck:
                showDialog();
                DeviceApi.lightLevelDelay(deviceId, Integer.parseInt(levelDelay.getParamValue()), !levelDelayEnable.isEnable(), this);
                break;
            case R.id.offDelayCheck:
                showDialog();
                DeviceApi.lightOffDelay(deviceId, Integer.parseInt(offDelay.getParamValue()), !offDelayEnable.isEnable(), this);
                break;
            case R.id.levelDalayTimeView:
                //延时默认1-10秒
                mDeviceSelectTimePopup.showView(getString(R.string.skyworth_rgb_select_level_delaytime), Integer.parseInt(levelDelay.getParamValue()), 10, true);
                break;
            case R.id.offDalayTimeView:
                mDeviceSelectTimePopup.showView(getString(R.string.skyworth_rgb_select_off_delaytime), Integer.parseInt(offDelay.getParamValue()) / 60, 10, false);
                break;
        }
    }


    @Override
    public void onTimeReturn(int time, boolean levelDalay) {
        showDialog();
        if (levelDalay) {
            DeviceApi.lightLevelDelay(device.getDeviceId(), time, true, this);
        } else {
            DeviceApi.lightOffDelay(device.getDeviceId(), time * 60, true, this);
        }
    }

    @Override
    public void onResultReturn(BaseEvent baseEvent) {
        dismissDialog();
        initData();
        if (!baseEvent.isSuccess()) {
            ToastUtil.toastError(baseEvent.getResult());
        }
    }
}
