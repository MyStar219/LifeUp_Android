package com.orvibo.homemate.device.control;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.bo.PayloadData;
import com.orvibo.homemate.dao.DeviceStatusDao;
import com.orvibo.homemate.data.DeviceStatusConstant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.NavigationType;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.view.custom.Curtain2HalvesInsideView;
import com.orvibo.homemate.view.custom.CurtainBaseView;
import com.orvibo.homemate.view.custom.CurtainControlOldView;
import com.orvibo.homemate.view.custom.CurtainOutsideView;
import com.orvibo.homemate.view.custom.CurtainRollerView;
import com.orvibo.homemate.view.custom.NavigationGreenBar;

/**
 * 非百分比窗帘都在此类处理
 * Created by smagret on 2015/12/21
 */
public class CurtainOldActivity extends BaseControlActivity implements CurtainControlOldView.OnStatusChangedListener {
    private static final String TAG = CurtainOldActivity.class
            .getSimpleName();
    private CurtainRollerView curtainRollerView;
    private Curtain2HalvesInsideView curtain2HalvesView;
    private CurtainOutsideView curtainOutsideView;
    private RelativeLayout curtainRelativeLayout;
    private CurtainControlOldView curtainControlOldView;
    private Context mContext;
    private DeviceStatusDao deviceStatusDao;
    private DeviceStatus deviceStatus;
    private int currentStatus;
    private LinearLayout curtainOldLineraLayout;
    private CurtainBaseView curtainBaseView;
    private int deviceType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curtain_old);
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
        setControlDeviceBar(NavigationType.greenType, deviceName);
    }

    private void init() {
        mContext = CurtainOldActivity.this;
        initView();
        initListener();
        initDate();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        initDate();
    }

    private void initView() {
        curtainOldLineraLayout = (LinearLayout) findViewById(R.id.curtainOldLinearLayout);
        curtainControlOldView = (CurtainControlOldView) findViewById(R.id.curtainControlOldView);
        curtainControlOldView.setStopButtonVisible(true);
        NavigationGreenBar navigationGreenBar = (NavigationGreenBar) findViewById(R.id.nbTitle);
        if (navigationGreenBar != null) {
            navigationGreenBar.setRightImageViewVisibility(View.VISIBLE);
            navigationGreenBar.setRightImageViewRes(R.drawable.more_white_selector);
            navigationGreenBar.setRightTextVisibility(View.GONE);
            navigationGreenBar.setText(deviceName);
        }
        addOldCurtianView();

    }

    private void addOldCurtianView() {
        //这里判断是否和先前类别一致，如果不一致才需要重新加载布局，如果一致的情况就不需要
        if (device != null) {
            int deviceType_int = device.getDeviceType();
            if (deviceType_int != deviceType) {
                deviceType = deviceType_int;
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                curtainOldLineraLayout.removeAllViews();
                if (device != null) {

                    switch (deviceType) {
                        case DeviceType.ROLLING_GATE:
                        case DeviceType.ROLLER_SHUTTERS:
                        case DeviceType.SCREEN:
                            curtainRelativeLayout = (RelativeLayout) inflater.inflate(R.layout.activity_curtain_dropdown_old_view, null);
                            curtainRollerView = (CurtainRollerView) curtainRelativeLayout.findViewById(R.id.curtainDropdownView);
                            curtainOldLineraLayout.addView(curtainRelativeLayout, 0);
                            curtainRollerView.setTouchable(false);
                            curtainBaseView = curtainRollerView;
                            break;
                        case DeviceType.CURTAIN:
                            curtainRelativeLayout = (RelativeLayout) inflater.inflate(R.layout.activity_curtain_2halves_old_view, null);
                            curtain2HalvesView = (Curtain2HalvesInsideView) curtainRelativeLayout.findViewById(R.id.curtain2HalvesInsideView);
                            curtainOldLineraLayout.addView(curtainRelativeLayout, 0);
                            curtainBaseView = curtain2HalvesView;
                            break;

                        case DeviceType.PUSH_WINDOW:
                            curtainRelativeLayout = (RelativeLayout) inflater.inflate(R.layout.activity_curtain_outside_view, null);
                            curtainOutsideView = (CurtainOutsideView) curtainRelativeLayout.findViewById(R.id.curtainOutsideView);
                            curtainOldLineraLayout.addView(curtainRelativeLayout, 0);
                            curtainBaseView = curtainOutsideView;
                            break;
                    }
                }
            }
        }
    }

    private void initListener() {
        curtainControlOldView.setOnStatusChangedListener(this);
    }

    private void initDate() {
        deviceStatusDao = new DeviceStatusDao();
        deviceStatus = deviceStatusDao.selDeviceStatus(uid, deviceId);
        if (deviceStatus != null) {
            currentStatus = deviceStatus.getValue1();
            curtainBaseView.post(new Runnable() {
                @Override
                public void run() {
                    if (!isFinishingOrDestroyed()) {
                        if (currentStatus == DeviceStatusConstant.CURTAIN_ON) {
                            open();
                        } else if (currentStatus == DeviceStatusConstant.CURTAIN_OFF) {
                            close();
                        } else {
                            stop();
                        }
                    }
                }
            });
        } else {
            curtainBaseView.post(new Runnable() {
                @Override
                public void run() {
                    LogUtil.d(TAG, "initDate()-deviceStatus:" + deviceStatus);
                    if (!isFinishingOrDestroyed()) {
                        close();
                    }
                }
            });
        }
    }

    @Override
    protected boolean onControlResult(String uid, String deviceId, int result) {
        return super.onControlResult(uid, deviceId, result);
    }

    @Override
    public void onPropertyReport(String deviceId, int statusType, int value1, int value2, int value3, int value4, int alarmType, PayloadData payloadData) {
        if (device != null && device.getUid().equals(uid) && device.getDeviceId().equals(deviceId)) {
            LogUtil.d(TAG, "onPropertyReport()-value1:" + value1);
            if (value1 == DeviceStatusConstant.CURTAIN_ON) {
                open();
            } else if (value1 == DeviceStatusConstant.CURTAIN_OFF) {
                close();
            } else {
                stop();
            }
        }
    }

    private void open() {
        if (deviceType == DeviceType.SCREEN) {
            curtainControlOldView.close();
        } else {
            curtainControlOldView.open();
        }
        curtainBaseView.setProgress(100);
    }

    private void close() {
        if (deviceType == DeviceType.SCREEN) {
            curtainControlOldView.open();
        } else {
            curtainControlOldView.close();
        }
        curtainBaseView.setProgress(0);
    }

    private void stop() {
        curtainControlOldView.stopUnCheck();
        curtainBaseView.setProgress(50);
    }

    @Override
    public void onCurtainOpen() {
        showDialog();
        if (deviceType == DeviceType.SCREEN) {
            controlDevice.curtainClose(uid, deviceId);
        } else {
            controlDevice.curtainOpen(uid, deviceId);
        }
    }

    @Override
    public void onCurtainClose() {
        showDialog();
        if (deviceType == DeviceType.SCREEN) {
            controlDevice.curtainOpen(uid, deviceId);
        } else {
            controlDevice.curtainClose(uid, deviceId);
        }
    }

    @Override
    public void onCurtainStop() {
        showDialog();
        controlDevice.curtainStop(uid, deviceId);
    }
}
