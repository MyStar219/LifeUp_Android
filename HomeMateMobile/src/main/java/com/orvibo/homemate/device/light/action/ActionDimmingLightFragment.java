package com.orvibo.homemate.device.light.action;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.data.DeviceStatusConstant;
import com.orvibo.homemate.util.LogUtil;

/**
 * Created by snown on 2016/7/1.
 *
 * @描述: 调光灯绑定fragment
 */
public class ActionDimmingLightFragment extends BaseLightActionFragment implements SeekBar.OnSeekBarChangeListener {
    private static final String TAG = ActionDimmingLightFragment.class
            .getSimpleName();
    private ImageView ivLight;

    private int currentLightProgress;
    private float percent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_dimming_light, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initListener();
    }


    protected void initView(View view) {
        super.initView(view);
        ivLight = (ImageView) view.findViewById(R.id.imageView5);
        seekBarLight = (SeekBar) view.findViewById(R.id.seekBarLight);
    }

    private void initListener() {
        seekBarLight.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        percent = (float) progress / seekBar.getMax();
        ivLight.setAlpha(percent);

        if (fromUser) {
            controlLight(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    /**
     * 调整调光灯亮度
     *
     * @param progress
     */
    private void controlLight(int progress) {
        seekBarLight.setProgress(progress);
        value2 = progress == 0 ? Constant.DIMMING_LIGHT_MIN : progress;
        command = DeviceOrder.MOVE_TO_LEVEL;
    }

    /**
     * 已被选中的状态
     *
     * @param action
     */
    @Override
    protected void onSelectedAction(Action action) {
        super.onSelectedAction(action);
        setAction(action);
    }

    /**
     * 设备当前真实的状态
     *
     * @param deviceStatus
     */
    @Override
    protected void onDeviceStatus(DeviceStatus deviceStatus) {
//        if (deviceStatus != null) {
//            //如果当前为关闭状态，则设置亮度为0
//            if (deviceStatus.getValue1() == DeviceStatusConstant.OFF) {
//                deviceStatus.setValue2(0);
//            }
//        }
        setAction(deviceStatus);
    }

    /**
     * 设备默认的状态
     */
    @Override
    protected void onDefaultAction(Action defaultAction) {
        setAction(defaultAction);
    }

    private void setAction(Action action) {
        LogUtil.d(TAG, "setAction()-action:" + action);
        command = action.getCommand();
        currentLightProgress = action.getValue2();
        value1 = action.getValue1();
        value2 = currentLightProgress;
        if (DeviceOrder.TOGGLE.equals(command)) {

        } else if (DeviceOrder.OFF.equals(command) || value1 == DeviceStatusConstant.OFF || value2 == 0) {
            percent = (float) 0 / Constant.DIMMER_MAX;
            command = DeviceOrder.OFF;
        } else {
            percent = (float) currentLightProgress / Constant.DIMMER_MAX;
        }
        float curPercent = percent;
        seekBarLight.setProgress(currentLightProgress);
        ivLight.setAlpha(curPercent);
    }
}

