package com.orvibo.homemate.device.control;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.dao.DeviceStatusDao;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.NavigationType;
import com.orvibo.homemate.data.StatusType;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.CurtainRollerView;
import com.orvibo.homemate.view.custom.NavigationGreenBar;

/**
 * 支持百分比控制的卷帘
 * Created by Allen on 2015/4/9.
 * Modified by smagret on 2015/04/13
 */
public class CurtainRollerActivity extends BaseControlActivity implements CurtainRollerView.OnProgressChangedListener {
    private static final String TAG = CurtainRollerActivity.class
            .getSimpleName();
    private ImageView         ivBG;
    private CurtainRollerView curtainDropdownView;
    private DeviceStatusDao   deviceStatusDao;
    private DeviceStatus      deviceStatus;
    private int               currentProgress;
    private int               oldProgress;
    private int               progress;
    private final int REFRESH_STATUS = 1;
    private final int REFRESH_TIME   = 15;
    private int deviceType;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case REFRESH_STATUS:
                    Log.d(TAG, "handleMessage()-REFRESH_STATUS" + REFRESH_STATUS);
                    refreshStatus();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curtain_dropdown);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setControlDeviceBar(NavigationType.greenType, deviceName);
    }

    private void init() {
        mContext = CurtainRollerActivity.this;
        if (device != null) {
            deviceType = device.getDeviceType();
        }
        initView();
        initListener();
        initDate();
    }

    private void initView() {
        ivBG = (ImageView) findViewById(R.id.ivBG);
        curtainDropdownView = (CurtainRollerView) findViewById(R.id.curtainDropdownView);
        NavigationGreenBar navigationGreenBar = (NavigationGreenBar) findViewById(R.id.nbTitle);
        if (navigationGreenBar != null) {
            navigationGreenBar.setRightImageViewVisibility(View.VISIBLE);
            navigationGreenBar.setRightImageViewRes(R.drawable.more_white_selector);
            navigationGreenBar.setRightTextVisibility(View.GONE);
            navigationGreenBar.setText(deviceName);
        }
    }

    private void initListener() {
        curtainDropdownView.setOnProgressChangedListener(this);
    }

    private void initDate() {
        refreshStatus();
    }

    private void refreshStatus() {
        deviceStatusDao = new DeviceStatusDao();
        deviceStatus = deviceStatusDao.selDeviceStatus(uid, deviceId);
        if (deviceStatus != null) {
            progress = deviceStatus.getValue1();
            currentProgress = progress;
        }
        oldProgress = currentProgress;
        if (deviceType == DeviceType.SCREEN) {
            curtainDropdownView.setProgress(100 - currentProgress);
        } else {
            curtainDropdownView.setProgress(currentProgress);
        }
    }

    @Override
    protected boolean onControlResult(String uid, String deviceId, int result) {
        dismissDialog();
        if (result == ErrorCode.SUCCESS) {
            ToastUtil.toastError(ErrorCode.SUCCESS);
            //TODO
            //curtainDropdownView.setProgress(currentProgress);
        } else {
            currentProgress = oldProgress;
            if (deviceType == DeviceType.SCREEN) {
                curtainDropdownView.setProgress(100 - currentProgress);
            } else {
                curtainDropdownView.setProgress(currentProgress);
            }
        }
        return super.onControlResult(uid, deviceId, result);
    }

    public void onPropertyReport(String deviceId, int statusType, int value1, int value2, int value3, int value4, int alarmType) {

        deviceStatus = deviceStatusDao.selDeviceStatus(uid, deviceId);
        Log.d(TAG, "OnPropertyReport()-deviceStatus" + deviceStatus);

        if (statusType == StatusType.RANGE_STATUS) {
            if (deviceType == DeviceType.SCREEN) {
                curtainDropdownView.setProgress(100 - value1);
            } else {
                curtainDropdownView.setProgress(value1);
            }
        }
    }


    @Override
    public void onProgressChanged(int progress) {
        Log.d(getClass().getName(), "progress:" + progress);
    }

    @Override
    public void onProgressFinish(int progress) {
        Log.d(getClass().getName(), "progress:" + progress);
        if (deviceType == DeviceType.SCREEN) {
            controlDevice.curtainPercentOpen(uid, deviceId, 100 - progress, 0);
        } else {
            controlDevice.curtainPercentOpen(uid, deviceId, progress, 0);
        }
        showDialog();
        if (handler.hasMessages(REFRESH_STATUS)) {
            handler.removeMessages(REFRESH_STATUS);
            handler.sendEmptyMessageDelayed(REFRESH_STATUS, REFRESH_TIME);
        }
    }
}
