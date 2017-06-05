package com.orvibo.homemate.device.action;

import android.os.Bundle;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.bo.FrequentlyMode;
import com.orvibo.homemate.dao.FrequentlyModeDao;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.data.NavigationType;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.view.custom.Curtain2HalvesView;

import java.util.ArrayList;
import java.util.List;

/**
 * 对开窗帘支持百分比控制
 * Created by Allen on 2015/4/20.
 * modified by smagret on 2015/05/12
 */
public class ActionCurtain2HalvesActivity extends BaseActionActivity implements Curtain2HalvesView.OnProgressChangedListener, Curtain2HalvesView.OnActionClickListener {
    private static final String TAG = ActionCurtain2HalvesActivity.class
            .getSimpleName();
    private Curtain2HalvesView curtain2HalvesView;
    private int                currentProgress;
    private int                progress;

    private FrequentlyModeDao frequentlyModeDao;
    private List<FrequentlyMode> frequentlyModes = new ArrayList<FrequentlyMode>();
    /**
     * 百分比，比如80%，percent就为80
     */
    private float percent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curtain_2halves);
        init();
        frequentlyModeDao = new FrequentlyModeDao();
        frequentlyModes = frequentlyModeDao.selFrequentlyModes( deviceId);
        if (frequentlyModes.size() == 4) {
            curtain2HalvesView.setSeekBarGone();
            curtain2HalvesView.setFrequentlyModes(frequentlyModes);
        } else {
            curtain2HalvesView.setSeekBarVisible();
        }
    }

    private void init() {
        initView();
        initListener();
    }

    private void initView() {
        curtain2HalvesView = (Curtain2HalvesView) findViewById(R.id.curtain2HalvesView);
        curtain2HalvesView.setActionControl();
        setBindBar(NavigationType.greenType);
//        if (bindType == BindType.NEED_TOGGLE) {
//            curtain2HalvesView.setStopButtonVisible();
//        } else if (bindType == BindType.NOT_NEED_TOGGLE) {
//            curtain2HalvesView.setStopButtonGone();
//        }
    }

    private void initListener() {
        curtain2HalvesView.setOnProgressChangedListener(this);
        curtain2HalvesView.setOnActionClickListener(this);
    }

    @Override
    public void onProgressChanged(int progress) {
        LogUtil.d(TAG, "progress:" + progress);
    }

    @Override
    public void onProgressFinish(int progress) {
        LogUtil.d(TAG, "progress:" + progress);
        value1 = progress;
        setStatus(value1);
//        percent = progress;
//        if (currentProgress < progress) {
//            command = DeviceOrder.CLOSE;
//            String curtainString = getResources().getString(R.string.device_timing_action_curtain_percent);
//            keyName = String.format(curtainString, deviceName, command, percent + "%");
//        } else if (currentProgress > progress) {
//            command = DeviceOrder.OPEN;
//            String curtainString = getResources().getString(R.string.device_timing_action_curtain_percent);
//            keyName = String.format(curtainString, deviceName, command, percent + "%");
//        }
    }

    @Override
    protected void onSelectedAction(Action action) {
        super.onSelectedAction(action);
        setAction(action);
    }

    @Override
    protected void onDeviceStatus(DeviceStatus deviceStatus) {
        super.onDeviceStatus(deviceStatus);
        setAction(deviceStatus);
    }

    @Override
    protected void onDefaultAction(Action action) {
        super.onDefaultAction(action);
        setAction(action);
    }

    private void setAction(Action action) {
        LogUtil.d(TAG, "setAction()-action:" + action);
        value1 = action.getValue1();
        setStatus(value1);
        progress = value1;
        currentProgress = progress;
        curtain2HalvesView.setProgress(currentProgress);
    }

    private void setStatus(int value1) {
        percent = value1;
//        if (currentProgress < progress) {
//            command = DeviceOrder.CLOSE;
//            String curtainString = getResources().getString(R.string.device_timing_action_curtain_percent);
//            keyName = String.format(curtainString, deviceName, command, percent + "%");
//
//        } else if (currentProgress >= progress) {
        command = DeviceOrder.OPEN;
        String curtainString = getResources().getString(R.string.device_timing_action_curtain_percent);
        keyName = String.format(curtainString, deviceName, command, percent + "%");
//        }
    }

    private void setChecked(Action action) {
        String order = action.getCommand();

        if (order.equals(DeviceOrder.OPEN)) {
        } else if (order.equals(DeviceOrder.CLOSE)) {

        } else if (order.equals(DeviceOrder.STOP)) {
        }
    }

    private void setStatus(String order, int value1) {
//        this.value1 = value1;
        percent = value1;
        if (order.equals(DeviceOrder.STOP)) {
            command = DeviceOrder.STOP;
            String curtainString = getResources().getString(R.string.device_timing_action_curtain_percent);
            keyName = String.format(curtainString, deviceName, command, percent + "%");
        } else {
            command = DeviceOrder.OPEN;
            String curtainString = getResources().getString(R.string.device_timing_action_curtain_percent);
            keyName = String.format(curtainString, deviceName, command, percent + "%");
        }
    }

    @Override
    public void onActionClick(String order, int progress) {
        LogUtil.d(TAG, "progress:" + progress);
        value1 = progress;
        setStatus(order, value1);
        curtain2HalvesView.setProgress(progress);
    }
}
